package com.hrm.model.request.hr;

import com.hrm.model.request.PaginationRequest;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ListEquipmentRegistrationRequest extends PaginationRequest{
    @ApiModelProperty(notes="Thời gian tạo xin phép từ ngày:")
    private String fromDate;
    @ApiModelProperty(notes="Tới ngày")
    private String toDate;
    @ApiModelProperty(notes="Trạng thái đơn")
    private Integer confirm;
    @ApiModelProperty(notes="Tên nhân viên")
    private String name;
    @ApiModelProperty(notes="Phòng ban")
    private Integer departmentId;
    @ApiModelProperty(notes="Loại thiết bị")
    private Integer category;
}
