package com.hrm.model.request.hr;

import com.hrm.model.request.PaginationRequest;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ListEmployeeRequest extends PaginationRequest{
    private String key;
    private Boolean isLeader = false;
    private String contract;
    private String department;
    private String position;
    private Boolean inWorking;
}
