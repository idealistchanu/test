package com.skmwizard.iot.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@RestControllerAdvice
@Slf4j
public class UserExceptionHandler {
    private final DateTimeFormatter formatter;

    UserExceptionHandler() {
        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
    }

    @ExceptionHandler(NotAuthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ExceptionResponse handleNotAuthorizedException(NotAuthorizedException exception) {
        log.error("exception: {}", exception.getMessage());
        ExceptionResponse response = new ExceptionResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        response.setMessage(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        response.setCause(exception.getMessage());
        response.setTimestamp(LocalDateTime.now().format(this.formatter));

        return response;
    }
}
