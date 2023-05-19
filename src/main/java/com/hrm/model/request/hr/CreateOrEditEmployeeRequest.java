package com.hrm.model.request.hr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrEditEmployeeRequest {
    private Integer id;
    private String address;
    private String dateEntry;
    private String dateOut;
    private String dateOfBirth;
    private Integer department;
    private String email;
    private String employeeCode;
    private String fullName;
    private Boolean gender;
    private Integer status;
    private String password;
    private String phoneNumber;
    private String safeCode;
    private Double salaryBasic;
    private String taxCode;
    private Integer typeContract;
    private String workName;
    private String pictureProfile;
    private List<String> selectedRoles;
    private String bankName;
    private String bankAccount;
    private String reviewDate;
    private String discordId;
    private String permAddress;
}
