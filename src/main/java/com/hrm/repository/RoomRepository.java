package com.hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hrm.entity.Room;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Integer>{

    @Query(value = "Select * from room order by status",nativeQuery = true)
    List<Room> findAllOrderByStatus();
}
