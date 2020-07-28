package com.skmwizard.iot.user.apis;

import com.skmwizard.iot.user.services.Token;
import org.springframework.stereotype.Component;

@Component
class TokenResourceConverter {
    TokenResponse converts(Token token) {
        TokenResponse resource = new TokenResponse();
        resource.setIdToken(token.getIdToken());
        resource.setAccessToken(token.getAccessToken());
        resource.setRefreshToken(token.getRefreshToken());
        resource.setTokenType(token.getTokenType());
        resource.setExpiresIn(token.getExpiresIn());

        return resource;
    }
}
