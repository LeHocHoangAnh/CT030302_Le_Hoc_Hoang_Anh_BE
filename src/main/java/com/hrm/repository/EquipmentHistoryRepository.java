package com.hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hrm.entity.EquipmentHistory;
import com.hrm.model.response.hr.EquipmentHistoryListResponse;

import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EquipmentHistoryRepository extends JpaRepository<EquipmentHistory, Integer>{
	
	@Query(value="SELECT * FROM equipment_history as eqh "
			+ "	  JOIN equipment AS equ ON (eqh.equipment_id = equ.id AND eqh.employee_id = equ.employee_id) "
			+ "	  WHERE eqh.equipment_id=:equipmentId"
			+ "	  ORDER BY eqh.id DESC LIMIT 1 ", nativeQuery=true)
	public EquipmentHistory findCurrentHistoryByEquipmentId(Integer equipmentId);
	
	@Query(value="SELECT new com.hrm.model.response.hr.EquipmentHistoryListResponse("
			+ "     eqh.id,"
			+ "		emp.employeeCode,"
			+ "     pro.fullName,"
			+ "     dep.name,"
			+ "		TO_CHAR(eqh.requestDate, 'DD-MM-YYYY'),"
			+ "		TO_CHAR(eqh.backDate, 'DD-MM-YYYY')) "
			+ "	  FROM EquipmentHistory as eqh "
			+ "	  JOIN Equipment AS equ ON eqh.equipmentId = equ.id"
			+ "	  JOIN Employee AS emp ON eqh.employeeId = emp.id"
			+ "   JOIN Profile AS pro ON emp.id = pro.employee.id"
			+ "	  LEFT JOIN Department AS dep ON emp.department.id = dep.id"
			+ "	  WHERE equ.id = :id"
			+ "	  ORDER BY eqh.requestDate DESC")
	public List<EquipmentHistoryListResponse> getHistoryListByEquipmentId(Integer id);
	
	@Query(value="SELECT * FROM equipment_history as eqh "
            + "   WHERE eqh.equipment_id = :id "
            + "     AND eqh.employee_id = :employeeId"
            + "     AND eqh.back_date is null"
            + "   ORDER BY eqh.id DESC LIMIT 1 ", nativeQuery=true)
    public EquipmentHistory findCurrentHistoryByEquipmentIdAndEmployeeId(Integer id, Integer employeeId);
}
