package com.hrm.entity;

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
@Table(name ="company")
public class Company extends CommonEntity{
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "location")
    private String location;
    
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Column(name = "representer")
    private String representer;
    
    @Column(name = "tax_code")
    private String taxCode;
    
}
