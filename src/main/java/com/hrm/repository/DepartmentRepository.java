package com.hrm.repository;

import com.hrm.entity.Department;
import com.hrm.model.response.DropDownResponse;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department,Integer> {
    @Query(value="SELECT de.id as Value,de.name as Name FROM department as de",nativeQuery = true)
    List<DropDownResponse> getListDropDownDepartment();
}
