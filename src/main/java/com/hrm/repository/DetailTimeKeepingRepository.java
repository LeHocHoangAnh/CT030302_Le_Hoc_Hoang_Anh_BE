package com.hrm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hrm.entity.DetailTimeKeeping;
import com.hrm.model.response.DetailTimeKeepingForExportResponse;

@Repository
public interface DetailTimeKeepingRepository extends JpaRepository<DetailTimeKeeping, Integer> {
    
    @Query(value="Select * FROM detail_time_keeping as de WHERE de.time_save =:timeSave",nativeQuery = true)
    List<DetailTimeKeeping> findByTimeSave(String timeSave);
    
    @Query(value="SELECT\r\n"
            + "                em.employee_code AS EmployeeCode,\r\n"
            + "                pro.full_name AS FullName,\r\n"
            + "                em.email AS Email,\r\n"
            + "                de.salary_real AS SalaryReal,\r\n"
            + "                de.keeping_forget AS KeepingForget,\r\n"
            + "                de.salary_count AS SalaryCount,\r\n"
            + "                de.late_time AS LateTime,\r\n"
            + "                de.late_hour AS LateHour,\r\n"
            + "                de.leave_day_accept AS LeaveDayAccept,\r\n"
            + "            CASE\r\n"
            + "                    \r\n"
            + "                    WHEN em.type_contract = 1 THEN\r\n"
            + "                    'Chính thức' \r\n"
            + "                    WHEN em.type_contract = 2 THEN\r\n"
            + "                    'Thử việc' \r\n"
            + "                    WHEN em.type_contract = 3 THEN\r\n"
            + "                    'Freelance' \r\n"
            + "                    WHEN em.type_contract = 4 THEN\r\n"
            + "                    'Thực tập' \r\n"
            + "                END AS TypeContract,\r\n"
            + "                de.ot_normal AS OtNormal,\r\n"
            + "                de.ot_morning_7 AS OtMorning7,\r\n"
            + "                de.ot_sat_sun AS OtSatSun,\r\n"
            + "                de.ot_holiday AS OtHoliday,\r\n"
            + "                de.sum_ot_month AS SumOtMonth,\r\n"
            + "                de.ot_unpaid AS OtUnpaid,\r\n"
            + "                de.compensatory_leave AS CompensatoryLeave, \r\n"
            + "                de.ot_pay_in_month AS OtPayInMonth,\r\n"
            + "                de.csr_leave_plus AS CsrLeavePlus,\r\n"
            + "                de.csr_leave_plus_round AS CsrLeavePlusRound,\r\n"
            + "                de.leave_remain_now as LeaveRemainNow,\r\n"
            + "                de.csr_leave_now as CsrLeaveNow,\r\n"
            + "                de.welfare_leave as WelfareLeave, \r\n\r\n"
            + "                (de2.leave_remain_now - de.leave_day_accept) AS LeaveRemainLastMonth,\r\n"
            + "                (de2.csr_leave_now - de.compensatory_leave) AS CsrLeaveLastMonth\r\n"
            + "            FROM detail_time_keeping AS de\r\n"
            + "            JOIN employee AS em ON de.employee_id = em.ID "
            + "            JOIN profile AS pro ON em.ID = pro.employee_id"
            + "            LEFT JOIN  detail_time_keeping as de2\r\n"
            //              Convert time_save of current month to date, then subtract 1 month to get date of lastmonth, then convert back to yyyy-mm
            + "             ON de2.time_save = to_char(to_date(de.time_save, 'YYYY-MM-DD') - INTERVAL '1 months', 'YYYY-MM') AND de2.employee_id = de.employee_id \r\n"
            + "    Where de.time_save =:timeSave ORDER BY em.id",nativeQuery = true)
    List<DetailTimeKeepingForExportResponse> findByListDetailTimeKeepingForExportFile(String timeSave);
    
    @Query(value="Select * FROM detail_time_keeping as de WHERE de.employee_id =:empId",nativeQuery = true)
    Optional<DetailTimeKeeping> findByEmployeeId(Integer empId);
    @Query(value="Select * FROM detail_time_keeping as de WHERE de.employee_id =:empId and de.time_save =:timeSave",nativeQuery = true)
    Optional<DetailTimeKeeping> findByEmployeeIdAndTime(Integer empId, String timeSave);
    
    @Query(value="SELECT dtk.* FROM detail_time_keeping as dtk JOIN employee as emp on dtk.employee_id = emp.id where emp.employee_code like upper(:code) "
    		+ "and ((:time is null) or dtk.time_save like :time)", nativeQuery=true)
    Optional<DetailTimeKeeping> findByEmployeeCode(String code, String time);
    
    @Query(value="SELECT * FROM detail_time_keeping WHERE time_save like :previousTime AND employee_id = :eID", nativeQuery=true)
	Optional<DetailTimeKeeping> getOtByTime(String previousTime, Integer eID);
    
    @Query(value="SELECT * FROM detail_time_keeping AS dtk \r\n"
    		+ "WHERE dtk.employee_id = :employeeId AND dtk.time_save like :timeSave", nativeQuery=true)
    DetailTimeKeeping findRemainLeavesByIdAndMonth(Integer employeeId, String timeSave);
    
    List<DetailTimeKeeping> findAllByTimeSave(String timeSave);
    
}
