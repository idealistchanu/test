package com.skmwizard.iot.user.apis;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
class RefreshTokenRequest {
    private String refreshToken;
}
