package com.hrm.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.hrm.entity.BookingRoomTime;

import org.springframework.stereotype.Repository;

@Repository
public interface BookingRoomTimeRepository extends JpaRepository<BookingRoomTime, Integer> {

    @Query(value = "SELECT brt.* FROM booking_room_time AS brt "
    		+ "JOIN booking_room AS br ON br.id = brt.booking_room_id "
    		+ "WHERE "
    		+ " ("
    		+ "	  (DATE(brt.time_start) >= :dateStart AND DATE(brt.time_start) <= :dateEnd) "
    		+ "	  OR "
    		+ "	  (DATE(brt.time_end) >= :dateStart AND DATE(brt.time_end) <= :dateEnd)"
    		+ " )"
    		+ "AND br.status != 2", nativeQuery = true)
    List<BookingRoomTime> getListBookingRoomByDate(Date dateStart, Date dateEnd);
    
    @Query(value = "SELECT * FROM booking_room_time AS brt WHERE brt.booking_room_id = :id", nativeQuery = true)
    List<BookingRoomTime> getListBookingRoomByBookingRoomId(Integer id);
	
	@Modifying
	@Query(value="DELETE FROM booking_room_time WHERE booking_room_id = :bookingRoomId", nativeQuery = true)
	int deleteByBookingRoomId(int bookingRoomId);
	
	@Modifying
	@Query(value="DELETE FROM booking_room_time as brt "
			   + "WHERE brt.id>=:bookingRoomTimeId"
			   + "  AND brt.booking_room_id = (SELECT booking_room_id FROM booking_room_time WHERE id=:bookingRoomTimeId)", nativeQuery=true)
	int deleteByBookingRoomTimeId(int bookingRoomTimeId);
}
