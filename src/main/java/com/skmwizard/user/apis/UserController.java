package com.skmwizard.user.apis;

import com.skmwizard.user.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author ingu_ko
 * @since 2020-07-30
 */
@Tag(name = "사용자 관리")
@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;
    private final UserResourceConverter converter;

    @Operation(summary = "특정 사용자 조회", description = "특정 사용자 조회한다.")
    @Parameters({
        @Parameter(name = "email", description = "이메일", in = ParameterIn.PATH, example = "user_id@gmail.com")
    })
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "사용 중복 확인 성공, 사용 가능한 이메일입니다."),
        @ApiResponse(responseCode = "409", description = "사용자 중복 확인 실패, 이미 가입된 이메일입니다.")
    })
    @GetMapping("/users/{email}")
    public Mono<UserResponse> get(@PathVariable("email") String email) {
        log.info("[GET] /users/{}", email);
        return userService.get(email)
            .map(converter::converts);
    }
}
