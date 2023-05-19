package com.hrm.controller.hrController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hrm.model.ApiResponse;
import com.hrm.model.request.hr.CreateOrEditEquipmentRequest;
import com.hrm.model.request.hr.EquipmentListRequest;
import com.hrm.model.request.hr.ListEquipmentRegistrationRequest;
import com.hrm.model.response.hr.EquipmentHistoryListResponse;
import com.hrm.service.DropDownService;
import com.hrm.service.equipment.EquipmentService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/hr/equipment")
@Api(description = "Quản lý thiết bị")
public class EquipmentController {
    
    @Autowired
    EquipmentService equipmentService;
    
    @Autowired
    DropDownService dropDownService;
    
    @ApiOperation(value = "Lấy danh sách thiết bị")
    @PostMapping("/list")
    public ResponseEntity<ApiResponse> getListEquipment(@RequestBody EquipmentListRequest request) {
        return ResponseEntity.ok(equipmentService.getListEquipment(request));
    }
    
    @ApiOperation(value = "Chi tiết thiết bị")
    @GetMapping("/detail")
    public ResponseEntity<ApiResponse> detail(@RequestParam Integer id) {
        return ResponseEntity.ok(equipmentService.detail(id));
    } 
    
    @ApiOperation(value = "Lưu/ Sửa chi tiết thiết bị")
    @PostMapping("/edit")
    public ResponseEntity<ApiResponse> edit(@RequestBody CreateOrEditEquipmentRequest request) {
        return ResponseEntity.ok(equipmentService.edit(request));
    } 
    
    @ApiOperation(value = "Thay đổi người sở hữu thiết bị")
    @PostMapping("/switch")
    public ResponseEntity<ApiResponse> switchOwnership(@RequestParam Integer id, @RequestBody Integer employeeId) {
        return ResponseEntity.ok(equipmentService.switchOwnership(id, employeeId));
    } 
    
    @ApiOperation(value = "Xóa thiết bị")
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse> delete(@RequestParam Integer id) {
        return ResponseEntity.ok(equipmentService.delete(id));
    }
    
    @ApiOperation(value = "lấy Dropdown nhân viên công ty")
    @GetMapping("/employee-list")
    public ResponseEntity<ApiResponse> getListEmployee() {
        return ResponseEntity.ok(equipmentService.getListEmployee());
    }
    
    @ApiOperation(value = "lấy Dropdown các phòng ban")
    @GetMapping("/department-list")
    public ResponseEntity<ApiResponse> getListDepartment() {
        return ResponseEntity.ok().body(dropDownService.dropListDepartment());
    }
    
    @ApiOperation(value = "Lấy danh sách đơn đăng ký thiết bị")
    @PostMapping("/registration-list")
    public ResponseEntity<ApiResponse> getListEquipmentRegistration(@RequestBody ListEquipmentRegistrationRequest request) {
        return ResponseEntity.ok(equipmentService.getEquipmentRegistrationList(request));
    }
    
    @ApiOperation(value = "Chỉnh sửa thông tin lịch sử thiết bị")
    @PostMapping("/history-edit")
    public ResponseEntity<ApiResponse> editHistory(@RequestBody EquipmentHistoryListResponse request) {
        return ResponseEntity.ok(equipmentService.editHistory(request));
    }
}
