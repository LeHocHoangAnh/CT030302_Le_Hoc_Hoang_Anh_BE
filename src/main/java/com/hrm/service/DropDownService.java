package com.hrm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hrm.common.Constants;
import com.hrm.model.ApiResponse;
import com.hrm.repository.BookingDayOffRepository;
import com.hrm.repository.DepartmentRepository;

@Service
public class DropDownService {
    
    @Autowired
   private DepartmentRepository departmentRepository;
    
    @Autowired
   private BookingDayOffRepository bookingDayOffRepository;
    
    public ApiResponse dropListDepartment() {
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS,
                departmentRepository.getListDropDownDepartment());
    }
    
    public ApiResponse dropListBooking() {
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS,bookingDayOffRepository.getListDropDownBooking());
    }
}
