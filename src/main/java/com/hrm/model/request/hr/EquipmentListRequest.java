package com.hrm.model.request.hr;
import java.util.Date;
import java.util.List;

import com.hrm.model.request.PaginationRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class EquipmentListRequest extends PaginationRequest{
	@ApiModelProperty(value="tên thiết bị")
    private String name;
	@ApiModelProperty(value="số seri")
    private String serialNumber;
	@ApiModelProperty(value="loại thiết bị")
    private Integer category;
	@ApiModelProperty(value="trạng thái thiết bị")
    private Integer status;
	@ApiModelProperty(value="khoảng ngày nhập thiết bị")
    private List<Date> date;
	@ApiModelProperty(value="List nhân viên")
    private List<Integer> employeeList;
}
