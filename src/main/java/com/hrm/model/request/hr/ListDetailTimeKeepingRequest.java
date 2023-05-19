package com.hrm.model.request.hr;

import com.hrm.model.request.PaginationRequest;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ListDetailTimeKeepingRequest extends PaginationRequest{
    
    @ApiModelProperty(value="mã nhân viên")
    private String employeeCode;
    @ApiModelProperty(value="thời gian")
    private String fullName;
    @ApiModelProperty(value="năm")
    private String timeYear;
}
