package com.YammyEater.demo.service.user;

import com.YammyEater.demo.Util.RandomUtil;
import com.YammyEater.demo.constant.error.ErrorCode;
import com.YammyEater.demo.constant.user.OAuthProvider;
import com.YammyEater.demo.domain.user.EmailVerification;
import com.YammyEater.demo.domain.user.User;
import com.YammyEater.demo.dto.user.OAuthUserJoinRequest;
import com.YammyEater.demo.dto.user.oauth.OAuthJoinTokenSubject;
import com.YammyEater.demo.exception.GeneralException;
import com.YammyEater.demo.exception.jwt.JwtExpiredException;
import com.YammyEater.demo.exception.jwt.JwtInvalidException;
import com.YammyEater.demo.repository.user.EmailVerificationRepository;
import com.YammyEater.demo.repository.user.UserRepository;
import com.YammyEater.demo.service.mail.MailService;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserJoinServiceImpl implements UserJoinService {
    //회원가입 이메일 인증 코드 만료 시간
    @Value("${user.join.verification_code_expire_second}")
    private int verificationExpireSecond;

    //회원가입 이메일 인증 코드의 길이
    @Value("${user.join.verification_code_len}")
    private int verificationCodeLen;

    //이메일 코드 인증 후 회원가입 까지의 유효 시간
    @Value("${user.join.verification_code_expire_second_after_verification}")
    private int joinValidTimeAfterVerification;

    private final String verificationEmailTitle = "YammyEater 회원가입 인증번호 입니다.";
    private String JOINCODE_MAIL_BODY;
    private final String VERIFICATION_EMAIL_CODE_REPLACE_STRING = "@_CODE_@";

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;

    private final MailService mailService;
    private final UserService userService;

    private final RandomUtil randomUtil;
    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider jwtTokenProvider;


    @PostConstruct
    public void init() throws IOException {
        ClassPathResource resource = new ClassPathResource("mail/mail-verification.html");
        InputStream ipt = resource.getInputStream();
        JOINCODE_MAIL_BODY = new String(ipt.readAllBytes());
    }

    //인증코드를 이메일로 보냄
    @Override
    public void sendVerifyingEmail(String email) {
        //이미 등록된 유저인지 검사
        if (userService.existsByEmail(email)) {
            throw new GeneralException(ErrorCode.BAD_REQUEST);
        }

        //인증 코드 생성
        String verificationCode = randomUtil.getRandomString(verificationCodeLen);
        //인증 코드 중복 방지
        while (emailVerificationRepository.hasKey(verificationCode)) {
            verificationCode = randomUtil.getRandomString(verificationCodeLen);
        }

        //인증코드 저장
        emailVerificationRepository.save(verificationCode, new EmailVerification(email, false));
        emailVerificationRepository.setExpire(verificationCode, verificationExpireSecond);

        //메일 본문 생성
        String mailBody = JOINCODE_MAIL_BODY.replace(VERIFICATION_EMAIL_CODE_REPLACE_STRING, verificationCode);

        //인증코드 메일로 발송
        mailService.sendEmail(email, verificationEmailTitle, mailBody);
    }

    //이메일을 코드로 인증
    //인증 실패시 false 리턴
    //인증 성공시 true 리턴, 코드의 유효 시간을 재설정
    //이미 인증된 코드를 재 인증 시도 시에 BAD_REQUEST
    @Override
    public boolean verifyEmailByCode(String code) {
        EmailVerification emailVerification = emailVerificationRepository.findByKey(code);
        //코드가 존재하지 않을 때
        if(emailVerification == null) {
            return false;
        }
        //이미 인증된 코드
        if(emailVerification.isVerified()) {
            throw new GeneralException(ErrorCode.BAD_REQUEST);
        }
        emailVerificationRepository.save(code, new EmailVerification(emailVerification.getEmail(), true));
        emailVerificationRepository.setExpire(code, joinValidTimeAfterVerification);
        return true;
    }

    //중복 이메일 검사는 코드를 보낼 때 이미 수행
    //올바른 코드인지, 유저명에 중복은 없는지 검사
    //유저명 중복은 이미 클라이언트에서 검증하므로 중복 발생 시 자세한 설명 없이 BAD REQUEST
    @Override
    public void joinByCode(String code, String email, String username, String password) {
        EmailVerification emailVerification = emailVerificationRepository.findByKey(code);
        //해당 코드가 존재하지 않음
        if(emailVerification == null) {
            throw new GeneralException(ErrorCode.BAD_REQUEST);
        }
        //코드가 아직 인증이 안됨
        if(emailVerification.isVerified() == false) {
            throw new GeneralException(ErrorCode.BAD_REQUEST);
        }
        //인증 코드를 전송한 이메일과 회원가입 요청 이메일이 다름
        if(email.equals(emailVerification.getEmail()) == false) {
            throw new GeneralException(ErrorCode.BAD_REQUEST);
        }
        //요청한 유저명을 이미 다른 유저가 사용중
        if(userService.existsByUsername(username)) {
            throw new GeneralException(ErrorCode.BAD_REQUEST);
        }

        //유저 등록
        userRepository.save(
                User.builder()
                        .email(email)
                        .username(username)
                        .password(passwordEncoder.encode(password))
                        .oauthProviderName(OAuthProvider.NOT_USE.getName())
                        .build()
        );

        //인증이 끝났으므로 인증 코드 삭제
        emailVerificationRepository.deleteByKey(code);

    }

    @Override
    public Long joinByOAuth(OAuthUserJoinRequest oAuthUserJoinRequest) {
        OAuthJoinTokenSubject oAuthJoinTokenSubject;
        try {
             oAuthJoinTokenSubject = jwtTokenProvider.validateOAuthJoinTokenAndGetSubject(
                    oAuthUserJoinRequest.joinToken());
        }
        catch (JwtExpiredException jwtExpiredException) {
            throw new GeneralException(ErrorCode.UOJ_JOINTOKEN_EXPIRED);
        }
        catch (JwtInvalidException jwtInvalidException) {
            throw new GeneralException(ErrorCode.BAD_REQUEST);
        }

        User user = null;
        try {
            user = userRepository.save(
                    User.builder()
                            .email(oAuthJoinTokenSubject.getEmail())
                            .username(oAuthUserJoinRequest.username())
                            .password(null)
                            .oauthProviderName(oAuthJoinTokenSubject.getOAuthProvider().getName())
                            .build()
            );
        }
        // 데이터 제약조건 위반. 중복된 닉네임은 사용자에게 알려줌.
        // 이 외의 잘못된 요청 등은 전부 BAD REQUEST
        catch (DataIntegrityViolationException dataIntegrityViolationException) {
            if(userRepository.existsByUsername(oAuthUserJoinRequest.username())) {
                throw new GeneralException(ErrorCode.UOJ_DUPLICATE_USERNAME);
            }
            throw new GeneralException(ErrorCode.BAD_REQUEST);
        }
        return user.getId();
    }
}
