package com.skmwizard.iot.user.apis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
@ToString
class RefreshTokenRequest {
    @Schema(title = "Refresh Token", example = "eyJjdHkiOiJKV1QiLCJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiUlNBLU9BRVAifQ.", required = true)
    @NotBlank(message = "Refresh Token을 입력하세요")
    private String refreshToken;
}
