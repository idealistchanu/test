package com.skmwizard.user.services;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class Token {

    private final String idToken;

    private final String accessToken;
    /**
     * 기간이 만료되지 않은 새로 고침 토큰
     * <pre>참고 : https://docs.aws.amazon.com/ko_kr/cognito/latest/developerguide/authentication.html</pre>
     */
    private final String refreshToken;

    private final Integer expiresIn;

    private final String tokenType;
}
