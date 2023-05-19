package com.hrm.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Map;
@Getter
@Setter
@AllArgsConstructor
public class ErrorResponse {
    private String status;
    private Date timestamp;
    private String message;
    private Map<String, String> details;
}
