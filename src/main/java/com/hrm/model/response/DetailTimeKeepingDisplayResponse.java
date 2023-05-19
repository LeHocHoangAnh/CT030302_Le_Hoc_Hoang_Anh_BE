package com.hrm.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetailTimeKeepingDisplayResponse{
	private Float salaryReal;

    private Integer keepingForget;

    private Float salaryCount;

    private Integer lateTime;

    private String timeSave;

    private String lateHour;

    private Float leaveDayAccept;

    private Float otNormal = (float) 0;

    private Float otMorning7 = (float) 0;

    private Float otSatSun = (float) 0;

    private Float otHoliday = (float) 0;

    private Float sumOtMonth = (float) 0;

    private Float otUnpaid = (float) 0;

    private Float compensatoryLeave = (float) 0;

    private Float otPayInMonth = (float) 0;
    
    private Float csrLeavePlus = (float) 0;
    
    private Float csrLeavePlusRound = (float) 0;
    
    private Float leaveRemainNow = (float) 0;
    
    private Float csrLeaveNow = (float) 0;
    
    private Float welfareLeave = (float) 0;
    
    private Float unpaidLeave;
    
    private Float remoteTime;
    
    private Integer awdTime;
}
