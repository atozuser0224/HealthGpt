package uni.server.healthgpt.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import uni.server.healthgpt.advice.exception.CEmailSigninFailedException;
import uni.server.healthgpt.config.security.JwtTokenProvider;
import uni.server.healthgpt.data.dto.MailRequest;
import uni.server.healthgpt.data.dto.SignInRequest;
import uni.server.healthgpt.data.dto.SignUpRequest;
import uni.server.healthgpt.data.dto.isMailRequest;
import uni.server.healthgpt.data.entity.User;
import uni.server.healthgpt.data.response.CommonResult;
import uni.server.healthgpt.data.response.SingleResult;
import uni.server.healthgpt.repo.UserJpaRepo;
import uni.server.healthgpt.service.impl.MailService;
import uni.server.healthgpt.service.impl.ResponseService;

import java.util.Collections;

@Api(tags = {"1. Sign"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1")
@Log4j2
public class SignController {

    private final UserJpaRepo userJpaRepo; // jpa 쿼리 활용
    private final JwtTokenProvider jwtTokenProvider; // jwt 토큰 생성
    private final ResponseService responseService; // API 요청 결과에 대한 code, messageㅍ
    private final PasswordEncoder passwordEncoder; // 비밀번호 암호화
    private final MailService mailService;
    @ApiOperation(value = "로그인", notes = "이메일 회원 로그인을 한다.")
    @PostMapping(value = "/signin")
    public SingleResult<String> signin(@RequestBody SignInRequest sign) {
        User user = userJpaRepo.findByUid(sign.getEmail()).orElseThrow(CEmailSigninFailedException::new);
        if (!passwordEncoder.matches(sign.getPassword(), user.getPassword())) {
            log.error(sign.getEmail()+" user is signin is failed");
            // matches : 평문, 암호문 패스워드 비교 후 boolean 결과 return
            throw new CEmailSigninFailedException();
        }
        log.info(sign.getEmail()+" user is signin is success");
        return responseService.getSingleResult(jwtTokenProvider.createToken(String.valueOf(user.getMsrl()), user.getRoles()));
    }

    @ApiOperation(value = "가입", notes = "회원가입을 한다.")
    @PostMapping(value = "/signup")
    public CommonResult signup(@RequestBody SignUpRequest sign) {

        userJpaRepo.save(User.builder()
                .uid(sign.getEmail())
                .password(passwordEncoder.encode(sign.getPassword()))
                .name(sign.getName())
                .roles(Collections.singletonList("ROLE_USER"))
                .height(sign.getHeight())
                .weight(sign.getWeight())
                .birth(sign.getBirth())
                .etcs(sign.getEtcs())
                .build());
        log.info(sign.getEmail()+" user is signup is success");
        return responseService.getSuccessResult();
    }
    @ApiOperation(value = "메일", notes = "메일인증을 한다.")
    @PostMapping("/mail")
    public CommonResult sendMail(@RequestBody MailRequest mail) throws Exception {
        if (mailService.sendSimpleMessage(mail.getMail())){
            return responseService.getFailResult(-1009, "이메일이 옳바르지 않습니다.");
        }
        return responseService.getSuccessResult();
    }
    @ApiOperation(value = "메일", notes = "메일이 있는지 확인한다.")
    @PostMapping("/ismail")
    public boolean ExistMail(@RequestBody MailRequest mail) {
        return userJpaRepo.findByUid(mail.getMail()).isPresent();
    }
    @ApiOperation(value = "메일", notes = "메일이 있는지 확인한다.")
    @PostMapping("/equalMailcode")
    public CommonResult IsMail(@RequestBody isMailRequest isMailRequest) {
        int n = mailService.isMail(isMailRequest.getMail(), isMailRequest.getCode());
        if (n==1){//n==1 은 인증중인 이메일이 없을때 n==2은 시간초과 , n==3은 인증 실패를 나타냄
            return responseService.getFailResult(-1006,"인증 중인 이메일을 찾을수 없어요.");
        } else if (n==2) {
            return responseService.getFailResult(-1007,"이메일 인증이 시간초과 되었습니다.");
        } else if (n==3) {
            return responseService.getFailResult(-1008,"이메일 인증이 실패하였습니다.");
        }else {
            return responseService.getSuccessResult();
        }

    }
}