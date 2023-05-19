package com.hrm.controller.dayoff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hrm.entity.ConfigDayOff;
import com.hrm.model.ApiResponse;
import com.hrm.model.request.hr.ConfigDayoffRequest;
import com.hrm.service.dayoff.DayoffService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/api/hr/dayoff")
@Api(description = "Quản lý ngày nghỉ")
public class DayoffController {
    
    @Autowired
    private DayoffService dayoffService;
  
    @ApiOperation(value = "Tất cả năm")
    @GetMapping("/get-year-range")
    public ResponseEntity<ApiResponse> getYearRange() {
    	return ResponseEntity.ok(dayoffService.getYearRange());
    }
    
    @ApiOperation(value = "Danh sách ngày nghỉ lấy theo năm")
    @GetMapping("/list/{year}")
    public ResponseEntity<ApiResponse> getDayOffs(@ApiParam(value="Số năm")@PathVariable String year) {
    	return ResponseEntity.ok(dayoffService.getDayOffs(year));
    }

    @ApiOperation(value = "Chi tiết ngày nghỉ lấy theo id")
    @GetMapping("/detail")
    public ResponseEntity<ApiResponse> getDayOffDetail(@ApiParam(value="id")@RequestParam String id) {
    	return ResponseEntity.ok(dayoffService.getDayOffDetail(id));
    }
    
    @ApiOperation(value = "Thêm mới/ sửa ngày nghỉ")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> saveDayOffConfig(@ApiParam(value="object config day off")@RequestBody ConfigDayoffRequest req) {
    	return ResponseEntity.ok(dayoffService.saveDayOffConfig(req));
    }
    
    @ApiOperation(value = "Xóa ngày nghỉ")
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse> deleteDayOff(@ApiParam(value="object config day off")@RequestParam Integer id) {
    	return ResponseEntity.ok(dayoffService.deleteDayOff(id));
    }
}
