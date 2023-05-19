package com.hrm.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "equipment")
public class Equipment extends CommonEntity {
	@Column(name = "name")
	private String name;

	@Column(name = "serial_number")
	private String serialNumber;

	@Column(name = "description")
	private String description;

	@Column(name = "import_date")
	private Date importDate;

	@Column(name = "status")
	private Integer status;

	@Column(name = "employee_id")
	private Integer employeeId;
	
	@Column(name = "category")
	private Integer category;
	
	@Column(name = "delete_flag")
	private Boolean deleteFlag;

	@Column(name = "warranty_time")
	private Date warrantyTime;

	@Column(name = "vendor")
	private String vendor;

	@Override
	public void setCommonDelete() {
		// TODO Auto-generated method stub
		super.setCommonDelete();
		this.deleteFlag = true;
	}
}
