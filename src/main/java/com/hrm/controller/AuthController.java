package com.hrm.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

import javax.mail.MessagingException;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hrm.common.Constants;
import com.hrm.event.OnUserLogoutSuccessEvent;
import com.hrm.model.ApiResponse;
import com.hrm.model.request.LogOutRequest;
import com.hrm.model.request.LoginRequest;
import com.hrm.model.response.LoginResponse;
import com.hrm.repository.EmployeeRepository;
import com.hrm.security.UserDetailsImpl;
import com.hrm.service.AuthService;
import com.hrm.service.CurrentUser;
import com.hrm.service.employee.EmployeeService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/auth")
@Api(description = "Đăng nhập")
public class AuthController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    
    @Autowired
    private AuthService authService;
    
    @ApiOperation(value = "Đăng nhập")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request)
            throws GeneralSecurityException, IOException {
        return ResponseEntity.ok().body(authService.login(request));
    }

    @ApiOperation(value = "Đăng xuất")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logoutUser(@CurrentUser UserDetailsImpl currentUser,
            @RequestBody LogOutRequest logOutRequest) {
        OnUserLogoutSuccessEvent logoutSuccessEvent = new OnUserLogoutSuccessEvent(currentUser.getUsername(),
                logOutRequest.getToken(), logOutRequest);
        applicationEventPublisher.publishEvent(logoutSuccessEvent);
        return ResponseEntity.ok().body(new ApiResponse(Constants.HTTP_CODE_200, Constants.LOGGED_OUT_SUCCESS, null));
    }
    
    @ApiOperation(value = "gửi link thay đổi mật khẩu đến gmail")
    @PostMapping("/sendMail")
    public ResponseEntity<ApiResponse> sendMailForgotPassword(@RequestParam String email) throws UnsupportedEncodingException, MessagingException {
        employeeService.sendMailForgotPassword(email);
        return ResponseEntity.ok().body(new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, null));
    }
    
    @ApiOperation(value = "kiểm tra token đặt lại mật khẩu hợp lệ")
    @PostMapping("/check/tokens")
    public ResponseEntity<ApiResponse> checkToken(@RequestParam String token) {
        employeeService.checkToken(token);
        return ResponseEntity.ok().body(new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, null));
    }
    
    @ApiOperation(value = "thay đổi mật khẩu")
    @PostMapping("/change/pass")
    public ResponseEntity<ApiResponse> checkToken(@RequestBody LoginRequest request) {
        employeeService.resetPassword(request);
        return ResponseEntity.ok().body(new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, null));
    }
    
    
}
