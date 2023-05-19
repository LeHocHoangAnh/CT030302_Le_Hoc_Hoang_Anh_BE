package com.hrm.model.response;

public interface DetailTimeKeepingForExportResponse {
    String getEmployeeCode();

    String getFullName();

    String getEmail();

    Float getSalaryReal();

    Integer getKeepingForget();

    Float getSalaryCount();

    Integer getLateTime();

    String getTypeContract();

    String getLateHour();

    Float getLeaveDayAccept();

    Float getOtNormal();

    Float getOtMorning7();

    Float getOtSatSun();

    Float getOtHoliday();

    Float getSumOtMonth();

    Float getOtUnpaid();

    Float getCompensatoryLeave();
    
    Float getOtPayInMonth();
    
    Float getCsrLeavePlus();
    
    Float getCsrLeavePlusRound();
    
    Float getLeaveRemainNow();
    
    Float getCsrLeaveNow();
    
    Float getWelfareLeave();
    
    Float getLeaveRemainLastMonth();
    
    Float getCsrLeaveLastMonth();
    
}
