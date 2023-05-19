package com.hrm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.hrm.entity.OtGeneral;

public interface OtGeneralRepository extends JpaRepository<OtGeneral, Integer>{
    
    @Query(value="SELECT * FROM ot_general as ot \r\n"
            + "    WHERE ot.month_action = :time",nativeQuery = true)
    OtGeneral getByMonthAction(String time);

    @Query(value="SELECT * FROM ot_general as ot \r\n"
            + "    WHERE ot.employee_id = :employeeId AND ot.month_action = :time",nativeQuery = true)
    OtGeneral getByMonthActionAndEmployee(Integer employeeId,String time);
    
    @Query(value="SELECT * FROM ot_general as ot WHERE ot.employee_id IN(:id) AND ot.month_action like :time",nativeQuery = true)
    List<OtGeneral> getOtFindByListId(List<Integer> id, String time);
}
