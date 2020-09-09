package com.skmwizard.user;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfiguration {
    @Bean
    public GroupedOpenApi accountApi() {
        return GroupedOpenApi.builder()
            .group("account")
            .packagesToScan("com.skmwizard.user.apis")
            .pathsToMatch("/signup", "/me", "/me/**", "/users/**/check", "/users/**/find", "/users/**/reset-password")
            .build();
    }

    @Bean
    public GroupedOpenApi authenticatonApi() {
        return GroupedOpenApi.builder()
            .group("authentication")
            .packagesToScan("com.skmwizard.user.apis")
            .pathsToMatch("/login", "/logout", "/token/refresh")
            .build();
    }

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
            .group("user")
            .packagesToScan("com.skmwizard.user.apis")
            .pathsToMatch("/users", "/users/**")
            .build();
    }

    @Bean
    public GroupedOpenApi verificationApi() {
        return GroupedOpenApi.builder()
            .group("verification")
            .packagesToScan("com.skmwizard.user.apis")
            .pathsToMatch("/verifications", "/verifications/**")
            .build();
    }

    @Bean
    public OpenAPI iokOpenAPI() {
        return new OpenAPI()
            .info(new Info().title("User API Documentation")
                .description("사용자, 인증 관리에 관한 API 입니다.")
                .version("0.0.1")
                .license(new License().name("Copyright © SK매직(주) All rights reserved.").url("https://www.skmagic.com")));
    }
}