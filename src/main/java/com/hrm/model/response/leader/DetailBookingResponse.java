package com.hrm.model.response.leader;

public interface DetailBookingResponse {
    Integer getId();
    
    Integer getEmployeeId();
    
    String getFullName();
   
    String getDepartmentName();

    String getStatus();

    String getRequestDay();

    String getBackDay();

    String getReason();

    Integer getConfirm();
    
    String getApprover();
    
    String getEvidenceImage();
    
    String getProjectName();
    
    Boolean getDeleteFlag();
    
    Float getTotalOtTime();
    
    void setTotalOtTime(float otTime);
    
    void setApprover(String approver);
}
