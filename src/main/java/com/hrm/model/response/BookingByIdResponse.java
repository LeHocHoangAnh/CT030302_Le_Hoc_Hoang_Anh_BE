package com.hrm.model.response;

import java.sql.Timestamp;
import java.util.List;

import com.hrm.entity.BookingDayOff;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingByIdResponse{
	private Integer id;
	
	private String reason;
	
	private Integer status;
	
	private Integer roomId;
	
	private String roomName;
	
	private Timestamp timeStart;
	
	private Timestamp timeEnd;
	
	private Integer periodType;
	
	private List<String> daysOfWeek;
	
	private Integer employeeId;
	
	private String employeeName;
} 
