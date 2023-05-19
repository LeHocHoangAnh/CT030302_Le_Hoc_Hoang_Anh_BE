package com.hrm.model.request.employee;

import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class CreateOrUpdateBookingRoomRequest {
    private Integer id;

    private Timestamp timeStart;

    private Timestamp timeEnd;

    private String reason;

    private Integer roomId;

    private Integer status;
    
    private Integer periodType;
    
    private List<String> daysOfWeek;
}
