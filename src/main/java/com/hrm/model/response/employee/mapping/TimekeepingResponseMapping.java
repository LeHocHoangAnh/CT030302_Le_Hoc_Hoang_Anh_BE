package com.hrm.model.response.employee.mapping;

import java.util.Date;

public interface TimekeepingResponseMapping {
    Date getCheckIn();

    Date getCheckOut();

    Date getDateWorking();
}
