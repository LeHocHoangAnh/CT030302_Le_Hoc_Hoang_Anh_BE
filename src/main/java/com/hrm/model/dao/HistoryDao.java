package com.hrm.model.dao;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.hrm.common.Constants;
import com.hrm.model.ResultPageResponse;
import com.hrm.model.request.PaginationRequest;
import com.hrm.model.response.hr.ListHistoryResponse;
import com.hrm.utils.PaginationResult;
import com.hrm.utils.Utils;

@Repository
public class HistoryDao {
    @Autowired
    EntityManager entityManager;
    
    public ResultPageResponse getListHistory(Integer id,PaginationRequest request) {
        Session session = entityManager.unwrap(Session.class);
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT his.id,his.id_employee as IdEmployee,emp.employee_code as EmployeeCode,pro.full_name as FullName,\r\n"
                + "to_char(his.time_start, 'dd-mm-yyyy') as timeStart,to_char(his.time_end, 'dd-mm-yyyy') as timeEnd,his.role as Role,  \r\n"
                + "(SELECT SUM(EXTRACT(EPOCH from bo.back_day) - EXTRACT(EPOCH FROM bo.request_day)) FROM booking_day_off AS bo WHERE bo.employee_id = emp.id AND bo.project_id = his.id_projects AND confirm=1) as otTime\r\n"
                + "FROM history_work AS his\r\n"
                + "JOIN employee as emp ON emp.id = his.id_employee\r\n"
                + "JOIN profile as pro ON pro.employee_id = emp.id\r\n"
                + "WHERE his.id_projects = "+ id );
        sql.append("\r\n ORDER BY his.id DESC");
        @SuppressWarnings("unchecked")
        NativeQuery<ListHistoryResponse> query = session.createNativeQuery(sql.toString());
        Utils.addScalr(query, ListHistoryResponse.class);
        PaginationResult<ListHistoryResponse> result = new PaginationResult<>(query, request.getPageNo(),
                request.getPageSize(), Constants.MAX_NAVIGATION_RESULT);
        ResultPageResponse resultPageResponse = new ResultPageResponse();
        resultPageResponse.setItems(result.getList());
        resultPageResponse.setTotalPages(result.getTotalPages());
        resultPageResponse.setTotalItems(result.getTotalRecords());
        resultPageResponse.setCurrentPage(result.getCurrentPage());
        // 1 2 3 4 5 ... 11 12 13
        return resultPageResponse;
    }
}
