package com.hrm.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetailTimeKeepingWithEmployeeResponse{
	private Integer id;//Employee id
	private String employeeCode;
	private String fullName;
	private Integer totalRecord;
	private DetailTimeKeepingDisplayResponse detailTimeKeeping;
}
