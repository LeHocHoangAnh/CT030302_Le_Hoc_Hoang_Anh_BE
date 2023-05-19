package com.hrm.model.dao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.hrm.common.CommonService;
import com.hrm.common.Constants;
import com.hrm.entity.Employee;
import com.hrm.model.ResultPageResponse;
import com.hrm.model.request.hr.ListBookingRequest;
import com.hrm.model.response.leader.ListBookingResponse;
import com.hrm.repository.EmployeeRepository;
import com.hrm.utils.PaginationResult;
import com.hrm.utils.Utils;

@Repository
public class ApproveGeneralDao {
    @Autowired
    EntityManager entityManager;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private CommonService commonService;

    public ResultPageResponse getListBooking(ListBookingRequest request, String requestID) {
        Session session = entityManager.unwrap(Session.class);
        StringBuilder sql = new StringBuilder();
        sql.append(
                "SELECT bo.reason, bo.id,pro.full_name as FullName,de.name as DepartmentName, bo.delete_flag as deleteFlag, \r\n"
                        + " CASE " + "    WHEN bo.status = 0 THEN 'Nghỉ Phép' "
                        + "    WHEN bo.status = 1 THEN 'Đi Muộn/Về Sớm' " + "    WHEN bo.status = 2 THEN 'Remote' "
                        + "    WHEN bo.status = 3 THEN 'Ra Ngoài' " + "    WHEN bo.status = 4 THEN 'OT' "
                        + "    WHEN bo.status = 5 THEN 'Nghỉ Phúc Lợi' " + "    WHEN bo.status = 6 THEN 'Nghỉ Bù' "
                        + "    WHEN bo.status = 7 THEN 'Nghỉ Không Lương' "
                        + "    WHEN bo.status = 8 THEN 'Quên Chấm Công' "
                        + "    WHEN bo.status = 9 THEN 'Đăng Ký Thiết Bị' "
                        + " END as Status,bo.request_day as RequestDay, " + " CASE "
                        + "    WHEN bo.confirm = 0 THEN 'Chờ Xác Nhận' " + "    WHEN bo.confirm = 1 THEN 'Xác Nhận' "
                        + "    WHEN bo.confirm = 2 THEN 'Từ Chối' " + " END as Confirm \r\n "
                        + " FROM booking_day_off as bo \r\n" + " JOIN employee as emp ON bo.employee_id = emp.id \r\n "
                        + " JOIN profile as pro ON emp.id = pro.employee_id \r\n "
                        + " JOIN department as de ON emp.department_id = de.id \r\n " + " WHERE 1=1");
        if (request.getWait() == true && request.getApprove() == false && request.getRefuse() == false) {
            sql.append("\r\n AND bo.confirm = 0 \r\n");
        }
        if (request.getApprove() == true && request.getWait() == false && request.getRefuse() == false) {
            sql.append(" AND bo.confirm = 1 \r\n");
        }
        if (request.getRefuse() == true && request.getApprove() == false && request.getWait() == false) {
            sql.append(" AND bo.confirm = 2 \r\n");
        }
        if (request.getWait() == true && request.getApprove() == true && request.getRefuse() == false) {
            sql.append("\r\n AND (bo.confirm = 0 OR bo.confirm = 1) \r\n");
        }
        if (request.getWait() == true && request.getApprove() == false && request.getRefuse() == true) {
            sql.append(" AND (bo.confirm = 0 OR bo.confirm = 2)\r\n");
        }
        if (request.getWait() == false && request.getApprove() == true && request.getRefuse() == true) {
            sql.append(" AND (bo.confirm = 1 OR bo.confirm = 2)\r\n");
        }
        if ((request.getRefuse() == true && request.getApprove() == true && request.getWait() == true)
                || (request.getRefuse() == false && request.getApprove() == false && request.getWait() == false)) {
            sql.append(" AND (bo.confirm = 0 OR bo.confirm = 1 OR bo.confirm = 2)\r\n");
        }
        if (StringUtils.isNotBlank(request.getFromDate())) {
            sql.append(" AND bo.request_day >= :fromDate \r\n");
        }
        if (StringUtils.isNotBlank(request.getToDate())) {
            sql.append(" AND bo.request_day <= :toDate \r\n");
        }
        if (request.getStatus() != null) {
            sql.append(" AND bo.status = :status \r\n");
        }
        if (StringUtils.isNotBlank(request.getName())) {
            sql.append(" AND lower(pro.full_name) LIKE lower(:name) \r\n");
        }
        if (StringUtils.isNotBlank(request.getDepartment())) {
            sql.append(" AND de.name = :department \r\n");
        }
        if (request.getDeleteFlag() == true) {
            sql.append("\r\n AND bo.delete_flag = true \r\n");
        }
        if (request.getDeleteFlag() == false) {
            sql.append("\r\n AND bo.delete_flag = false \r\n");
        }
        // Fetch data according to leader's department
        if (request.getIsLeader()) {
            Optional<Employee> optionalEmployee = employeeRepository.findById(commonService.idUserAccountLogin());
//            sql.append(" AND (emp.department_id ="+optionalEmployee.get().getDepartment().getId());
            sql.append(" and ('" + requestID + "' like any(bo.approver_ids))");
        }
        //

        sql.append("\r\n ORDER BY bo.confirm ASC, bo.ID DESC");

        @SuppressWarnings("unchecked")
        NativeQuery<ListBookingResponse> query = session.createNativeQuery(sql.toString());
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
        if (request.getStatus() != null) {
            query.setParameter("status", request.getStatus());
        }
        if (StringUtils.isNotBlank(request.getName())) {
            query.setParameter("name", "%" + request.getName() + "%");
        }
        if (StringUtils.isNotBlank(request.getDepartment())) {
            query.setParameter("department", request.getDepartment());
        }

        Utils.addScalr(query, ListBookingResponse.class);
        PaginationResult<ListBookingResponse> result = new PaginationResult<>(query, request.getPageNo(),
                request.getPageSize(), Constants.MAX_NAVIGATION_RESULT);
        ResultPageResponse resultPageResponse = new ResultPageResponse();
        resultPageResponse.setItems(result.getList());
        resultPageResponse.setTotalPages(result.getTotalPages());
        resultPageResponse.setTotalItems(result.getTotalRecords());
        resultPageResponse.setCurrentPage(result.getCurrentPage());
        // 1 2 3 4 5 ... 11 12 13
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
