package com.skmwizard.user.services;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * @author ingu_ko
 * @since 2020-07-24
 */
@Getter
@Builder
@ToString
public class AgreeReceive {
    private final String code;

    private final LocalDateTime agreedDatetime;
}
