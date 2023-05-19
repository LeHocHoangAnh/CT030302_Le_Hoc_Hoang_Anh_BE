package com.hrm.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hrm.entity.Employee;
import com.hrm.model.DropDownResponse;
import com.hrm.model.response.EmployeeToExcelResponse;
import com.hrm.model.response.ReviewRemindingResponse;
import com.hrm.model.response.anonymous.EmployeeDiscordResponse;
import com.hrm.model.response.employee.EmployeeProfileResponse;
import com.hrm.model.response.employee.HistoryByEmployeeIdResponse;
import com.hrm.model.response.hr.CreateOrEditEmployeeResponse;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    Optional<Employee> findByEmailAndDeleteFlag(String email, Integer deleteFlag);
    
    Optional<Employee> findByResetPasswordTokenAndDeleteFlag(String token, Integer deleteFlag);
    
    List<Employee> findAllByDeleteFlag(Integer deleteFlag);

    @Query(value = "select e.id,e.employee_code employeeCode,e.email, e.picture_profile pictureProfile, p.full_name fullName,p.date_entry dateEntry, " +
            "p.date_of_birth dateOfBirth,p.gender,p.address, p.perm_address as permAddress, p.phone_number phoneNumber,d.name departmentName, p.bank_name as bankName, p.bank_account as bankAccount, " + 
            "e.booking_day_off_notify as bookingDayOffNotify, e.confirm_day_off_notify as confirmDayOffNotify, e.booking_meeting_notify as bookingMeetingNotify, e.confirm_meeting_notify as confirmMeetingNotify " +
            "from employee e " +
            "left join profile p on e.id = p.employee_id " +
            "left join department d  on d.id = e.department_id " +
            "where e.delete_flag = 0 and e.id =:id",nativeQuery = true)
    EmployeeProfileResponse getEmployeeProfileById(Integer id);

    Optional<Employee> findByIdAndPassword(Integer idUserAccountLogin, String password);
    
    @Query(value ="Select * FROM employee e WHERE e.employee_code=:code AND e.delete_flag=:deleteFlag",nativeQuery = true)
    Optional<Employee> findByEmployeeCode(String code,Integer deleteFlag);

    Optional<Employee> findByIdAndDeleteFlag(Integer integer,Integer deleteFlag);
    
    @Query(value = "select employee_code from employee where employee_code not like :directorCode order by employee_code desc limit 1",nativeQuery = true)
    String findLastEmployee(String directorCode);

    @Query(value = " SELECT\r\n"
            + "        e.id,\r\n"
            + "        e.employee_code AS EmployeeCode,\r\n"
            + "        e.email AS Email,e.status as Status,\r\n"
            + "        e.role_group_id AS roleGroupId,\r\n"
            + "        p.full_name AS FullName,\r\n"
            + "        p.gender,\r\n"
            + "        e.type_contract as TypeContract,\r\n"
            + "        p.phone_number AS PhoneNumber,\r\n"
            + "        to_char(p.date_of_birth, 'DD-MM-YYYY') AS DateOfBirth,\r\n"
            + "        e.department_id AS DepartmentId,\r\n"
            + "        p.address as Address,\r\n"
            + "        e.position AS WorkName,\r\n"
            + "        to_char(p.date_entry,'DD-MM-YYYY') as DateEntry,\r\n"
            + "        to_char(p.date_out,'DD-MM-YYYY') as DateOut,\r\n"
            + "        p.tax_code as TaxCode,\r\n"
            + "        p.safe_code as SafeCode,\r\n"
            + "        p.salary_basic as SalaryBasic,\r\n"
            + "        e.picture_name as PictureName,\r\n"
            + "        e.picture_profile as PictureProfile,\r\n"
            + "		   p.bank_name as BankName,\r\n"
            + "		   p.bank_account as BankAccount,\r\n"
            + "		   to_char(e.review_date,'DD-MM-YYYY') as reviewDate, \r\n"
            + "        p.discord_id as discordId, \r\n"
            + "        p.perm_address as permAddress \r\n"
            + "    FROM\r\n"
            + "        employee AS e\r\n"
            + "    JOIN\r\n"
            + "        profile AS p \r\n"
            + "            ON e.ID = p.employee_id\r\n"
            + "    WHERE\r\n"
            + "        e.delete_flag = 0 \r\n"
            + "        AND e.id=:id",nativeQuery = true)
    CreateOrEditEmployeeResponse findEmployeeInformationById(Integer id);
    
    @Query(value = "SELECT em.id as Id,pro.full_name as Name FROM employee as em JOIN profile as pro ON pro.employee_id = em.id\r\n"
            + "WHERE pro.full_name =:key OR em.employee_code =:key",nativeQuery = true)
    DropDownResponse findByEmployeeCodeOrName(String key);
    
    @Query(value = "SELECT pro.full_name FROM employee as emp JOIN profile as pro ON pro.employee_id = emp.id\r\n"
            + "WHERE emp.delete_flag =:deleteFlag",nativeQuery = true)
    List<String> findListAutoEmployee(Integer deleteFlag);
    
    @Query(value = "SELECT pro.id,pro.code_projects as CodeProjects,pro.name_projects as NameProjects,\r\n"
            + "to_char(his.time_start, 'dd-mm-yyyy') as TimeStart,to_char(his.time_end, 'dd-mm-yyyy') as TimeEnd,his.role as Role \r\n"
            + "FROM history_work as his JOIN projects as pro ON pro.id = his.id_projects\r\n"
            + "WHERE his.id_employee =:id",nativeQuery = true)
    List<HistoryByEmployeeIdResponse> getListHistoryByEmployeeId(Integer id);
    
    @Query(value="select e from Employee e where id=:id")
	Employee findSingleRecord(Integer id);
    
    @Query(value="SELECT emp.email \r\n"
    		+ "FROM employee AS emp \r\n"
    		+ "	JOIN profile AS pro ON pro.employee_id = emp.id \r\n"
    		+ "WHERE CAST(emp.id AS CHARACTER VARYING) IN :idList AND pro.date_out IS NULL", nativeQuery = true)
	List<String> getListEmailByListID(List<String> idList);
    
    @Query(value="SELECT * FROM employee WHERE CAST(id AS CHARACTER VARYING) IN :listId", nativeQuery=true)
    List<Employee> getListEmployeeByStringListId(List<String> listId);
    
//    @Query(value="SELECT discord_id as dicordId FROM profile WHERE CAST(employee_id AS CHARACTER VARYING) IN :listId", nativeQuery=true)
//    List<String> getListDiscordIdByStringListId(List<String> listId);
    
    @Query(value="SELECT "
    		+ "		emp.id AS employeeId,"
    		+ "		emp.employee_code AS employeeCode,"
    		+ "		pro.full_name AS employeeName,"
    		+ "		emp.type_contract AS typeContract,"
    		+ "		emp.review_date AS reviewDate,"
    		+ "		dep.name AS departmentName, "
    		+ "     CEILING(EXTRACT(EPOCH FROM (emp.review_date - pro.date_entry))/86400) AS seniority "
    		+ " FROM employee AS emp \r\n"
    		+ "	JOIN profile AS pro ON emp.id = pro.employee_id \r\n"
    		+ "	JOIN department AS dep ON emp.department_id = dep.id \r\n"
    		+ "	WHERE "
    		+ "	((emp.type_contract = :internType AND CEILING(EXTRACT(EPOCH FROM (emp.review_date - :currentDate))/86400) = :INTERN_REMIND_DAYS_BEFORE)"
    		+ "	OR (emp.type_contract = :probationaryType AND CEILING(EXTRACT(EPOCH FROM (emp.review_date - :currentDate))/86400) = :PROBATION_REMIND_DAYS_BEFORE)"
    		+ "	OR (emp.type_contract = :officialType AND CEILING(EXTRACT(EPOCH FROM (emp.review_date - :currentDate))/86400) = :OFFICIAL_REMIND_DAYS_BEFORE))"
    		+ "	AND emp.delete_flag = :delFlag"
    		+ " ORDER BY emp.id ASC", nativeQuery = true)
    List<ReviewRemindingResponse> findEmployeDueToReviewByTypeContract(
    		Integer internType, Integer probationaryType, Integer officialType, 
    		Integer INTERN_REMIND_DAYS_BEFORE, Integer PROBATION_REMIND_DAYS_BEFORE, Integer OFFICIAL_REMIND_DAYS_BEFORE, 
    		Date currentDate, Integer delFlag);
    
    @Query(value="SELECT "
    		+ "		emp.id AS employeeId, emp.employee_code as employeeCode, pro.full_name AS employeeName, "
    		+ "		CASE "
    		+ "		 WHEN pro.gender IS TRUE THEN 'Nam' "
    		+ "		 WHEN pro.gender IS FALSE THEN 'Nữ' "
    		+ "		END AS gender, to_char(pro.date_of_birth,'DD-MM-YYYY') AS dateOfBirth, "
    		+ "		pro.address AS address, pro.phone_number as phoneNumber, "
    		+ "     emp.email as email,"
    		+ "     CASE "
    		+ "		 WHEN emp.role_group_id = 1 THEN 'Nhân viên' "
    		+ "		 WHEN emp.role_group_id = 2 THEN 'Leader' "
    		+ "		 WHEN emp.role_group_id = 3 THEN 'Sub Leader' "
    		+ "		 WHEN emp.role_group_id = 4 THEN 'HR' "
    		+ "		 WHEN emp.role_group_id = 6 THEN 'Comtor' "
    		+ "		 WHEN emp.role_group_id = 7 THEN 'Leader, HR' "
    		+ "		 WHEN emp.role_group_id = 8 THEN 'Leader, Comtor' "
    		+ "		 WHEN emp.role_group_id = 9 THEN 'Leader, HR, Comtor' "
    		+ "		 WHEN emp.role_group_id = 10 THEN 'Sub Leader, HR' "
    		+ "		 WHEN emp.role_group_id = 11 THEN 'Sub Leader, Comtor' "
    		+ "		 WHEN emp.role_group_id = 12 THEN 'Sub Leader, HR, Comtor' "
    		+ "		 WHEN emp.role_group_id = 13 THEN 'HR, Comtor' "
    		+ "		 WHEN emp.role_group_id = 14 THEN 'Khách Hàng' "
    		+ "		END AS role, "
    		+ "		CASE "
    		+ "      WHEN emp.type_contract = 1 THEN 'Chính thức' "
    		+ "      WHEN emp.type_contract = 2 THEN 'Thử việc' "
    		+ "      WHEN emp.type_contract = 3 THEN 'Thực tập' "
    		+ "      WHEN emp.type_contract = 4 THEN 'Freelance'"
    		+ "		 WHEN emp.type_contract IS NULL THEN '' "
    		+ "     END AS typeContract, "
    		+ "     to_char(emp.review_date,'DD-MM-YYYY') AS reviewDate, "
    		+ "		CASE "
    		+ "      WHEN emp.status = 0 THEN 'Onsite' "
    		+ "      WHEN emp.status = 1 THEN 'Trực Tiếp' "
			+ " 	 WHEN emp.status = 2 THEN 'Remote'"
    		+ "     END AS status, "
    		+ "     to_char(pro.date_entry,'DD-MM-YYYY') AS dateEntry,"
    		+ "     to_char(pro.date_out,'DD-MM-YYYY') AS dateOut, "
    		+ "     dep.name as departmentName, emp.position AS position, "
    		+ "		pro.bank_name AS bankName, pro.bank_account AS bankAccount, "
    		+ "		pro.tax_code AS taxCode, pro.safe_code AS safeCode, "
    		+ "		CAST(pro.salary_basic AS VARCHAR) AS salaryBasic "
    		+ "	  FROM employee AS emp"
    		+ "		JOIN profile AS pro ON pro.employee_id = emp.id "
    		+ "		LEFT JOIN department AS dep ON dep.id = emp.department_id "
    		+ "	  WHERE emp.id IN :idList ORDER BY emp.id ASC"
    		, nativeQuery=true)
    List<EmployeeToExcelResponse> findAllEmployeeInformationByListId(List<Integer> idList);

    @Query(value="SELECT emp.* FROM employee AS emp JOIN profile AS pro ON emp.id = pro.employee_id " + 
			 "WHERE emp.delete_flag = 0 AND "+ 
			 " emp.type_contract < 3 AND " + //type contract is not intern or free-lance 
			 " ((:code IS NULL) OR (lower(emp.employee_code) LIKE '%'||lower(:code)||'%')) AND " + 
			 " ((:name IS NULL) OR (lower(pro.full_name) LIKE '%'||lower(:name)||'%')) " + 
			 "ORDER BY emp.id", nativeQuery=true)
	List<Employee> findByEmployeeCodeAndName(@Param("code") String employeeCode, @Param("name")String fullName);

    
    @Query(value=" SELECT "
            + " emp.id AS id,"
            + " pro.full_name AS fullName, "
            + " emp.email AS email, "
            + " emp.picture_profile AS avatarUrl, "
            + " TO_CHAR(pro.date_of_birth, 'DD-MM-YYYY') AS dob\r\n"
            + "FROM employee AS emp\r\n"
            + "JOIN profile AS pro ON emp.id = pro.employee_id\r\n"
            + "WHERE pro.date_out IS NULL\r\n"
            + "ORDER BY emp.id", 
            nativeQuery = true)
    List<EmployeeDiscordResponse> findEmployeeListForDiscordConfig();
}
