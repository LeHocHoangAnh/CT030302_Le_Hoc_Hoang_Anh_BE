package com.hrm.model.response;

import java.util.Date;


public interface EmployeeToExcelResponse {
	Integer employeeId();
	
	String getEmployeeCode();
	
	String getEmployeeName();

	String getGender();
	
	String getDateOfBirth();
	
	String getAddress();
	
	String getPhoneNumber();
	
	String getEmail();
	
	String getRole();
	
	String getTypeContract();
	
	String getReviewDate();
	
	String getStatus();
	
	String getDateEntry();
	
	String getDateOut();
	
	String getDepartmentName();
	
	String getPosition();
	
	String getBankName();
	
	String getBankAccount();
	
	String getTaxCode();
	
	String getSafeCode();
	
	String getSalaryBasic();
}
