package com.hrm.model.request.hr;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrEditProjectRequest {
    @ApiModelProperty(value="id dự án")
    private Integer id;
    @ApiModelProperty(value="mã dự án")
    private String codeProjects;
    @ApiModelProperty(value="tên dự án")
    private String nameProjects;
    @ApiModelProperty(value="khách hàng")
    private String customer;
    @ApiModelProperty(value="công nghệ sử dụng")
    private String technology;
    @ApiModelProperty(value="thời gian bắt đầu")
    private String timeStart;
    @ApiModelProperty(value="thời gian kết thúc")
    private String timeEnd;
    @ApiModelProperty(value="mô tả dự án")
    private String description;

}
