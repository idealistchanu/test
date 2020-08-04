package com.skmwizard.iot.user.apis;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
@ToString
class RefreshTokenRequest {
    @NotBlank(message = "Refresh Token을 입력하세요")
    private String refreshToken;
}
