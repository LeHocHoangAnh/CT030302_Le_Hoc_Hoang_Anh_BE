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
@Table(name = "day_off")
public class DayOff extends CommonEntity{
    
    @Column(name = "day_off_remain_year")
    private Integer dayOffRemainYear;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonProperty
    private Employee employee;
    
    @Column(name = "day_off_present")
    private Integer dayOffPresent;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_holiday_of_year", nullable = false)
    @JsonProperty
    private HolidayOfYear holidayOfYear;
    
}
