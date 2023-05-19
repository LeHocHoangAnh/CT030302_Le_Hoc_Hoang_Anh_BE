package com.hrm.entity;

import java.sql.Timestamp;

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
@Table(name = "history_work")
public class HistoryWork extends CommonEntity{
    
    @Column(name = "time_start")
    private Timestamp timeStart;
    
    @Column(name = "time_end")
    private Timestamp timeEnd;
    
    @Column(name = "role")
    private String role;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_employee", nullable = false)
    @JsonProperty
    private Employee employee;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_projects", nullable = false)
    @JsonProperty
    private Projects projects;
}
