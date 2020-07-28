package com.skmwizard.iot.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderAsyncClient;

/**
 * @author ingu_ko
 * @since 2020-07-28
 */
@Configuration
public class AWSConfiguration {

    @Bean
    public CognitoIdentityProviderAsyncClient providerClient(
        @Value("${aws.region}") String region,
        @Value("${aws.accessKeyId}") String accessKeyId,
        @Value("${aws.secretAccessKey}") String secretAccessKey) {
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        StaticCredentialsProvider staticCredentialsProvider = StaticCredentialsProvider.create(awsBasicCredentials);

        return CognitoIdentityProviderAsyncClient.builder()
            .region(Region.of(region))
            .credentialsProvider(staticCredentialsProvider)
            .build();
    }
}
