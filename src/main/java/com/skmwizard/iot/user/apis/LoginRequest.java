package com.skmwizard.iot.user.apis;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Setter
@Getter
class LoginRequest {
    @NotBlank(message = "이메일을 입력하세요")
    @Pattern(regexp = "^[a-z0-9._%+-]+@[a-z0-9]+.[a-z]{2,6}$", flags = Pattern.Flag.CASE_INSENSITIVE)
    private String email;

    @NotBlank(message = "비밀번호를 입력하세요")
    private String password;
}
