package uni.server.healthgpt.controller;

import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import uni.server.healthgpt.advice.exception.UserNotFoundExceptionCustom;
import uni.server.healthgpt.data.entity.User;
import uni.server.healthgpt.data.response.CommonResult;
import uni.server.healthgpt.data.response.ListResult;
import uni.server.healthgpt.data.response.SingleResult;
import uni.server.healthgpt.repo.UserJpaRepo;
import uni.server.healthgpt.service.impl.ResponseService;
@PreAuthorize("hasRole('ROLE_USER')") //추가내용
@Api(tags = {"2. User"}) // UserController를 대표하는 최상단 타이틀 영역에 표시될 값 세팅
@RequiredArgsConstructor // class 내부의 final 객체는 Constructor Injection 수행, @Autowired도 가능
@RestController // 결과를 JSON으로 도출
@RequestMapping(value = "/v1") // api resource를 버전별로 관리, /v1 을 모든 리소스 주소에 적용
public class UserController {

    private final UserJpaRepo userJpaRepo; // Jpa를 활용한 CRUD 쿼리 가능
    private final ResponseService responseService; // 결과를 처리하는 Service

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 access_token",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "회원 리스트 조회", notes = "모든 회원을 조회한다.") // 각각의 resource에 제목과 설명 표시
    @GetMapping(value = "/users") // user 테이블의 모든 정보를 읽어옴
    public ListResult<User> findAllUser() { // 데이터가 1개 이상일 수 있기에 List<User>로 선언
        return responseService.getListResult(userJpaRepo.findAll()); // JPA를 사용하면 CRUD에 대해 설정 없이 쿼리 사용 가능 (select * from user 와 같음)
        //결과 데이터가 여러개인 경우 getListResult 활용
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 access_token",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "회원 단건 조회", notes = "회원번호(msrl)로 회원을 조회한다.")
    @GetMapping(value = "/user")
    public SingleResult<User> findUserById(@ApiParam(value = "언어", defaultValue = "ko") @RequestParam String lang) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String id = authentication.getName();
        return responseService.getSingleResult(userJpaRepo.findByUid(id).orElseThrow(UserNotFoundExceptionCustom::new));
        // 결과 데이터가 단일건인 경우 getSingleResult를 이용하여 결과를 출력
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 access_token",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "회원 수정", notes = "회원정보를 수정한다.")
    @PutMapping(value = "/user")
    public SingleResult<User> modify(
            @ApiParam(value = "회원번호", required = true) @RequestParam long msrl,
            @ApiParam(value = "회원이름", required = true) @RequestParam String name,
            @ApiParam(value = "키", required = true) @RequestParam float height,
            @ApiParam(value = "몸무게", required = true) @RequestParam float weight,
            @ApiParam(value = "생일 " ,required = true)@RequestParam String birth) {
        User user = User.builder()
                .msrl(msrl)
                .name(name)
                .height(height)
                .weight(weight)
                .birth(birth)
                .build();

        return responseService.getSingleResult(userJpaRepo.save(user));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 access_token",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "회원 삭제", notes = "msrl로 회원정보를 삭제한다.")
    @DeleteMapping(value = "/user/{msrl}")
    public CommonResult delete (
            @ApiParam(value = "회원정보", required = true) @PathVariable long msrl ) {
        userJpaRepo.deleteById(msrl); // deleteById id를 받아 delete query 실행
        return responseService.getSuccessResult();
        // 성공 결과 정보만 필요한 경우 getSuccessResult()를 이용하여 결과를 출력
    }
    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 access_token",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "날짜 추가", notes = "msrl로 회원정보의 따른 유저를")
    @PostMapping(value = "/user/{msrl}")
    public CommonResult addToday (
            @ApiParam(value = "회원정보", required = true) @PathVariable long msrl ) {
        userJpaRepo.deleteById(msrl); // deleteById id를 받아 delete query 실행
        return responseService.getSuccessResult();
        // 성공 결과 정보만 필요한 경우 getSuccessResult()를 이용하여 결과를 출력
    }
}