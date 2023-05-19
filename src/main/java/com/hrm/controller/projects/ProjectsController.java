package com.hrm.controller.projects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hrm.model.ApiResponse;
import com.hrm.model.request.hr.CreateOrEditProjectRequest;
import com.hrm.model.request.leader.ListProjectsRequest;
import com.hrm.service.projects.ProjectsService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/api/hr")
@Api(description = "Quản lý dự án")
public class ProjectsController {
    
    @Autowired
    private ProjectsService projectsService;

    @ApiOperation(value = "Danh sách dự án")
    @PostMapping("/list-projects")
    public ResponseEntity<ApiResponse> getListProjects(@ApiParam(value="data search dự án")@RequestBody ListProjectsRequest request) {
        return ResponseEntity.ok(projectsService.getListProjects(request));
    }
    
    @ApiOperation(value = "Chi tiết dự án")
    @GetMapping("/projects-detail")
    public ResponseEntity<ApiResponse> getDetailProjects(@ApiParam(value="id dự án") @RequestParam("id") Integer id){
        return ResponseEntity.ok(projectsService.getDetailProjects(id));
    }
    
    @ApiOperation(value = "Chỉnh sửa dự án")
    @PostMapping("/edit-projects")
    public ResponseEntity<ApiResponse> updateOrCreate(@ApiParam(value="data chỉnh sửa") @RequestBody CreateOrEditProjectRequest request){
        if(request.getId() == null) {
            return ResponseEntity.ok(projectsService.createProjects(request));
        }
        return ResponseEntity.ok(projectsService.updateProjects(request));
    }
    
    @ApiOperation(value = "Xóa dự án")
    @PostMapping("/delete-projects")
    public ResponseEntity<ApiResponse> deleteProjects(@ApiParam(value="id dự án") @RequestParam("id")Integer id){
        return ResponseEntity.ok(projectsService.deleteProjects(id));
    }
}
