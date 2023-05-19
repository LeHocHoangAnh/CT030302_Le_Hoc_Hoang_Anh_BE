package com.hrm.model.response.employee;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimeKeepingSummary {
    private String totalTimeLate;

    private String seriouslyTotalTimeLate;

    private Float totalNumberSeriouslyTimeLate;

    private Integer countTotalDayLate;

    private Float countTotalDayOffNoAccept;

    private Float countTotalDayOffAccept;

    private Float countTotalStandardWorkDay;

    private Integer totalForgotTimeKeeping;

    private Float countPersonalLeave;

    private Float countCompensatoryLeave;

    private Float totalNumberTimeLate;

    private String totalAwayFromDesk;

    private Float TotalForgotTimeKeepingAccept;

    private Float countTotalRemoteTime;
}
