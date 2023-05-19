package com.hrm.model.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class PaginationRequest {

    @ApiModelProperty(value = "Số lượng phần tử trên 1 trang")
    private Integer pageSize;
    @ApiModelProperty(value = "Trang số")
    private Integer pageNo;
}
