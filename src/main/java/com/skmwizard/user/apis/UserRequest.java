package com.skmwizard.user.apis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class  UserRequest {
    @Schema(title = "이메일", example = "user_id@gmail.com", required = true)
    @NotBlank(message = "이메일을 입력하세요")
    @Pattern(regexp = "^[a-z0-9._%+-]+@[a-z0-9]+.[a-z]{2,6}$", flags = Pattern.Flag.CASE_INSENSITIVE)
    private String email;

    @Schema(title = "이름", example = "홍길동", required = true)
    @NotBlank(message = "이름을 입력하세요")
    private String name;

    @Schema(title = "비밀번호", example = "8자 이상의 영문/숫자/특수기호 중 2개 이상 조합", required = true)
    @NotBlank(message = "비밀번호를 입력하세요")
    private String password;

    @Schema(title = "휴대폰 번호", example = "01012348765", required = true)
    @NotBlank(message = "휴대폰 번호를 입력하세요")
    private String phoneNumber;

    @Schema(title = "수신 동의", example = "[\"SMS\", \"EMAIL\"]")
    private Set<String> agreeList;
}
