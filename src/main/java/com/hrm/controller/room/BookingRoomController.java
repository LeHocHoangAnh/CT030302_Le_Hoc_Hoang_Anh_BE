package com.hrm.controller.room;

//import com.hrm.common.CommonService;
//import com.hrm.entity.Room;
//import com.hrm.exception.RecordNotFoundException;
import com.hrm.model.ApiResponse;
import com.hrm.model.request.BookingRoomListRequest;
import com.hrm.model.request.employee.CreateOrUpdateBookingRoomRequest;
//import com.hrm.model.request.hr.ConfirmBookingMeetingRoomRequest;
import com.hrm.model.request.hr.ListBookingMeetingRoom;
//import com.hrm.repository.RoomRepository;
import com.hrm.service.room.BookingRoomService;
import io.swagger.annotations.ApiOperation;

//import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employee/booking-room")
public class BookingRoomController {

	@Autowired
	private BookingRoomService bookingRoomService;

//	@Autowired
//	private RoomRepository roomRepository;

//	@Autowired
//	private CommonService commonService;

	@ApiOperation(value = "danh sách đăng ký phòng họp")
	@PostMapping("/list-booking-room-by-date")
	public ResponseEntity<ApiResponse> getListBookingRoomByDate(@RequestBody BookingRoomListRequest req) {
		return ResponseEntity.ok(bookingRoomService.getListBookingRoomByDate(req));
	}

	@ApiOperation(value = "chi tiết đăng ký phòng họp")
	@GetMapping("/booking-room-by-id")
	public ResponseEntity<ApiResponse> getBookingRoomById(@RequestParam(value = "id") Integer id) {
		return ResponseEntity.ok(bookingRoomService.getBookingRoomById(id));
	}

	@ApiOperation(value = "xoá đăng ký phòng họp")
	@PostMapping("/delete-booking-room")
	public ResponseEntity<ApiResponse> deleteBookingRoomById(@RequestParam(value = "id") Integer id,
			@RequestParam(value = "type") String type) {
		return ResponseEntity.ok(bookingRoomService.deleteBookingRoomById(id, type));
	}

	@ApiOperation(value = "cập nhập - đăng ký phòng họp")
	@PostMapping("/update-create-booking-room")
	public ResponseEntity<ApiResponse> createOrUpdateBookingRoom(
			@RequestBody CreateOrUpdateBookingRoomRequest bookingRoom, @RequestParam String type) {
		return ResponseEntity.ok(bookingRoomService.changeBookingRoom(bookingRoom, type));
	}

	@ApiOperation(value = "Tìm kiếm danh sách đăng ký phòng họp")
	@PostMapping("/search-list-booking-room")
	public ResponseEntity<ApiResponse> searchListBookingRoom(@RequestBody ListBookingMeetingRoom request) {
		return ResponseEntity.ok(bookingRoomService.searchListBookingRoom(request));
	}



//	**** approve booked room is no longer in use, so is its mail notification function ****
//
//	@ApiOperation(value = "Xác nhận đăng ký phòng họp")
//	@PostMapping("/confirm-booking-meeting-room")
//	public ResponseEntity<ApiResponse> confirmBookingMeetingRoom(
//			@RequestBody ConfirmBookingMeetingRoomRequest request) {
//		return ResponseEntity.ok(bookingRoomService.confirmBookingMeetingRoom(request));
//	}
//	
//	@ApiOperation(value = "gửi mail đăng ký phòng họp")
//	@PostMapping("/send-mail-booking-room")
//	public void sendMaiBookingRoom(@RequestBody CreateOrUpdateBookingRoomRequest bookingRoom) {
//		Optional<Room> roomOptional = roomRepository.findById(bookingRoom.getRoomId());
//		if (!roomOptional.isPresent()) {
//			throw new RecordNotFoundException("Room not found");
//		}
//		bookingRoomService.sendMailBookingRoom(bookingRoom, roomOptional.get(), commonService.idUserAccountLogin());
//	}
//
//	@ApiOperation(value = "gửi mail phê duyệt phòng họp")
//	@GetMapping("/send-mail-confirm-room")
//	public void sendMaiConfirmRoom(@RequestParam("id") Integer bookingRoomId) {
//		bookingRoomService.replyMailBookingRoom(bookingRoomId);
//	}
}
