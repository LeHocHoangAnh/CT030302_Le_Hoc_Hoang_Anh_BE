package com.hrm.model.response.hr;

import lombok.Data;

@Data
public class ListDetailBookingMeetingRoomResponse {
    private Integer id;
    private String reason;
    private String employeeName;
    private String roomName;
    private String timeStart;
    private String timeEnd;
    private String status;
}
