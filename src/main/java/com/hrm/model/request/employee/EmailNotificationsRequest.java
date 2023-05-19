package com.hrm.model.request.employee;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailNotificationsRequest {
    private Integer employeeId;
    private Boolean bookingDayOffNotify;
    private Boolean confirmDayOffNotify;
    private Boolean bookingMeetingNotify;
    private Boolean confirmMeetingNotify;
}
