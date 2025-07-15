package com.korit.BoardStudy.service;

import com.korit.BoardStudy.dto.ApiRespDto;
import com.korit.BoardStudy.dto.mail.SendMailReqDto;
import com.korit.BoardStudy.entity.User;
import com.korit.BoardStudy.entity.UserRole;
import com.korit.BoardStudy.repository.UserRepository;
import com.korit.BoardStudy.repository.UserRoleRepository;
import com.korit.BoardStudy.security.jwt.JwtUtils;
import com.korit.BoardStudy.security.model.PrincipalUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class MailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRoleRepository userRoleRepository;

    public ApiRespDto<?> sendMail(SendMailReqDto sendMailReqDto , PrincipalUser principalUser) { // 메일 보내기
        if (!principalUser.getEmail().equals(sendMailReqDto.getEmail())) { // 가입 정보 db랑 입력받은 email이 다르면
            return new ApiRespDto<>("failed", "잘못된 요청 입니다.", null);
        }
        // db에 입력된 이메일 유무 확인
        Optional<User> optionalUser = userRepository.getUserByEmail(sendMailReqDto.getEmail());
        if (optionalUser.isEmpty()) {
            return new ApiRespDto<>("failed", "존재하지 않는 이메일 입니다.", null);
        }

        User user = optionalUser.get();

        boolean hasTempRole = user.getUserRoles().stream() // 권한 목록 순회
                .anyMatch(userRole -> userRole.getRoleId() == 3); // 미인증 권한(3)인지 확인

        if (!hasTempRole) { // 3번이 아니면(2 or 1)
            return new ApiRespDto<>("failed", "인증이 필요한 계정이 아닙니다", null);
        }
        // 전부 통과 시, JwtUtils의 인증 토큰 들고옴
        String verifyToken = jwtUtils.generateVerifyToken(user.getUserId().toString());
        // 유저의 verify 토큰을 생성하기 위해서 string 값으로 user의 id(integer)를 string 으로 들고옴

        // 메일 내용을 담을 변수
        SimpleMailMessage message = new SimpleMailMessage();

        // 누가 받을것인지
        message.setTo(user.getEmail());
        // 보낼 메세지 제목
        message.setSubject("이메일 인증 입니다.");
        // 보낼 내용
        message.setText("링크를 클릭하여 인증을 완료해주세요 : " +
                "http://localhost:8080/mail/verify?verifyToken=" + verifyToken);

        javaMailSender.send(message);

        return new ApiRespDto<>("success","이메일 전송이 완료되었습니다.",null);
    }

    // 링크를 타고 들어간 유저의 유저 권한을 변경(3 -> 2)
    public Map<String, Object> verify(String token) { // RequestParam으로 받은 verifyToken을 넘겨줌

        // 값 초기화
        Claims claims = null;
        Map<String, Object> resultMap = null;

        try {
            claims = jwtUtils.getClaims(token);
            String subject = claims.getSubject(); // verifyToken
            if (!"VerifyToken".equals(subject)) { //들어온 verifyToken이 subject와 다르면
                resultMap = Map.of("status","failed","message","잘못된 요청입니다.");
            }
            // 같다면 통과

            Integer userId = Integer.parseInt(claims.getId()); // 들어온 Id는 String으로 되어있기때문에 Integer값으로 바꿔줌
            // id를 가지고 있는지 확인
            Optional<User> optionalUser = userRepository.getUserByUserId(userId);
            if (optionalUser.isEmpty()) {
                resultMap = Map.of("status","failed","message","존재하지 않는 사용자입니다.");
            }


            // 찾아온 userRole이 3번(임시 사용자) 가 맞는지 확인 - 위 userId 값 받아옴
            Optional<UserRole> optionalUserRole = userRoleRepository.getUserRoleByUserIdAndRoleId(userId,3);
            if (optionalUserRole.isEmpty()) {
                resultMap = Map.of("status","failed","message","이미 인증 완료된 메일입니다.");
            } else {
                userRoleRepository.updateRoleId(optionalUserRole.get().getUserRoleId(), userId);
                resultMap = Map.of("status","success","message","이메일 인증이 완료되었습니다.");
            }
        } catch (ExpiredJwtException e) {
            resultMap = Map.of("status","failed","message","인증 시간이 만료된 요청입니다. \n 인증 메일을 다시 요청하세요.");
        } catch (Exception e) {
            resultMap = Map.of("status","failed","message","잘못된 요청입니다. \n 인증 메일을 다시 요청해주세요.");
        }
        return resultMap;
    }
}