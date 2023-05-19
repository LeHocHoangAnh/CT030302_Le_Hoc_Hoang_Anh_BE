package com.hrm.model.request.employee;

import java.util.List;

import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrUpdateBookingRequest {
    @ApiModelProperty(value = "id xin phép")
    private Integer id;
    @ApiModelProperty(value = "id nhân viên")
    private Integer idUser;
    @ApiModelProperty(value = "loại xin phép")
    @NotBlank
    private String registrationType;
    @ApiModelProperty(value = "ngày bắt đầu")
    @NotBlank
    private String dateStart;
    @ApiModelProperty(value = "ngày kết thúc")
    @NotBlank
    private String dateEnd;
    @ApiModelProperty(value = "lý do")
    private String reason;
    @ApiModelProperty(value = "danh sách người phê duyệt được chọn")
    private List<String> approver;
    @ApiModelProperty(value = "danh sách người liên quan được chọn để cc")
    private List<String> relatedEmployee;
    @ApiModelProperty(value = "id dự án(chỉ cho hình thức đăng ký OT)")
    private Integer projectId;
    @ApiModelProperty(value = "hình thức xin nghỉ(sáng, chiều, cả ngày, nhiều ngày)")
    private String selectedTypeTime;

}
