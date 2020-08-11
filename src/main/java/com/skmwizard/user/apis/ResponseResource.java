package com.skmwizard.user.apis;

import lombok.Getter;
import lombok.Setter;

/**
 * @author ingu_ko
 * @since 2020-08-05
 */
@Setter
@Getter
class ResponseResource<T> {
    private int statusCode;
    private String message;
    private T payload;
}
