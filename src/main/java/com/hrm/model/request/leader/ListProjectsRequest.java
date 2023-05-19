package com.hrm.model.request.leader;

import com.hrm.model.request.PaginationRequest;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ListProjectsRequest extends PaginationRequest {
    @ApiModelProperty(value="tên dự án")
    private String nameProjects;
    @ApiModelProperty(value="mã dự án")
    private String codeProjects;

}
