package com.hrm.entity;

import java.sql.Timestamp;

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
@Table(name = "projects")
public class Projects extends CommonEntity {

    @Column(name = "name_projects")
    private String nameProjects;
    
    @Column(name = "code_projects")
    private String codeProjects;
    
    @Column(name = "customer")
    private String customer;
    
    @Column(name = "technology")
    private String technology;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "time_start")
    private Timestamp timeStart;
    
    @Column(name = "time_end")
    private Timestamp timeEnd;
}
