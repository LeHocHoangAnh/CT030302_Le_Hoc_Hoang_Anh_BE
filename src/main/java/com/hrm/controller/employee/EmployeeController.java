package com.hrm.controller.employee;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.hrm.model.request.employee.ListSearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.hrm.model.ApiResponse;
import com.hrm.model.request.employee.ChangePasswordRequest;
import com.hrm.model.request.employee.EmailNotificationsRequest;
import com.hrm.model.request.hr.CreateOrEditEmployeeRequest;
import com.hrm.model.request.hr.ListEmployeeRequest;
import com.hrm.service.DocumentService;
import com.hrm.service.DropDownService;
import com.hrm.service.employee.EmployeeService;
import com.hrm.service.importFile.TimeKeepService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("api/employee")




@Api(description = "Quản lý nhân viên")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    DropDownService dropDownService;
    
    @Autowired
    TimeKeepService timeKeepService;
    
    @Autowired
    DocumentService documentService;

    @ApiOperation(value="Thông tin cá nhân")
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse> getEmployeeProfile(@RequestParam("id") Integer id) {
        return ResponseEntity.ok().body(employeeService.getEmployeeProfile(id));
    }

    @ApiOperation(value="Thay đổi mật khẩu")
    @PostMapping("/changePassword")
    public ResponseEntity<ApiResponse> changePassword(@ApiParam(value="data thay đổi")@Valid @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok().body(employeeService.changePassword(request));
    }

    @ApiOperation(value="Danh sách nhân viên")
    @PostMapping("/list")
    public ResponseEntity<ApiResponse> findAllEmployees(@ApiParam(value="data search nhân viên")@RequestBody ListEmployeeRequest req) {
        return ResponseEntity.ok().body(employeeService.getEmployeeInformation(req));
    }
    @ApiOperation(value="Chi tiết nhân viên")
    @GetMapping("/getById")
    public ResponseEntity<ApiResponse> findEmployeeInformationById(@ApiParam(value="id nhân viên")@RequestParam(name = "id") Integer id) {
        return ResponseEntity.ok().body(employeeService.getEmployeeInformationById(id));
    }

    @ApiOperation(value="chỉnh sửa nhân viên")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@ApiParam(value="data nhân viên")@RequestBody CreateOrEditEmployeeRequest request) {
        if (request.getId() != null) {
            return ResponseEntity.ok().body(employeeService.updateEmployee(request));
        } else {
            return ResponseEntity.ok().body(employeeService.createEmployee(request));
        }
    }

    @ApiOperation(value="Tải ảnh nhân viên")
    @PostMapping("/uploadImg")
    public ResponseEntity<ApiResponse> uploadImg(@ApiParam(value="file ảnh")@RequestPart("file")MultipartFile file,@ApiParam(value="id nhân viên")@RequestParam("code")String code) throws IOException{
        return ResponseEntity.ok().body(employeeService.uploadImage(file,code));
    }

    @ApiOperation(value="Xoá ảnh nhân viên")
    @GetMapping("/deleteImage")
    public ResponseEntity<ApiResponse> deleteImage(@ApiParam(value="id nhân viên")@RequestParam("code")String code){
        return ResponseEntity.ok().body(employeeService.deleteImage(code));
    }

    @ApiOperation(value="Xóa nhân viên")
    @PostMapping("/delete")
    public ResponseEntity<ApiResponse> deleteEmployee(@ApiParam(value="id nhân viên")@RequestParam(name = "id") Integer id) {
        return ResponseEntity.ok().body(employeeService.delete(id));
    }

    @ApiOperation(value="Danh sách phòng ban")
    @GetMapping("/getListDepartment")
    public ResponseEntity<ApiResponse> getListDepartment() {
        return ResponseEntity.ok().body(dropDownService.dropListDepartment());
    }

    @ApiOperation(value="Thông tin nhân viên mới nhất")
    @GetMapping("/getLastEmployee")
    public ResponseEntity<ApiResponse> getLastEmployee() {
        return ResponseEntity.ok().body(employeeService.getLastEmployee());
    }
    
    @ApiOperation(value="Danh sách tên nhân viên")
    @GetMapping("/getListAutoEmployee")
    public ResponseEntity<ApiResponse> getListAutoEmployee() {
        return ResponseEntity.ok().body(employeeService.getListAutoEmployee());
    }
    
    @ApiOperation(value="thời gian chấm công")
    @GetMapping("list/timesDropDown")
    public ResponseEntity<ApiResponse> getListTimeDropDown() {
        return ResponseEntity.ok().body(timeKeepService.getListTimeDropDown());
    }
    @ApiOperation(value="Xuất file excel thông tin nhân viên")
    @PostMapping("/toExcel")
    public ResponseEntity<ApiResponse> exportEmployeesToExcel(@ApiParam(value="id nhân viên cần xuất excel")@RequestBody ListSearchRequest request, @ApiParam(value="cờ xuất all")@RequestParam boolean exportAllFlag, HttpServletResponse response) throws IOException {
        employeeService.exportEmployeesToExcel(request,exportAllFlag,response);
    	return ResponseEntity.ok().build();
    }
    
    @ApiOperation(value="Cài đặt bật/tắt thông báo")
    @PostMapping("/set-notifications")
    public ResponseEntity<ApiResponse> updateEmailNotifications(@RequestBody EmailNotificationsRequest emailRequest) {
    	return ResponseEntity.ok().body(employeeService.updateEmailNotifications(emailRequest));
    }
    
    // Document screen fetch data
    @ApiOperation(value="Danh sách tài liệu nội bộ")
    @GetMapping("/document-list")
    public ResponseEntity<ApiResponse> getDocumentList() {
    	return ResponseEntity.ok().body(documentService.getDocumentList());
    }
    @ApiOperation(value="Chi tiết tài liệu nội bộ")
    @GetMapping("/document-detail")
    public ResponseEntity<ApiResponse> getDetailDocumentById(@RequestParam String id) {
    	return ResponseEntity.ok().body(documentService.getDetailDocumentById(id));
    }
    @ApiOperation(value="Chi tiết tài liệu nội bộ hiển thị lên header")
    @GetMapping("/header-document")
    public ResponseEntity<ApiResponse> getDocumentToHeader() {
    	return ResponseEntity.ok().body(documentService.getDocumentToHeader());
    }
    //
}
