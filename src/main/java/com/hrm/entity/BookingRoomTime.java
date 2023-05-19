package com.hrm.entity;

import java.sql.Timestamp;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.JoinColumn;
import javax.persistence.FetchType;

@Entity
@Table(name = "booking_room_time")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookingRoomTime extends CommonEntity{

    @Column(name = "time_start")
    private Timestamp timeStart;

    @Column(name = "time_end")
    private Timestamp timeEnd;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "booking_room_id", nullable = false)
    @JsonProperty
    private BookingRoom bookingRoom;
}
