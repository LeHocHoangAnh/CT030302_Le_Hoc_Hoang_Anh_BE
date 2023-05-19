package com.hrm.model.request.employee;

import java.sql.Timestamp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterEquipmentRequest {
    @ApiModelProperty(value = "id đơn đăng ký thiết bị")
    private Integer id;
    @ApiModelProperty(value = "loại thiết bị đăng ký")
    private Integer category;
    @ApiModelProperty(value = "mô tả chi tiết thiết bị")
    private String description;
    @ApiModelProperty(value = "ngày nhận mong muốn")
    private Timestamp requestDate;
    @ApiModelProperty(value = "lý do đăng ký")
    private String reason;
    @ApiModelProperty(value = "người phê duyệt đơn")
    private Integer approver;
}
