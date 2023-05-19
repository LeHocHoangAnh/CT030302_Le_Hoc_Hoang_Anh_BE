package com.hrm.model.request.leader;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookingRequest {
    @ApiModelProperty(value = "Id xin phép")
    private Integer id;
    @ApiModelProperty(value = "Phê duyệt xin phép")
    private Integer confirm;
    @ApiModelProperty(value = "Loại xin phép")
    private String status;
    @ApiModelProperty(value = "Lý do phê duyệt")
    private String confirmReason;
}
