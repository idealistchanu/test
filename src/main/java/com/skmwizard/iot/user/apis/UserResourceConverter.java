package com.skmwizard.iot.user.apis;

import com.skmwizard.iot.user.services.User;
import org.springframework.stereotype.Component;

@Component
class UserResourceConverter {
    UserResource converts(User user) {
        UserResource resource = new UserResource();
        resource.setEmail(user.getEmail());
        resource.setName(user.getName());
        resource.setNickname(user.getNickname());
        resource.setAddress(user.getAddress());
        resource.setStatus(user.getStatus());

        return resource;
    }

    User converts(UserResource resource) {
        return User.builder()
                .nickname(resource.getNickname())
                .address(resource.getAddress())
                .build();
    }
}
