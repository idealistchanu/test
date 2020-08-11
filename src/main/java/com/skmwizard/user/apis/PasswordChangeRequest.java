package com.skmwizard.user.apis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Setter
@Getter
class PasswordChangeRequest {
    @Schema(title = "현재 비밀번호", example = "8자 이상의 영문/숫자/특수기호 중 2개 이상 조합", required = true)
    @NotBlank(message = "현재 비밀번호를 입력하세요")
    private String currentPassword;

    @Schema(title = "새로운 비밀번호", example = "8자 이상의 영문/숫자/특수기호 중 2개 이상 조합", required = true)
    @NotBlank(message = "새로운 비밀번호를 입력하세요")
    private String newPassword;
}
