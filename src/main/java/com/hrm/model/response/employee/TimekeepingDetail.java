package com.hrm.model.response.employee;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimekeepingDetail {
    private String checkIn;

    private String checkOut;

    private String dateWorking;

    private Integer status;

    private Boolean checkDayOff;

    private String totalTimeLateByDay;
}
