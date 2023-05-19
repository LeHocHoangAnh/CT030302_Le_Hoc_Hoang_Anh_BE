package com.hrm.utils;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hrm.entity.DetailTimeKeeping;
import com.hrm.entity.Employee;
import com.hrm.repository.DetailTimeKeepingRepository;
import com.hrm.repository.EmployeeRepository;

@Component
public class UtilsComponent {
	
	@Autowired
	DetailTimeKeepingRepository detailTimeKeepingRepository;
	
	@Autowired
	EmployeeRepository employeeRepository;
	
	// initialize a 0 values detailKeeping record for a new user
 	public DetailTimeKeeping initializeDetailKeeping(Integer userId, String timeSave) {
 		Float floatZero = Float.valueOf(0);
 		DetailTimeKeeping detailTimeKeeping = new DetailTimeKeeping();
 		Employee employee = employeeRepository.findSingleRecord(userId);
 		// value
 		detailTimeKeeping.setEmployee(employee);
 		detailTimeKeeping.setTimeSave(timeSave);
 		// still value but its 0
 		detailTimeKeeping.setLateTime(0);
 		detailTimeKeeping.setKeepingForget(0);
 		detailTimeKeeping.setLateHour("0");
 		detailTimeKeeping.setSalaryReal(floatZero);
 		detailTimeKeeping.setSalaryCount(floatZero);
 		detailTimeKeeping.setLeaveDayAccept(floatZero);

 		return detailTimeKeepingRepository.save(detailTimeKeeping);
 	}
}
