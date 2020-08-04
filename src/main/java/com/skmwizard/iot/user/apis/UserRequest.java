package com.skmwizard.iot.user.apis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class UserRequest {
    @NotBlank(message = "이메일을 입력하세요")
    @Pattern(regexp = "^[a-z0-9._%+-]+@[a-z0-9]+.[a-z]{2,6}$", flags = Pattern.Flag.CASE_INSENSITIVE)
    private String email;

    @NotBlank(message = "이름을 입력하세요")
    private String name;

    @NotBlank(message = "비밀번호을 입력하세요")
    private String password;

    @NotBlank(message = "휴대폰 번호을 입력하세요")
    private String phoneNumber;

    private Set<String> agreeList;

    private String status;
}
