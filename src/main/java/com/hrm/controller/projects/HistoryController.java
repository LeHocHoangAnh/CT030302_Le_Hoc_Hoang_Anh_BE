package com.hrm.controller.projects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hrm.model.ApiResponse;
import com.hrm.model.request.PaginationRequest;
import com.hrm.model.request.hr.CreateOrEditHistoryRequest;
import com.hrm.service.projects.HistoryService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/api/hr")
@Api(description = "Lịch sử dự án")
public class HistoryController {
    
    @Autowired
    HistoryService historyService;
    
    @ApiOperation(value = "Danh sách lịch sử nhân viên tham gia dự án")
    @PostMapping("/list-history")
    public ResponseEntity<ApiResponse> getListHistory(@ApiParam(value="Id dự án") @RequestParam("id")Integer id,
            @ApiParam(value="Phân trang theo số lượng") @RequestBody PaginationRequest request) {
        return ResponseEntity.ok(historyService.getListHistory(id,request));
    }
    
    @ApiOperation(value = "Chi tiết lịch sử dự án")
    @GetMapping("/history-detail")
    public ResponseEntity<ApiResponse> getDetailHistory(@ApiParam(value="Id lịch sử") @RequestParam("id") Integer id){
        return ResponseEntity.ok(historyService.getDetailHistory(id));
    }
    
    @ApiOperation(value = "Chỉnh sửa lịch sử dự án")
    @PostMapping("/edit-history")
    public ResponseEntity<ApiResponse> updateOrCreateHistory(@RequestBody CreateOrEditHistoryRequest request){
        if(request.getId() == null) {
            return ResponseEntity.ok(historyService.createHistory(request));
        }
        return ResponseEntity.ok(historyService.updateHistory(request));
    }
    
    @ApiOperation(value = "Xóa lịch sử dự án")
    @PostMapping("/delete-history")
    public ResponseEntity<ApiResponse> deleteHistory(@ApiParam("Id lịch sử") @RequestParam("id")Integer id){
        return ResponseEntity.ok(historyService.deleteHistory(id));
    }
    
    @ApiOperation(value = "Kiểm tra nhân viên")
    @GetMapping("/check-employee")
    public ResponseEntity<ApiResponse> checkEmployee(@ApiParam("Mã nhân viên hoặc tên nhân viên") @RequestParam("key") String key){
        return ResponseEntity.ok(historyService.checkEmployee(key));
    }
}
