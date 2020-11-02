package com.skmwizard.user.clouds.amazon;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderAsyncClient;

import javax.annotation.PreDestroy;

/**
 * @author ingu_ko
 * @since 2020-10-12
 */
@Configuration
public class AmazonWebServiceConfiguration {
    private final CognitoIdentityProviderAsyncClient providerClient;

    public AmazonWebServiceConfiguration(
        @Value("${aws.accessKeyId}") String accessKeyId,
        @Value("${aws.secretAccessKey}") String secretAccessKey,
        @Value("${aws.region}") String region) {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);
        this.providerClient = CognitoIdentityProviderAsyncClient.builder()
            .region(Region.of(region))
            .credentialsProvider(credentialsProvider)
            .build();
    }

    @Bean
    public CognitoIdentityProviderAsyncClient cognitoIdentityProviderAsyncClient() {
        return this.providerClient;
    }

    @PreDestroy
    public void preDestroy() {
        providerClient.close();
    }
}
