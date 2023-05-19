package com.hrm.model.response.leader;

import java.util.List;

import com.hrm.model.response.employee.TimeKeepingSummary;
import com.hrm.model.response.employee.TimekeepingDetail;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DetailBookingEntityResponse{
	private Integer id;
	private Integer employeeId;
	private String fullName;
	private String departmentName;
	private String status;
	private String requestDay;
	private String backDay;
	private String reason;
	private Integer confirm;
	private String approver;
	private String evidenceImage;
	private String projectName;
	private Boolean deleteFlag;
	private Float totalOtTime;
	
}
