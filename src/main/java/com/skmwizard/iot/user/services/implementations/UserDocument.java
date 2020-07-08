package com.skmwizard.iot.user.services.implementations;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document("user")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
class UserDocument {
    @Id
    private String email;

    private String nickname;
}
