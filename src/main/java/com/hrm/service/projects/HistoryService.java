package com.hrm.service.projects;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hrm.common.Constants;
import com.hrm.entity.Employee;
import com.hrm.entity.HistoryWork;
import com.hrm.entity.Projects;
import com.hrm.exception.RecordNotFoundException;
import com.hrm.model.ApiResponse;
import com.hrm.model.DropDownResponse;
import com.hrm.model.dao.HistoryDao;
import com.hrm.model.request.PaginationRequest;
import com.hrm.model.request.hr.CreateOrEditHistoryRequest;
import com.hrm.model.request.hr.HistoryWorkReponse;
import com.hrm.repository.EmployeeRepository;
import com.hrm.repository.HistoryWorkRepository;
import com.hrm.repository.ProjectsRepository;
import com.hrm.utils.Utils;

@Service
@Transactional
public class HistoryService {
    
    @Autowired
    private HistoryWorkRepository historyWorkRepository;
    
    @Autowired
    private ProjectsRepository projectsRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private HistoryDao historyDao;

    public ApiResponse getListHistory(Integer id,PaginationRequest request) {
        Optional<Projects> projects = projectsRepository.findById(id);
        if(!projects.isPresent()) {
            throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
        }
        return new ApiResponse(Constants.HTTP_CODE_200,Constants.SUCCESS,historyDao.getListHistory(id, request));
    }

    public ApiResponse getDetailHistory(Integer id) {
       Optional<HistoryWorkReponse> history = historyWorkRepository.getDetailById(id);
       if(!history.isPresent()) {
           throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
       }
        return new ApiResponse(Constants.HTTP_CODE_200,Constants.SUCCESS,history);
    }

    public ApiResponse createHistory(CreateOrEditHistoryRequest request) {
        HistoryWork history = new HistoryWork();
        setHistoryWork(history,request);
        history.setCommonRegister();
        historyWorkRepository.save(history);
        return new ApiResponse(Constants.HTTP_CODE_200,Constants.SUCCESS,null);
    }

    public ApiResponse updateHistory(CreateOrEditHistoryRequest request) {
       Optional<HistoryWork> history = historyWorkRepository.findById(request.getId());
       if(!history.isPresent()) {
           throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
       }
       setHistoryWork(history.get(), request);
       history.get().setCommonUpdate();
       historyWorkRepository.save(history.get());
        return new ApiResponse(Constants.HTTP_CODE_200,Constants.SUCCESS,null);
    }

    public ApiResponse deleteHistory(Integer id) {
        Optional<HistoryWork> history = historyWorkRepository.findById(id);
        if(!history.isPresent()) {
            throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
        }
        historyWorkRepository.deleteById(id);
        return new ApiResponse(Constants.HTTP_CODE_200,Constants.DELETE_SUCCESS,null);
    }
    
    private void setHistoryWork(HistoryWork history, CreateOrEditHistoryRequest request) {
       Optional<Projects> project = projectsRepository.findById(request.getIdProjects());
       Optional<Employee> employee = employeeRepository.findById(request.getIdEmployee());
       if(!project.isPresent() || !employee.isPresent()) {
           throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
       }
        history.setProjects(project.get());
        history.setEmployee(employee.get());
        history.setTimeStart(Utils.convertStringToTimestamp("dd-MM-yyyy", request.getTimeStart()));
        history.setTimeEnd(Utils.convertStringToTimestamp("dd-MM-yyyy", request.getTimeEnd()));
        history.setRole(request.getRole());
    }

    public ApiResponse checkEmployee(String key) {
       DropDownResponse resp = employeeRepository.findByEmployeeCodeOrName(key.trim());
       if(resp == null) {
           return new ApiResponse(Constants.HTTP_CODE_404,Constants.ERROR,null);
       }
        return new ApiResponse(Constants.HTTP_CODE_200,Constants.SUCCESS,resp);
    }

}
