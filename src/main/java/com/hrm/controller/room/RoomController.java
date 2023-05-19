package com.hrm.controller.room;

import com.hrm.entity.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hrm.model.ApiResponse;
import com.hrm.service.room.RoomService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/api/employee")
@Api(description = "Quản lý phòng họp")
public class RoomController {
    
    @Autowired
    private RoomService roomService;

    @ApiOperation(value = "Danh sách phòng họp")
    @GetMapping("/list-room")
    public ResponseEntity<ApiResponse> getListRoom() {
        return ResponseEntity.ok(roomService.getListRoom());
    }
    
    @ApiOperation(value = "Chỉnh sửa phòng họp")
    @PostMapping("/updateOrCreateRoom")
    public ResponseEntity<ApiResponse> updateOrCreate(@ApiParam(value="data chỉnh sửa") @RequestBody Room request){
        if(request.getId() == null) {
            return ResponseEntity.ok(roomService.createRoom(request));
        }
        return ResponseEntity.ok(roomService.updateRoom(request));
    }
    
    @ApiOperation(value = "Xóa phòng họp")
    @PostMapping("/delete-room")
    public ResponseEntity<ApiResponse> deleteRoom(@ApiParam(value="id dự án") @RequestParam("id")Integer id){
        return ResponseEntity.ok(roomService.deleteRoom(id));
    }
}
