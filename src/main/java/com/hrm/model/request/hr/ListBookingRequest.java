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
public class ListBookingRequest extends PaginationRequest{
    @ApiModelProperty(notes="Thời gian tạo xin phép từ ngày:")
    private String fromDate;
    @ApiModelProperty(notes="Tới ngày")
    private String toDate;
    @ApiModelProperty(notes="Loại Yêu Cầu")
    private Integer status;
    @ApiModelProperty(notes="Trạng thái chờ")
    private Boolean wait = false;
    @ApiModelProperty(notes="Trạng thái chấp nhận")
    private Boolean approve = false;
    @ApiModelProperty(notes="Thời gian từ chối")
    private Boolean refuse = false;
    @ApiModelProperty(notes="trạng thai đã xoá")
    private Boolean deleteFlag = false;
    @ApiModelProperty(notes="Tên nhân viên")
    private String name;
    @ApiModelProperty(notes="Phòng ban")
    private String department;
    @ApiModelProperty(notes="Trạng thái leader")
    private Boolean isLeader = false;
}
