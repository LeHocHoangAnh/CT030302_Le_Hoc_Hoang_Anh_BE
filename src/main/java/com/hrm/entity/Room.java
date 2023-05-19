package com.hrm.entity;

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.hrm.common.Constants;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "room")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Room extends CommonEntity{
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "status")
    private Integer status = Constants.STATUS_NOT_ACTIVE;
    
    @Column(name = "display_color")
    private String displayColor;
    
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "room")
    @JsonIgnore
    @Fetch(value = FetchMode.SELECT)
    private Collection<BookingRoom> bookingRoom;
}
