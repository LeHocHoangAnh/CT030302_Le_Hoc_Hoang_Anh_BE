package com.hrm.model.response.hr;

import lombok.Data;

@Data
public class ListDetailTimeKeepingResponse {
    private String employeeCode;
    private String fullName;
    private String email;
    private Float salaryReal;
    private String lateTimeHour;
    private Integer keepingForget;
    private Float salaryCount;
    private Integer lateTime;
    private String typeContract;
    private Float otNormal;
    private Float otMorning7;
    private Float otSatSun;
    private Float otHoliday;
    private Float sumOtMonth;
    private Float otUnpaid;
    private Float compensatoryLeave;
    private Float csrLeavePlus;
    private Float otPayInMonth;
    private Float csrLeavePlusRound;
    private Float leaveRemainNow;
    private Float csrLeaveNow;
    private Float leaveRemainLastMonth;
    private Float csrLeaveLastMonth;
    private Float welfareLeave;
    private Float leaveDayAccept;
    private Float remoteTime;
}
