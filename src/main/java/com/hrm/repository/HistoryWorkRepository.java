package com.hrm.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hrm.entity.HistoryWork;
import com.hrm.model.request.hr.HistoryWorkReponse;

@Repository
public interface HistoryWorkRepository extends JpaRepository<HistoryWork, Integer> {
    
    @Query(value="SELECT his.id_employee as IdEmployee,pro.full_name as Name,to_char(his.time_start, 'dd-mm-yyyy') as TimeStart,\r\n"
            + "to_char(his.time_end, 'dd-mm-yyyy') as TimeEnd,his.role as Role FROM history_work as his\r\n"
            + "JOIN employee as emp ON emp.id = his.id_employee\r\n"
            + "JOIN profile as pro ON pro.employee_id = emp.id WHERE his.id=:id",nativeQuery = true)
   Optional<HistoryWorkReponse> getDetailById(Integer id);
    
}
