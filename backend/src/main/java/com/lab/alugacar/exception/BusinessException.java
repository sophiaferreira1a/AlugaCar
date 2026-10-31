package com.lab.alugacar.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends ApiException {

    public BusinessException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
