package com.hrm.model.response.employee;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfilesGeneralResponse {
    
    private EmployeeProfileResponse employee;
    
    private List<HistoryByEmployeeIdResponse> history;

}
