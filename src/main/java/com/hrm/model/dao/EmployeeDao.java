package com.hrm.model.dao;

import javax.persistence.EntityManager;

import com.hrm.common.CommonService;
import com.hrm.entity.Department;

import com.hrm.model.request.hr.ListEmployeeRequest;

import com.hrm.repository.EmployeeRepository;
import org.apache.commons.lang3.StringUtils;
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
public class EmployeeDao {
    
    @Autowired
    EntityManager entityManager;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private CommonService commonService;
    
    public ResultPageResponse getListEmployee(ListEmployeeRequest request) {
        Session session = entityManager.unwrap(Session.class);
        StringBuilder sql = new StringBuilder();
        sql.append("With emp as (SELECT e.id,e.employee_code AS EmployeeCode,e.email AS Email,p.full_name AS FullName,e.position as Position,\r\n"
                + "    CASE\r\n"
                + "        WHEN p.gender IS TRUE THEN 'Nam' \r\n"
                + "        WHEN p.gender IS FALSE THEN 'Nữ' \r\n"
                + "    END AS Gender,\r\n"
                + "  CASE\r\n"
                + "    WHEN e.type_contract = 1 THEN 'Chính thức' \r\n"
                + "    WHEN e.type_contract = 2 THEN 'Thử việc' \r\n"
                + "    WHEN e.type_contract = 3 THEN 'Freelance' \r\n"
                + "    WHEN e.type_contract = 4 THEN 'Thực tập' \r\n"
                + "  END AS TypeContract,p.phone_number AS PhoneNumber, p.date_entry AS dateEntry, \r\n"
                + "    to_char(p.date_of_birth,'dd-MM-yyyy') AS DateOfBirth,\r\n"
                + "    d.NAME AS DepartmentName,e.delete_flag,e.department_id, e.type_contract, p.date_out as date_out\r\n"
                + "    FROM employee AS e\r\n"
                + "        JOIN profile AS p ON e.ID = p.employee_id\r\n"
                + "   LEFT JOIN department AS d ON e.department_id = d.ID)\r\n"
                + "SELECT * FROM emp\r\n"
                + "WHERE emp.delete_flag = " + Constants.DELETE_NONE);
        if (StringUtils.isNotBlank(request.getKey())) {
            sql.append("\r\n AND lower(emp\\:\\:text) LIKE lower(:key)\r\n");
        }
        if (StringUtils.isNotBlank(request.getContract())) {
            sql.append("\r\n AND emp.type_contract = :contract \r\n");
        }
        if (StringUtils.isNotBlank(request.getDepartment())) {
            sql.append("\r\n AND emp.department_id = :department\r\n");
        }
        if(request.getInWorking()!=null) {
            if (request.getInWorking()) {
                sql.append("\r\n AND emp.date_out IS NULL \r\n");
            }
            if (!request.getInWorking()) {
                sql.append("\r\n AND emp.date_out IS NOT NULL \r\n");
            }
        }
        if (StringUtils.isNotBlank(request.getPosition())) {
            sql.append("\r\n AND lower(emp.position) LIKE lower(:position)\r\n");
        }
        
        if(request.getInWorking()!=null) {        	
        	if (request.getInWorking()) {
        		sql.append("\r\n AND emp.date_out IS NULL \r\n");
        	}
        	if (!request.getInWorking()) {
        		sql.append("\r\n AND emp.date_out IS NOT NULL \r\n");
        	}
        }

        if (request.getIsLeader()){
            Department employeeDepartment = employeeRepository.findById(commonService.idUserAccountLogin()).get().getDepartment();
            if(employeeDepartment==null) {            	
            	return null;
            }
        	sql.append(" AND emp.department_id ="+employeeDepartment.getId());
            
        }
        sql.append("\r\n ORDER BY emp.ID ASC");
        
        @SuppressWarnings("unchecked")
        NativeQuery<ListEmployeeResponse> query = session.createNativeQuery(sql.toString());
        if (StringUtils.isNotBlank(request.getKey())) {
            query.setParameter("key", "%" + request.getKey().trim() + "%");
        }
        if (StringUtils.isNotBlank(request.getContract())) {
            query.setParameter("contract",Integer.parseInt(request.getContract()));
        }
        if (StringUtils.isNotBlank(request.getDepartment())) {
            query.setParameter("department", Integer.parseInt(request.getDepartment()));
        }
        if (StringUtils.isNotBlank(request.getPosition())) {
            query.setParameter("position", "%" + request.getPosition().trim() + "%");
        }
        Utils.addScalr(query, ListEmployeeResponse.class);
        PaginationResult<ListEmployeeResponse> result = new PaginationResult<>(query, request.getPageNo(),
                request.getPageSize(), Constants.MAX_NAVIGATION_RESULT);
        ResultPageResponse resultPageResponse = new ResultPageResponse();
        resultPageResponse.setItems(result.getList());
        resultPageResponse.setTotalPages(result.getTotalPages());
        resultPageResponse.setTotalItems(result.getTotalRecords());
        resultPageResponse.setCurrentPage(result.getCurrentPage());
        return resultPageResponse;
    }
}


