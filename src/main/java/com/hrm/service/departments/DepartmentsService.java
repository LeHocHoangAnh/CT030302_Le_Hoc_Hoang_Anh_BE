package com.hrm.service.departments;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hrm.common.Constants;
import com.hrm.entity.Department;
import com.hrm.entity.Projects;
import com.hrm.exception.RecordNotFoundException;
import com.hrm.model.ApiResponse;
import com.hrm.model.dao.DepartmentsDao;
import com.hrm.model.dao.EmployeeByDepartmentIdDAO;
import com.hrm.model.request.hr.DepartmentsListRequest;
import com.hrm.model.response.hr.ListEmployeeResponse;
import com.hrm.repository.DepartmentRepository;
import com.hrm.repository.EmployeeRepository;

@Service
@Transactional
public class DepartmentsService {
    
    @Autowired
    DepartmentsDao departmentsDao;
    
    @Autowired
    EmployeeByDepartmentIdDAO employeeDeptIdDAO;
    
    @Autowired
    DepartmentRepository departmentsRepository;
    
    
    public ApiResponse getDepartmentsList(DepartmentsListRequest request, String action) {
        return new ApiResponse(Constants.HTTP_CODE_200,Constants.SUCCESS,departmentsDao.getDepartmentsList(request, action));
    }

	public ApiResponse createDepartment(String name, Integer action) {
		Timestamp now = new Timestamp(System.currentTimeMillis());
		
		Department department = new Department();
		department.setName(name);
		department.setAction(action);
		department.setNumberMember(0);
		department.setCreatedAt(now);
		department.setUpdatedAt(now);
		
		departmentsRepository.save(department);
		return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, null);
	}

	public ApiResponse getEmployeeByDepartmentID(Integer id, Integer page, Integer size) {
		if(id==null) {
			return new ApiResponse(Constants.HTTP_CODE_500, "ID phòng ban null",null);
		}
		return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, employeeDeptIdDAO.getListEmployee(id, page, size));
	}

	public ApiResponse getDepartmentByID(Integer id) {
		if(id==null) {
			return new ApiResponse(Constants.HTTP_CODE_500, "ID phòng ban null",null);
		}
		return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, departmentsRepository.findById(id));
	}

	public ApiResponse updateDepartment(Department department) {
		Timestamp now = new Timestamp(System.currentTimeMillis());
		department.setUpdatedAt(now);
		departmentsRepository.save(department);
		return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, null);
	}

	public ApiResponse deleteDepartment(Integer id) {
		if(id==null) {
			return new ApiResponse(Constants.HTTP_CODE_500, "ID phòng ban null",null);
		}
		Optional<Department> department = departmentsRepository.findById(id);
        if(!department.isPresent()) {
            throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
        }
        departmentsRepository.deleteById(id);
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, null);
	}

	public ApiResponse updateMember(Integer id, Integer member) {
		if(id==null) {
			return new ApiResponse(Constants.HTTP_CODE_500, "ID phòng ban null",null);
		}
		if(member!=null) {
			// find department by id
			Optional<Department> dept = departmentsRepository.findById(id);
			if(!dept.isPresent()) {
	            throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
			}
			else {
				// set new member number and save to database
				dept.get().setNumberMember(member);
				departmentsRepository.save(dept.get());
			}
		}
		return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, null);
	}
}
