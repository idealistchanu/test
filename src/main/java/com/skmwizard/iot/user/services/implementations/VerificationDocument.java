package com.skmwizard.iot.user.services.implementations;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * @author jongduck_yoon
 * @since 2020-06-11
 */
@Document("verification")
@Setter
@Getter
class VerificationDocument {
    @Id
    private String checker;

    @NotBlank
    private String verificationCode;

    @NotBlank
    private LocalDateTime createdDatetime;
}
