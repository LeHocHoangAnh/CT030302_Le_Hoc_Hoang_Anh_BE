package com.hrm.model.dao;

import com.hrm.common.Constants;
import com.hrm.model.ResultPageResponse;
import com.hrm.model.request.hr.EquipmentListRequest;
import com.hrm.model.response.hr.EquipmentListResponse;
import com.hrm.utils.PaginationResult;
import com.hrm.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;

import javax.persistence.EntityManager;

@Repository
public class EquipmentManageDao {
	@Autowired
	EntityManager entityManager;

	public ResultPageResponse searchListEquipment(EquipmentListRequest request) {
		Session session = entityManager.unwrap(Session.class);
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT " 
				+ "		equ.id , " 
				+ "		equ.name ," 
				+ "		equ.serial_number AS serialNumber, "
				+ "		equ.category AS category, "
				+ "		equ.description AS description,"
				+ "		TO_CHAR(equ.import_date, 'DD-MM-YYYY') AS importDate,\n" 
				+ "		TO_CHAR(equ.warranty_time, 'DD-MM-YYYY') AS warrantyTime,\n" 
				+ "		equ.vendor AS vendor,\n" 
				+ "		equ.status AS status,\n"
				+ "		CAST((emp.employee_code||' - '||pro.full_name) AS CHARACTER VARYING) AS employee\n"
				+ "FROM equipment AS equ\n" + "LEFT JOIN employee AS emp ON equ.employee_id = emp.id \n"
				+ "LEFT JOIN profile AS pro ON emp.id = pro.employee_id \n" + "WHERE equ.delete_flag = false\n");
		if (StringUtils.isNotBlank(request.getName())) {
			sql.append(" AND lower(equ.name) like lower(:name) \r\n");
		}
		if (StringUtils.isNotBlank(request.getSerialNumber())) {
			sql.append(" AND lower(equ.serial_number) like lower(:serialNumber) \r\n");
		}
		if (request.getCategory() != null) {
			sql.append(" AND equ.category = :category \r\n");
		}
		if (request.getStatus() != null) {
			sql.append(" AND equ.status = :status \r\n");
		}
		if (request.getDate() != null) {
			sql.append(" AND equ.import_date >= :dateFrom");
			if (request.getDate().get(1) != null)
				sql.append(" AND equ.import_date <= :dateTo \r\n");
		}
		if (request.getEmployeeList() != null && request.getEmployeeList().size()>0) {
			sql.append(" AND equ.employee_id IN :employeeList \r\n");
		}
		sql.append(" ORDER BY equ.id DESC");

		@SuppressWarnings("unchecked")
		NativeQuery<EquipmentListResponse> query = session.createNativeQuery(sql.toString());
		if (StringUtils.isNotBlank(request.getName())) {
			query.setParameter("name", "%" + request.getName() + "%");
		}
		if (StringUtils.isNotBlank(request.getSerialNumber())) {
			query.setParameter("serialNumber", "%" + request.getSerialNumber() + "%");
		}
		if (request.getCategory() != null) {
			query.setParameter("category", request.getCategory());
		}
		if (request.getStatus() != null) {
			query.setParameter("status", request.getStatus());
		}
		if (request.getDate() != null) {
			query.setParameter("dateFrom", request.getDate().get(0));
			if (request.getDate().get(1) != null) {
				query.setParameter("dateTo", request.getDate().get(1));
			}
		}
		if (request.getEmployeeList() != null && request.getEmployeeList().size()>0) {
			query.setParameter("employeeList", request.getEmployeeList());
		}
		Utils.addScalr(query, EquipmentListResponse.class);
		PaginationResult<EquipmentListResponse> result = new PaginationResult<>(query, request.getPageNo(), request.getPageSize(),
				Constants.MAX_NAVIGATION_RESULT);
		ResultPageResponse resultPageResponse = new ResultPageResponse();
		resultPageResponse.setItems(result.getList());
		resultPageResponse.setTotalPages(result.getTotalPages());
		resultPageResponse.setTotalItems(result.getTotalRecords());
		resultPageResponse.setCurrentPage(result.getCurrentPage());
		// 1 2 3 4 5 ... 11 12 13
		return resultPageResponse;
	}
}
