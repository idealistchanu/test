package com.skmwizard.user.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@ToString
@AllArgsConstructor
public class ChangePassword {
    @NotNull
    @NotEmpty
    private final String oldPassword;

    @NotNull
    @NotEmpty
    private final String newPassword;
}
