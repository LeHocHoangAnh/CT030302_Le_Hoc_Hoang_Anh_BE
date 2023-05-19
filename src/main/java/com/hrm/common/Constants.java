package com.hrm.common;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Constants {
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String YYYY_MM = "yyyy-MM";
    public static final String DD_MM_YYYY = "dd/MM/yyyy";
    public static final String DD_MM = "dd/MM";
    public static final String SEARCH_LIKE = "%";
    public static final String SECRET = "secret";
    public static final String SUCCESS = "Success get information record";
    public static final String ERROR = "Error get information record";
    public static final String DELETE_SUCCESS = "Delete record success";
    public static final String DELETE_FAIL = "Delete record fail";
    public static final String FAIL_AUTH = "Không có quyền truy cập";
    public static final String CREATE_SUCCESS = "Create or update record success";
    public static final String RECORD_DOES_NOT_EXIST = "The record does not exist";
    public static final String FIELD_INVALID = "Some field invalid";
    public static final String RECORD_ALREADY_EXISTS = "The record already exists";
    public static final String RECORD_NOT_FOUND = "Record Not Found";
    public static final String REMAIN_DAY_OFF_NOT_ENOUGH = "Số ngày nghỉ phép/ nghỉ bù còn lại không đủ";
    public static final String PREVIOUS_MONTH_RECORD_NOT_FOUND = "Dữ liệu tháng trước không tồn tại";
    public static final String DEPARTMENT_NOT_FOUND = "Dữ liệu phòng ban không tồn tại";
    
    // api key
    public static final String API_KEY_NOT_FOUND = "API key not found";
    public static final String API_KEY_FAIL = "API key does not match";
    
    public static final Integer MAX_NAVIGATION_RESULT = 20;

    // complete code
    public static final Integer COMPLETED = 1;
    public static final Integer NONE_COMPLETE = 0;

    // Flag
    public static final Integer DELETE_TRUE = 1;
    public static final Integer DELETE_NONE = 0;

    // http code
    public static final String HTTP_CODE_200 = "200";
    public static final String HTTP_CODE_400 = "400";
    public static final String HTTP_CODE_403 = "403";
    public static final String HTTP_CODE_404 = "404";
    public static final String HTTP_CODE_405 = "405";
    public static final String HTTP_CODE_500 = "500";

    public static final String LOGGED_OUT_SUCCESS = "User has successfully logged out from the system!";

    public static final String VALIDATE_THE_FIELD = "The field can't empty";

    public static final List<String> HEADER_TIME_KEEP = Arrays.asList("STT", "Ngày", "ID", "Họ và Tên", "Giờ vào", "Giờ ra");

    // Employee status
    public static final Integer STATUS_WAIT = 0;
    public static final Integer STATUS_ACCEPT = 1;
    public static final Integer STATUS_NONE_ACCEPT = 0;

    public static final Integer STATUS_ACTIVE = 1;

    public static final Integer STATUS_NOT_ACTIVE = 0;

    // Confirm day-off status
    public static final Integer CONFIRM_WAIT = 0;
    public static final Integer CONFIRM_ACCEPT = 1;
    public static final Integer CONFIRM_REJECT = 2;
    public static final Double ACCPET_PERCENTAGE_THRESHOLD = 65.0;
    public static final String APPROVER_NOT_FOUND = "Bạn không có quyền phê duyệt đơn này";
    
    //Employee Code: Director
    public static final String DIRECTOR_CODE = "ITS-10000";
    
    //Extensions accepted for image upload
    public static final List<String> EXTENSION_ACCEPT_IMAGE = List.of("image/bmp", "image/tiff", "image/jpg", "image/jpeg", "image/png", "image/jfif");

    // Contract Name
    public static final String OFFICIAL_UNLIMITED_CONTRACT = " Hợp đồng chính thức vô thời hạn";
    public static final String OFFICIAL_3Y_CONTRACT = " Hợp đồng chính thức 36 tháng";
    public static final String OFFICIAL_CONTRACT = "Hợp đồng chính thức";
    public static final String PROBATIONARY_CONTRACT = "Hợp đồng thử việc";
    public static final String FREELANCE_CONTRACT = "Hợp đồng freelance";
    public static final String INTERNSHIP_CONTRACT = "Hợp đồng thực tập";
    
    //Excel Name
    public static final String EMPLOYEE_INFO_EXCEL = "Employees Information";
    
    //S3 pre-name
    public static final String EVIDENCE_IMAGE = "evdImg-";
    public static final String PROFILE_IMAGE = "prfImg-";
    
    //Periodically Meeting Type
    public static final Integer INDAY_MEETING = 0;
    public static final Integer DAILY_MEETING = 1;
    public static final Integer WEEKLY_MEETING = 2;
    
    // Type Of Update/Delete Booking Room  
    public static final Integer UNIQ_BOOKING_ROOM = 0;
    public static final Integer ALL_BOOKING_ROOM = 1;
    public static final String EDIT_AUTH_FAIL = "Bạn không có quyền chỉnh sửa đơn này";
    
    // Create/Update type 
    public static final Integer CREATE = 0;
    public static final Integer UPDATE = 1;
    
    // Avatar upload type
    public static final Integer AVATAR = 0;
    public static final Integer EVIDENCE = 1;
    
    // Equipment status
    public static final Integer NOT_YET_USED = 0;
    public static final Integer IN_USED = 1;
    public static final Integer STOP_USED = 2;
    public static final Integer RETURNED_STORED = 3;
    public static final Integer IN_MAINTAIN = 4;
    
    // Equipemnt category
    public static Map<Integer, String> EQUIPMENT_CATEGORY = Map.ofEntries(
            new AbstractMap.SimpleEntry<Integer, String>(0, "Khác"),
            new AbstractMap.SimpleEntry<Integer, String>(1, "Laptop"),
            new AbstractMap.SimpleEntry<Integer, String>(2, "Máy tính Case(PC)"),
            new AbstractMap.SimpleEntry<Integer, String>(3, "Máy tính Bảng(Tablet)"),
            new AbstractMap.SimpleEntry<Integer, String>(4, "Điện thoại test"),
            new AbstractMap.SimpleEntry<Integer, String>(5, "Chuột"),
            new AbstractMap.SimpleEntry<Integer, String>(6, "Bàn phím"),
            new AbstractMap.SimpleEntry<Integer, String>(7, "Phụ kiện"),
            new AbstractMap.SimpleEntry<Integer, String>(8, "Màn hình")
            );
}
