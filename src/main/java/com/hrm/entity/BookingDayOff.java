package com.hrm.entity;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Type;

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
@Table(name = "booking_day_off")
public class BookingDayOff extends CommonEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    @JsonProperty
    private Employee employee;

    @Column(name = "status")
    private int status;

    @Column(name = "request_day")
    private Timestamp requestDay;

    @Column(name = "back_day")
    private Timestamp backDay;

    @Column(name = "reason")
    private String reason;

    @Column(name = "approver")
    private Integer approver;

    @Column(name = "time_late")
    private Integer timeLate;

    @Column(name = "confirm")
    private Integer confirm;

    @Column(name = "project_id")
    private Integer projectId;

    @Type(type = "com.vladmihalcea.hibernate.type.array.ListArrayType")
    @Column(name = "approver_IDs", columnDefinition = "text[]")
    private List<String> approverIDs;

    @Type(type = "com.vladmihalcea.hibernate.type.array.ListArrayType")
    @Column(name = "approve_progress", columnDefinition = "text[]")
    private List<String> approveProgress;

    @Type(type = "com.vladmihalcea.hibernate.type.array.ListArrayType")
    @Column(name = "related_employee_ids", columnDefinition = "text[]")
    private List<String> relatedEmployeeIDs;

    @Column(name = "evidence_image")
    private String evidenceImage;

    @Type(type = "com.vladmihalcea.hibernate.type.array.ListArrayType")
    @Column(name = "approve_reason", columnDefinition = "text[]")
    private List<String> approveReason;

    @Column(name = "selected_type_time")
    private String selectedTypeTime;

    @Column(name = "delete_flag")
    private boolean deleteFlag;
}
