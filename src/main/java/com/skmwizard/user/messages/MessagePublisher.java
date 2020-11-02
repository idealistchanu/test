package com.skmwizard.user.messages;

import reactor.core.publisher.Mono;

/**
 * @author jongduck_yoon
 * @since 2020-06-24
 */
public interface MessagePublisher {
    Mono<Void> publish(PublishableEvent event, String messageId, byte[] message);

    Mono<Void> publish(PublishableEvent event, String messageId, String message);

    Mono<Void> publish(PublishableEvent event, String messageId, Publishable publishable);
}
