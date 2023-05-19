package com.hrm.service.projects;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hrm.common.Constants;
import com.hrm.entity.Projects;
import com.hrm.exception.RecordNotFoundException;
import com.hrm.model.ApiResponse;
import com.hrm.model.dao.ProjectsDao;
import com.hrm.model.request.hr.CreateOrEditProjectRequest;
import com.hrm.model.request.leader.ListProjectsRequest;
import com.hrm.model.response.hr.CreateOrEditProjectsResponse;
import com.hrm.repository.ProjectsRepository;
import com.hrm.utils.Utils;

@Service
@Transactional
public class ProjectsService {
    
    @Autowired
    ProjectsDao projectsDao;
    
    @Autowired
    ProjectsRepository projectsRepository;
    
    public ApiResponse getListProjects(ListProjectsRequest request) {
        return new ApiResponse(Constants.HTTP_CODE_200,Constants.SUCCESS,projectsDao.getListProjects(request));
    }

    public ApiResponse getDetailProjects(Integer id) {
        Optional<CreateOrEditProjectsResponse> projects = projectsRepository.findDetailById(id);
        if(!projects.isPresent()) {
            throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
        }
        return new ApiResponse(Constants.HTTP_CODE_200,Constants.SUCCESS,projects.get());
    }

    public ApiResponse createProjects(CreateOrEditProjectRequest request) {
        Projects projects = new Projects();
        setProjects(projects,request);
        projects.setCommonRegister();
        projectsRepository.save(projects);
        return new ApiResponse(Constants.HTTP_CODE_200,Constants.CREATE_SUCCESS,request);
    }

    public ApiResponse updateProjects(CreateOrEditProjectRequest request) {
        Optional<Projects> projects = projectsRepository.findById(request.getId());
        if(!projects.isPresent()) {
            throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
        }
        setProjects(projects.get(),request);
        projects.get().setCommonUpdate();
        projectsRepository.save(projects.get());
        return new ApiResponse(Constants.HTTP_CODE_200,Constants.SUCCESS,projects.get());
    }

    public ApiResponse deleteProjects(Integer id) {
        Optional<Projects> projects = projectsRepository.findById(id);
        if(!projects.isPresent()) {
            throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
        }
        projectsRepository.deleteById(id);
        return new ApiResponse(Constants.HTTP_CODE_200,Constants.DELETE_SUCCESS,null);
    }
    
    private void setProjects(Projects projects,CreateOrEditProjectRequest request) {
        projects.setCodeProjects(request.getCodeProjects());
        projects.setNameProjects(request.getNameProjects());
        projects.setCustomer(request.getCustomer());
        projects.setTechnology(request.getTechnology());
        projects.setTimeStart(Utils.convertStringToTimestamp("dd-MM-yyyy", request.getTimeStart()));
        projects.setTimeEnd(Utils.convertStringToTimestamp("dd-MM-yyyy",request.getTimeEnd()));
        projects.setDescription(request.getDescription());
    }
}
