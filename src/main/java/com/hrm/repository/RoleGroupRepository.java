package com.hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hrm.entity.RoleGroup;

@Repository
public interface RoleGroupRepository extends JpaRepository<RoleGroup, Integer> {

}
