package com.skmwizard.iot.user.services;

import java.util.Optional;

public interface UserService {
    /**
     * 사용자 회원가입
     *
     * @param user 사용자 정보
     * @return 사용자 회원가입 정보
     */
    User signUp(User user);

    /**
     * 사용자 로그인
     *
     * @param user 사용자 정보
     * @return 토큰
     */
    Token login(User user);

    /**
     * 사용자 로그아웃
     *
     * @param accessToken 접근 토큰
     */
    void logout(String accessToken);

    /**
     * 사용자 정보 가져오기
     *
     * @param accessToken 접근 토큰
     * @return 사용자 정보
     */
    Optional<User> getUserInfo(String accessToken);

    /**
     * 사용자 속성 변경하기
     *
     * @param accessToken 접근 토큰
     * @param user        사용자 정보
     * @return 갱신한 사용자 정보
     */
    User updateUserInfo(String accessToken, User user);

    /**
     * 비밀번호 변경하기
     *
     * @param accessToken    접근 토큰
     * @param changePassword 변경할 비밀번호 정보
     */
    void resetPassword(String accessToken, ChangePassword changePassword);

    /**
     * Expired Token 을 갱신하여 Token 가져오기
     *
     * @param refreshToken 갱신 토큰
     * @return 발급된 토큰 정보
     */
    Token refreshToken(String refreshToken);
}
