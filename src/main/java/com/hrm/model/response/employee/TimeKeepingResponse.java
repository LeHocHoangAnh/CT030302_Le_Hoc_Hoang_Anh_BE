package com.hrm.model.response.employee;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimeKeepingResponse {
    private List<TimekeepingDetail> timekeepingDetail;
    private TimeKeepingSummary timeKeepingSummary;
}
