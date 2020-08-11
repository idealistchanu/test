package com.skmwizard.user.apis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @author jongduck_yoon
 * @since 2020-06-11
 */
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class VerificationSmsRequest {
    @Schema(title = "휴대폰 번호", example = "01012348765")
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "Check your phone number")
    @NotBlank(message = "휴대폰 번호를 입력하세요")
    private String phoneNumber;
}
