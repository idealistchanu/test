package com.skmwizard.iot.user.apis;

import com.skmwizard.iot.user.services.User;
import org.springframework.stereotype.Component;

@Component
class UserResourceConverter {
    UserResponse converts(User user) {
        UserResponse resource = new UserResponse();
        resource.setEmail(user.getEmail());
        resource.setName(user.getName());
        resource.setNickname(user.getNickname());
        resource.setAddress(user.getAddress());
        resource.setStatus(user.getStatus());

        return resource;
    }

    User converts(UserResponse resource) {
        return User.builder()
                .email(resource.getEmail())
                .name(resource.getName())
                .password(resource.getPassword())
                .nickname(resource.getNickname())
                .address(resource.getAddress())
                .status(resource.getStatus())
                .build();
    }

    User converts(UserRequest resource) {
        return User.builder()
            .email(resource.getEmail())
            .name(resource.getName())
            .password(resource.getPassword())
            .nickname(resource.getNickname())
            .address(resource.getAddress())
            .status(resource.getStatus())
            .build();
    }
}
