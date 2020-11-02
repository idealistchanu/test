package com.skmwizard.user.messages.activemq;

import com.skmwizard.user.messages.MessagePublisher;
import com.skmwizard.user.messages.Publishable;
import com.skmwizard.user.messages.PublishableEvent;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Optional;

/**
 * @author jongduck_yoon
 * @since 2020-06-24
 */
@Component
@Slf4j
public class MqttActiveMqPublisher implements MessagePublisher {
    private final MqttAsyncClient mqttClient;
    private final MqttConnectOptions mqttConnectOptions;

    MqttActiveMqPublisher(
        @Value("${activemq.endpoint}") String endpoint,
        @Value("${activemq.client.id") String baseClientId,
        MqttConnectOptions mqttConnectOptions) throws MqttException {
        final String clientId = baseClientId + this.hashCode();
        this.mqttClient = new MqttAsyncClient(endpoint, clientId, new MemoryPersistence());
        this.mqttConnectOptions = mqttConnectOptions;
    }

    @PostConstruct
    public void connect() throws MqttException {
        mqttClient.connect(this.mqttConnectOptions).waitForCompletion();
    }

    @Override
    public Mono<Void> publish(PublishableEvent event, String messageId, byte[] message) {
        return Mono.just(new MqttMessage(message))
            .map(payload -> {
                String topic = Optional.ofNullable(messageId)
                    .map(id -> event.getTopic().replace("{id}", id))
                    .orElse(event.getTopic());

                try {
                    mqttClient.publish(topic, payload);
                    log.info("[publish] topic: {}, payload: {}", topic, new String(payload.getPayload()));
                    return Mono.empty();
                } catch (MqttException e) {
                    log.error("[publish] Exception: {}", e.getMessage());
                    return Mono.error(e);
                }
            }).then();
    }

    @Override
    public Mono<Void> publish(PublishableEvent event, String messageId, String message) {
        return this.publish(event, messageId, message.getBytes());
    }

    @Override
    public Mono<Void> publish(PublishableEvent event, String messageId, Publishable publishable) {
        return this.publish(event, messageId, publishable.toPayload());
    }

    @PreDestroy
    public void disconnect() throws MqttException {
        mqttClient.disconnect();
        mqttClient.close();
    }

}
