package com.skmwizard.iot.user.apis;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Setter
@Getter
class PasswordChangeRequest {
    @NotNull
    @NotEmpty
    private String currentPassword;

    @NotNull
    @NotEmpty
    private String newPassword;
}
