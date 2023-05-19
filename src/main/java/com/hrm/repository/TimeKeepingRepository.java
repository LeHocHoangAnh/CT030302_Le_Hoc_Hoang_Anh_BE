package com.hrm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hrm.entity.TimeKeeping;
import com.hrm.model.DropDownResponse;
import com.hrm.model.response.employee.mapping.TimekeepingResponseMapping;

@Repository
public interface TimeKeepingRepository extends JpaRepository<TimeKeeping, Integer> {
    @Query(value = "select tk.check_in checkIn,tk.check_out checkOut,tk.date_working dateWorking "
            + "from time_keeping tk " + "join employee e on tk.employee_id = e.id "
            + "where e.id =:id and e.delete_flag = 0 and  (DATE(tk.date_working) between DATE(:startDate) AND  DATE(:endDate)) ", nativeQuery = true)
    List<TimekeepingResponseMapping> findByEmployeeIdAndTime(Integer id, String startDate, String endDate);

    @Query(value = "SELECT * FROM time_keeping as tk Where to_char(tk.date_working, 'YYYY-MM') =:year", nativeQuery = true)
    List<TimeKeeping> getListTimeKeepingByDateWorking(String year);

    @Query(value = "SELECT DISTINCT to_char(tk.date_working, 'YYYY-MM') as name FROM time_keeping as tk \r\n"
            + "ORDER BY name DESC", nativeQuery = true)
    List<DropDownResponse> getListTime();
    
    @Query(value = "SELECT id, to_char(tk.date_working, 'YYYY-MM') as name FROM time_keeping as tk \r\n"
            + "ORDER BY id DESC limit 1", nativeQuery = true)
    DropDownResponse getLastestUpdateTime();

    @Query(value="Select * from  time_keeping as ti WHERE to_char(ti.date_working,'YYYY-MM')=:time",nativeQuery = true)
    List<TimeKeeping> findByDateWorking(String time);
}
