package com.hrm.model.response.hr;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EquipmentHistoryListResponse {
	private Integer id;

	private String employeeCode;

	private String employeeName;

	private String departmentName;
	
	private String requestDate;
	
	private String backDate;
}
