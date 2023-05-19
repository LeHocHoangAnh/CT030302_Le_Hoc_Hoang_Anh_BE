package com.hrm.model.dao;

import javax.persistence.EntityManager;

import com.hrm.common.CommonService;
import com.hrm.repository.EmployeeRepository;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.hrm.common.Constants;
import com.hrm.model.ResultPageResponse;
import com.hrm.model.response.hr.ListEmployeeResponse;
import com.hrm.utils.PaginationResult;
import com.hrm.utils.Utils;
@Repository
public class EmployeeByDepartmentIdDAO {
    
    @Autowired
    EntityManager entityManager;
    
    public ResultPageResponse getListEmployee(Integer deptID, Integer page, Integer size) {
    	Integer pageNo = page; // Current page
    	Integer pageSize = size; // Number of record in page
    	
        Session session = entityManager.unwrap(Session.class);
        StringBuilder sql = new StringBuilder();
        sql.append("With emp as (SELECT e.id,e.employee_code AS EmployeeCode,e.email AS Email,p.full_name AS FullName,e.position as Position, \r\n"
                + "    CASE\r\n"
                + "        WHEN p.gender IS TRUE THEN 'Nam' \r\n"
                + "        WHEN p.gender IS FALSE THEN 'Nữ' \r\n"
                + "    END AS Gender,\r\n"
                + "  CASE\r\n"
                + "    WHEN e.type_contract = 1 THEN 'Chính thức' \r\n"
                + "    WHEN e.type_contract = 2 THEN 'Thử việc' \r\n"
                + "    WHEN e.type_contract = 3 THEN 'Freelance' \r\n"
                + "    WHEN e.type_contract = 4 THEN 'Thực tập' \r\n"
                + "  END AS TypeContract,to_char(p.date_entry,'DD-MM-YYYY') AS dateEntry, p.phone_number AS PhoneNumber,\r\n"
                + "    to_char(p.date_of_birth,'DD-MM-YYYY') AS DateOfBirth,\r\n"
                + "    d.NAME AS DepartmentName,e.delete_flag,e.department_id,p.date_out\r\n"
                + "    FROM employee AS e\r\n"
                + "        JOIN profile AS p ON e.ID = p.employee_id\r\n"
                + "   LEFT JOIN department AS d ON e.department_id = d.ID)\r\n"
                + "SELECT * FROM emp\r\n"
                + "WHERE emp.delete_flag = " + Constants.DELETE_NONE);
        if (deptID!=null){
            sql.append(" AND emp.department_id ="+deptID);
        }
        sql.append("\r\n AND emp.date_out IS NULL");
        @SuppressWarnings("unchecked")
        NativeQuery<ListEmployeeResponse> query = session.createNativeQuery(sql.toString());
        Utils.addScalr(query, ListEmployeeResponse.class);
        
        // Pagination Result
        PaginationResult<ListEmployeeResponse> result = new PaginationResult<>(query, pageNo,
                pageSize, Constants.MAX_NAVIGATION_RESULT);
        ResultPageResponse resultPageResponse = result.getResultPageResponse();
        
        return resultPageResponse;
    }
}


