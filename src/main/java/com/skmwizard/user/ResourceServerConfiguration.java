package com.skmwizard.user;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;


@EnableWebFluxSecurity
public class ResourceServerConfiguration {

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .csrf().disable()
            .authorizeExchange(exchanges ->
                exchanges
                    .pathMatchers("/signup").permitAll()
                    .pathMatchers("/login").permitAll()
                    .pathMatchers("/token/refresh").permitAll()
                    .pathMatchers("/users/find").permitAll()
                    .pathMatchers("/users/check").permitAll()
                    .pathMatchers("/users/reset-password").permitAll()
                    .pathMatchers("/verifications/sms", "/verifications/email", "/verifications/confirm", "/verifications/check").permitAll()
                    .anyExchange().authenticated()
            )
            .logout().logoutUrl("/admin/logout").and()
            .oauth2ResourceServer((oauth2ResourceServer) ->
                oauth2ResourceServer
                    .jwt(withDefaults())
            );
        return http.build();
    }
}