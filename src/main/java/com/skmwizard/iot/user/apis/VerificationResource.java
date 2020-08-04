package com.skmwizard.iot.user.apis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Pattern;

/**
 * @author jongduck_yoon
 * @since 2020-06-11
 */
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class VerificationResource {
    private String username;
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "Check your phone number")
    private String phoneNumber;
    private String verificationCode;

    public byte[] toPayload() {
        try {
            return new ObjectMapper().writeValueAsBytes(this);
        }catch (JsonProcessingException e) {
            return new byte[]{};
        }
    }
}
