package com.hrm.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hrm.entity.Employee;
import com.hrm.entity.Profile;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Integer> {
    @Query(value = "SELECT * FROM profile WHERE employee_id =:id", nativeQuery = true)
    Optional<Profile> getByEmployeeId(Integer id);

    Profile findByEmployeeId(Integer employeeId);

    Optional<Profile> findByEmployee(Employee employee);
    
    @Query(value="SELECT full_name FROM profile WHERE employee_id = :id", nativeQuery=true)
    String findFullNameByEmployeeId(Integer id);
    
}
