package com.skmwizard.user.apis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class UserUpdateRequest {
    @Schema(title = "이름", example = "홍길동", required = true)
    @NotBlank(message = "이름을 입력하세요")
    private String name;

    @Schema(title = "휴대폰 번호", example = "01012348765", required = true)
    @NotBlank(message = "휴대폰 번호를 입력하세요")
    private String phoneNumber;

    @Schema(title = "수신 동의", example = "[\"SMS\", \"EMAIL\"]")
    private Set<String> agreeList;
}
