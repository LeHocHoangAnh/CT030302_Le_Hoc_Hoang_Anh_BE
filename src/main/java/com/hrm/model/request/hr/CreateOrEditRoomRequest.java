package com.hrm.model.request.hr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrEditRoomRequest {
    private Integer id;
    private String name;
    private Integer status;
}
