package com.hrm.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportFileResponse {
    private String idEmployee;
    private String name;
    private String email;
    private String contract;
    private Integer lateTime;
    private String lateHour;
    private Integer forgetTimeKeeping;
    private Float keepingReal;
    private Float onLeave;
    private Float paidLeave;
    private Float plusLeave;
    private Float salaryKeeping;
    private Float oTNormal;
    private Float oTMorningSaturday;
    private Float oTAfternoonSaturdayAndSunDay;
    private Float oTHoliday;
    private Float oTConvertSalary1;
    private Float plusLeaveAddMonthNow;
    private Float plusLeaveAdd;
    private Float oTPayWageInMonth;
    private Float oTUnpaid;
    private Float onLeaveRemainOnNow;
    private Float paidLeaveOTRemainOnNow;
    private Float onLeaveRemainOnLastMonth;
    private Float paidLeaveOTRemainOnOnLastMonth;
}
