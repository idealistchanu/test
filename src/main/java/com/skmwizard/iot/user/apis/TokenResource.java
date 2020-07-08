package com.skmwizard.iot.user.apis;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
class TokenResource {
    private String idToken;
    private String accessToken;
    private String refreshToken;
    private Integer expiresIn;
    private String tokenType;
}
