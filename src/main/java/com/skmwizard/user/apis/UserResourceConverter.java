package com.skmwizard.user.apis;

import com.skmwizard.user.services.User;
import org.springframework.stereotype.Component;

@Component
class UserResourceConverter {
    UserResponse converts(User user) {
        UserResponse resource = new UserResponse();
        resource.setEmail(user.getEmail());
        resource.setName(user.getName());
        resource.setPicture(user.getPicture());
        resource.setPhoneNumber(user.getPhoneNumber());
        return resource;
    }

    User converts(UserRequest resource) {
        return User.builder()
            .email(resource.getEmail())
            .name(resource.getName())
            .password(resource.getPassword())
            .phoneNumber(resource.getPhoneNumber())
            .build();
    }

    User converts(UserUpdateRequest resource) {
        return User.builder()
            .name(resource.getName())
            .phoneNumber(resource.getPhoneNumber())
            .build();
    }

    User converts(String username, UserUpdateRequest resource) {
        return User.builder()
            .email(username)
            .name(resource.getName())
            .phoneNumber(resource.getPhoneNumber())
            .build();
    }
}
