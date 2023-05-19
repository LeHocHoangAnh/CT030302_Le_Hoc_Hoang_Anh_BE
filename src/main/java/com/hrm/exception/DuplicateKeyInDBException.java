package com.hrm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class DuplicateKeyInDBException extends RuntimeException{

    public DuplicateKeyInDBException(String exception){
        super(exception);
    }
}
