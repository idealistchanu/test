package com.skmwizard.user.messages.activemq;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author minkyu_kim
 * @since 2020-05-27
 */
@Configuration
class MqttActiveMqConfiguration {
    @Bean
    public MqttConnectOptions mqttConnectOptions(
        @Value("${activemq.username}") String username,
        @Value("${activemq.password}") String password) {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(username);
        options.setPassword(password.toCharArray());

        return options;
    }
}
