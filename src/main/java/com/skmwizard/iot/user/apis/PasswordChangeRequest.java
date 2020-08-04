package com.skmwizard.iot.user.apis;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Setter
@Getter
class PasswordChangeRequest {
    @NotBlank(message = "현재 비밀번호를 입력하세요")
    private String currentPassword;

    @NotBlank(message = "새로운 비밀번호를 입력하세요")
    private String newPassword;
}
