package com.hrm.model.response.hr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListHistoryResponse {
    private Integer id;
    private String employeeCode;
    private String fullName;
    private String timeStart;
    private String timeEnd;
    private String role;
    private Integer idEmployee;
    private Long otTime;
}
