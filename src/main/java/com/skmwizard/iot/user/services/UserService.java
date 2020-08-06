package com.skmwizard.iot.user.services;

import reactor.core.publisher.Mono;

public interface UserService {

    /**
     * 사용자 이메일 찾기
     *
     * @param username 이름
     * @param phoneNumber 휴대폰 번호
     * @return 사용자 정보
     */
    Mono<User> find(String username, String phoneNumber);

    /**
     * 사용자 존재 여부
     *
     * @param email 이메일
     */
    Mono<Void> exists(String email);

    /**
     * 사용자 회원가입
     *
     * @param user 사용자 정보
     * @return 사용자 회원가입 정보
     */
    Mono<Void> signUp(User user);

    /**
     * 사용자 로그인
     *
     * @param user 사용자 정보
     * @return 토큰
     */
    Mono<Token> login(User user);

    /**
     * 사용자 로그아웃
     *
     * @param email 이메일
     */
    Mono<Void> logout(String email);

    /**
     * 사용자 정보 가져오기
     *
     * @param accessToken 접근 토큰
     * @return 사용자 정보
     */
    Mono<User> getUserInfo(String accessToken);

    /**
     * 사용자 정보 가져오기
     *
     * @param username 이름
     * @return 사용자 정보
     */
    Mono<User> get(String username);

    /**
     * 사용자 속성 변경하기
     *
     * @param accessToken 접근 토큰
     * @param user        사용자 정보
     * @return 갱신한 사용자 정보
     */
    Mono<User> updateUserInfo(String accessToken, User user);

    /**
     * 비밀번호 변경하기
     *
     * @param accessToken    접근 토큰
     * @param changePassword 변경할 비밀번호 정보
     */
    Mono<Void> resetPassword(String accessToken, ChangePassword changePassword);

    /**
     * 비밀번호 변경하기
     * @param email 이메일
     * @param changePassword 변경할 비밀번호 정보
     */
    Mono<Void> changePassword(String email, ChangePassword changePassword);

    /**
     * Expired Token 을 갱신하여 Token 가져오기
     *
     * @param refreshToken 갱신 토큰
     * @return 발급된 토큰 정보
     */
    Mono<Token> refreshToken(String refreshToken);
}
