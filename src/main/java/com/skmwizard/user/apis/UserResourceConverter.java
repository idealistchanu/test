package com.skmwizard.user.apis;

import com.skmwizard.user.services.User;
import org.springframework.stereotype.Component;

@Component
class UserResourceConverter {
    UserResponse converts(User user) {
        UserResponse resource = new UserResponse();
        resource.setEmail(user.getEmail());
        resource.setName(user.getName());
        resource.setPhoneNumber(user.getPhoneNumber());
        resource.setStatus(user.getStatus());
        return resource;
    }

    User converts(UserRequest resource) {
        return User.builder()
            .email(resource.getEmail())
            .name(resource.getName())
            .password(resource.getPassword())
            .phoneNumber(resource.getPhoneNumber())
            .status(resource.getStatus())
            .build();
    }
}
