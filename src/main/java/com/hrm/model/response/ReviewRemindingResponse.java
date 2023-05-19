package com.hrm.model.response;

import java.util.Date;


public interface ReviewRemindingResponse {
	Integer getEmployeeId();
	
	String getEmployeeCode();
	
	String getEmployeeName();
	
	Integer getTypeContract();
	
	Date getReviewDate();
	
	String getDepartmentName();
	
	Integer getSeniority();
}
