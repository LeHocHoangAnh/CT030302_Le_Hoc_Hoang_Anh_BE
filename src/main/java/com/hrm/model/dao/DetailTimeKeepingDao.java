package com.hrm.model.dao;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.hrm.common.Constants;
import com.hrm.model.DropDownResponse;
import com.hrm.model.ResultPageResponse;
import com.hrm.model.request.hr.ListDetailTimeKeepingRequest;
import com.hrm.model.response.hr.ListDetailTimeKeepingResponse;
import com.hrm.repository.TimeKeepingRepository;
import com.hrm.utils.PaginationResult;
import com.hrm.utils.Utils;

@Repository
public class DetailTimeKeepingDao {

    @Autowired
    EntityManager entityManager;

    @Autowired
    private TimeKeepingRepository timeKeepingRepository;

    public ResultPageResponse getListDetailTimeKeeping(ListDetailTimeKeepingRequest request) {
        Session session = entityManager.unwrap(Session.class);
        StringBuilder sql = new StringBuilder();
        sql.append(
                "SELECT\r\n"
                + "        em.employee_code as EmployeeCode,\r\n"
                + "        pro.full_name as FullName,\r\n"
                + "        em.email as Email,\r\n"
                + "        de.salary_real as SalaryReal,\r\n"
                + "		   de.late_hour as LateTimeHour,\r\n"
                + "        de.keeping_forget as KeepingForget,\r\n"
                + "        de.salary_count as SalaryCount,\r\n"
                + "        de.late_time as LateTime,\r\n"
                + "        CASE          \r\n"
                + "            WHEN em.type_contract = 1 THEN 'Chính thức'          \r\n"
                + "            WHEN em.type_contract = 2 THEN 'Thử việc'          \r\n"
                + "            WHEN em.type_contract = 3 THEN 'Freelance'          \r\n"
                + "            WHEN em.type_contract = 4 THEN 'Thực tập'      \r\n"
                + "        END AS TypeContract,\r\n"
                + "        de.ot_normal AS OtNormal,\r\n"
                + "        de.ot_morning_7 AS OtMorning7,\r\n"
                + "        de.ot_sat_sun AS OtSatSun,\r\n"
                + "        de.ot_holiday AS OtHoliday,\r\n"
                + "        de.sum_ot_month AS SumOtMonth,\r\n"
                + "        de.ot_unpaid AS OtUnpaid,\r\n"
                + "        de.compensatory_leave AS CompensatoryLeave,\r\n"
                + "        de.leave_day_accept AS LeaveDayAccept,\r\n"
                + "        de.csr_leave_plus AS CsrLeavePlus,\r\n"
                + "        de.ot_pay_in_month AS OtPayInMonth,\r\n"
                + "        de.csr_leave_plus_round AS CsrLeavePlusRound,\r\n"
                + "        de.leave_remain_now as LeaveRemainNow,\r\n"
                + "        de.csr_leave_now as CsrLeaveNow,\r\n"
                + "        (de2.leave_remain_now - de.leave_day_accept) AS LeaveRemainLastMonth,\r\n"
                + "        (de2.csr_leave_now - de.compensatory_leave) AS CsrLeaveLastMonth,\r\n"
                + "        de.welfare_leave as WelfareLeave,\r\n"
                + "        de.remote_time as remoteTime\r\n"
                + "    FROM\r\n"
                + "        detail_time_keeping as de \r\n"
                + "	   LEFT JOIN \r\n"
                + "		detail_time_keeping as de2\r\n"
                // 			Convert time_save of current month to date, then subtract 1 month to get date of lastmonth, then convert back to yyyy-mm
                + "			ON de2.time_save = to_char(to_date(de.time_save, 'YYYY-MM-DD') - INTERVAL '1 months', 'YYYY-MM') \r\n" 
                + "			AND de2.employee_id = de.employee_id \r\n"
                + "    JOIN\r\n"
                + "        employee as em \r\n"
                + "            ON de.employee_id = em.id  \r\n"
                + "    JOIN\r\n"
                + "        profile as pro \r\n"
                + "            ON em.id = pro.employee_id \r\n" + " Where 1=1 \r\n");
        if (StringUtils.isNotBlank(request.getEmployeeCode())) {
            sql.append("\r\n AND lower(em.employee_code) LIKE lower(:employeeCode)");
        }
        if (StringUtils.isNotBlank(request.getFullName())) {
            sql.append("\r\n AND lower(pro.full_name) LIKE lower(:fullName)");
        }
        if (StringUtils.isNotBlank(request.getTimeYear())) {
            sql.append("\r\n AND de.time_save = :timeYear ");
        }
        if (StringUtils.isBlank(request.getEmployeeCode()) && StringUtils.isBlank(request.getFullName())
                && StringUtils.isBlank(request.getTimeYear())) {
            List<DropDownResponse> listTime = timeKeepingRepository.getListTime();
            if (!listTime.isEmpty()) {
                String firstTime = listTime.get(0).getName();
                sql.append("\r\n AND de.time_save = '" + firstTime + "'");
            }
        }
        sql.append("\r\n ORDER BY em.employee_code ASC");

        @SuppressWarnings("unchecked")
        NativeQuery<ListDetailTimeKeepingResponse> query = session.createNativeQuery(sql.toString());
        if (StringUtils.isNotBlank(request.getEmployeeCode())) {
            query.setParameter("employeeCode", "%" + request.getEmployeeCode() + "%");
        }
        if (StringUtils.isNotBlank(request.getFullName())) {
            query.setParameter("fullName", "%" + request.getFullName() + "%");
        }
        if (StringUtils.isNotBlank(request.getTimeYear())) {
            query.setParameter("timeYear", request.getTimeYear());
        }
        Utils.addScalr(query, ListDetailTimeKeepingResponse.class);
        PaginationResult<ListDetailTimeKeepingResponse> result = new PaginationResult<>(query, request.getPageNo(),
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