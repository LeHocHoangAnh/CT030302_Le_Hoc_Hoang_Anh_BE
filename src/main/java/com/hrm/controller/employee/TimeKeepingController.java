package com.hrm.controller.employee;

import com.hrm.common.CommonService;
import com.hrm.common.Constants;
import com.hrm.model.ApiResponse;
import com.hrm.model.request.employee.TimeKeepingRequest;
import com.hrm.service.employee.TimeKeepingService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/employee/timekeeping")
@Api(description = "bảng chấm công theo tháng")
public class TimeKeepingController {

	@Autowired
	private TimeKeepingService timeKeepingService;

	@Autowired
	private CommonService commonService;

	@ApiOperation(value = "bảng chấm chấm công")
	@PostMapping("/getInformation")
	public ResponseEntity<ApiResponse> getTimeKeepingEmployee(
			@ApiParam(value = "thời gian bảng chấm công") @RequestBody TimeKeepingRequest request) {
		return ResponseEntity.ok().body(new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS,
				timeKeepingService.getTimeKeepingByEmployee(request.getDate(), commonService.idUserAccountLogin())));
	}
}
