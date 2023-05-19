package com.hrm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hrm.entity.Projects;
import com.hrm.model.DropDownResponse;
import com.hrm.model.response.hr.CreateOrEditProjectsResponse;

@Repository
public interface ProjectsRepository extends JpaRepository<Projects, Integer>{
    @Query(value="SELECT pro.id,pro.code_projects as CodeProjects,pro.name_projects as NameProjects,\r\n"
            + "       to_char(pro.time_start,'dd-mm-yyyy') as TimeStart,to_char(pro.time_end,'dd-mm-yyyy') as TimeEnd,\r\n"
            + "             pro.customer as Customer,pro.technology as Technology,pro.description as Description \r\n"
            + "       FROM projects AS pro WHERE pro.id=:id",nativeQuery = true)
    Optional<CreateOrEditProjectsResponse> findDetailById(Integer id);
    
    @Query(value="SELECT prj.id as id, prj.name_projects as name FROM projects as prj", nativeQuery=true)
    List<DropDownResponse> getProjectDropdown();

}
