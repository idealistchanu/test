package com.skmwizard.user.apis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Setter
@Getter
@ToString(doNotUseGetters = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class LoginRequest {
    @Schema(title = "이메일", example = "user_id@gmail.com", required = true)
    @NotBlank(message = "이메일을 입력하세요")
    @Pattern(regexp = "^[a-z0-9._%+-]+@[a-z0-9]+.[a-z]{2,6}$", flags = Pattern.Flag.CASE_INSENSITIVE, message = "이메일 형식이 아닙니다")
    private String email;

    @Schema(title = "비밀번호", example = "8자 이상의 영문/숫자/특수기호 중 2개 이상 조합", required = true)
    @NotBlank(message = "비밀번호를 입력하세요")
    private String password;
}
