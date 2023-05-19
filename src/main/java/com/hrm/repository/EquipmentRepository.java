package com.hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hrm.entity.Equipment;
import com.hrm.entity.EquipmentHistory;
import com.hrm.model.DropDownResponse;
import com.hrm.model.response.employee.EquipmentListResponse;

import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EquipmentRepository extends JpaRepository<Equipment, Integer>{


	@Query(value="SELECT DISTINCT"
			+ "		equ.id, "
			+ "		equ.name, "
			+ "		equ.serial_number AS serialNumber, "
			+ "		equ.category, equ.description, "
			+ "		TO_CHAR(eqh.request_date, 'DD-MM-YYYY')AS requestDate"
			+ "	  FROM equipment AS equ "
			+ "	  JOIN equipment_history AS eqh ON eqh.equipment_id = equ.id"
			+ "	  WHERE delete_flag = false AND equ.employee_id = :id AND eqh.back_date is null", nativeQuery = true)
	List<EquipmentListResponse> getUserEquipmentList(Integer id);
    
    @Query(value="SELECT "
 		   + "	CAST(emp.id AS CHARACTER VARYING) AS id, "
 		   + "  CAST((emp.employee_code||' - '||pro.full_name||'( '||dep.name||')') AS CHARACTER VARYING) AS name "
 		   + "FROM profile AS pro "
 		   + "	JOIN employee AS emp ON pro.employee_id = emp.id "
 		   + "	JOIN department AS dep ON emp.department_id = dep.id "
 		   + "WHERE emp.role_group_id in (2, 7, 8, 9) " // where employee's role is 'Leader' or 'Leader HR' or 'Leader Comtor' or 'Leader HR Comtor'
 		   + "	 AND pro.date_out IS NULL "
 		   + "	 AND dep.action = 1 "
 		   + "ORDER BY emp.id ASC", nativeQuery = true) // and department is still in use
	List<DropDownResponse> getAllApprover();
    
}
