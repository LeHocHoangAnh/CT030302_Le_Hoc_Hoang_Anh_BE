package com.hrm.model.request.hr;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrEditHistoryRequest {
    @ApiModelProperty(value = "Id lịch sử")
    private Integer id;
    @ApiModelProperty(value = "Id nhân viên")
    private Integer idEmployee;
    @ApiModelProperty(value = "Id dự án")
    private Integer idProjects;
    @ApiModelProperty(value = "Thời gian bắt đầu")
    private String timeStart;
    @ApiModelProperty(value = "Thời gian kết thúc")
    private String timeEnd;
    @ApiModelProperty(value = "Chức vụ nhân viên trong dự án")
    private String role;
    
}
