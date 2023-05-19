package com.hrm.entity;

import java.sql.Timestamp;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import javax.persistence.JoinColumn;
import javax.persistence.FetchType;

@Entity
@Table(name = "booking_room")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookingRoom extends CommonEntity{

    @Column(name = "time_start")
    private Timestamp timeStart;

    @Column(name = "time_end")
    private Timestamp timeEnd;

    @Column(name = "reason")
    private String reason;

    @Column(name = "status")
    private Integer status;

    @Column(name = "period_type")
    private Integer periodType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_room", nullable = false)
    @JsonProperty
    private Room room;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_employee", nullable = false)
    @JsonProperty
    private Employee employee;
	
	@Type(type = "com.vladmihalcea.hibernate.type.array.ListArrayType")
	@Column(name = "days_of_week", columnDefinition="text[]")
	private List<String> daysOfWeek;
}
