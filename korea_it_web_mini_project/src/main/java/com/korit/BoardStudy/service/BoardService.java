package com.korit.BoardStudy.service;

import com.korit.BoardStudy.dto.ApiRespDto;
import com.korit.BoardStudy.dto.board.AddBoardReqDto;
import com.korit.BoardStudy.entity.Board;
import com.korit.BoardStudy.repository.BoardRepository;
import com.korit.BoardStudy.security.model.PrincipalUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;

    @Transactional(rollbackFor = Exception.class)
    public ApiRespDto<?> addBoard(AddBoardReqDto addBoardReqDto, PrincipalUser principalUser) { // 인증된 유저인지 확인 해야해서
        // 만약 유저 정보가 없거나, 유저 정보가 게시물 생성을 할 수 있는 권한이 없는 사용자라면,
        if (principalUser == null || !addBoardReqDto.getUserId().equals(principalUser.getUserId())) {
            return new  ApiRespDto<>("failed","잘못된 접근입니다. 로그인 정보가 유효하지 않거나 권한이 없습니다.",null);
        }

        // 제목이 비어있거나, 공백만 있는 문자열이면
        if (addBoardReqDto.getTitle() == null || addBoardReqDto.getTitle().trim().isEmpty()) { // trim = 양쪽 공백 제거
            return new ApiRespDto<>("failed","제목은 필수 입력 사항입니다.", null);
        }

        // 내용이 비어있거나, 공백만 있는 문자열이면
        if (addBoardReqDto.getContent() == null || addBoardReqDto.getContent().trim().isEmpty()) { // trim = 양쪽 공백 제거
            return new ApiRespDto<>("failed","내용은 필수 입력 사항입니다.", null);
        }

        try {
            int result = boardRepository.addBoard(addBoardReqDto.toEntity());
            if (result != 1) {
                return new ApiRespDto<>("failed","게시물 추가에 실패했습니다.",null);
            }
            return new ApiRespDto<>("success", "게시물이 성공적으로 추가되었습니다.", null);
        } catch (Exception e) {
            return new ApiRespDto<>("failed","서버 오류로 게시물 추가에 실패했습니다. : " + e.getMessage(), null);
        }
    }
    public ApiRespDto<?> getBoardByBoardId(Integer boardId) {
        if (boardId == null || boardId <= 0) {
            return new ApiRespDto<>("failed", "유효하지 않은 게시물 ID입니다.", null);
        }

        Optional<Board> optionalBoard = boardRepository.getBoardByBoardId(boardId);
        if (optionalBoard.isPresent()) { // 게시물이 존재 한다면,
            return new ApiRespDto<>("sucess","게시물 조회 성공", optionalBoard.get());
        } else {
            return new ApiRespDto<>("failed", "해당 ID의 게시물을 찾을 수 없습니다.",null);
        }
    }

    public ApiRespDto<?> getBoardList() {
        List<Board> boardList = boardRepository.getBoardList();

        if (boardList.isEmpty()) {
            return new ApiRespDto<> ("failed","조회할 게시물이 없습니다.",null);
        } else {
            return new ApiRespDto<>("success","게시물 목록 조회 성공",boardList);
        }
    }
}