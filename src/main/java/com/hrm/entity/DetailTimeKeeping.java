package com.hrm.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "detail_time_keeping")
public class DetailTimeKeeping extends CommonEntity {
    @Column(name = "salary_real")
    private Float salaryReal;

    @Column(name = "keeping_forget")
    private Integer keepingForget;

    @Column(name = "salary_count")
    private Float salaryCount;

    @Column(name = "late_time")
    private Integer lateTime;

    @Column(name = "time_save")
    private String timeSave;

    @Column(name = "late_hour")
    private String lateHour;

    @Column(name = "leave_day_accept")
    private Float leaveDayAccept;

    @Column(name = "ot_normal")
    private Float otNormal = (float) 0;

    @Column(name = "ot_morning_7")
    private Float otMorning7 = (float) 0;

    @Column(name = "ot_sat_sun")
    private Float otSatSun = (float) 0;

    @Column(name = "ot_holiday")
    private Float otHoliday = (float) 0;

    @Column(name = "sum_ot_month")
    private Float sumOtMonth = (float) 0;

    @Column(name = "ot_unpaid")
    private Float otUnpaid = (float) 0;

    @Column(name = "compensatory_leave")
    private Float compensatoryLeave = (float) 0;

    @Column(name = "ot_pay_in_month")
    private Float otPayInMonth = (float) 0;

    @Column(name = "csr_leave_plus")
    private Float csrLeavePlus = (float) 0;

    @Column(name = "csr_leave_plus_round")
    private Float csrLeavePlusRound = (float) 0;

    @Column(name = "leave_remain_now")
    private Float leaveRemainNow = (float) 0;

    @Column(name = "csr_leave_now")
    private Float csrLeaveNow = (float) 0;

    @Column(name = "welfare_leave")
    private Float welfareLeave = (float) 0;

    @Column(name = "remote_time")
    private Float remoteTime = (float) 0;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    @JsonProperty
    private Employee employee;
}
