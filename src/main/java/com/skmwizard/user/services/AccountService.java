package com.skmwizard.user.services;

import reactor.core.publisher.Mono;

public interface AccountService {

    /**
     * 사용자 아이디 찾기
     *
     * @param name        이름
     * @param phoneNumber 휴대폰 번호
     * @return 사용자 정보
     */
    Mono<User> find(String name, String phoneNumber);

    /**
     * 사용자 존재 여부
     *
     * @param username 아이디
     */
    Mono<Void> exists(String username);

    /**
     * 사용자 회원가입
     *
     * @param user 사용자 정보
     * @return 사용자 회원가입 정보
     */
    Mono<User> signUp(User user);

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
     * @param username 아이디
     */
    Mono<Void> logout(String username);

    /**
     * 사용자 속성 변경하기
     *
     * @param username 아이디
     * @param user     사용자 정보
     * @return 갱신한 사용자 정보
     */
    Mono<User> updateUserInfo(String username, User user);

    /**
     * 사진 변경하기
     *
     * @param username 아이디
     * @param picture 새로운 사진
     */
    Mono<Void> changePicture(String username, String picture);

    /**
     * 비밀번호 재설정하기
     *
     * @param username      아이디
     * @param resetPassword 새로운 비밀번호
     */
    Mono<Void> resetPassword(String username, String resetPassword);

    /**
     * Expired Token 을 갱신하여 Token 가져오기
     *
     * @param refreshToken 갱신 토큰
     * @return 발급된 토큰 정보
     */
    Mono<Token> refreshToken(String refreshToken);
}
