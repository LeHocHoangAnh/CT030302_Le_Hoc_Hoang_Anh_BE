package com.hrm.model.response.hr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentsListResponse {
	private Integer departmentID;

	private String departmentName;

	private int action;

	private Integer member;
}
