package com.hrm.entity;

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "holiday_of_year")
public class HolidayOfYear extends CommonEntity{
    
    @Column(name = "company_id")
    private Integer companyId;
    
    @Column(name = "year")
    private Integer year;
    
    @Column(name = "holiday_of_year")
    private Integer holidayOfYear;
    
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "holidayOfYear")
    @JsonIgnore
    @Fetch(value = FetchMode.SELECT)
    private Collection<DayOff> dayOff;

}
