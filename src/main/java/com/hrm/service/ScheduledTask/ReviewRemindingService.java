package com.hrm.service.ScheduledTask;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.hrm.common.CommonFilter;
import com.hrm.common.Constants;
import com.hrm.model.response.ReviewRemindingResponse;
import com.hrm.repository.EmployeeRepository;
import com.hrm.utils.Utils;

@EnableAsync
@Service
public class ReviewRemindingService {
	
	@Autowired
	EmployeeRepository employeeRepository;
	@Autowired
    private JavaMailSender emailSender;
	
	private Integer officialType = CommonFilter.TYPE_CONTRACT.OFFICIAL_CONTRACT.getValue();
	private Integer probationaryType = CommonFilter.TYPE_CONTRACT.PROBATIONARY_CONTRACT.getValue();
	private Integer internType = CommonFilter.TYPE_CONTRACT.INTERNSHIP_CONTRACT.getValue();
	private Integer INTERN_REMIND_DAYS_BEFORE = 7;
	private Integer PROBATION_REMIND_DAYS_BEFORE = 5;
	private Integer OFFICIAL_REMIND_DAYS_BEFORE = 45;
	private Integer aboutOneYearDays = 400;
    @Value("${spring.mail.username}")
    private String mailFrom;
    private String mailTo = "anhlhh@its-global.vn";
	
	@Async
	@Scheduled(cron = "00 00 10 * * ?") // 10:00:00 AM every day, every month, every year
	public void dailyCheckEmployeeDueReview() {
		remindEmployeesDueToReview();
	}
	
	public void remindEmployeesDueToReview(){
		Date currentDate = new Date();
		// find all employee that will be reviewed after 7/ 5/ 45 days
		List<ReviewRemindingResponse> employeeListDueToReview = employeeRepository.findEmployeDueToReviewByTypeContract(
				internType, probationaryType, officialType, 
				INTERN_REMIND_DAYS_BEFORE, PROBATION_REMIND_DAYS_BEFORE, OFFICIAL_REMIND_DAYS_BEFORE,
				currentDate, Constants.DELETE_NONE);	
		
		if(!employeeListDueToReview.isEmpty() && employeeListDueToReview!=null) {
			sendRemindingMail(employeeListDueToReview, internType);
		}
	}
	
	public void sendRemindingMail(List<ReviewRemindingResponse> listEmployee, Integer reviewType) {
		// email subject
		StringBuilder emailSubject = new StringBuilder();
		emailSubject.append("[Thông Báo Tự Động] Nhắc nhở lịch review nhân viên");
		
		// email content
        StringBuilder emailContent = new StringBuilder();
        int index=0;
    	emailContent.append("Danh sách nhân viên: ");
		emailContent.append("\r");
        for (ReviewRemindingResponse employee : listEmployee) {
        	index++;
			emailContent.append("\r ");
        	emailContent.append(index+". Nhân viên: "+employee.getEmployeeName());
        	emailContent.append("\r ");
			emailContent.append("   - Phòng ban: "+employee.getDepartmentName());
			emailContent.append("\r ");
			emailContent.append("   - Hợp đồng: ");
			emailContent.append(employee.getTypeContract()==internType?Constants.PROBATIONARY_CONTRACT:
				employee.getTypeContract()==probationaryType?Constants.OFFICIAL_CONTRACT:
				employee.getTypeContract()==officialType && employee.getSeniority()<=aboutOneYearDays?Constants.OFFICIAL_3Y_CONTRACT:
				employee.getTypeContract()==officialType && employee.getSeniority()>aboutOneYearDays?Constants.OFFICIAL_UNLIMITED_CONTRACT:null
			);
			emailContent.append("\r ");
			emailContent.append("   - Ngày Review: "+Utils.convertDateToString("dd/MM/yyyy", employee.getReviewDate()));
			emailContent.append("\r");
		}
        
        try {
        	SimpleMailMessage message = new SimpleMailMessage();
        	// send from
            message.setFrom(mailFrom);
            // send to
            message.setTo(mailTo);
            // subject
            message.setSubject(emailSubject.toString()); 
            // content
            message.setText(emailContent.toString());
            
            emailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}
