package com.hrm.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeKeepingGeneralResponse {
    String employeeCode;
    String fullName;
    String typeContract;
    String email;
    Integer salaryReal;
    Integer forgetTimeKeeping;
    Integer countTotalDayLate;
    Integer totalSalary;
}
