package com.hrm.repository;

import com.hrm.entity.BookingDayOff;
import com.hrm.model.response.DropDownResponse;
import com.hrm.model.response.employee.EquipmentRegistrationListResponse;
import com.hrm.model.response.leader.DetailBookingResponse;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingDayOffRepository extends JpaRepository<BookingDayOff, Integer> {
    @Query(value = "select * from booking_day_off where delete_flag=false and employee_id =:empId  and confirm =:confirm and status != 9 and" +
            "(( DATE(request_day) between DATE(:startDate) AND DATE(:endDate)) " +
            "OR ( DATE(back_day) between DATE(:startDate) AND DATE(:endDate)))", nativeQuery = true)
    List<BookingDayOff> findByEmployeeIdAndConfirmAndTime(Integer empId, Integer confirm, String startDate, String endDate);

    @Query(value = "select * from booking_day_off where delete_flag=false and employee_id =:empId and status != 9 and " +
            "(( DATE(request_day) between DATE(:startDate) AND DATE(:endDate)) " +
            "OR ( DATE(back_day) between DATE(:startDate) AND DATE(:endDate)))", nativeQuery = true)
    List<BookingDayOff> findByEmployeeIdAndTime(Integer empId, String startDate, String endDate);
    
    @Query(value = "SELECT\r\n"
            + "  bo.id, pro.employee_id AS employeeId, pro.full_name AS fullName,\r\n"
            + "    de.NAME AS departmentName, bo.delete_flag as deleteFlag, \r\n"
            + "CASE\r\n"
            + "        WHEN bo.status = 0 THEN 'Nghỉ Phép' \r\n"
            + "        WHEN bo.status = 1 THEN 'Đi Muộn/Về Sớm' \r\n"
            + "        WHEN bo.status = 2 THEN 'Remote' \r\n"
            + "        WHEN bo.status = 3 THEN 'Ra Ngoài' \r\n"
            + "        WHEN bo.status = 4 THEN 'OT' \r\n"
            + "        WHEN bo.status = 5 THEN 'Nghỉ Phúc Lợi' \r\n"
            + "   	   WHEN bo.status = 6 THEN 'Nghỉ Bù' \r\n"
            + "        WHEN bo.status = 7 THEN 'Nghỉ Không Lương' \r\n"
            + "        WHEN bo.status = 8 THEN 'Quên Chấm Công' \r\n"
            + "        WHEN bo.status = 9 THEN 'Đăng Ký Thiết Bị' \r\n"
            + "    END AS status,bo.request_day AS requestDay,bo.back_day AS backDay,bo.reason AS reason,\r\n"
            + "    bo.confirm AS confirm, pro2.full_name as approver, bo.evidence_image as evidenceImage, prj.name_projects as projectName \r\n"
            + "	   FROM booking_day_off AS bo\r\n"
            + "    JOIN employee AS emp ON bo.employee_id = emp.ID\r\n"
            + "    JOIN profile AS pro ON emp.ID = pro.employee_id\r\n"
            + "    LEFT JOIN profile AS pro2 ON bo.approver = pro2.employee_id\r\n"
            + "    JOIN department AS de ON emp.department_id = de.ID\r\n"
            + "    LEFT JOIN projects AS prj ON bo.project_id = prj.id\r\n"
            + "    WHERE bo.id =:id AND emp.delete_flag =:deleteFlag",nativeQuery = true)
    DetailBookingResponse getDetailBookingById(Integer id, Integer deleteFlag);
    
    @Query(value = "SELECT\r\n"
            + " bo.id,   pro.full_name AS fullName,\r\n"
            + "    de.NAME AS departmentName, bo.delete_flag as deleteFlag, \r\n"
            + "CASE\r\n"
            + "        WHEN bo.status = 0 THEN 'Nghỉ Phép' \r\n"
            + "        WHEN bo.status = 1 THEN 'Đi Muộn/Về Sớm' \r\n"
            + "        WHEN bo.status = 2 THEN 'Remote' \r\n"
            + "        WHEN bo.status = 3 THEN 'Ra Ngoài' \r\n"
            + "        WHEN bo.status = 4 THEN 'OT' \r\n"
            + "        WHEN bo.status = 5 THEN 'Nghỉ Phúc Lợi' \r\n"
            + "        WHEN bo.status = 6 THEN 'Nghỉ Bù' \r\n"
            + "        WHEN bo.status = 7 THEN 'Nghỉ Không Lương' \r\n"
            + "        WHEN bo.status = 8 THEN 'Quên Chấm Công' \r\n"
            + "    END AS status,bo.request_day AS requestDay,bo.back_day AS backDay,bo.reason AS reason,\r\n"
            + "    bo.confirm AS confirm, pro2.full_name as approver, bo.evidence_image as evidenceImage, prj.name_projects as projectName \r\n"
            + "    FROM booking_day_off AS bo\r\n"
            + "    JOIN employee AS emp ON bo.employee_id = emp.ID\r\n"
            + "    JOIN profile AS pro ON emp.ID = pro.employee_id\r\n"
            + "    LEFT JOIN profile AS pro2 ON bo.approver = pro2.employee_id\r\n"
            + "    JOIN department AS de ON emp.department_id = de.ID\r\n"
            + "    LEFT JOIN projects AS prj ON bo.project_id = prj.id\r\n"
            + "    WHERE bo.id in :ids AND emp.delete_flag =:deleteFlag",nativeQuery = true)
    List<DetailBookingResponse> getMultipleDetailBooking( List<Integer> ids,  Integer deleteFlag );
    
    @Query(value = "SELECT DISTINCT to_char(bo.created_at,'MM/YYYY') as name FROM booking_day_off as bo ORDER BY name DESC",nativeQuery = true)
    List<DropDownResponse> getListDropDownBooking();
    
//    @Query(value="SELECT emp.email FROM employee as emp\r\n"
//            + "JOIN role_group as rol ON rol.id = emp.role_group_id\r\n"
//            + "WHERE (rol.leader_flag = true OR rol.sub_leader_flag = true OR rol.comtor_flag = true) AND emp.department_id =:id",nativeQuery = true)
//    List<String> getListEmailSendBooking(Integer id);
    @Query(value="SELECT emp.email FROM employee as emp\r\n"
            + "JOIN role_group as rol ON rol.id = emp.role_group_id\r\n"
            + "JOIN profile as pro ON pro.employee_id = emp.id\r\n"
            + "WHERE (rol.leader_flag = true) AND emp.department_id =:id AND pro.date_out is null AND emp.booking_day_off_notify=true",nativeQuery = true)
    List<String> getListEmailSendBooking(Integer id);
    
    // Get all day-off value in a month, confirm status != refused
    @Query(value = "select * from booking_day_off where delete_flag=false and employee_id =:empId  "+ 
    		" AND  status = :status"+ 
    		" AND confirm != 2 " + // 2: refused booking
            " AND (( to_char(request_day,'YYYY-MM') like :time"+ 
    		" OR to_char(back_day,'YYYY-MM') like :time))", nativeQuery = true)
    List<BookingDayOff> findTotalDayOffInMonth(Integer empId, Integer status, String time);
    
 // Get all day-off value in a month, confirm status != refused
    @Query(value = "SELECT * FROM booking_day_off "+
    		"WHERE delete_flag = false "+
    		" 	AND status = 4"+ // application type: OT
    		" 	AND confirm = 1 " + // 1: confirmed booking
    		" 	AND employee_id =:empId  "+ 
            " 	AND (( to_char(request_day,'YYYY-MM') like :time"+ 
    		" OR to_char(back_day,'YYYY-MM') like :time))", nativeQuery = true)
    List<BookingDayOff> findTotalOtInMonth(Integer empId, String time);
    
    // Get all type of day-off value in a month, confirm status != refused
    @Query(value = "select * from booking_day_off where delete_flag=false and employee_id =:empId  "+ 
    		" AND status != 9 " +
    		" AND confirm != 2 " + // 2: refused booking
            " AND (( to_char(request_day,'YYYY-MM') like :time"+ 
    		" OR to_char(back_day,'YYYY-MM') like :time))", nativeQuery = true)
    List<BookingDayOff> findTotalAllDayOffInMonth(Integer empId, String time);
    
    @Query(value="SELECT CAST(emp.id as CHARACTER VARYING) FROM employee AS emp \r\n"
    		+ "JOIN department AS dep ON dep.id = emp.department_id \r\n "
    		+ "JOIN profile as pro ON emp.id = pro.employee_id\r\n"
    		+ "JOIN role_group AS rol ON rol.id = emp.role_group_id \r\n"
    		+ "WHERE rol.leader_flag = true AND dep.id = :departmentID AND pro.date_out is null", nativeQuery=true)
    List<String> findDepartmentLeaderID(Integer departmentID);
    
    @Query(value="SELECT "
    		   + "	CAST(emp.id AS CHARACTER VARYING) AS value, "
    		   + "  CAST((emp.employee_code||' - '||pro.full_name||'( '||dep.name||')') AS CHARACTER VARYING) AS name "
    		   + "FROM profile AS pro "
    		   + "	JOIN employee AS emp ON pro.employee_id = emp.id "
    		   + "	JOIN department AS dep ON emp.department_id = dep.id "
    		   + "WHERE emp.role_group_id in (2, 7, 8, 9) " // where employee's role is 'Leader' or 'Leader HR' or 'Leader Comtor' or 'Leader HR Comtor'
    		   + "	 AND pro.date_out IS NULL "
    		   + "	 AND dep.action = 1 "
    		   + "ORDER BY emp.id ASC", nativeQuery = true) // and department is still in use
	List<DropDownResponse> getAllApprover();
    
    @Query(value="SELECT "
    		   + "	CAST(emp.id AS CHARACTER VARYING) AS value, "
    		   + "	CAST((emp.employee_code||' - '||pro.full_name) AS CHARACTER VARYING) AS name "
    		   + "FROM profile AS pro "
    		   + "	JOIN employee AS emp ON pro.employee_id = emp.id "
    		   + "WHERE pro.date_out IS NULL "
    		   + "  AND emp.delete_flag = 0 "
    		   + "ORDER BY emp.id ASC", nativeQuery = true) 
	List<DropDownResponse> getAllEmployeeDropdown();

    @Query(value = "SELECT "
    		+ "			bdo.id AS id,"
    		+ "			bdo.approver AS category,"
    		+ "			bdo.selected_type_time AS description,"
    		+ "			TO_CHAR(bdo.request_day, 'DD-MM-YYYY') AS requestDate,"
    		+ "			bdo.reason AS reason,"
    		+ "			bdo.confirm AS confirm"
    		+ " 	FROM booking_day_off AS bdo "
    		+ "		JOIN employee AS emp ON bdo.employee_id = emp.id "
    		+ "		WHERE bdo.delete_flag = false"
    		+ "		  AND emp.id = :id" 
    		+ "		  AND bdo.status = 9"
    		+ "		ORDER BY bdo.confirm ASC, bdo.id DESC" // 9 is order type of equipment registration
    		, nativeQuery = true)
	List<EquipmentRegistrationListResponse> findEquipmentRegistrationListByEmployeeId(Integer id);
}
