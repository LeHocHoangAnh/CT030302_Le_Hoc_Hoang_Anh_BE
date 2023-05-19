package com.hrm.controller.departments;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hrm.common.Constants;
import com.hrm.entity.Department;
import com.hrm.model.ApiResponse;
import com.hrm.model.request.hr.DepartmentsListRequest;
import com.hrm.service.departments.DepartmentsService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/api/hr")
@Api(description = "Quản lý dự án")
public class DepartmentsController {
    
    @Autowired
    private DepartmentsService departmentsService;
  
    @ApiOperation(value = "Danh sách phòng ban")
    @PostMapping("/list-departments")
    public ResponseEntity<ApiResponse> getDepartmentsList(
    		@ApiParam(value="data search phòng ban")@RequestBody DepartmentsListRequest request, 
    		@RequestParam(required = false) String action) {
    	return ResponseEntity.ok(departmentsService.getDepartmentsList(request, action));
    }
    
    @ApiOperation(value = "Tạo mới phòng ban")
    @PostMapping("/create-department")
    public ResponseEntity<ApiResponse> createDepartment(
    		@ApiParam(value="Tên phòng ban")@RequestParam String name, 
    		@ApiParam(value="trạng thái phòng ban")@RequestParam Integer action) {
        return ResponseEntity.ok(departmentsService.createDepartment(name, action));
    }
    
    @ApiOperation(value = "Lấy danh sách nhân viên theo mã phòng ban")
    @GetMapping("/employee-by-departmentID")
    public ResponseEntity<ApiResponse> getEmployeeByDepartmentID(
    		@ApiParam(value="ID phòng ban")@RequestParam Integer id, 
    		@ApiParam(value="Trang hiện tại")@RequestParam Integer page,
    		@ApiParam(value="Tổng bản ghi trong 1 trang")@RequestParam Integer size) {
        return ResponseEntity.ok(departmentsService.getEmployeeByDepartmentID(id, page, size));
    }
    
    @ApiOperation(value = "Lấy danh sách phòng ban theo ID")
    @GetMapping("/find-department")
    public ResponseEntity<ApiResponse> getDepartmentByID(@ApiParam(value="ID phòng ban")@RequestParam Integer id) {
        return ResponseEntity.ok(departmentsService.getDepartmentByID(id));
    }
    
    @ApiOperation(value = "Cập nhật phòng ban")
    @PutMapping("/update-department")
    public ResponseEntity<ApiResponse> updateDepartment(@ApiParam(value="entity department ")@RequestBody Department department) {
        return ResponseEntity.ok(departmentsService.updateDepartment(department));
    }
    
    @ApiOperation(value = "Xóa phòng ban theo ID")
    @DeleteMapping("/delete-department")
    public ResponseEntity<ApiResponse> deleteDepartment(@ApiParam(value="ID phòng ban")@RequestParam Integer id) {
        return ResponseEntity.ok(departmentsService.deleteDepartment(id));
    }
    
    @ApiOperation(value = "Cập nhật số nhân viên")
    @PutMapping("/update-member")
    public ResponseEntity<ApiResponse> updateMember(
    		@ApiParam(value="ID phòng ban")@RequestParam Integer id,
    		@ApiParam(value="Số nhân viên")@RequestParam Integer member) {
        return ResponseEntity.ok(departmentsService.updateMember(id, member));
    }
}
