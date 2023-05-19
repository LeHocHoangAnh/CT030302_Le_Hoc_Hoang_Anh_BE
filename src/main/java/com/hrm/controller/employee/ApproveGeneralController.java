package com.hrm.controller.employee;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hrm.common.CommonService;
import com.hrm.common.Constants;
import com.hrm.entity.Employee;
import com.hrm.model.ApiResponse;
import com.hrm.model.request.hr.ListBookingRequest;
import com.hrm.model.request.hr.ListDetailTimeKeepingRequest;
import com.hrm.model.request.leader.UpdateBookingRequest;
import com.hrm.repository.EmployeeRepository;
import com.hrm.service.DropDownService;
import com.hrm.service.employee.ApproveGeneralService;
import com.hrm.service.employee.BookingDayOffService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("api/leader/")
@Api(description = "Phê duyệt xin phép")
public class ApproveGeneralController {

    @Autowired
    private ApproveGeneralService approveGeneralService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DropDownService dropDownService;

    @Autowired
    private BookingDayOffService bookingService;

    @ApiOperation(value = "Danh sách thời gian tạo xin phép")
    @GetMapping("listDropDownBooking")
    public ResponseEntity<ApiResponse> getListDropDownBooking() {
        return ResponseEntity.ok().body(dropDownService.dropListBooking());
    }

    @ApiOperation(value = "Xoá xin phép của nhân viên")
    @GetMapping("deleteBooking")
    public ResponseEntity<ApiResponse> deleteBooking(@ApiParam(value = "Id xin phép") @RequestParam("id") Integer id) {
        return ResponseEntity.ok().body(approveGeneralService.deleteBooking(id));
    }

    @ApiOperation(value = "Danh sách xin phép của nhân viên")
    @PostMapping("listBooking")
    public ResponseEntity<ApiResponse> getListBooking(
            @ApiParam(value = "Dữ liệu danh sách xin phép cần tìm kiếm") @RequestBody ListBookingRequest request) {
        return ResponseEntity.ok().body(approveGeneralService.getListBooking(request));
    }

    @ApiOperation(value = "Chi tiết xin phép của nhân viên")
    @GetMapping("detailBooking")
    public ResponseEntity<ApiResponse> getDetailBooking(
            @ApiParam(value = "Id xin phép") @RequestParam("id") Integer id) {
        return ResponseEntity.ok().body(approveGeneralService.getDetailBooking(id));
    }

    @ApiOperation(value = "Chi tiết trạng thái xác nhận của đơn xin phép")
    @GetMapping("getDetailBookingStatus")
    public ResponseEntity<ApiResponse> getDetailBookingStatus(
            @ApiParam(value = "Id xin phép") @RequestParam("id") Integer id) {
        return ResponseEntity.ok().body(approveGeneralService.getDetailBookingStatus(id));
    }

    @ApiOperation(value = "Phê duyệt xin phép")
    @PostMapping("updateBooking")
    public ResponseEntity<ApiResponse> updateBooking(
            @ApiParam(value = "Dữ liệu cập nhật xin phép") @RequestBody UpdateBookingRequest req) {
        return ResponseEntity.ok().body(approveGeneralService.updateBooking(req));
    }

    @ApiOperation(value = "Phê duyệt hàng loạt xin phép")
    @PostMapping("updateBookings")
    public ResponseEntity<ApiResponse> updateBookings(
            @ApiParam(value = "Dữ liệu cập nhật xin phép") @RequestBody List<UpdateBookingRequest> req) {
        return ResponseEntity.ok().body(approveGeneralService.updateBookings(req));
    }

    @Async
    @ApiOperation(value = "Gửi mail báo phê duyệt xin phép")
    @GetMapping("reply-confirm-mail")
    public void replyMailBooking(
            @ApiParam(value = "Dữ liệu cập nhật xin phép") @RequestParam("id") Integer bookingDayOffId) {
        approveGeneralService.replyMailBooking(bookingDayOffId);
    }

    @ApiOperation(value = "Chi tiết xin phép của nhân viên")
    @GetMapping("detailMultiple/booking")
    public ResponseEntity<ApiResponse> getDetailMultipleBooking(
            @ApiParam(value = "Id xin phép") @RequestParam("multipleSelected") List<Integer> multipleSelected) {
        return ResponseEntity.ok().body(approveGeneralService.getDetailMultipleBooking(multipleSelected));
    }

    @ApiOperation(value = "Lấy danh sách tổng hợp dữ liệu đơn xin theo tháng")
    @PostMapping("list-aggregate-data")
    public ResponseEntity<ApiResponse> getListAggregate(
            @ApiParam(value = "search request") @RequestBody ListDetailTimeKeepingRequest request) {
        Optional<Employee> employeeOptional = employeeRepository.findById(new CommonService().idUserAccountLogin());
        if (!employeeOptional.get().getRoleGroup().getHrFlag()) {
            return ResponseEntity.ok().body(new ApiResponse(Constants.HTTP_CODE_400, Constants.FAIL_AUTH, null));
        }
        return ResponseEntity.ok().body(approveGeneralService.getListAggregate(request));
    }

    @ApiOperation(value = "Xuất excel dữ liệu đơn xin theo tháng")
    @PostMapping("export-aggregate-data")
    public ResponseEntity<ApiResponse> exportAggregateData(
            @ApiParam(value = "search request") @RequestBody ListDetailTimeKeepingRequest request,
            HttpServletResponse response) throws IOException {
        ApiResponse exportResponse = approveGeneralService.exportAggregateData(request, response);
        return ResponseEntity.ok().body(exportResponse);
    }

//    @ApiOperation(value = "Build message thông báo lên slack khi duyệt đơn đăng ký thiết bị")
//    @PostMapping("/slack-message")
//    public ResponseEntity<ApiResponse> buildSlackMessage(@RequestParam Integer id, @RequestParam String webhookType,
//            @RequestBody String registType) {
//        return ResponseEntity.ok().body(bookingService.buildSlackMessage(id, registType));
//    }

//    @ApiOperation(value = "Build message thông báo lên discord")
//    @PostMapping("/discord-message")
//    public ResponseEntity<ApiResponse> buildDiscordMessage(@RequestParam Integer id, @RequestBody String registType) {
//        return ResponseEntity.ok().body(bookingService.buildDiscordMessage(id, registType));
//    }

}
