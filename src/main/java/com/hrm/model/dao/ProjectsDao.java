package com.hrm.model.dao;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.hrm.common.Constants;
import com.hrm.model.ResultPageResponse;
import com.hrm.model.request.leader.ListProjectsRequest;
import com.hrm.model.response.hr.ListProjectsResponse;
import com.hrm.utils.PaginationResult;
import com.hrm.utils.Utils;

@Repository
public class ProjectsDao {
    @Autowired
    EntityManager entityManager;
    
    public ResultPageResponse getListProjects(ListProjectsRequest request) {
        Session session = entityManager.unwrap(Session.class);
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT pro.id,pro.code_projects as CodeProjects,pro.name_projects as NameProjects,to_char(pro.time_start,'dd-mm-yyyy') as TimeStart,\r\n"
                + "to_char(pro.time_end,'dd-mm-yyyy') as TimeEnd,pro.customer as Customer,pro.technology as Technology,\r\n"
                + "(SELECT sum(extract('epoch' from bo.back_day) - extract('epoch' from bo.request_day)) FROM booking_day_off as bo where bo.project_id = pro.id and bo.confirm=1) as totalOt\r\n"
                + "FROM projects AS pro \r\n"
                + "WHERE 1=1");
        if (StringUtils.isNotBlank(request.getCodeProjects())) {
            sql.append("\r\n AND lower(pro.code_projects) LIKE lower(:codeProjects) \r\n");
        }
        if (StringUtils.isNotBlank(request.getNameProjects())) {
            sql.append(" AND lower(pro.name_projects) LIKE lower(:nameProjects) \r\n");
        }
        sql.append("\r\n ORDER BY pro.time_start DESC");

        @SuppressWarnings("unchecked")
        NativeQuery<ListProjectsResponse> query = session.createNativeQuery(sql.toString());
        if (StringUtils.isNotBlank(request.getCodeProjects())) {
            query.setParameter("codeProjects","%" + request.getCodeProjects() + "%");
        }
        if(StringUtils.isNotBlank(request.getNameProjects())) {
            query.setParameter("nameProjects", "%" + request.getNameProjects() + "%");
        }

        Utils.addScalr(query, ListProjectsResponse.class);
        PaginationResult<ListProjectsResponse> result = new PaginationResult<>(query, request.getPageNo(),
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
