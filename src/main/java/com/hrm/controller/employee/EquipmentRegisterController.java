package com.hrm.controller.employee;

import com.hrm.model.request.employee.RegisterEquipmentRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hrm.common.CommonService;
import com.hrm.model.ApiResponse;
import com.hrm.service.equipment.EquipmentService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("api/employee/equipment")
@Api(description = "Thiết bị nhân viên")
public class EquipmentRegisterController {
    @Autowired
    EquipmentService equipmentService;
    @Autowired
    CommonService commonService;
    
    @ApiOperation(value="Danh sách thiết bị sở hữu")
    @GetMapping("/equipment-list")
    public ResponseEntity<ApiResponse> getOnwedEquipmentList() {
        return ResponseEntity.ok().body(equipmentService.getUserEquipmentList(commonService.idUserAccountLogin()));
    }
    
    @ApiOperation(value="Danh sách đơn đăng ký thiết bị")
    @GetMapping("/registration-list")
    public ResponseEntity<ApiResponse> getRegistrationList() {
        return ResponseEntity.ok().body(equipmentService.getRegistrationList(commonService.idUserAccountLogin()));
    }
    
    @ApiOperation(value="Chi tiết đơn đăng ký thiết bị")
    @GetMapping("/registration-detail")
    public ResponseEntity<ApiResponse> getDetailRegistration(@RequestParam Integer id) {
        return ResponseEntity.ok().body(equipmentService.getDetailRegistration(id));
    }
    
    @ApiOperation(value = "lấy lịch sử sử dụng thiết bị")
    @PostMapping("/history-list")
    public ResponseEntity<ApiResponse> getHistoryList(@RequestBody Integer id) {
        return ResponseEntity.ok(equipmentService.getHistoryList(id));
    }
    
    @ApiOperation(value="Đăng ký/ chỉnh sửa đơn đăng ký thiết bị")
    @PostMapping("/registration-edit")
    public ResponseEntity<ApiResponse> edit(@RequestBody RegisterEquipmentRequest request) {
        return ResponseEntity.ok().body(equipmentService.editRegister(request));
    }
    
    @ApiOperation(value="Xóa đơn đăng ký thiết bị")
    @DeleteMapping("/registration-delete")
    public ResponseEntity<ApiResponse> delete(@RequestParam Integer id) {
        return ResponseEntity.ok().body(equipmentService.deleteRegistration(id));
    }
    
    @ApiOperation(value="Danh sách nhân viên phê duyệt")
    @GetMapping("/approvers-list")
    public ResponseEntity<ApiResponse> getAllApprover() {
        return ResponseEntity.ok().body(equipmentService.getAllApprover());
    }
}
