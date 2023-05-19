package com.hrm.controller.employee;

import java.util.Date;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hrm.common.CommonService;
import com.hrm.model.ApiResponse;
import com.hrm.model.request.employee.CreateOrUpdateBookingRequest;
import com.hrm.model.request.employee.TimeKeepingRequest;
import com.hrm.service.employee.BookingDayOffService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("api/bookingDayOff")
@Api(description = "Xin phép")
public class BookingDayOffController {
    @Autowired
    private BookingDayOffService bookingService;

    @Autowired
    private CommonService commonService;

    @ApiOperation(value = "thông tin xin phép")
    @GetMapping("/getInformation")
    public ResponseEntity<ApiResponse> getInformationBooking(
            @ApiParam(value = "id xin phép") @RequestParam(value = "id") Integer id) {
        return ResponseEntity.ok().body(bookingService.getBookingById(id));
    }

    @ApiOperation(value = "Danh sách xin phép của nhân viên")
    @PostMapping("/getAllBooking")
    public ResponseEntity<ApiResponse> getAllBooking(
            @ApiParam(value = "thời gian") @RequestBody TimeKeepingRequest request) {
        return ResponseEntity.ok().body(
                bookingService.getAllBookingByEmployeeAndTime(request.getDate(), commonService.idUserAccountLogin()));
    }

    @ApiOperation(value = "Chỉnh sửa xin phép của nhân viên")
    @PostMapping("/createOrUpdate")
    public ResponseEntity<ApiResponse> createOrUpdateBooking(
            @ApiParam(value = "data chỉnh sửa xin phép") @Valid @RequestBody CreateOrUpdateBookingRequest request) {
        return ResponseEntity.ok().body(bookingService.onChangeBooking(request));
    }

    @ApiOperation(value = "Gửi mail thông báo đăng ký")
    @GetMapping("/send-mail-booking")
    public void sendMailBooking(@ApiParam("ID đơn đăng ký") @RequestParam("id") Integer bookingDayOffId) {
        bookingService.sendMailBooking(bookingDayOffId, commonService.idUserAccountLogin());
    }

    @ApiOperation(value = "Xóa xin phép của nhân viên")
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse> deleteBooking(
            @ApiParam(value = "id xin phép") @RequestParam(value = "id") Integer id) {
        return ResponseEntity.ok().body(bookingService.deleteBooking(id));
    }

    @ApiOperation(value = "Danh sách lịch nghỉ lễ")
    @PostMapping("/getListConfigDayOff")
    public ResponseEntity<ApiResponse> getListConfigDayOff(@RequestBody Date requestDate) {
        return ResponseEntity.ok().body(bookingService.getListConfigDayOff(requestDate));
    }

    @ApiOperation(value = "Quy định lịch làm việc")
    @GetMapping("/getStandardTime")
    public ResponseEntity<ApiResponse> getStandardTime() {
        return ResponseEntity.ok().body(bookingService.getStandardTime());
    }

    @ApiOperation(value = "Lấy danh sách leader để phê duyệt và tất cả nhân viên để cc")
    @GetMapping("/getEmployeeDropdown")
    public ResponseEntity<ApiResponse> getAllEmployeeListDropdown() {
        return ResponseEntity.ok().body(bookingService.getAllEmployeeListDropdown());
    }

    @ApiOperation(value = "số lượng xin nghỉ phép/nghỉ bù còn lại")
    @GetMapping("/remainLeaves")
    public ResponseEntity<ApiResponse> getLeavePaidRemainByEmployeeAndMonth(
            @ApiParam(value = "Thời gian hiện tại") @RequestParam(name = "date") String Date) {
        return ResponseEntity.ok()
                .body(bookingService.getLeavePaidRemainByEmployeeAndMonth(commonService.idUserAccountLogin(), Date));
    }

    @ApiOperation(value = "upload ảnh bằng chứng")
    @PostMapping("/uploadEvidenceImg")
    public ResponseEntity<ApiResponse> uploadEvidenceImg(
            @ApiParam(value = "id đơn xin phép") @RequestParam("id") Integer id,
            @ApiParam(value = "file ảnh bằng chứng") @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok().body(bookingService.uploadEvidenceImg(id, file));
    }

    @ApiOperation(value = "Lấy danh sách dropdown các dự án cho phần OT")
    @GetMapping("/projectDropdown")
    public ResponseEntity<ApiResponse> getProjectDropdown() {
        return ResponseEntity.ok().body(bookingService.getProjectDropdown());
    }

//    @ApiOperation(value = "Build message thông báo lên slack")
//    @PostMapping("/slack-message")
//    public ResponseEntity<ApiResponse> buildSlackMessage(@RequestParam Integer id, @RequestBody String registType) {
//        return ResponseEntity.ok().body(bookingService.buildSlackMessage(id, registType));
//    }
//
//    @ApiOperation(value = "Build message thông báo lên discord")
//    @PostMapping("/discord-message")
//    public ResponseEntity<ApiResponse> buildDiscordMessage(@RequestParam Integer id, @RequestBody String registType) {
//        return ResponseEntity.ok().body(bookingService.buildDiscordMessage(id, registType));
//    }
}
