package com.hrm.model.response.leader;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListBookingResponse {
    private String fullName;
    private String departmentName;
    private String status;
    private String requestDay;
    private String confirm;
    private Integer id;
    private Boolean deleteFlag;
    private String reason;
}
