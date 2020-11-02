package com.skmwizard.user.messages;

import lombok.Getter;

/**
 * @author ingu_ko
 * @since 2020-06-24
 */
@Getter
public enum PublishableEvent {
    USER_CREATED("skmagic/user/created"),
    USER_UPDATED("skmagic/user/{id}/updated"),
    USER_DELETED("skmagic/user/{id}/deleted");

    String topic;

    PublishableEvent(String topic) {
        this.topic = topic;
    }
}
