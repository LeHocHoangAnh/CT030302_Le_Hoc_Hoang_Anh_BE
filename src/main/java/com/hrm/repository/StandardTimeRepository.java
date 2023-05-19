package com.hrm.repository;

import com.hrm.entity.StandardTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StandardTimeRepository extends JpaRepository<StandardTime,Integer> {
        @Query(value = "select * from standard_time where delete_flag=0 order by id desc limit 1",nativeQuery = true)
        Optional<StandardTime> findTop1ByStandardTimeOrderByIdDesc();
}
