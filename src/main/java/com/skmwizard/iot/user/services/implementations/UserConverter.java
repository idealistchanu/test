package com.skmwizard.iot.user.services.implementations;

import com.skmwizard.iot.user.services.User;
import org.springframework.stereotype.Component;

/**
 * @author ingu_ko
 * @since 2020-07-28
 */
@Component
public class UserConverter {
    UserDocument converts(User user) {
        UserDocument document = new UserDocument();
        document.setEmail(user.getEmail());
        document.setName(user.getName());
        return document;
    }

    User converts(UserDocument user) {
        return User.builder()
            .email(user.getEmail())
            .sub(user.getSub())
            .name(user.getName())
            .phoneNumber(user.getPhoneNumber())
            .build();
    }
}
