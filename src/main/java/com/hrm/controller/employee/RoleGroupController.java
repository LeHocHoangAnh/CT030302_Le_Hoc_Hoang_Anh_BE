package com.hrm.controller.employee;

import com.hrm.model.ApiResponse;
import com.hrm.service.employee.RoleGroupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/employee")
@Api(description = "Nhóm quyền")
public class RoleGroupController {

    @Autowired
    private RoleGroupService roleGroupService;

    @ApiOperation(value="Danh sách quyền")
    @GetMapping("/detail-roleGroup")
    public ResponseEntity<ApiResponse> getDetailRoleGroup(@RequestParam(value = "id") Integer id) {
        return ResponseEntity.ok().body(roleGroupService.getDetailRoleGroup(id));
    }
}
