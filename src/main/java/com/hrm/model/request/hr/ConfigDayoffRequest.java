package com.hrm.model.request.hr;

import java.util.Date;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ConfigDayoffRequest {
	@ApiModelProperty(notes="id ngày nghỉ")
	private Integer id;
	@ApiModelProperty(notes="từ ngày")
	private Date dayFrom;
	@ApiModelProperty(notes="đến ngày")
	private Date dayTo;
	@ApiModelProperty(notes="lý do")
	private String reasonApply;
}
