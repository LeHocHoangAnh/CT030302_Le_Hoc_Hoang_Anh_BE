package com.hrm.model.response.hr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeAndPasswordResponse {
    CreateOrEditEmployeeResponse response;
    String password;
}
