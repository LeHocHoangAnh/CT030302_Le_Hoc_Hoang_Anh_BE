package com.hrm.service;

import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hrm.common.Constants;
import com.hrm.entity.ConfigDayOff;
import com.hrm.entity.Employee;
import com.hrm.entity.OtGeneral;
import com.hrm.repository.ConfigDayOffRepository;
import com.hrm.repository.EmployeeRepository;
import com.hrm.repository.OtGeneralRepository;
import com.hrm.service.employee.ApproveGeneralService;
import com.hrm.utils.Utils;

@Service
@Transactional
public class RefuseOtService {

    @Autowired
    private ConfigDayOffRepository configDayOffRepository;

    @Autowired
    private OtGeneralRepository otGeneralRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    ApproveGeneralService approveGeneralService;

    public void refuseOT(Timestamp startDay, Timestamp backDay, OtGeneral ot, Employee employee) {
        Calendar cal = Calendar.getInstance();
        Calendar calendarStart = Calendar.getInstance();
        calendarStart.setTime(startDay);
        Calendar calendarBack = Calendar.getInstance();
        calendarBack.setTime(backDay);
        String start = Utils.convertDateToString(Constants.YYYY_MM_DD, new Date(startDay.getTime()));
        String back = Utils.convertDateToString(Constants.YYYY_MM_DD, new Date(backDay.getTime()));
        List<ConfigDayOff> listConfig = configDayOffRepository.getConfigByYear(calendarStart.get(Calendar.YEAR));
        LocalTime hourStart = LocalTime.of(calendarStart.get(Calendar.HOUR_OF_DAY), calendarStart.get(Calendar.MINUTE),
                calendarStart.get(Calendar.SECOND));
        LocalTime hourBack = LocalTime.of(calendarBack.get(Calendar.HOUR_OF_DAY), calendarBack.get(Calendar.MINUTE),
                calendarBack.get(Calendar.SECOND));
        LocalTime timeStartMorning = LocalTime.of(8, 30, 00);
        LocalTime timeEndMorning = LocalTime.of(12, 00, 00);
        LocalTime timeStartAfternoon = LocalTime.of(13, 30, 00);
        LocalTime timeEndAfternoon = LocalTime.of(18, 00, 00);
        LocalTime timeStartNight = LocalTime.of(19, 00, 00);
        // trong ngay
        if (start.equals(back)) {
            cal.setTime(new Date(startDay.getTime()));
            Integer checkDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            // check cuoi tuan
            if (!(checkDayOfWeek == 1 || checkDayOfWeek == 7)) {
                if (approveGeneralService.filterConfigDayOff(listConfig, startDay.getTime(),
                        backDay.getTime()) == true) {
                    long MiliSecond = backDay.getTime() - startDay.getTime();
                    ot.setOtHoliday(ot.getOtHoliday() - convertSecondsToHour(MiliSecond));
                } else {
                    long MiliSecond = backDay.getTime() - startDay.getTime();
                    ot.setOtNormal(ot.getOtNormal() - convertSecondsToHour(MiliSecond));
                }
                // check thu 7
            } else if (checkDayOfWeek == 7) {
                if (hourStart.compareTo(timeStartMorning) >= 0 && hourStart.compareTo(timeEndMorning) < 0
                        && hourBack.compareTo(timeStartAfternoon) <= 0 && hourBack.compareTo(timeStartMorning) > 0) {
                    if (approveGeneralService.filterConfigDayOff(listConfig, startDay.getTime(),
                            backDay.getTime()) == true) {
                        long MiliSecond = backDay.getTime() - startDay.getTime();
                        ot.setOtHoliday(ot.getOtHoliday() - convertSecondsToHour(MiliSecond));
                    } else {
                        long MiliSecond = backDay.getTime() - startDay.getTime();
                        ot.setOtMorning7(ot.getOtMorning7() - convertSecondsToHour(MiliSecond));
                    }
                } else if (hourStart.compareTo(timeStartAfternoon) >= 0 && hourStart.compareTo(timeEndAfternoon) < 0
                        && (hourBack.compareTo(timeStartNight) <= 0 && hourBack.compareTo(timeStartAfternoon) > 0)) {
                    if (approveGeneralService.filterConfigDayOff(listConfig, startDay.getTime(),
                            backDay.getTime()) == true) {
                        long MiliSecond = backDay.getTime() - startDay.getTime();
                        ot.setOtHoliday(ot.getOtHoliday() - convertSecondsToHour(MiliSecond));
                    } else {
                        long MiliSecond = backDay.getTime() - startDay.getTime();
                        ot.setOtMorning7(ot.getOtMorning7() - convertSecondsToHour(MiliSecond));
                    }
                } else if (hourStart.compareTo(timeStartNight) == 0) {
                    if (approveGeneralService.filterConfigDayOff(listConfig, startDay.getTime(),
                            backDay.getTime()) == true) {
                        long MiliSecond = backDay.getTime() - startDay.getTime();
                        ot.setOtHoliday(ot.getOtHoliday() - convertSecondsToHour(MiliSecond));
                    } else {
                        long MiliSecond = backDay.getTime() - startDay.getTime();
                        ot.setOtMorning7(ot.getOtMorning7() - convertSecondsToHour(MiliSecond));
                    }
                } else if (hourStart.compareTo(timeStartMorning) >= 0 && hourStart.compareTo(timeEndMorning) < 0
                        && hourBack.compareTo(timeEndAfternoon) <= 0 && hourBack.compareTo(timeStartAfternoon) > 0) {
                    Timestamp timeMedial = Utils.convertStringToTimestamp(Constants.YYYY_MM_DD_HH_MM_SS,
                            calendarStart.get(Calendar.YEAR) + "-" + (calendarStart.get(Calendar.MONTH) + 1) + "-"
                                    + calendarStart.get(Calendar.DATE) + " 12:00:0");
                    if (approveGeneralService.filterConfigDayOff(listConfig, startDay.getTime(),
                            backDay.getTime()) == true) {
                        long MiliSecond = backDay.getTime() - startDay.getTime();
                        ot.setOtHoliday(ot.getOtHoliday() - ((float) (convertSecondsToHour(MiliSecond) - 1.5)));
                    } else {
                        long MiliSecondMorning = timeMedial.getTime() - startDay.getTime();
                        ot.setOtMorning7(ot.getOtMorning7() - convertSecondsToHour(MiliSecondMorning));
                        long MiliSecondApternoon = backDay.getTime() - timeMedial.getTime();
                        ot.setOtMorning7(
                                ot.getOtMorning7() - ((float) (convertSecondsToHour(MiliSecondApternoon) - 1.5)));
                    }
                }
                // check chu nhat
            } else if (checkDayOfWeek == 1) {
                if (hourStart.compareTo(timeStartMorning) >= 0 && hourStart.compareTo(timeEndMorning) < 0
                        && hourBack.compareTo(timeStartAfternoon) <= 0 && hourBack.compareTo(timeStartMorning) > 0) {
                    if (approveGeneralService.filterConfigDayOff(listConfig, startDay.getTime(),
                            backDay.getTime()) == true) {
                        long MiliSecond = backDay.getTime() - startDay.getTime();
                        ot.setOtHoliday(ot.getOtHoliday() - ((float) (convertSecondsToHour(MiliSecond))));
                    } else {
                        long MiliSecond = backDay.getTime() - startDay.getTime();
                        ot.setOtSatSun(ot.getOtSatSun() - ((float) (convertSecondsToHour(MiliSecond))));
                    }
                } else if (hourStart.compareTo(timeStartAfternoon) >= 0 && hourStart.compareTo(timeEndAfternoon) < 0
                        && hourBack.compareTo(timeStartNight) <= 0 && hourBack.compareTo(timeStartAfternoon) > 0) {
                    if (approveGeneralService.filterConfigDayOff(listConfig, startDay.getTime(),
                            backDay.getTime()) == true) {
                        long MiliSecond = backDay.getTime() - startDay.getTime();
                        ot.setOtHoliday(ot.getOtHoliday() - ((float) (convertSecondsToHour(MiliSecond) - 1)));
                    } else {
                        long MiliSecond = backDay.getTime() - startDay.getTime();
                        ot.setOtSatSun(ot.getOtSatSun() - ((float) (convertSecondsToHour(MiliSecond) - 1)));
                    }
                } else if (hourStart.compareTo(timeStartNight) >= 0) {
                    if (approveGeneralService.filterConfigDayOff(listConfig, startDay.getTime(),
                            backDay.getTime()) == true) {
                        long MiliSecond = backDay.getTime() - startDay.getTime();
                        ot.setOtHoliday(ot.getOtHoliday() - convertSecondsToHour(MiliSecond));
                    } else {
                        long MiliSecond = backDay.getTime() - startDay.getTime();
                        ot.setOtSatSun(ot.getOtSatSun() - convertSecondsToHour(MiliSecond));
                    }
                } else if (hourStart.compareTo(timeStartMorning) >= 0 && hourStart.compareTo(timeEndMorning) < 0
                        && hourBack.compareTo(timeEndAfternoon) <= 0 && hourBack.compareTo(timeStartAfternoon) > 0) {
                    if (approveGeneralService.filterConfigDayOff(listConfig, startDay.getTime(),
                            backDay.getTime()) == true) {
                        long MiliSecond = backDay.getTime() - startDay.getTime();
                        ot.setOtHoliday(ot.getOtHoliday() - ((float) (convertSecondsToHour(MiliSecond) - 1.5)));
                    } else {
                        long MiliSecondApternoon = backDay.getTime() - startDay.getTime();
                        ot.setOtSatSun(ot.getOtSatSun() - ((float) (convertSecondsToHour(MiliSecondApternoon) - 1.5)));
                    }
                }
            }
            // khac ngay
        } else {
            cal.setTime(new Date(startDay.getTime()));
            Integer checkDayOfWeekStart = cal.get(Calendar.DAY_OF_WEEK);
            cal.setTime(new Date(backDay.getTime()));
            Integer checkDayOfWeekBack = cal.get(Calendar.DAY_OF_WEEK);
            if (checkDayOfWeekStart == 7 && checkDayOfWeekBack == 1) {
                if (approveGeneralService.filterConfigDayOff(listConfig, startDay.getTime(),
                        backDay.getTime()) == true) {
                    long MiliSecond = backDay.getTime() - startDay.getTime();
                    ot.setOtHoliday(ot.getOtHoliday() - convertSecondsToHour(MiliSecond));
                } else {
                    long MiliSecond = backDay.getTime() - startDay.getTime();
                    ot.setOtSatSun(ot.getOtSatSun() - convertSecondsToHour(MiliSecond));
                }
            } else if (checkDayOfWeekStart == 1 && checkDayOfWeekBack == 2) {
                if (approveGeneralService.filterConfigDayOff(listConfig, startDay.getTime(),
                        backDay.getTime()) == true) {
                    long MiliSecond = backDay.getTime() - startDay.getTime();
                    ot.setOtHoliday(ot.getOtHoliday() - convertSecondsToHour(MiliSecond));
                } else {
                    long MiliSecond = backDay.getTime() - startDay.getTime();
                    ot.setOtSatSun(ot.getOtSatSun() - convertSecondsToHour(MiliSecond));
                }
            } else {
                if (approveGeneralService.filterConfigDayOff(listConfig, startDay.getTime(),
                        backDay.getTime()) == true) {
                    long MiliSecond = backDay.getTime() - startDay.getTime();
                    ot.setOtHoliday(ot.getOtHoliday() - convertSecondsToHour(MiliSecond));
                } else {
                    long MiliSecond = backDay.getTime() - startDay.getTime();
                    ot.setOtNormal(ot.getOtNormal() - convertSecondsToHour(MiliSecond));
                }
            }
        }
        // sum ot
        if (ot.getOtMorning7() > 4) {
            ot.setSumOtMonth((float) (ot.getOtNormal() + ((ot.getOtMorning7())) + (ot.getOtSatSun() * 1.5)
                    + (ot.getOtHoliday() * 2)));
            ot.setCompensatoryLeave((float) ((((ot.getOtNormal() + (ot.getOtMorning7()) + ot.getOtSatSun()) * 0.5))
                    + ot.getOtHoliday() / 8));
            Integer sumOtRounding = (int) ((((ot.getOtNormal() + (ot.getOtMorning7()) + ot.getOtSatSun()) * 0.5))
                    + ot.getOtHoliday() / 8);
            Integer numberDivide = (int) ((((ot.getOtNormal() + (ot.getOtMorning7()) + ot.getOtSatSun()) * 0.5))
                    + ot.getOtHoliday() % 8);
            if (numberDivide < 3) {
                ot.setCstLeaveRounding((float) sumOtRounding);
            } else if (numberDivide >= 3 && numberDivide < 7) {
                ot.setCstLeaveRounding((float) (sumOtRounding + 0.5));
            } else if (numberDivide >= 7) {
                ot.setCstLeaveRounding((float) sumOtRounding + 1);
            }
        } else {
            ot.setSumOtMonth((float) (ot.getOtNormal() + (ot.getOtSatSun() * 1.5) + (ot.getOtHoliday() * 2)));
            ot.setCompensatoryLeave((float) ((((ot.getOtNormal() + ot.getOtSatSun()) * 0.5) + ot.getOtHoliday()) / 8));
        }
        employee.setCompensatoryLeave(employee.getCompensatoryLeave() == null ? 0
                : employee.getCompensatoryLeave() + ot.getCompensatoryLeave());
        employee.setOtUnpaid(employee.getOtUnpaid() == null ? 0 : employee.getOtUnpaid() + ot.getSumOtMonth());
        employeeRepository.save(employee);
        ot.setCommonUpdate();
        otGeneralRepository.save(ot);
    }

    private Float convertSecondsToHour(long MiliSecond) {
        Float second = (float) MiliSecond / 1000;
        Float hours = second / 3600;
        return (float) (Math.round(hours * Math.pow(10, 5)) / Math.pow(10, 5));
    }

}
