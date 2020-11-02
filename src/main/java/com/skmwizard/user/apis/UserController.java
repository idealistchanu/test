package com.skmwizard.user.apis;

import com.skmwizard.user.services.User;
import com.skmwizard.user.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import javax.validation.constraints.Min;

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

    @Operation(summary = "사용자 추가", description = "사용자를 추가 한다.")
    @Parameters({
        @Parameter(name = "Authorization", description = "인증 토큰", in = ParameterIn.HEADER, example = "Authorization Bearer INVALID", schema = @Schema(type = "string"), required = true),
    })
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "사용 추가 성공. 추가된 사용자 정보를 반환한다."),
        @ApiResponse(responseCode = "400", description = "사용 추가 실패, 잘못된 요청입니다."),
        @ApiResponse(responseCode = "409", description = "사용자 추가 실패, 이미 가입된 아이디입니다.")
    })
    @PostMapping("/users")
    public Mono<UserResponse> add(@AuthenticationPrincipal Jwt jwt, @RequestBody @Valid UserRequest request) {
        String username = jwt.getClaimAsString("email");
        log.info("[GET] /users, username: {}, request: {}", username, request);
        return userService.add(username, converter.converts(request))
            .map(converter::converts);
    }

    @Operation(summary = "사용자 목록 조회", description = "사용자 목록 조회한다.")
    @Parameters({
        @Parameter(name = "Authorization", description = "인증 토큰", in = ParameterIn.HEADER, example = "Authorization Bearer INVALID", schema = @Schema(type = "string"), required = true),
        @Parameter(name = "page", description = "페이지 번호", in = ParameterIn.PATH, example = "0"),
        @Parameter(name = "sizePerPage", description = "페이지 객체 수", in = ParameterIn.PATH, example = "20")
    })
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "사용 중복 확인 성공, 사용 가능한 아이디입니다."),
        @ApiResponse(responseCode = "409", description = "사용자 중복 확인 실패, 이미 가입된 아이디입니다.")
    })
    @GetMapping("/users")
    public Mono<Page<UserResponse>> get(
        UserRequest request,
        @RequestParam(name = "page", required = false, defaultValue = "0") @Min(0) int page,
        @RequestParam(name = "sizePerPage", required = false, defaultValue = "20") @Min(1) int sizePerPage) {
        log.info("[GET] /users?page={}&sizePerPage={}, request: {}", page, sizePerPage, request);
        User parameter = converter.converts(request);
        return userService.get(parameter)
            .skip(page * (long) sizePerPage)
            .take(sizePerPage)
            .map(converter::converts)
            .collectList()
            .flatMap(resourceList -> {
                Pageable pageable = page < 1 ? Pageable.unpaged() : PageRequest.of(page, sizePerPage);
                return userService.count(parameter)
                    .map(count -> new PageImpl<>(resourceList, pageable, count));
            });
    }

    @Operation(summary = "특정 사용자 조회", description = "특정 사용자 조회한다.")
    @Parameters({
        @Parameter(name = "Authorization", description = "인증 토큰", in = ParameterIn.HEADER, example = "Authorization Bearer INVALID", schema = @Schema(type = "string"), required = true),
    })
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "사용자 조회 성공, 작업이 완료된 사용자 정보를 반환한다."),
        @ApiResponse(responseCode = "404", description = "사용자 조회 실패, 등록되지 않은 사용자 아이디입니다.")
    })
    @GetMapping("/users/{username}")
    public Mono<UserResponse> get(@AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("email");
        log.info("[GET] /users/{}", username);
        return userService.get(username)
            .map(converter::converts);
    }

    @Operation(summary = "특정 사용자 수정", description = "특정 사용자의 정보를 수정한다.")
    @Parameters({
        @Parameter(name = "Authorization", description = "인증 토큰", in = ParameterIn.HEADER, example = "Authorization Bearer INVALID", schema = @Schema(type = "string"), required = true),
        @Parameter(name = "username", description = "사용자 아이디", in = ParameterIn.HEADER, example = "user_id", hidden = true),
        @Parameter(name = "username", description = "사용자 아이디", in = ParameterIn.PATH, example = "user_id")
    })
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공, 작업이 완료된 사용자 정보를 반환한다."),
        @ApiResponse(responseCode = "409", description = "사용자 중복 확인 실패, 이미 가입된 아이디입니다.")
    })
    @PutMapping("/users/{username}")
    public Mono<UserResponse> edit(@AuthenticationPrincipal Jwt jwt, @PathVariable("username") String id, @RequestBody @Valid UserUpdateRequest request) {
        String username = jwt.getClaimAsString("email");
        log.info("[PUT] /users/{} username: {}, request: {}", id, username, request);
        return userService.edit(username, converter.converts(id, request))
            .map(converter::converts);
    }

    @Operation(summary = "특정 사용자 삭제", description = "특정 사용자를 삭제한다. 조건에 상관없이 항상 성공한다.")
    @Parameters({
        @Parameter(name = "Authorization", description = "인증 토큰", in = ParameterIn.HEADER, example = "Authorization Bearer INVALID", schema = @Schema(type = "string"), required = true),
        @Parameter(name = "username", description = "사용자 아이디", in = ParameterIn.HEADER, example = "user_id", hidden = true),
        @Parameter(name = "username", description = "사용자 아이디", in = ParameterIn.PATH, example = "user_id")
    })
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "삭제 성공.")
    })
    @DeleteMapping("/users/{username}")
    public Mono<Void> remove(@AuthenticationPrincipal Jwt jwt, @PathVariable("username") String id) {
        String username = jwt.getClaimAsString("email");
        log.info("[DELETE] /users/{} username: {}", id, username);
        return userService.remove(id);
    }
}
