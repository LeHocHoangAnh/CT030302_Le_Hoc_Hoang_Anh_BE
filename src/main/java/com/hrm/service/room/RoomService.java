package com.hrm.service.room;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hrm.common.Constants;
import com.hrm.entity.Room;
import com.hrm.exception.RecordNotFoundException;
import com.hrm.model.ApiResponse;
import com.hrm.repository.RoomRepository;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    public ApiResponse getListRoom() {
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS,
                roomRepository.findAllOrderByStatus());
    }

    public ApiResponse createRoom(Room request) {
        Room room = new Room();
        room.setName(request.getName());
        room.setStatus(request.getStatus());
        room.setDisplayColor(request.getDisplayColor());
        room.setCommonRegister();
        roomRepository.save(room);
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, null);
    }

    public ApiResponse updateRoom(Room request) {
        Optional<Room> room = roomRepository.findById(request.getId());
        if (room.isEmpty()) {
            throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
        }
        room.get().setName(request.getName());
        room.get().setStatus(request.getStatus());
        room.get().setDisplayColor(request.getDisplayColor());
        room.get().setCommonUpdate();
        roomRepository.save(room.get());
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, null);
    }

    public ApiResponse deleteRoom(Integer id) {
        Optional<Room> room = roomRepository.findById(id);
        if (room.isEmpty()) {
            throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
        }
        roomRepository.deleteById(id);
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, null);
    }

}
