package com.hrm.model.response.employee;

import java.util.Date;

public interface EmployeeProfileResponse {
    Integer getId();
    String getEmployeeCode();
    String getEmail();
    String getDateEntry();
    String getPictureProfile();
    String getFullName();
    Date getDateOfBirth();
    Boolean getGender();
    String getAddress();
    String getPermAddress();
    String getPhoneNumber();
    String getDepartmentName();
    String getBankName();
    String getBankAccount();
    Boolean getBookingDayOffNotify();
    Boolean getConfirmDayOffNotify();
    Boolean getBookingMeetingNotify();
    Boolean getConfirmMeetingNotify();
}
