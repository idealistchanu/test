package com.skmwizard.iot.user.services;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class User {
    private final String email;
    private final String sub;
    private final String name;
    private final String password;
    private final String phoneNumber;
    private final String status;
}
