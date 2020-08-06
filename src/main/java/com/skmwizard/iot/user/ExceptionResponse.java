package com.skmwizard.iot.user;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ExceptionResponse {
    private int statusCode;
    private String message;
    private String cause;
    private String timestamp;
}