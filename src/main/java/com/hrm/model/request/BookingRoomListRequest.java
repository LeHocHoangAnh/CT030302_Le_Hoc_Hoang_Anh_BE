package com.hrm.model.request;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingRoomListRequest {
    private Date dateRequest;
    private String days;
}
