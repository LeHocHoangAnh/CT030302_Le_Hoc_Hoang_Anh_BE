package com.hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hrm.entity.BookingRoom;
import com.hrm.entity.BookingRoomTime;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface BookingRoomRepository extends JpaRepository<BookingRoom, Integer> {
    @Query(value = "Select * from booking_room_time where DATE(time_start) = DATE(:date) OR DATE(time_end) =DATE(:date)", nativeQuery = true)
    List<BookingRoomTime> getListBookingRoomByDate(String date);

    @Query(value = "SELECT COUNT(*) FROM booking_room_time AS brt JOIN booking_room AS br ON br.id = brt.booking_room_id "+ 
    		" WHERE br.id_room = :roomId "+
    		" 	AND br.status!=2" +
            "	AND ((:timeStart > brt.time_start AND :timeStart < brt.time_end)\n" +
            " 		OR (:timeEnd > brt.time_start AND :timeEnd < brt.time_end)\n"  + 
            "		OR (:timeStart <= brt.time_start AND :timeEnd >= brt.time_end))\n" + 
            " 	AND (:timeId = -1 OR brt.id!=:timeId)", nativeQuery = true)
    Integer checkTimeBookingRoom(Integer roomId, Timestamp timeStart, Timestamp timeEnd, Integer timeId);
}
