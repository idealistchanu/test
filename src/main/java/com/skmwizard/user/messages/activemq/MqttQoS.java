package com.skmwizard.user.messages.activemq;

import lombok.Getter;

/**
 * @author minkyu_kim
 * @since 2020-05-27
 */
@Getter
public enum MqttQoS {
    AT_MOST_ONCE(0),
    AT_LEAST_ONCE(1),
    EXACTLY_ONCE(2);

    private final int value;

    MqttQoS(int value) {
        this.value = value;
    }
}
