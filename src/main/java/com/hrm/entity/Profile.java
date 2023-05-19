package com.hrm.entity;

import java.util.Collection;
import java.util.Date;

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


@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "profile")
public class Profile extends CommonEntity{
    
    @Column(name = "full_name")
    private String fullName;
    
    @Column(name = "date_of_birth")
    private Date dateOfBirth;
    
    @Column(name = "gender")
    private Boolean gender;
    
    @Column(name = "address")
    private String address;
    
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Column(name = "date_entry")
    private Date dateEntry;
    
    @Column(name = "date_out")
    private Date dateOut;
    
    @Column(name = "tax_code")
    private String taxCode;
    
    @Column(name = "safe_code")
    private String safeCode;
    
    @Column(name = "salary_basic")
    private Double salaryBasic;
    
    @Column(name = "bank_name")
    private String bankName;
    
    @Column(name = "bank_account")
    private String bankAccount;
    
    @Column(name = "discord_id")
    private String discordId;
    
    @Column(name = "perm_address")
    private String permAddress;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonProperty
    private Employee employee;

}
