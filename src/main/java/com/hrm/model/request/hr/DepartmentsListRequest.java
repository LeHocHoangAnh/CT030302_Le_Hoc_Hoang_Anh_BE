package com.hrm.model.request.hr;
import com.hrm.model.request.PaginationRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DepartmentsListRequest extends PaginationRequest{
	@ApiModelProperty(value="tên phòng ban")
    private String departmentName;
}
