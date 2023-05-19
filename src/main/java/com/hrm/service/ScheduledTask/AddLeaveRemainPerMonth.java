package com.hrm.service.ScheduledTask;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.hrm.common.Constants;
import com.hrm.entity.DetailTimeKeeping;
import com.hrm.repository.DetailTimeKeepingRepository;
import com.hrm.repository.EmployeeRepository;
import com.hrm.utils.Utils;

@EnableAsync
@Service
public class AddLeaveRemainPerMonth {
	
	@Autowired
	EmployeeRepository employeeRepository;
	@Autowired
	DetailTimeKeepingRepository detailTimeKeepingRepository;
	
	@Async
	@Scheduled(cron = "0 0 22 1W * ?") // every 10PM of the First weekday of the month 
	public void monthlyAddLeaveRemain() {
		Date currentDate = new Date();
		List<DetailTimeKeeping> detailTimeKeeping = getPreviousTimeKeeping(currentDate);
		// add leave remain of the month by 1
		detailTimeKeeping.forEach(item -> item.setLeaveRemainNow(item.getLeaveRemainNow()+1));
		detailTimeKeepingRepository.saveAll(detailTimeKeeping);
	}
	// get and return detail time keeping of the previous month
	List<DetailTimeKeeping> getPreviousTimeKeeping (Date currentDate) {
		Calendar cal = Calendar.getInstance(); 
		cal.setTime(currentDate);
		cal.add(Calendar.MONTH, -1);
		List<DetailTimeKeeping> detailTimeKeeping = detailTimeKeepingRepository.findAllByTimeSave(Utils.convertDateToString(Constants.YYYY_MM, cal.getTime()));
		
		// if the previous month is null, call method again and get the previous month of the previous month		
		return Utils.isNullList(detailTimeKeeping)?getPreviousTimeKeeping(cal.getTime()):detailTimeKeeping;
	}
	
}
