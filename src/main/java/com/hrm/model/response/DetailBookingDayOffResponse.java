package com.hrm.model.response;

import java.util.List;

import com.hrm.entity.BookingDayOff;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetailBookingDayOffResponse{
	BookingDayOff bookingDayOff;
	List<String> approverFullName;
}
