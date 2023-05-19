package com.hrm.model.request.hr;

import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrEditEquipmentRequest {
    @ApiModelProperty(value = "Id thiết bị")
    private Integer id;
    @ApiModelProperty(value = "Tên thiết bị")
    private String name;
    @ApiModelProperty(value = "Mã số seri")
    private String serialNumber;
    @ApiModelProperty(value = "Loại thiết bị")
    private Integer category;
    @ApiModelProperty(value = "Mô tả thiết bị")
    private String description;
    @ApiModelProperty(value = "Ngày nhập thiết bị về")
    private Date importDate;
    @ApiModelProperty(value = "Nhà cung cấp thiết bị")
    private String vendor;
    @ApiModelProperty(value = "Thời hạn bảo hành")
    private Date warrantyTime;
    @ApiModelProperty(value = "Id nhân viên mượn")
    private Integer employeeId;
    @ApiModelProperty(value = "Trạng thái thiết bị")
    private Integer status;
}
