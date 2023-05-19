package com.hrm.exception;

import com.hrm.common.Constants;
import com.hrm.model.ErrorResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@SuppressWarnings({"unchecked", "rawtypes"})
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Map<String, String> errors = new HashMap<>();

    @ExceptionHandler(RecordNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(RecordNotFoundException ex) {
        errors.put(Constants.RECORD_NOT_FOUND, ex.getLocalizedMessage());
        ErrorResponse error = new ErrorResponse(Constants.HTTP_CODE_404, new Date(), Constants.RECORD_NOT_FOUND,
                errors);
        return new ResponseEntity(error, HttpStatus.NOT_FOUND);
    }

    @Override
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        ErrorResponse error = new ErrorResponse(Constants.HTTP_CODE_400, new Date(), ex.getLocalizedMessage(), errors);
        return new ResponseEntity(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<Object> handleAnyException(Exception ex) {
        ErrorResponse error = new ErrorResponse(Constants.HTTP_CODE_500, new Date(), ex.getLocalizedMessage(), errors);
        return new ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
