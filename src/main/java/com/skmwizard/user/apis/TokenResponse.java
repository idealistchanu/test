package com.skmwizard.user.apis;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private Integer expiresIn;
    private String tokenType;
}
