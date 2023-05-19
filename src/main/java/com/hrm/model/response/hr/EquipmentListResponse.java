package com.hrm.model.response.hr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentListResponse {
	private Integer id;

	private String name;

	private String serialNumber;
	
	private Integer category;

	private String description;
	
	private String importDate;
	
	private String vendor;
	
	private String warrantyTime;
	
	private Integer status;
	
	private String employee;
}
