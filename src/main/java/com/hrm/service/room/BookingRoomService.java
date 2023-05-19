package com.hrm.service.room;

import com.hrm.common.CommonService;
import com.hrm.common.Constants;
import com.hrm.entity.BookingRoom;
import com.hrm.entity.BookingRoomTime;
import com.hrm.entity.Employee;
import com.hrm.entity.Profile;
import com.hrm.entity.Room;
import com.hrm.exception.RecordNotFoundException;
import com.hrm.model.ApiResponse;
import com.hrm.model.dao.BookingMeetingRoomDAO;
import com.hrm.model.request.BookingRoomListRequest;
import com.hrm.model.request.employee.CreateOrUpdateBookingRoomRequest;
import com.hrm.model.request.hr.ConfirmBookingMeetingRoomRequest;
import com.hrm.model.request.hr.ListBookingMeetingRoom;
import com.hrm.model.response.BookingByIdResponse;
import com.hrm.repository.BookingRoomRepository;
import com.hrm.repository.BookingRoomTimeRepository;
import com.hrm.repository.EmployeeRepository;
import com.hrm.repository.ProfileRepository;
import com.hrm.repository.RoomRepository;
import com.hrm.utils.Utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional
@Service
public class BookingRoomService {

    @Autowired
    private BookingRoomRepository bookingRoomRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private BookingRoomTimeRepository bookingRoomTimeRepository;
    
    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private CommonService commonService;

    @Autowired
    private BookingMeetingRoomDAO bookingMeetingRoomDAO;
    
    @Autowired
    private JavaMailSender emailSender;
    
    private final String mailTo = "admin@its-global.vn";
    
    @Value("${spring.mail.username}")
    private String mailFrom;

    public ApiResponse getListBookingRoomByDate(BookingRoomListRequest req) {
    	int interval = req.getDays().equals("day")?0:req.getDays().equals("week")?6:0;
    	Date startDate = req.getDateRequest();
    	Calendar cal = Calendar.getInstance();
    	cal.setTime(startDate);
    	cal.add(Calendar.DATE, interval);
    	cal.set(Calendar.HOUR, 0);
    	cal.set(Calendar.MINUTE, 0);
    	cal.set(Calendar.SECOND, 0);
        List<BookingRoomTime> bookingRoomRepositories = bookingRoomTimeRepository.getListBookingRoomByDate(startDate, cal.getTime());
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, bookingRoomRepositories);
    }

    public ApiResponse getBookingRoomById(Integer id) {
        Optional<BookingRoom> bookingRoomOptional = bookingRoomRepository.findById(id);
        if (bookingRoomOptional.isEmpty()) {
            throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
        }
        Profile profile = profileRepository.findByEmployeeId(bookingRoomOptional.get().getEmployee().getId());
        BookingByIdResponse response = new BookingByIdResponse();
        
        response.setId(bookingRoomOptional.get().getId());
        response.setReason(bookingRoomOptional.get().getReason());
        response.setStatus(bookingRoomOptional.get().getStatus());
        response.setRoomId(bookingRoomOptional.get().getRoom().getId());
        response.setRoomName(bookingRoomOptional.get().getRoom().getName());
        response.setTimeStart(bookingRoomOptional.get().getTimeStart());
        response.setTimeEnd(bookingRoomOptional.get().getTimeEnd());
        response.setPeriodType(bookingRoomOptional.get().getPeriodType());
        response.setDaysOfWeek(bookingRoomOptional.get().getDaysOfWeek());
        response.setEmployeeId(bookingRoomOptional.get().getEmployee().getId());
        response.setEmployeeName(profile.getFullName());
        
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, response);
    }

    public ApiResponse deleteBookingRoomById(Integer id, String type) {
    	// Delete a specific bookingTime record by booking-room-time ID
        if(Integer.parseInt(type)==Constants.UNIQ_BOOKING_ROOM) {
        	Optional<BookingRoomTime> bookingRoomTimeOptional = bookingRoomTimeRepository.findById(id);
        	if (bookingRoomTimeOptional.isEmpty()) {
        		throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
        	}
        	bookingRoomTimeRepository.deleteById(id);
        }
    	// Delete bookingRoom by booking-room ID and all of its bookingTime records
        else if(Integer.parseInt(type)==Constants.ALL_BOOKING_ROOM) {   
        	Optional<BookingRoomTime> bookingRoomTime = bookingRoomTimeRepository.findById(id);
            if (bookingRoomTime.isEmpty()) {
                throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
            }
            BookingRoom bookingRoom = bookingRoomTime.get().getBookingRoom();
            int bookingStatus = bookingRoom.getStatus();
            // If bookingRoom has been accepted
            if(bookingStatus==Constants.STATUS_ACCEPT) { 
            	bookingRoomTimeRepository.deleteByBookingRoomTimeId(bookingRoomTime.get().getId());
            }
            // If bookingRoom is still waiting for approving
            else if(bookingStatus==Constants.STATUS_WAIT) {        	
            	bookingRoomTimeRepository.deleteByBookingRoomId(bookingRoom.getId());
            	bookingRoomRepository.deleteById(bookingRoom.getId());
            }
        }
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, null);
    }

    public ApiResponse changeBookingRoom(CreateOrUpdateBookingRoomRequest bookingRoom, String type) {
    	if(bookingRoom.getPeriodType()==Constants.INDAY_MEETING) {    		
    		boolean checkTimeBookingRoom = checkTimeBookingRoom(bookingRoom);
    		if (!checkTimeBookingRoom) {
    			throw new RecordNotFoundException(Constants.RECORD_ALREADY_EXISTS);
    		}
    	}
        if (bookingRoom.getId() == null && type.equals("null")) {
            return createBookingRoom(bookingRoom);
        } else {
            return updateBookingRoom(bookingRoom, type);
        }
    }
    
    @Transactional
    public ApiResponse updateBookingRoom(CreateOrUpdateBookingRoomRequest bookingRoom, String type) {
    	// check room exists
    	Optional<Room> roomOptional = roomRepository.findById(bookingRoom.getRoomId());
    	if (roomOptional.isEmpty()) {
    		throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
    	}
    	// get local time start/end from update request 
    	LocalTime requestStartTime = bookingRoom.getTimeStart().toLocalDateTime().toLocalTime();
    	LocalTime requestEndTime = bookingRoom.getTimeEnd().toLocalDateTime().toLocalTime();
    	int countBookedTime = 0;
    	// Update a specific bookingTime record by booking-room-time ID
    	if(Integer.parseInt(type)==Constants.UNIQ_BOOKING_ROOM) {
    		// check exists
            Optional<BookingRoomTime> bookingRoomTimeOptional = bookingRoomTimeRepository.findById(bookingRoom.getId());
            if (bookingRoomTimeOptional.isEmpty()) {
                throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
            }
        	// check bookingRoom record exists
    		Optional<BookingRoom> bookingRoomOptional = bookingRoomRepository.findById(bookingRoomTimeOptional.get().getBookingRoom().getId());
            if (bookingRoomOptional.isEmpty()) {
                throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
            }
            // check authorization of user to edit record
            if(bookingRoomOptional.get().getEmployee().getId() != commonService.idUserAccountLogin()) {
                return new ApiResponse(Constants.HTTP_CODE_400, Constants.EDIT_AUTH_FAIL, null);
            }
            
            updateTimeBookingRoom(requestStartTime, requestEndTime, bookingRoomTimeOptional.get());
    		countBookedTime = bookingRoomRepository.checkTimeBookingRoom(roomOptional.get().getId(), bookingRoomTimeOptional.get().getTimeStart(), bookingRoomTimeOptional.get().getTimeEnd(), bookingRoom.getId());
    		if(countBookedTime>0) {
        		throw new RecordNotFoundException(Constants.RECORD_ALREADY_EXISTS);
    		}
            bookingRoom.setTimeStart(bookingRoomTimeOptional.get().getTimeStart());
            bookingRoom.setTimeEnd(bookingRoomTimeOptional.get().getTimeEnd());
            bookingRoom.setPeriodType(Constants.DAILY_MEETING);
            bookingRoomTimeRepository.deleteById(bookingRoomTimeOptional.get().getId());
            createBookingRoom(bookingRoom);
    	}
    	// update bookingRoom by booking-room ID and all of its bookingTime records
    	else if(Integer.parseInt(type)==Constants.ALL_BOOKING_ROOM) {
        	// check bookingRoom record exists
    		Optional<BookingRoom> bookingRoomOptional = bookingRoomRepository.findById(bookingRoom.getId());
            if (bookingRoomOptional.isEmpty()) {
                throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
            }
            // check authorization of user to edit record
            if(bookingRoomOptional.get().getEmployee().getId() != commonService.idUserAccountLogin()) {
                return new ApiResponse(Constants.HTTP_CODE_400, Constants.EDIT_AUTH_FAIL, null);
            }
            // update booking room time records
            List<BookingRoomTime> bookingRoomTimeList = bookingRoomTimeRepository.getListBookingRoomByBookingRoomId(bookingRoom.getId());
            for (BookingRoomTime item : bookingRoomTimeList) {
            	updateTimeBookingRoom(requestStartTime, requestEndTime, item); 
        		// check if date&time has existed before
        		countBookedTime = bookingRoomRepository.checkTimeBookingRoom(roomOptional.get().getId(), item.getTimeStart(), item.getTimeEnd(), item.getId());
        		if(countBookedTime>0) {
            		throw new RecordNotFoundException(Constants.RECORD_ALREADY_EXISTS);
        		}
			}
            bookingRoomTimeRepository.saveAll(bookingRoomTimeList);
            
            // update booking room record
            bookingRoomOptional.get().setTimeStart(Timestamp.valueOf(LocalDateTime.of(bookingRoomOptional.get().getTimeStart().toLocalDateTime().toLocalDate(), requestStartTime)));
            bookingRoomOptional.get().setTimeEnd(Timestamp.valueOf(LocalDateTime.of(bookingRoomOptional.get().getTimeEnd().toLocalDateTime().toLocalDate(), requestEndTime)));
            bookingRoomOptional.get().setRoom(roomOptional.get());
            bookingRoomOptional.get().setReason(bookingRoom.getReason());
            bookingRoomOptional.get().setStatus(Constants.STATUS_WAIT);
            bookingRoomOptional.get().setCommonUpdate();
            bookingRoomRepository.save(bookingRoomOptional.get());
    	}
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, null);
    }
    public void updateTimeBookingRoom(LocalTime requestStartTime, LocalTime requestEndTime, BookingRoomTime roomTime) {
    	// update time of timeStart
    	LocalDateTime startDate =roomTime.getTimeStart().toLocalDateTime();
    	startDate = LocalDateTime.of(startDate.toLocalDate(), requestStartTime);
    	roomTime.setTimeStart(Timestamp.valueOf(startDate));
    	// update time of timeEnd
    	LocalDateTime endDate =roomTime.getTimeEnd().toLocalDateTime();
    	endDate = LocalDateTime.of(endDate.toLocalDate(), requestEndTime);
    	roomTime.setTimeEnd(Timestamp.valueOf(endDate));
    	
    	roomTime.setCommonUpdate();
    }
    
    @Transactional
    public ApiResponse createBookingRoom(CreateOrUpdateBookingRoomRequest bookingRoom) {
        Optional<Room> roomOptional = roomRepository.findById(bookingRoom.getRoomId());
        if (roomOptional.isEmpty()) {
            throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
        }
        List<BookingRoomTime> bookingRoomTime = new ArrayList<>();
        Employee user = employeeRepository.findByIdAndDeleteFlag(commonService.idUserAccountLogin(), Constants.DELETE_NONE).get();
        // If the meeting is in-day
        if(bookingRoom.getPeriodType() == Constants.INDAY_MEETING) {
        	BookingRoom parentRecord = bookingRoomRepository.save(setBookingRoomValue(user, bookingRoom.getTimeStart(), bookingRoom.getTimeEnd(),bookingRoom.getPeriodType(), bookingRoom.getReason(), roomOptional.get(), null));
        	bookingRoomTime.add(setBookingRoomTimeValue(bookingRoom.getTimeStart(), bookingRoom.getTimeEnd(), parentRecord));
        }
        // If booking days are in periods of multiple days
        else if(bookingRoom.getPeriodType() == Constants.DAILY_MEETING){
        	Integer countBookedTime = 0;
        	// get registered time in booking request
        	LocalDateTime timeStart = bookingRoom.getTimeStart().toLocalDateTime();
        	LocalDateTime timeEnd = bookingRoom.getTimeEnd().toLocalDateTime();
        	// calculate days between start and end date
        	Long daysBetween = Duration.between(timeStart, timeEnd).toDays();
        	//save parent booking to DB
        	BookingRoom parentRecord = bookingRoomRepository.save(setBookingRoomValue(user, bookingRoom.getTimeStart(), bookingRoom.getTimeEnd(), bookingRoom.getPeriodType(), bookingRoom.getReason(), roomOptional.get(), bookingRoom.getDaysOfWeek()));
        	//
        	for(int i = 0;i<=daysBetween.intValue();i++) {
        		// increase register days
        		LocalDateTime registerStartDay = timeStart.plusDays(i);
        		LocalDateTime registerEndDay = LocalDateTime.of(registerStartDay.toLocalDate(), timeEnd.toLocalTime());
        		// 
        		// if user books room everyday
        		if(bookingRoom.getDaysOfWeek()==null || bookingRoom.getDaysOfWeek().size()<=0) {		
        			// skip Saturday & Sunday, add the booking date to List
        			if(registerStartDay.getDayOfWeek()!=DayOfWeek.SATURDAY && registerStartDay.getDayOfWeek()!=DayOfWeek.SUNDAY) {
                		// check if date&time has existed before
                		countBookedTime = bookingRoomRepository.checkTimeBookingRoom(parentRecord.getRoom().getId(), Timestamp.valueOf(registerStartDay), Timestamp.valueOf(registerEndDay), -1);
                		if(countBookedTime>0) {
                    		throw new RecordNotFoundException(Constants.RECORD_ALREADY_EXISTS);
                		}
        				bookingRoomTime.add(setBookingRoomTimeValue(Timestamp.valueOf(registerStartDay), Timestamp.valueOf(registerEndDay), parentRecord));
        			}
        		}
        		// if the user books a room on specific days in a week
        		else {
        			if(bookingRoom.getDaysOfWeek().indexOf(String.valueOf(registerStartDay.getDayOfWeek().getValue()))>-1) {
                		// check if date&time has existed before
                		countBookedTime = bookingRoomRepository.checkTimeBookingRoom(parentRecord.getRoom().getId(), Timestamp.valueOf(registerStartDay), Timestamp.valueOf(registerEndDay), -1);
                		if(countBookedTime>0) {
                    		throw new RecordNotFoundException(Constants.RECORD_ALREADY_EXISTS);
                		}
        				bookingRoomTime.add(setBookingRoomTimeValue(Timestamp.valueOf(registerStartDay), Timestamp.valueOf(registerEndDay), parentRecord));
        			}
        		}
        	}
        }
        
        // save list of booking date
        bookingRoomTimeRepository.saveAll(bookingRoomTime);
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, null);
    }
    
    public BookingRoom setBookingRoomValue(Employee userRequest, Timestamp timeStart,  Timestamp timeEnd,Integer periodType, String reason, Room room, List<String> daysOfWeek) {
        BookingRoom bookingRoomRegister = new BookingRoom();
        bookingRoomRegister.setEmployee(userRequest);
        bookingRoomRegister.setTimeStart(timeStart);
        bookingRoomRegister.setTimeEnd(timeEnd);
        bookingRoomRegister.setPeriodType(periodType);
        bookingRoomRegister.setDaysOfWeek(daysOfWeek);
        bookingRoomRegister.setReason(reason);
        bookingRoomRegister.setRoom(room);
        bookingRoomRegister.setStatus(Constants.STATUS_WAIT);
        bookingRoomRegister.setCommonRegister();
        
        return bookingRoomRegister;
    	
    }
    public BookingRoomTime setBookingRoomTimeValue(Timestamp timeStart,  Timestamp timeEnd, BookingRoom bookingRoom) {
        BookingRoomTime detailRegister = new BookingRoomTime();
        detailRegister.setTimeStart(timeStart);
        detailRegister.setTimeEnd(timeEnd);
        detailRegister.setBookingRoom(bookingRoom);
        detailRegister.setCommonRegister();
        
        return detailRegister;
    	
    }
    public ApiResponse searchListBookingRoom(ListBookingMeetingRoom request) {
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS,
                bookingMeetingRoomDAO.searchListBookingRoom(request));
    }
    public ApiResponse confirmBookingMeetingRoom(ConfirmBookingMeetingRoomRequest request) {
        Optional<BookingRoom> bookingRoomOptional = bookingRoomRepository.findById(request.getId());
        if (bookingRoomOptional.isEmpty()){
            throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
        }
        bookingRoomOptional.get().setStatus(request.getStatus());
        bookingRoomOptional.get().setCommonUpdate();
        bookingRoomRepository.save(bookingRoomOptional.get());
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, null);
    }

    public boolean checkTimeBookingRoom(CreateOrUpdateBookingRoomRequest request) {
        Integer countBookingRoom = bookingRoomRepository.checkTimeBookingRoom(request.getRoomId(),request.getTimeStart(), request.getTimeEnd(), -1);
        if (countBookingRoom > 0) {
            return false;
        }
        return true;
    }
    
    @Async
    public void sendMailBookingRoom(CreateOrUpdateBookingRoomRequest bookingRoom, Room room, Integer userId){
    	Integer mailType = bookingRoom.getId()==null?Constants.CREATE:Constants.UPDATE;
    	String newLine = "\n";
    	Profile profile = profileRepository.getByEmployeeId(userId).get();
    	Employee employee = employeeRepository.findByIdAndDeleteFlag(userId, Constants.DELETE_NONE).get();
    	
    	LocalDateTime timeStart = bookingRoom.getTimeStart().toLocalDateTime();
    	LocalDate datefrom = timeStart.toLocalDate();
    	LocalTime timefrom = timeStart.toLocalTime();
    	LocalDateTime timeEnd = bookingRoom.getTimeEnd().toLocalDateTime();
    	LocalDate dateTo = timeEnd.toLocalDate();
    	LocalTime timeTo = timeEnd.toLocalTime();
    	
    	//  email subject
    	String emailSubject = "["+(mailType==Constants.CREATE?"Đăng ký":"Cập nhật")+"]"+profile.getFullName()+" - phòng ban "+employee.getDepartment().getName() +" - Đặt phòng họp " +room.getName();
    	// email content
    	StringBuilder emailContent = new StringBuilder();
    	emailContent.append("Loại định kỳ: ");
    	emailContent.append(bookingRoom.getPeriodType()==Constants.INDAY_MEETING?"Trong ngày":"Hàng ngày");
    	if(bookingRoom.getDaysOfWeek()!=null && bookingRoom.getDaysOfWeek().size()>0) {
        	emailContent.append(newLine);
    		emailContent.append("Các ngày trong tuần: ");
        	emailContent.append(daysOfWeekToString(bookingRoom.getDaysOfWeek()));
    	}
    	emailContent.append(newLine);
    	emailContent.append("Từ ");
    	emailContent.append(datefrom.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    	emailContent.append(" đến ");
    	emailContent.append(dateTo.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    	emailContent.append(newLine);
    	emailContent.append("Thời gian: ");
    	emailContent.append(timefrom);
    	emailContent.append(" - ");
    	emailContent.append(timeTo);
    	emailContent.append(newLine);
    	emailContent.append("Lý do: ");
    	emailContent.append(bookingRoom.getReason());
    	emailContent.append(newLine);
    	emailContent.append(newLine);
    	emailContent.append("Link xác nhận: http://hrm.its-global.vn/confirm-booking-meeting-room");
    	
    	try {
			SimpleMailMessage message = new SimpleMailMessage();
			// send from
			message.setFrom(mailFrom);
			// send to
			message.setTo(mailTo);
			// cc to
			if(employee.getBookingMeetingNotify()) {				
				message.setCc(employee.getEmail());
			}
			// subject
			message.setSubject(emailSubject);
			// content
			message.setText(emailContent.toString());
			
			emailSender.send(message);
		} catch (Exception e) {
            e.printStackTrace();
		}
    }
    
    @Async
    public void replyMailBookingRoom(Integer bookingRoomId){
    	String newLine = "\n";
    	BookingRoom bookingRoom = bookingRoomRepository.findById(bookingRoomId).get();
    	Profile profile = profileRepository.getByEmployeeId(bookingRoom.getEmployee().getId()).get();
    	
    	LocalDateTime timeStart = bookingRoom.getTimeStart().toLocalDateTime();
    	LocalDateTime timeEnd = bookingRoom.getTimeEnd().toLocalDateTime();
    	int status = bookingRoom.getStatus();
    	//  email subject
    	String emailSubject = profile.getFullName()
    			+" - Phòng ban "+bookingRoom.getEmployee().getDepartment().getName() 
    			+" - Đặt phòng họp " +bookingRoom.getRoom().getName()
    			+" - Thời gian "+timeStart.toLocalDate().toString() + (timeStart.getDayOfMonth()==timeEnd.getDayOfMonth()?"":(" đến " + timeEnd.toLocalDate().toString()));
    	// email content
    	StringBuilder emailContent = new StringBuilder();
    	emailContent.append("Giờ: ");
    	emailContent.append(timeStart.toLocalTime().toString() +" - "+timeEnd.toLocalTime().toString());
    	emailContent.append(newLine);
    	if(bookingRoom.getDaysOfWeek()!=null && bookingRoom.getDaysOfWeek().size()>0) {
    		emailContent.append("Các ngày trong tuần: ");
        	emailContent.append(daysOfWeekToString(bookingRoom.getDaysOfWeek()));
    	}
    	else {
        	emailContent.append("Loại định kỳ: ");
        	emailContent.append(bookingRoom.getPeriodType()==Constants.INDAY_MEETING?"Trong ngày":"Hàng ngày");
    	}
    	emailContent.append(newLine);
        emailContent.append("Trạng Thái:  ");
        if (status== 1){
        	emailContent.append(" Đã Xác Nhận");
        }
        else if(status== 2) {
        	emailContent.append(" Đã Từ Chối");        	
        }
        try {
			SimpleMailMessage message = new SimpleMailMessage();
			// send from
			message.setFrom(mailFrom);
			// send to
			message.setTo(mailTo);
			// cc to
			if(bookingRoom.getEmployee().getConfirmMeetingNotify()) {				
				message.setCc(bookingRoom.getEmployee().getEmail());
			}
			// subject
			message.setSubject(emailSubject);
			// content
			message.setText(emailContent.toString());
			
			emailSender.send(message);
		} catch (Exception e) {
            e.printStackTrace();
		}
    }
    
    public String daysOfWeekToString(List<String> daysOfWeek) {
    	String result = "";
    	// sort list
    	daysOfWeek = daysOfWeek.stream().sorted().collect(Collectors.toList());
    	for (String item : daysOfWeek) {
			switch (item) {
			case "1":
				result+="Thứ Hai, ";
				break;
			case "2":
				result+="Thứ Ba, ";
				break;
			case "3":
				result+="Thứ Tư, ";
				break;
			case "4":
				result+="Thứ Năm, ";
				break;
			case "5":
				result+="Thứ Sáu, ";
				break;
			case "6":
				result+="Thứ Bảy, ";
				break;
			case "7":
				result+="Chủ nhật, ";
				break;
			}
		}
    	return result.substring(0, result.length()-2);
    }
}
