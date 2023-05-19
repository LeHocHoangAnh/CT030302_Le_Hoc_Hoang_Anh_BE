package com.hrm.repository;

import com.hrm.entity.ConfigDayOff;
import com.hrm.model.response.ListConfigDayOffResponse;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ConfigDayOffRepository extends JpaRepository<ConfigDayOff, Integer> {
    @Query(value = "select * from config_day_off where (day_from between DATE(:dayFrom) AND DATE(:dayTo) OR "
            + "day_to between DATE(:dayFrom) AND DATE(:dayTo) )", nativeQuery = true)
    List<ConfigDayOff> getConfigDayOffByTime(String dayFrom, String dayTo);

    @Query(value = "Select * from config_day_off as co where co.year_apply = :year", nativeQuery = true)
    List<ConfigDayOff> getConfigByYear(Integer year);

    @Query(value = "SELECT co.day_from AS DayFrom, co.day_to AS DayTo, co.reason_apply AS reasonApply FROM config_day_off AS co"
            + "  WHERE :requestDate LIKE TO_CHAR(co.day_from, 'YYYY-MM') AND :requestDate LIKE TO_CHAR(co.day_to, 'YYYY-MM')", nativeQuery = true)
    List<ListConfigDayOffResponse> getListConfig(String requestDate);

    @Query(value = "SELECT co.day_from AS DayFrom, co.day_to AS DayTo, co.reason_apply AS reasonApply FROM config_day_off AS co"
            + "  where (day_from between DATE(:dayFrom) AND DATE(:dayTo) OR \r\n"
            + "            day_to between DATE(:dayFrom) AND DATE(:dayTo))", nativeQuery = true)
    List<ListConfigDayOffResponse> getListConfigDtoByTime(String dayFrom, String dayTo);

    @Query(value = "select distinct year_apply from config_day_off", nativeQuery = true)
    List<Integer> getYearRange();

    List<ConfigDayOff> findAllByOrderByIdAsc();

    List<ConfigDayOff> findByYearApply(Integer yearApply);
}
