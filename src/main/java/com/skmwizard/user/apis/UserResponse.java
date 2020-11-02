package com.skmwizard.user.apis;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
class UserResponse {
    private String email;
    private String name;
    private String phoneNumber;
    private String picture;
}
