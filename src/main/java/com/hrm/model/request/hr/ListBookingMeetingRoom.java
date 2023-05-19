package com.hrm.model.request.hr;

import com.hrm.model.request.PaginationRequest;
import lombok.Data;

@Data
public class ListBookingMeetingRoom extends PaginationRequest{
        private String time;
        private Boolean wait = true;
        private Boolean approve = true;
        private Boolean refuse = true;
        private String name;
        private Integer roomId;
}
