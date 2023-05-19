package com.hrm.model.dao;

import com.hrm.common.CommonFilter;
import com.hrm.model.request.hr.CreateOrEditEmployeeRequest;
import com.hrm.model.response.hr.ListProjectsResponse;
import com.hrm.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

@Repository
public class RoleGroupDAO {

    @Autowired
    private EntityManager entityManager;

    public Integer getRoleGroupEmployee(CreateOrEditEmployeeRequest request) {
        Session session = entityManager.unwrap(Session.class);
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id from role_group WHERE delete_flag = 0 ");
        boolean leaderFlag = false;
        boolean subLeaderFlag = false;
        boolean hrFlag = false;
        boolean comtorFlag = false;
        boolean customerFlag = false;
        sql.append(" AND leader_flag =:leaderFlag \n");
        sql.append(" AND sub_leader_flag =:subLeaderFlag \n");
        sql.append(" AND comtor_flag =:comtorFlag \n");
        sql.append(" AND hr_flag =:hrFlag \n");
        sql.append(" AND customer_flag =:customerFlag \n");
        for (String role:request.getSelectedRoles()){
            if(role.equalsIgnoreCase(String.valueOf(CommonFilter.ROLES.LEADER))){
                leaderFlag = true;
            }
            if(role.equalsIgnoreCase(String.valueOf(CommonFilter.ROLES.SUB_LEADER))){
                subLeaderFlag = true;
            }
            if(role.equalsIgnoreCase(String.valueOf(CommonFilter.ROLES.COMTOR))){
                comtorFlag = true;
            }
            if(role.equalsIgnoreCase(String.valueOf(CommonFilter.ROLES.HR))){
                hrFlag = true;
            }
            if(role.equalsIgnoreCase(String.valueOf(CommonFilter.ROLES.CUSTOMER))){
                customerFlag = true;
            }
        }
        session.close();
        Query<?> query = session.createNativeQuery(sql.toString());
        query.setParameter("leaderFlag",leaderFlag);
        query.setParameter("subLeaderFlag",subLeaderFlag);
        query.setParameter("comtorFlag",comtorFlag);
        query.setParameter("hrFlag",hrFlag);
        query.setParameter("customerFlag",customerFlag);
        return Integer.parseInt(query.getSingleResult().toString());
    }
}
