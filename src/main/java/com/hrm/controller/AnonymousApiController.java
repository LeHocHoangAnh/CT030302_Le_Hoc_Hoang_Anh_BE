package com.hrm.controller;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hrm.common.Constants;
import com.hrm.model.ApiResponse;
import com.hrm.service.AnonymousService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/anonymous-api")
@Api(description = "API truy cập vô danh")
public class AnonymousApiController {

    @Autowired
    private AnonymousService anonymousService;

    private final String apiKey = "GltMrKy0sEGJDi5nhvUsLJhUUHyeLz1F";

    @ApiOperation(value = "Danh sách nhân viên")
    @GetMapping("/employee-list")
    public ResponseEntity<ApiResponse> login(@RequestHeader Map<String, String> header) {
        String apiKeyRequest = header.get("api-key");
        if (StringUtils.isBlank(apiKeyRequest)) {
            return ResponseEntity.ok()
                    .body(new ApiResponse(Constants.HTTP_CODE_500, Constants.API_KEY_NOT_FOUND, null));
        }
        if (apiKeyRequest.equals(apiKey)) {
            return ResponseEntity.ok().body(
                    new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, anonymousService.getListEmployee()));
        } else {
            return ResponseEntity.ok().body(new ApiResponse(Constants.HTTP_CODE_403, Constants.API_KEY_FAIL, null));
        }
    }

}
