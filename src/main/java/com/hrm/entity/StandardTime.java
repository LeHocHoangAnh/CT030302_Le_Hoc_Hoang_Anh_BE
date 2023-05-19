package com.hrm.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import java.sql.Time;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "standard_time")
public class StandardTime  extends CommonEntity{
    @Column(name = "check_in_morning")
    private Time checkInMorning;

    @Column(name = "check_out_morning")
    private Time checkOutMorning;

    @Column(name = "check_in_afternoon")
    private Time checkInAfternoon;

    @Column(name = "check_out_afternoon")
    private Time checkOutAfternoon;

    @Column(name = "delete_flag")
    private Integer deleteFlag;
}