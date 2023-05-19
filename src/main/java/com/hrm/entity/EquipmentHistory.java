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
@Table(name = "equipment_history")
public class EquipmentHistory extends CommonEntity {
	@Column(name = "equipment_id")
	private Integer equipmentId;

	@Column(name = "employee_id")
	private Integer employeeId;

	@Column(name = "request_date")
	private Date requestDate;

	@Column(name = "back_date")
	private Date backDate;
}
