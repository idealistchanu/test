package com.skmwizard.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.exception.SdkServiceException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotFoundException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UsernameExistsException;

import java.rmi.NoSuchObjectException;
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
    public ExceptionResponse handleNotAuthorizedException(CognitoIdentityProviderException exception) {
        return makeResponse(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ExceptionResponse handleUserNotFoundException() {
        return makeResponse(HttpStatus.UNAUTHORIZED, "Incorrect username or password.");
    }

    @ExceptionHandler(UsernameExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionResponse handleNotAuthorizedException(UsernameExistsException exception) {
        return makeResponse(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionResponse handleDuplicateKeyException(DuplicateKeyException exception) {
        return makeResponse(HttpStatus.CONFLICT, exception.getMessage() + " already exist.");
    }

    @ExceptionHandler(NoSuchObjectException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleBadRequestException(NoSuchObjectException exception) {
        return makeResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler({SdkException.class, SdkClientException.class, SdkServiceException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleSdkException(SdkException exception) {
        return makeResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleMethodArgumentNotValidException(WebExchangeBindException exception) {
        return makeResponse(HttpStatus.BAD_REQUEST, exception.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

    private ExceptionResponse makeResponse(HttpStatus status, String message) {
        log.error("Exception: {}", message);
        ExceptionResponse response = new ExceptionResponse();
        response.setStatusCode(status.value());
        response.setMessage(status.getReasonPhrase());
        response.setCause(message.replaceAll(" \\(.*\\)", "")); // Regex : from blank to () -> " \\(.*\\)"
        response.setTimestamp(LocalDateTime.now().format(this.formatter));
        return response;
    }
}
