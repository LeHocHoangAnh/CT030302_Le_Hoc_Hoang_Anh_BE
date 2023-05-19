package com.hrm.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.hrm.common.Constants;
import com.hrm.model.ApiResponse;
import com.hrm.model.response.anonymous.EmployeeDiscordResponse;
import com.hrm.repository.EmployeeRepository;

@Service
@Transactional
public class AnonymousService {

    @Autowired
    private EmployeeRepository employeeRepository;

    public ApiResponse getListEmployee() {
        List<EmployeeDiscordResponse> employeeList = employeeRepository.findEmployeeListForDiscordConfig();

        return new ApiResponse(Constants.HTTP_CODE_200, Constants.CREATE_SUCCESS, employeeList);
    }

}
