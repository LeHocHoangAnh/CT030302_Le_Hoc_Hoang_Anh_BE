package com.hrm.model.response.hr;

public interface CreateOrEditEmployeeResponse {
    Integer getId();

    String getEmployeeCode();

    String getEmail();

    String getFullName();

    Boolean getGender();

    Integer getStatus();

    Integer getRoleGroupId();

    Integer getTypeContract();

    String getPhoneNumber();

    String getDateOfBirth();

    Integer getDepartmentId();

    String getAddress();

    String getWorkName();

    String getDateEntry();
    
    String getDateOut();

    String getTaxCode();

    String getSafeCode();

    Float getSalaryBasic();
    
    String getPictureName();
    
    String getPictureProfile();
    
    String getBankName();
    
    String getBankAccount();
    
    String getReviewDate();

    String getDiscordId();
    
    String getPermAddress();
}
