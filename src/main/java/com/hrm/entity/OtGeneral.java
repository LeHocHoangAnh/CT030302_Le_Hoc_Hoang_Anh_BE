package com.hrm.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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
@Table(name = "ot_general")
public class OtGeneral extends CommonEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonProperty
    private Employee employee;

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

    @Column(name = "compensatory_leave")
    private Float compensatoryLeave = (float) 0;

    @Column(name = "month_action")
    private String monthAction;
    
    @Column(name = "cst_leave_rounding")
    private Float cstLeaveRounding = (float) 0;
}
