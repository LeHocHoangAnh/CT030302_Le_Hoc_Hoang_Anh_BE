package com.hrm.model.response.hr;

import lombok.Data;

@Data
public class ListEmployeeResponse {
    private Integer id;
    private String employeeCode;
    private String email;
    private String fullName;
    private String gender;
    private String typeContract;
    private String dateEntry;
    private String position;
    private String phoneNumber;
    private String dateOfBirth;
    private String departmentName;
}
