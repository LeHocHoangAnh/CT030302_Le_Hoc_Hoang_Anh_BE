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
@Table(name = "department")
public class Department extends CommonEntity{
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "action")
    private Integer action;
    
    @Column(name = "number_member")
    private Integer numberMember;
    
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "department")
    @JsonIgnore
    @Fetch(value = FetchMode.SELECT)
    private Collection<Employee> employee;

}
