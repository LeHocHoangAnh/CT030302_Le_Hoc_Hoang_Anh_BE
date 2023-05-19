package com.hrm.model.dao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.hrm.common.Constants;
import com.hrm.model.ResultPageResponse;
import com.hrm.model.request.hr.ListEquipmentRegistrationRequest;
import com.hrm.model.response.hr.EquipmentRegistrationListResponse;
import com.hrm.utils.PaginationResult;
import com.hrm.utils.Utils;

@Repository
public class EquipmentRegistrationListDAO {
    @Autowired
    EntityManager entityManager;

    public ResultPageResponse getEquipmentRegistrationList(ListEquipmentRegistrationRequest request,
            String requestID) {
        Session session = entityManager.unwrap(Session.class);
        StringBuilder sql = new StringBuilder();
        sql.append(
                "SELECT bo.reason, bo.id,pro.full_name as FullName,de.name as DepartmentName, bo.delete_flag as deleteFlag, \r\n"
                        + " CASE "
                        + "    WHEN bo.confirm = 0 THEN 'Chờ Xác Nhận' " 
                        + "    WHEN bo.confirm = 1 THEN 'Xác Nhận' "
                        + "    WHEN bo.confirm = 2 THEN 'Từ Chối' " 
                        + " END as confirm, bo.selected_type_time as description, bo.approver as category, bo.request_day as RequestDay \r\n "
                        + " FROM booking_day_off as bo \r\n" + " JOIN employee as emp ON bo.employee_id = emp.id \r\n "
                        + " JOIN profile as pro ON emp.id = pro.employee_id \r\n "
                        + " JOIN department as de ON emp.department_id = de.id \r\n "
                        + " WHERE 1=1 AND bo.status = 9 AND bo.confirm != 2");
        if (request.getConfirm() != null) {
            sql.append("\r\n AND bo.confirm = :confirm \r\n");
        }
        if (StringUtils.isNotBlank(request.getFromDate())) {
            sql.append(" AND bo.request_day >= :fromDate \r\n");
        }
        if (StringUtils.isNotBlank(request.getToDate())) {
            sql.append(" AND bo.request_day <= :toDate \r\n");
        }
        if (StringUtils.isNotBlank(request.getName())) {
            sql.append(" AND lower(pro.full_name) LIKE lower(:name) \r\n");
        }
        if (request.getDepartmentId() != null) {
            sql.append(" AND de.id = :department \r\n");
        }
        if (request.getCategory() != null) {
            sql.append(" AND bo.approver = :category \r\n");
        }

        sql.append("\r\n ORDER BY bo.confirm DESC, bo.ID DESC");

        @SuppressWarnings("unchecked")
        NativeQuery<EquipmentRegistrationListResponse> query = session.createNativeQuery(sql.toString());
        if (StringUtils.isNotBlank(request.getFromDate())) {
            try {
                query.setParameter("fromDate", sqlDateFormat(request.getFromDate(), "from"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (StringUtils.isNotBlank(request.getToDate())) {
            try {
                query.setParameter("toDate", sqlDateFormat(request.getToDate(), "to"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (StringUtils.isNotBlank(request.getName())) {
            query.setParameter("name", "%" + request.getName() + "%");
        }
        if (request.getDepartmentId() != null) {
            query.setParameter("department", request.getDepartmentId());
        }
        if (request.getConfirm() != null) {
            query.setParameter("confirm", request.getConfirm());
        }
        if (request.getCategory() != null) {
            query.setParameter("category", request.getCategory());
        }

        Utils.addScalr(query, EquipmentRegistrationListResponse.class);
        PaginationResult<EquipmentRegistrationListResponse> result = new PaginationResult<>(query, request.getPageNo(),
                request.getPageSize(), Constants.MAX_NAVIGATION_RESULT);
        ResultPageResponse resultPageResponse = new ResultPageResponse();
        resultPageResponse.setItems(result.getList());
        resultPageResponse.setTotalPages(result.getTotalPages());
        resultPageResponse.setTotalItems(result.getTotalRecords());
        resultPageResponse.setCurrentPage(result.getCurrentPage());
        session.close();
        session.clear();

        return resultPageResponse;
    }

    // format request date(String) to sql date format
    public Date sqlDateFormat(String dateString, String type) throws ParseException {
        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, 1);
        if (type.equals("to")) {
            c.add(Calendar.HOUR, 23);
            c.add(Calendar.MINUTE, 59);
            c.add(Calendar.SECOND, 59);
        }

        return c.getTime();
    }
}
