package com.hrm.model.response.hr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListProjectsResponse {
    private Integer id;
    
    private String codeProjects;
    
    private String nameProjects;
    
    private String timeStart;
    
    private String timeEnd;
    
    private String customer;
    
    private String technology;
    
    private Long totalOt;
}
