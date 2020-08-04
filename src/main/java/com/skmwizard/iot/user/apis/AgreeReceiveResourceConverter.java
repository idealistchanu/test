package com.skmwizard.iot.user.apis;

import com.skmwizard.iot.user.services.AgreeReceive;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * @author ingu_ko
 * @since 2020-07-24
 */

@Component
@Validated
@RequiredArgsConstructor
class AgreeReceiveResourceConverter {
    AgreeReceive converts(String agree) {
        return AgreeReceive.builder()
            .code(agree)
            .build();
    }
}
