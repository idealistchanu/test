package com.skmwizard.iot.user.services.implementations;

import com.skmwizard.iot.user.services.AgreeReceive;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

/**
 * @author ingu_ko
 * @since 2020-07-24
 */
@Component
@Validated
public class AgreeReceiveConverter {

    AgreeReceive converts(@NotNull AgreeReceiveDocument document) {
        return AgreeReceive.builder()
            .code(document.getCode())
            .agreedDatetime(document.getAgreedDatetime())
            .build();
    }

    AgreeReceiveDocument converts(@NotNull AgreeReceive AgreeReceive) {
        AgreeReceiveDocument document = new AgreeReceiveDocument();
        document.setCode(AgreeReceive.getCode());
        document.setAgreedDatetime(AgreeReceive.getAgreedDatetime());
        return document;
    }
}
