package com.hrm.service.employee;

import com.hrm.common.Constants;
import com.hrm.model.ApiResponse;
import com.hrm.repository.RoleGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleGroupService {

    @Autowired
    private RoleGroupRepository roleGroupRepository;

    public ApiResponse getDetailRoleGroup(Integer id){
        return new ApiResponse(Constants.HTTP_CODE_200,Constants.SUCCESS,roleGroupRepository.findById(id));
    }
}
