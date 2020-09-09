package com.skmwizard.user.apis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Setter
@Getter
class PasswordResetRequest {
    @Schema(title = "아이디", example = "user_id@gmail.com", required = true)
    @NotBlank(message = "이메일을 입력하세요")
    @Pattern(regexp = "^[a-z0-9._%+-]+@[a-z0-9]+.[a-z]{2,6}$", flags = Pattern.Flag.CASE_INSENSITIVE, message = "이메일 형식이 아닙니다")
    private String email;

    @Schema(title = "인증번호")
    @NotBlank(message = "인증번호를 입력하세요")
    private String code;

    @Schema(title = "새로운 비밀번호", example = "8자 이상의 영문/숫자/특수기호 중 2개 이상 조합", required = true)
    @NotBlank(message = "새로운 비밀번호를 입력하세요")
    private String resetPassword;
}
