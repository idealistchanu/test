package com.skmwizard.user.services.implementations;

import com.skmwizard.user.services.User;
import org.springframework.stereotype.Component;

/**
 * @author ingu_ko
 * @since 2020-07-28
 */
@Component
public class UserConverter {
    User converts(UserDocument user) {
        return User.builder()
            .email(user.getEmail())
            .name(user.getName())
            .phoneNumber(user.getPhoneNumber())
            .build();
    }
}
