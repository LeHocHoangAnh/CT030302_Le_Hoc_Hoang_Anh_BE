package com.hrm.model.dao;

import com.hrm.common.Constants;
import com.hrm.model.ResultPageResponse;
import com.hrm.model.request.hr.ListBookingMeetingRoom;
import com.hrm.model.response.hr.ListDetailBookingMeetingRoomResponse;
import com.hrm.utils.PaginationResult;
import com.hrm.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

@Repository
public class BookingMeetingRoomDAO {
    @Autowired
    EntityManager entityManager;

    public ResultPageResponse searchListBookingRoom(ListBookingMeetingRoom request) {
        Session session = entityManager.unwrap(Session.class);
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT br.id,br.reason,p.full_name AS employeeName,r.NAME AS roomName,br.time_start AS timeStart,\n" +
                "br.time_end AS timeEnd,\n" +
                "CASE WHEN br.status = 0 THEN 'Chờ xác nhận' \n" +
                "WHEN br.status = 1 THEN 'Đồng ý' \n" +
                "WHEN br.status = 2 THEN 'Không đồng ý' ELSE'' END status\n" +
                "FROM booking_room AS br\n" +
                "LEFT JOIN room AS r ON br.id_room = r.ID \n" +
                "LEFT JOIN profile AS p ON br.id_employee = p.employee_id \n" +
                "WHERE \n");
        sql.append(" br.status IN ( -10 \r\n");
        if (request.getWait()) {
            sql.append(", 0");
        }
        if (request.getApprove()) {
            sql.append(", 1");
        }
        if (request.getRefuse()) {
            sql.append(", 2");
        }
        sql.append(") \r\n");
        if (StringUtils.isNotBlank(request.getTime())) {
            sql.append(" AND (to_char(br.time_start,'MM/YYYY') = :time OR to_char(br.time_end,'MM/YYYY') = :time )\r\n");
        }
        if (StringUtils.isNotBlank(request.getName())) {
            sql.append(" AND p.full_name like :fullName \r\n");
        }
        if (request.getRoomId() != null) {
            sql.append(" AND r.id =:roomId \r\n");
        }
        sql.append(" ORDER BY br.ID DESC");

        @SuppressWarnings("unchecked")
        NativeQuery<ListDetailBookingMeetingRoomResponse> query = session.createNativeQuery(sql.toString());
        if (StringUtils.isNotBlank(request.getTime())) {
            query.setParameter("time", request.getTime());
        }
        if (StringUtils.isNotBlank(request.getName())) {
            query.setParameter("fullName", "%" + request.getName() + "%");
        }
        if (request.getRoomId() != null) {
            query.setParameter("roomId", request.getRoomId());
        }
        Utils.addScalr(query, ListDetailBookingMeetingRoomResponse.class);
        PaginationResult<ListDetailBookingMeetingRoomResponse> result = new PaginationResult<>(query, request.getPageNo(),
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
