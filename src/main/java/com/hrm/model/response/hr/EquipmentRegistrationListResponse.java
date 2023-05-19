package com.hrm.model.response.hr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentRegistrationListResponse {
    private String fullName;
    private String departmentName;
    private Integer category;
    private String description;
    private String requestDay;
    private String confirm;
    private Integer id;
    private Boolean deleteFlag;
    private String reason;
}
