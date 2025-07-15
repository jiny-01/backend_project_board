import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Transactional(rollbackFor = Exception.class)
    // try문 안에다가 db에넣는 작업들을 여러개 할 시, 하나라도 실패하면 트랜잭션 걸어버림
    // (Exception 단위의 예외가 터지게 된마다면, 롤백시킴)
    public ApiRespDto<?> signup(SignupReqDto signupReqDto) {
        // 아이디 중복 확인
        Optional<User> userByUsername = userRepository.getUserByUsername(signupReqDto.getUsername());
        if (userByUsername.isPresent()) { // 만약 username이 존재하면,
            return new ApiRespDto<>("failed","이미 사용중인 아이디입니다.", null);
        }

        // 이메일 중복확인
        Optional<User> userByEmail = userRepository.getUserByEmail(signupReqDto.getEmail());
        if (userByEmail.isPresent()) { // 만약  email이 존재하면,
            return new ApiRespDto<>("failed","이미 사용중인 이메일입니다",null);
        }

        try {
            // 사용자 정보 추가
            Optional<User> optionalUser = userRepository.addUser(signupReqDto.toEntity(bCryptPasswordEncoder));

            if (optionalUser.isEmpty()) { // 회원가입 정보가 비어있다면
                throw new RuntimeException("회원정보 추가에 실패했습니다.");
            }

            User user = optionalUser.get(); // 유저 객체로 바로 쓸 수 있도록 꺼냄

            UserRole userRole = UserRole.builder()
                    .userId(user.getUserId())
                    .roleId(3) // 임시사용자
                    .build();

            int addUserRoleResult = userRoleRepository.addUserRole(userRole);
            // 결과값 (1 or 0) 이 int 로 들어옴
            if (addUserRoleResult != 1) {
                throw new RuntimeException("권한 추가에 실패했습니다.");
            }

            // 모든 if문을 통과 했을때 반환되는 값
            return new ApiRespDto<>("success", "회원가입이 성공적으로 완료되었습니다.",user);
        } catch (Exception e) {
            return new ApiRespDto<>("failed","회원가입 중 오류가 발생했습니다. : " + e.getMessage(),null);
            // 예외가 터지면서 전부 롤백될것임
        }

    }

    public ApiRespDto<?> signin(SigninReqDto signupReqDto) {
        Optional<User> optionalUser = userRepository.getUserByUsername(signupReqDto.getUsername());
        if (optionalUser.isEmpty()) { // 만약 유저명이 비어있다면
            return  new ApiRespDto<>("failed","아이디 또는 비밀번호가 일치하지 않습니다.", null);
        }

        User user = optionalUser.get();

        if (!bCryptPasswordEncoder.matches(signupReqDto.getPassword(), user.getPassword())) { // 첫번째 평문, 두번째 암호문
            return  new ApiRespDto<>("failed","아이디 또는 비밀번호가 일치하지 않습니다.", null);
        } // 검증 끝

        String accessToken = jwtUtils.generateAccessToken(user. getUserId().toString()); // 이러면 AccessToken이 만들어짐
        return  new ApiRespDto<>("success","로그인이 성공적으로 완료되었습니다.",accessToken);
    }

}