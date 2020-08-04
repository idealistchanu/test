package com.skmwizard.iot.user.services.implementations;

import com.skmwizard.iot.user.services.Token;
import com.skmwizard.iot.user.services.User;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;

import java.util.LinkedList;
import java.util.List;

@Component
class AmazonUserConverter {
    User converts(List<AttributeType> attributeTypes) {
        User.UserBuilder userBuilder = User.builder();
        for (AttributeType attributeType : attributeTypes) {
            switch (attributeType.name()) {
                case "name":
                    userBuilder.name(attributeType.value());
                    break;
                case "email":
                    userBuilder.email(attributeType.value());
                    break;
                default:
            }
        }

        return userBuilder.build();
    }

    List<AttributeType> converts(User user) {
        LinkedList<AttributeType> attributeTypes = new LinkedList<>();

        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            attributeTypes.add(AttributeType.builder().name("email").value(user.getEmail()).build());
        }
        if (user.getName() != null && !user.getName().isEmpty()) {
            attributeTypes.add(AttributeType.builder().name("name").value(user.getName()).build());
        }

        return attributeTypes;
    }

    Token converts(AuthenticationResultType authenticationResultType) {
        return Token.builder()
                .idToken(authenticationResultType.idToken())
                .accessToken(authenticationResultType.accessToken())
                .refreshToken(authenticationResultType.refreshToken())
                .expiresIn(authenticationResultType.expiresIn())
                .tokenType(authenticationResultType.tokenType())
                .build();
    }
}
