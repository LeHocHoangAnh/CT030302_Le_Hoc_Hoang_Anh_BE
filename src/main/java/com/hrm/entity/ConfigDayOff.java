package com.hrm.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "config_day_off")
public class ConfigDayOff extends CommonEntity{
    
    @Column(name = "month_apply")
    private Integer monthApply;
    
    @Column(name = "day_from")
    private Date dayFrom;
    
    @Column(name = "day_to")
    private Date dayTo;
    
    @Column(name = "reason_apply")
    private String reasonApply;
    
    @Column(name = "year_apply")
    private Integer yearApply;
    
}
