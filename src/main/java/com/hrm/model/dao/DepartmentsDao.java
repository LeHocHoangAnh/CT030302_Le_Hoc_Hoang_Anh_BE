package com.hrm.model.dao;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.hrm.common.Constants;
import com.hrm.model.ResultPageResponse;
import com.hrm.model.request.hr.DepartmentsListRequest;
import com.hrm.model.response.hr.DepartmentsListResponse;
import com.hrm.utils.PaginationResult;
import com.hrm.utils.Utils;

@Repository
public class DepartmentsDao {
    @Autowired
    EntityManager entityManager;
    
    public ResultPageResponse getDepartmentsList(DepartmentsListRequest request, String action) {
        Session session = entityManager.unwrap(Session.class);
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT "
        		+ "	dep.id as departmentID, "
        		+ "	dep.name as departmentName, "
        		+ "	dep.action as action, "
        		+ "	dep.number_member as member "
                + "FROM department AS dep WHERE 1=1");
        if (StringUtils.isNotBlank(request.getDepartmentName())) {
        	sql.append(" AND lower(dep.name) LIKE lower(:departmentName) \r\n");
        }
        if (action!=null && !action.isBlank()) {
            sql.append(" AND dep.action = :action \r\n");
        }
        sql.append("\r\n ORDER BY dep.id ASC");

        @SuppressWarnings("unchecked")
        NativeQuery<DepartmentsListResponse> query = session.createNativeQuery(sql.toString());
        if(StringUtils.isNotBlank(request.getDepartmentName())) {
            query.setParameter("departmentName", "%" + request.getDepartmentName() + "%");
        }
        if (action!=null && !action.isBlank())  {
            query.setParameter("action",Integer.parseInt(action));
        }
        Utils.addScalr(query, DepartmentsListResponse.class);
        
        // Pagination Result
        PaginationResult<DepartmentsListResponse> result = new PaginationResult<>(query, request.getPageNo(),
                request.getPageSize(), Constants.MAX_NAVIGATION_RESULT);
        ResultPageResponse resultPageResponse = result.getResultPageResponse();
        // 1 2 3 4 5 ... 11 12 13
        return resultPageResponse;
    }
}
