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
@Table(name = "role_group")
public class RoleGroup extends CommonEntity{
    
    @Column(name = "role_name")
    private String roleName;
    
    @Column(name = "hr_flag")
    private Boolean hrFlag;
    
    @Column(name = "leader_flag")
    private Boolean leaderFlag;

    @Column(name = "sub_leader_flag")
    private Boolean subLeaderFlag;

    @Column(name = "comtor_flag")
    private Boolean comtorFlag;

    @Column(name = "customer_flag")
    private Boolean customerFlag;
    
    @Column(name = "delete_flag")
    private Integer deleteFlag;
    
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "roleGroup")
    @JsonIgnore
    @Fetch(value = FetchMode.SELECT)
    private Collection<Employee> employee;

}
