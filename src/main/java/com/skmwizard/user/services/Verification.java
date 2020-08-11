package com.skmwizard.user.services;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * @author jongduck_yoon
 * @since 2020-06-11
 */
@Getter
@Builder
@ToString
public class Verification {
    private String checker;
    private String verificationCode;
    private LocalDateTime createdDatetime;
}
