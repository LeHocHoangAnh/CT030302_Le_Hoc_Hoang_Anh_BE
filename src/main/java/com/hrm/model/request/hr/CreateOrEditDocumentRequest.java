package com.hrm.model.request.hr;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrEditDocumentRequest {
    @ApiModelProperty(value = "Id document")
    private Integer id;
    @ApiModelProperty(value = "Tên document")
    private String name;
    @ApiModelProperty(value = "Mô tả document")
    private String description;
    @ApiModelProperty(value = "Nội dung html")
    private String content;
    
}
