package com.hrm.service.employee;

import com.hrm.common.Constants;
import com.hrm.entity.BookingDayOff;
import com.hrm.entity.ConfigDayOff;
import com.hrm.entity.StandardTime;
import com.hrm.exception.RecordNotFoundException;
import com.hrm.model.response.employee.TimeKeepingResponse;
import com.hrm.model.response.employee.TimeKeepingSummary;
import com.hrm.model.response.employee.TimekeepingDetail;
import com.hrm.model.response.employee.mapping.TimekeepingResponseMapping;
import com.hrm.repository.BookingDayOffRepository;
import com.hrm.repository.ConfigDayOffRepository;
import com.hrm.repository.StandardTimeRepository;
import com.hrm.repository.TimeKeepingRepository;
import com.hrm.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class TimeKeepingService {

    @Autowired
    private TimeKeepingRepository timeKeepingRepository;

    @Autowired
    private StandardTimeRepository standardTimeRepository;

    @Autowired
    private BookingDayOffRepository bookingDayOffRepository;

    @Autowired
    private ConfigDayOffRepository configDayOffRepository;

//    @Autowired
//    private ApproveGeneralService approveService;

    private final int OFF_FULL_DAY = 1;
    private final int OFF_MORNING = 2;
    private final int OFF_AFTERNOON = 3;
    private final int LATE_MORNING = 4;
    private final int EARLY_MORNING = 5;
    private final int LATE_AFTERNOON = 6;
    private final int EARLY_AFTERNOON = 7;
    private final int REMOTE_FULL_DAY = 8;
    private final int REMOTE_MORNING = 9;
    private final int REMOTE_AFTERNOON = 10;

    private final int PERSONAL_FULL_DAY = 11;
    private final int PERSONAL_MORNING = 12;
    private final int PERSONAL_AFTERNOON = 13;
    private final int COMPENSATORY_FULL_DAY = 14;
    private final int COMPENSATORY_MORNING = 15;
    private final int COMPENSATORY_AFTERNOON = 16;

    private final int UNPAID_FULL_DAY = 21;
    private final int UNPAID_MORNING = 22;
    private final int UNPAID_AFTERNOON = 23;

    private final int AWAY_FROM_DESK = 24;

    private final int KEEPING_FORGET_IN = 17;
    private final int KEEPING_FORGET_OUT = 18;

    public TimeKeepingResponse getTimeKeepingByEmployee(String calculatorDate, Integer employeeId) {
        Calendar calendar = Calendar.getInstance();
        Date date = Utils.convertStringToDate(Constants.YYYY_MM_DD, calculatorDate);
        if (date == null) {
            throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
        }
        calendar.setTime(date);
        int calculatorMonth = calendar.get(Calendar.MONTH);
        calendar.set(Calendar.MONTH, calculatorMonth);
        calendar.set(Calendar.DATE, 1);
        String startDate = Utils.convertDateToString(Constants.YYYY_MM_DD, calendar.getTime());
        int maxDayInMonth = calendar.getActualMaximum(Calendar.DATE);
        calendar.set(Calendar.DATE, maxDayInMonth);
        String endDate = Utils.convertDateToString(Constants.YYYY_MM_DD, calendar.getTime());

        List<TimekeepingResponseMapping> timeKeepingList = timeKeepingRepository.findByEmployeeIdAndTime(employeeId,
                startDate, endDate);
        Optional<StandardTime> optionalStandardTime = standardTimeRepository.findTop1ByStandardTimeOrderByIdDesc();

        if (optionalStandardTime.isEmpty() || timeKeepingList.size() == 0) {
            return null;
        }
        List<ConfigDayOff> configDayOffList = configDayOffRepository.getConfigDayOffByTime(startDate, endDate);
        List<BookingDayOff> bookingDayOffList = bookingDayOffRepository.findByEmployeeIdAndConfirmAndTime(employeeId,
                Constants.STATUS_ACCEPT, startDate, endDate);

        // standard time
        String stringInAM = "1970-01-01 " + optionalStandardTime.get().getCheckInMorning().toString();
        String stringOutAM = "1970-01-01 " + optionalStandardTime.get().getCheckOutMorning().toString();
        String stringInPM = "1970-01-01 " + optionalStandardTime.get().getCheckInAfternoon().toString();
        String stringOutPM = "1970-01-01 " + optionalStandardTime.get().getCheckOutAfternoon().toString();

        calendar.setTime(Utils.convertStringToDate(Constants.YYYY_MM_DD_HH_MM_SS, stringInAM));
        int totalStandardCheckInMorning = getSecond(calendar);
        calendar.setTime(Utils.convertStringToDate(Constants.YYYY_MM_DD_HH_MM_SS, stringOutAM));
        int totalStandardCheckOutMorning = getSecond(calendar);
        calendar.setTime(Utils.convertStringToDate(Constants.YYYY_MM_DD_HH_MM_SS, stringInPM));
        int totalStandardCheckInAfternoon = getSecond(calendar);
        calendar.setTime(Utils.convertStringToDate(Constants.YYYY_MM_DD_HH_MM_SS, stringOutPM));
        int totalStandardCheckOutAfternoon = getSecond(calendar);
        int totalStandardTimeMorning = totalStandardCheckOutMorning - totalStandardCheckInMorning;
        int totalStandardTimeAfternoon = totalStandardCheckOutAfternoon - totalStandardCheckInAfternoon;

        Map<Integer, Map<Integer, Integer>> totalDayBookingDateAccept = calculatorBookingDateOffAccept(calendar,
                calculatorMonth, maxDayInMonth, totalStandardCheckInMorning, totalStandardCheckOutMorning,
                totalStandardCheckInAfternoon, totalStandardCheckOutAfternoon, bookingDayOffList, configDayOffList);
        List<TimekeepingDetail> timekeepingDetailList = new ArrayList<>();
        TimeKeepingResponse timeKeepingResponse = new TimeKeepingResponse();
        int totalTimeCheckInLateMorning;
        int totalTimeCheckOutEarlyMorning;
        int totalTimeCheckInLateAfternoon;
        int totalTimeCheckOutEarlyAfternoon;
        boolean checkWeekend;
        int countTotalDayLate = 0;
        float countTotalDayOffNoAccept = 0;
        float countTotalDayOffAccept = 0;
        float countTotalStandardWorkDay = 0;
        float countPersonalLeave = 0;
        float countCompensatoryLeave = 0;
        float countTotalRemoteTime = 0;
        int totalTimeLateByDay;
        int totalTimeLate = 0;
        int totalForgotTimeKeeping = 0;
        float TotalForgotTimeKeepingAccept = 0;
        int dateWorking;
        int totalTimeKeepingCheckIn;
        int totalTimeKeepingCheckOut;
        int totalAwayFromDesk = 0;

        // extra value of @Ngocmai requests: display all lateTime, permission and no
        // permission
        int totalLate = 0;
        int totalEarly = 0;
        int totalLateEarlyHourToday = 0;
        int totalAllLateEarlyHour = 0;

        // calculator time-keeping
        for (TimekeepingResponseMapping timeKeeping : timeKeepingList) {
            TimekeepingDetail timekeepingDetail = new TimekeepingDetail();
            totalTimeKeepingCheckIn = 0;
            totalTimeKeepingCheckOut = 0;
            totalTimeCheckInLateMorning = 0;
            totalTimeCheckOutEarlyMorning = 0;
            totalTimeCheckInLateAfternoon = 0;
            totalTimeCheckOutEarlyAfternoon = 0;
            totalTimeLateByDay = 0;
            totalLateEarlyHourToday = 0;
            int totalAwayFromDeskByday = 0;
            checkWeekend = false;

            calendar.setTime(timeKeeping.getDateWorking());
            dateWorking = calendar.get(Calendar.DATE);
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
                    || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                checkWeekend = true;
            }
// co phep
            if (totalDayBookingDateAccept.containsKey(dateWorking)) {
                if (!checkWeekend) {
                    timekeepingDetail.setStatus(Constants.STATUS_ACCEPT);
                    Map<Integer, Integer> getValue = totalDayBookingDateAccept.get(dateWorking);
                    // xin remote ca ngay
                    if (getValue.containsKey(REMOTE_FULL_DAY)
                            || (getValue.containsKey(REMOTE_MORNING) && getValue.containsKey(REMOTE_AFTERNOON))) {
                        countTotalStandardWorkDay++;
                        countTotalRemoteTime++;
                        // xin nghi ca ngay
                    } else if (getValue.containsKey(OFF_FULL_DAY)
                            || (getValue.containsKey(OFF_AFTERNOON) && getValue.containsKey(OFF_MORNING))) {
                        countTotalDayOffAccept++;
                    } else if (getValue.containsKey(PERSONAL_FULL_DAY)
                            || (getValue.containsKey(PERSONAL_AFTERNOON) && getValue.containsKey(PERSONAL_MORNING))) {
                        countPersonalLeave++;
                    } else if (getValue.containsKey(COMPENSATORY_FULL_DAY)
                            || (getValue.containsKey(COMPENSATORY_AFTERNOON)
                                    && getValue.containsKey(COMPENSATORY_MORNING))) {
                        countCompensatoryLeave++;
                    } else if (getValue.containsKey(UNPAID_FULL_DAY) || (getValue.containsKey(UNPAID_MORNING)
                            && getValue.containsKey(UNPAID_AFTERNOON) && getValue.containsKey(UNPAID_MORNING))) {
                    } else {
                        if (timeKeeping.getCheckIn() != null) {
                            calendar.setTime(timeKeeping.getCheckIn());
                            totalTimeKeepingCheckIn = getSecond(calendar);
                        }
                        if (getValue.containsKey(KEEPING_FORGET_IN)) {
                            totalTimeKeepingCheckIn = getValue.get(KEEPING_FORGET_IN);
                        }
                        if (timeKeeping.getCheckOut() != null) {
                            calendar.setTime(timeKeeping.getCheckOut());
                            totalTimeKeepingCheckOut = getSecond(calendar);
                        }
                        if (getValue.containsKey(KEEPING_FORGET_IN)) {
                            if (totalTimeKeepingCheckOut <= 0) {
                                totalTimeKeepingCheckOut = totalTimeKeepingCheckIn;
                            }
                            totalTimeKeepingCheckIn = getValue.get(KEEPING_FORGET_IN);
                        }
                        if (getValue.containsKey(KEEPING_FORGET_OUT)) {
                            totalTimeKeepingCheckOut = getValue.get(KEEPING_FORGET_OUT);
                        }
                        // 1: checkIn - CheckOut day du
                        if (totalTimeKeepingCheckIn > 0 && totalTimeKeepingCheckOut > 0) {
                            // 1.1 xin nghi sang
                            if (getValue.containsKey(OFF_MORNING) || getValue.containsKey(PERSONAL_MORNING)
                                    || getValue.containsKey(COMPENSATORY_MORNING)
                                    || getValue.containsKey(UNPAID_MORNING)) {
                                if (getValue.containsKey(OFF_MORNING)) {
                                    countTotalDayOffAccept += 0.5;
                                } else if (getValue.containsKey(PERSONAL_MORNING)) {
                                    countPersonalLeave += 0.5;
                                } else if (getValue.containsKey(COMPENSATORY_MORNING)) {
                                    countCompensatoryLeave += 0.5;
                                }
                                // ko xin nghi chieu
                                if (!(getValue.containsKey(OFF_AFTERNOON) || getValue.containsKey(PERSONAL_AFTERNOON)
                                        || getValue.containsKey(COMPENSATORY_AFTERNOON)
                                        || getValue.containsKey(UNPAID_AFTERNOON))) {
                                    if (totalTimeKeepingCheckOut <= totalStandardCheckInAfternoon) {
                                        totalTimeCheckOutEarlyAfternoon = totalStandardTimeAfternoon;
                                        countTotalDayOffNoAccept += 0.5;
                                    } else {
                                        countTotalStandardWorkDay += 0.5;
                                        // xin di muon chieu
                                        totalTimeCheckInLateAfternoon = calculatorTimeLate(getValue, LATE_AFTERNOON,
                                                totalTimeKeepingCheckIn, totalStandardCheckInAfternoon, 0, 0);
                                        totalLate = calculatorTimeLate(getValue, -1, totalTimeKeepingCheckIn,
                                                totalStandardCheckInAfternoon, 0, 0);
                                        // xin ve som chieu
                                        totalTimeCheckOutEarlyAfternoon = calculatorTimeLate(getValue, EARLY_AFTERNOON,
                                                totalStandardCheckOutAfternoon, totalTimeKeepingCheckOut, 0, 0);
                                        totalEarly = calculatorTimeLate(getValue, -1, totalStandardCheckOutAfternoon,
                                                totalTimeKeepingCheckOut, 0, 0);
                                    }
                                }
                                // remote chieu
                                if (getValue.containsKey(REMOTE_AFTERNOON)) {
                                    countTotalStandardWorkDay += 0.5;
                                    countTotalRemoteTime += 0.5;
                                }
                            }
                            // 1.2 xin nghi chieu
                            if (getValue.containsKey(OFF_AFTERNOON) || getValue.containsKey(PERSONAL_AFTERNOON)
                                    || getValue.containsKey(COMPENSATORY_AFTERNOON)
                                    || getValue.containsKey(UNPAID_AFTERNOON)) {
                                if (getValue.containsKey(OFF_AFTERNOON)) {
                                    countTotalDayOffAccept += 0.5;
                                } else if (getValue.containsKey(PERSONAL_AFTERNOON)) {
                                    countPersonalLeave += 0.5;
                                } else if (getValue.containsKey(COMPENSATORY_AFTERNOON)) {
                                    countCompensatoryLeave += 0.5;
                                }
                                // ko xin nghi sang
                                if (!(getValue.containsKey(OFF_MORNING) || getValue.containsKey(PERSONAL_MORNING)
                                        || getValue.containsKey(COMPENSATORY_MORNING)
                                        || getValue.containsKey(UNPAID_MORNING))) {
                                    if (totalTimeKeepingCheckIn >= totalStandardCheckOutMorning) {
                                        totalTimeCheckInLateMorning = totalStandardTimeMorning;
                                        countTotalDayOffNoAccept += 0.5;
                                    } else {
                                        countTotalStandardWorkDay += 0.5;
                                        // xin di muon sang
                                        totalTimeCheckInLateMorning = calculatorTimeLate(getValue, LATE_MORNING,
                                                totalTimeKeepingCheckIn, totalStandardCheckInMorning, 0, 0);
                                        totalLate = calculatorTimeLate(getValue, -1, totalTimeKeepingCheckIn,
                                                totalStandardCheckInMorning, 0, 0);
                                        // xin ve som sang
                                        totalTimeCheckOutEarlyMorning = calculatorTimeLate(getValue, EARLY_MORNING,
                                                totalStandardCheckOutMorning, totalTimeKeepingCheckOut, 0, 0);
                                        totalEarly = calculatorTimeLate(getValue, -1, totalStandardCheckOutMorning,
                                                totalTimeKeepingCheckOut, 0, 0);
                                    }
                                }
                                // remote sang
                                if (getValue.containsKey(REMOTE_MORNING)) {
                                    countTotalStandardWorkDay += 0.5;
                                    countTotalRemoteTime += 0.5;
                                }

                            }
                            // 1.3 remote sang va ko xin nghi chieu
                            if (getValue.containsKey(REMOTE_MORNING)
                                    && !(getValue.containsKey(OFF_AFTERNOON) || getValue.containsKey(PERSONAL_AFTERNOON)
                                            || getValue.containsKey(COMPENSATORY_AFTERNOON)
                                            || getValue.containsKey(UNPAID_AFTERNOON))) {
                                countTotalStandardWorkDay += 0.5;
                                countTotalRemoteTime += 0.5;
                                if (totalTimeKeepingCheckOut <= totalStandardCheckInAfternoon) {
                                    totalTimeCheckOutEarlyAfternoon = totalStandardTimeAfternoon;
                                    countTotalDayOffNoAccept += 0.5;
                                } else {
                                    countTotalStandardWorkDay += 0.5;
                                    // xin di muon chieu
                                    totalTimeCheckInLateAfternoon = calculatorTimeLate(getValue, LATE_AFTERNOON,
                                            totalTimeKeepingCheckIn, totalStandardCheckInAfternoon, 0, 0);
                                    totalLate = calculatorTimeLate(getValue, -1, totalTimeKeepingCheckIn,
                                            totalStandardCheckInAfternoon, 0, 0);
                                    // xin ve som chieu
                                    totalTimeCheckOutEarlyAfternoon = calculatorTimeLate(getValue, EARLY_AFTERNOON,
                                            totalStandardCheckOutAfternoon, totalTimeKeepingCheckOut, 0, 0);
                                    totalEarly = calculatorTimeLate(getValue, -1, totalStandardCheckOutAfternoon,
                                            totalTimeKeepingCheckOut, 0, 0);
                                }
                            }
                            // 1.4 remote chieu va ko xin nghi sang
                            if (getValue.containsKey(REMOTE_AFTERNOON)
                                    && !(getValue.containsKey(OFF_MORNING) || getValue.containsKey(PERSONAL_MORNING)
                                            || getValue.containsKey(COMPENSATORY_MORNING)
                                            || getValue.containsKey(UNPAID_MORNING))) {
                                countTotalStandardWorkDay += 0.5;
                                countTotalRemoteTime += 0.5;
                                if (totalTimeKeepingCheckIn >= totalStandardCheckOutMorning) {
                                    totalTimeCheckInLateMorning = totalStandardTimeMorning;
                                    countTotalDayOffNoAccept += 0.5;
                                } else {
                                    countTotalStandardWorkDay += 0.5;
                                    // xin di muon sang
                                    totalTimeCheckInLateMorning = calculatorTimeLate(getValue, LATE_MORNING,
                                            totalTimeKeepingCheckIn, totalStandardCheckInMorning, 0, 0);
                                    totalLate = calculatorTimeLate(getValue, -1, totalTimeKeepingCheckIn,
                                            totalStandardCheckInMorning, 0, 0);
                                    // xin ve som sang
                                    totalTimeCheckOutEarlyMorning = calculatorTimeLate(getValue, EARLY_MORNING,
                                            totalStandardCheckOutMorning, totalTimeKeepingCheckOut, 0, 0);
                                    totalEarly = calculatorTimeLate(getValue, -1, totalStandardCheckOutMorning,
                                            totalTimeKeepingCheckOut, 0, 0);
                                }
                            }
                            // 1.5 ko xin sang va chieu
                            if (!getValue.containsKey(OFF_AFTERNOON) && !getValue.containsKey(OFF_MORNING)
                                    && !getValue.containsKey(REMOTE_AFTERNOON) && !getValue.containsKey(REMOTE_MORNING)
                                    && !getValue.containsKey(UNPAID_AFTERNOON) && !getValue.containsKey(UNPAID_MORNING)
                                    && !getValue.containsKey(PERSONAL_AFTERNOON)
                                    && !getValue.containsKey(PERSONAL_MORNING)
                                    && !getValue.containsKey(COMPENSATORY_MORNING)
                                    && !getValue.containsKey(COMPENSATORY_AFTERNOON)) {
                                // xin di muon sang
                                if (totalTimeKeepingCheckIn < totalStandardCheckOutMorning) {
                                    countTotalStandardWorkDay += 0.5;
                                    totalTimeCheckInLateMorning = calculatorTimeLate(getValue, LATE_MORNING,
                                            totalTimeKeepingCheckIn, totalStandardCheckInMorning, 0, 0);
                                    totalLate = calculatorTimeLate(getValue, -1, totalTimeKeepingCheckIn,
                                            totalStandardCheckInMorning, 0, 0);
                                } else {
                                    countTotalDayOffNoAccept += 0.5;
                                    // xin di muon chieu
                                    totalTimeCheckInLateAfternoon = calculatorTimeLate(getValue, LATE_AFTERNOON,
                                            totalTimeKeepingCheckIn, totalStandardCheckInAfternoon,
                                            totalStandardTimeMorning, totalStandardTimeMorning);
                                    totalLate = calculatorTimeLate(getValue, -1, totalTimeKeepingCheckIn,
                                            totalStandardCheckInAfternoon, totalStandardTimeMorning,
                                            totalStandardTimeMorning);
                                }
                                if (totalTimeKeepingCheckOut < totalStandardCheckInAfternoon) {
                                    countTotalDayOffNoAccept += 0.5;
                                    totalTimeCheckOutEarlyMorning = calculatorTimeLate(getValue, EARLY_MORNING,
                                            totalStandardCheckOutMorning, totalTimeKeepingCheckOut,
                                            totalStandardTimeAfternoon, totalStandardTimeAfternoon);
                                    totalEarly = calculatorTimeLate(getValue, -1, totalStandardCheckOutMorning,
                                            totalTimeKeepingCheckOut, totalStandardTimeAfternoon,
                                            totalStandardTimeAfternoon);
                                } else {
                                    countTotalStandardWorkDay += 0.5;
                                    totalTimeCheckOutEarlyAfternoon = calculatorTimeLate(getValue, EARLY_AFTERNOON,
                                            totalStandardCheckOutAfternoon, totalTimeKeepingCheckOut, 0, 0);
                                    totalEarly = calculatorTimeLate(getValue, -1, totalStandardCheckOutAfternoon,
                                            totalTimeKeepingCheckOut, 0, 0);
                                }
                            }
                            totalTimeLateByDay = totalTimeCheckInLateMorning + totalTimeCheckOutEarlyMorning
                                    + totalTimeCheckInLateAfternoon + totalTimeCheckOutEarlyAfternoon;
                            totalLateEarlyHourToday += totalLate + totalEarly;
                            // 2: checkIn - not checkOut
                        } else if (totalTimeKeepingCheckIn > 0 && totalTimeKeepingCheckOut <= 0) {
                            // 2.1 xin nghi sang
                            if (getValue.containsKey(OFF_MORNING) || getValue.containsKey(PERSONAL_MORNING)
                                    || getValue.containsKey(COMPENSATORY_MORNING)
                                    || getValue.containsKey(UNPAID_MORNING)) {
                                if (getValue.containsKey(OFF_MORNING)) {
                                    countTotalDayOffAccept += 0.5;
                                } else if (getValue.containsKey(PERSONAL_MORNING)) {
                                    countPersonalLeave += 0.5;
                                } else if (getValue.containsKey(COMPENSATORY_MORNING)) {
                                    countCompensatoryLeave += 0.5;
                                }
                                // di lam chieu
                                if (!(getValue.containsKey(OFF_AFTERNOON) && getValue.containsKey(PERSONAL_AFTERNOON)
                                        && getValue.containsKey(COMPENSATORY_AFTERNOON)
                                        && getValue.containsKey(UNPAID_AFTERNOON))) {
                                    if (totalTimeKeepingCheckIn <= totalStandardCheckInAfternoon) {
                                        totalTimeCheckOutEarlyAfternoon = totalStandardTimeAfternoon;
                                        countTotalDayOffNoAccept += 0.5;
                                    } else {
                                        countTotalStandardWorkDay += 0.5;
                                        // xin di muon chieu
                                        totalForgotTimeKeeping++;
                                        totalTimeCheckInLateAfternoon = calculatorTimeLate(getValue, LATE_AFTERNOON,
                                                totalTimeKeepingCheckIn, totalStandardCheckInAfternoon, 0, 0);
                                        totalLate = calculatorTimeLate(getValue, -1, totalTimeKeepingCheckIn,
                                                totalStandardCheckInAfternoon, 0, 0);
                                    }
                                }
                                // remote chieu
                                if (getValue.containsKey(REMOTE_AFTERNOON)) {
                                    countTotalStandardWorkDay += 0.5;
                                    countTotalRemoteTime += 0.5;
                                }
                            }
                            // 2.2 xin nghi chieu
                            if (getValue.containsKey(OFF_AFTERNOON) || getValue.containsKey(PERSONAL_AFTERNOON)
                                    || getValue.containsKey(COMPENSATORY_AFTERNOON)
                                    || getValue.containsKey(UNPAID_AFTERNOON)) {
                                if (getValue.containsKey(OFF_AFTERNOON)) {
                                    countTotalDayOffAccept += 0.5;
                                } else if (getValue.containsKey(PERSONAL_AFTERNOON)) {
                                    countPersonalLeave += 0.5;
                                } else if (getValue.containsKey(COMPENSATORY_AFTERNOON)) {
                                    countCompensatoryLeave += 0.5;
                                }
                                // di lam sang
                                if (!(getValue.containsKey(OFF_MORNING) && getValue.containsKey(PERSONAL_MORNING)
                                        && getValue.containsKey(COMPENSATORY_MORNING)
                                        && getValue.containsKey(UNPAID_MORNING))) {
                                    if (totalTimeKeepingCheckIn >= totalStandardCheckOutMorning) {
                                        totalTimeCheckInLateMorning = totalStandardTimeMorning;
                                        countTotalDayOffNoAccept += 0.5;
                                        // xin di muon sang
                                        totalForgotTimeKeeping++;
                                        totalTimeCheckInLateMorning = calculatorTimeLate(getValue, LATE_MORNING,
                                                totalTimeKeepingCheckIn, totalStandardCheckInMorning, 0, 0);
                                        totalLate = calculatorTimeLate(getValue, -1, totalTimeKeepingCheckIn,
                                                totalStandardCheckInMorning, 0, 0);
                                    }
                                }
                                // remote sang
                                if (getValue.containsKey(REMOTE_MORNING)) {
                                    countTotalStandardWorkDay += 0.5;
                                    countTotalRemoteTime += 0.5;
                                }
                            }
                            // 2.3 remote sang va ko xin nghi chieu
                            if (getValue.containsKey(REMOTE_MORNING)
                                    && !(getValue.containsKey(OFF_AFTERNOON) || getValue.containsKey(PERSONAL_AFTERNOON)
                                            || getValue.containsKey(COMPENSATORY_AFTERNOON)
                                            || getValue.containsKey(UNPAID_AFTERNOON))) {
                                countTotalStandardWorkDay += 0.5;
                                countTotalRemoteTime += 0.5;
                                if (totalTimeKeepingCheckOut <= totalStandardCheckInAfternoon) {
                                    totalTimeCheckOutEarlyAfternoon = totalStandardTimeAfternoon;
                                    countTotalDayOffNoAccept += 0.5;
                                } else {
                                    countTotalStandardWorkDay += 0.5;
                                    totalForgotTimeKeeping++;
                                    totalTimeCheckInLateAfternoon = calculatorTimeLate(getValue, LATE_AFTERNOON,
                                            totalTimeKeepingCheckIn, totalStandardCheckInAfternoon, 0, 0);
                                    totalLate = calculatorTimeLate(getValue, -1, totalTimeKeepingCheckIn,
                                            totalStandardCheckInAfternoon, 0, 0);
                                    // xin di muon chieu
                                }
                            }
                            // 2.4 remote chieu va ko xin nghi sang
                            if (getValue.containsKey(REMOTE_AFTERNOON)
                                    && !(getValue.containsKey(OFF_MORNING) || getValue.containsKey(PERSONAL_MORNING)
                                            || getValue.containsKey(COMPENSATORY_MORNING)
                                            || getValue.containsKey(UNPAID_MORNING))) {
                                countTotalStandardWorkDay += 0.5;
                                countTotalRemoteTime += 0.5;
                                if (totalTimeKeepingCheckIn >= totalStandardCheckOutMorning) {
                                    totalTimeCheckInLateMorning = totalStandardTimeMorning;
                                    countTotalDayOffNoAccept += 0.5;
                                } else {
                                    countTotalStandardWorkDay += 0.5;
                                    // xin di muon sang
                                    totalForgotTimeKeeping++;
                                    totalTimeCheckInLateMorning = calculatorTimeLate(getValue, LATE_MORNING,
                                            totalTimeKeepingCheckIn, totalStandardCheckInMorning, 0, 0);
                                    totalLate = calculatorTimeLate(getValue, -1, totalTimeKeepingCheckIn,
                                            totalStandardCheckInMorning, 0, 0);
                                }
                            }
                            // 2.5 ko xin sang va chieu
                            if (!getValue.containsKey(OFF_AFTERNOON) && !getValue.containsKey(OFF_MORNING)
                                    && !getValue.containsKey(REMOTE_AFTERNOON) && !getValue.containsKey(REMOTE_MORNING)
                                    && !getValue.containsKey(PERSONAL_AFTERNOON)
                                    && !getValue.containsKey(PERSONAL_MORNING)
                                    && !getValue.containsKey(COMPENSATORY_MORNING)
                                    && !getValue.containsKey(COMPENSATORY_AFTERNOON)
                                    && !getValue.containsKey(UNPAID_AFTERNOON)
                                    && !getValue.containsKey(UNPAID_MORNING)) {
                                countTotalStandardWorkDay += 0.5;
                                if (totalTimeKeepingCheckIn < totalStandardCheckOutMorning) {
                                    countTotalDayOffNoAccept += 0.5;
                                    totalTimeCheckInLateMorning = calculatorTimeLate(getValue, LATE_MORNING,
                                            totalTimeKeepingCheckIn, totalStandardCheckInMorning, 0, 0);
                                    totalLate = calculatorTimeLate(getValue, -1, totalTimeKeepingCheckIn,
                                            totalStandardCheckInMorning, 0, 0);
                                }
                                if (totalTimeKeepingCheckIn > totalStandardCheckOutMorning) {
                                    countTotalDayOffNoAccept += 0.5;
                                    // xin di muon chieu
                                    totalTimeCheckInLateAfternoon = calculatorTimeLate(getValue, LATE_AFTERNOON,
                                            totalTimeKeepingCheckIn, totalStandardCheckInAfternoon,
                                            totalStandardTimeMorning, totalStandardTimeMorning);
//                                	}
                                    totalLate = calculatorTimeLate(getValue, -1, totalTimeKeepingCheckIn,
                                            totalStandardCheckInAfternoon, totalStandardTimeMorning,
                                            totalStandardTimeMorning);
//                                	}
                                }
                            }
                            totalTimeLateByDay = totalTimeCheckInLateMorning + totalTimeCheckOutEarlyMorning
                                    + totalTimeCheckInLateAfternoon + totalTimeCheckOutEarlyAfternoon;
                            totalLateEarlyHourToday = totalLate + totalEarly;
                            // 3: khong checkIn và checkOut
                        } else {
                            // 3.1 xin nghi sang
                            if (getValue.containsKey(OFF_MORNING) || getValue.containsKey(PERSONAL_MORNING)
                                    || getValue.containsKey(COMPENSATORY_MORNING)
                                    || getValue.containsKey(UNPAID_MORNING)) {
                                // di lam chieu
                                if (getValue.containsKey(OFF_MORNING)) {
                                    countTotalDayOffAccept += 0.5;
                                } else if (getValue.containsKey(PERSONAL_MORNING)) {
                                    countPersonalLeave += 0.5;
                                } else if (getValue.containsKey(COMPENSATORY_MORNING)) {
                                    countCompensatoryLeave += 0.5;
                                }
                                if (!(getValue.containsKey(OFF_AFTERNOON) || getValue.containsKey(PERSONAL_AFTERNOON)
                                        || getValue.containsKey(COMPENSATORY_AFTERNOON)
                                        || getValue.containsKey(UNPAID_AFTERNOON)
                                        || getValue.containsKey(REMOTE_AFTERNOON))) {
                                    countTotalDayOffNoAccept += 0.5;
                                } // remote chieu
                                if (getValue.containsKey(REMOTE_AFTERNOON)) {
                                    countTotalStandardWorkDay += 0.5;
                                    countTotalRemoteTime += 0.5;
                                }
                            }
                            // 3.2 xin nghi chieu
                            if (getValue.containsKey(OFF_AFTERNOON) || getValue.containsKey(PERSONAL_AFTERNOON)
                                    || getValue.containsKey(COMPENSATORY_AFTERNOON)
                                    || getValue.containsKey(UNPAID_AFTERNOON)) {
                                // di lam sang
                                if (getValue.containsKey(OFF_AFTERNOON)) {
                                    countTotalDayOffAccept += 0.5;
                                } else if (getValue.containsKey(PERSONAL_AFTERNOON)) {
                                    countPersonalLeave += 0.5;
                                } else if (getValue.containsKey(COMPENSATORY_AFTERNOON)) {
                                    countCompensatoryLeave += 0.5;
                                }
                                if (!(getValue.containsKey(OFF_MORNING) || getValue.containsKey(PERSONAL_MORNING)
                                        || getValue.containsKey(COMPENSATORY_MORNING)
                                        || getValue.containsKey(UNPAID_MORNING)
                                        || getValue.containsKey(REMOTE_MORNING))) {
                                    countTotalDayOffNoAccept += 0.5;
                                } // remote sang
                                if (getValue.containsKey(REMOTE_MORNING)) {
                                    countTotalStandardWorkDay += 0.5;
                                    countTotalRemoteTime += 0.5;
                                }
                            }
                            // 3.3 remote sang va ko xin nghi chieu
                            if (getValue.containsKey(REMOTE_MORNING)
                                    && !(getValue.containsKey(OFF_AFTERNOON) || getValue.containsKey(PERSONAL_AFTERNOON)
                                            || getValue.containsKey(COMPENSATORY_AFTERNOON)
                                            || getValue.containsKey(UNPAID_AFTERNOON))) {
                                countTotalStandardWorkDay += 0.5;
                                countTotalRemoteTime += 0.5;
                                countTotalDayOffNoAccept += 0.5;
                                totalTimeCheckInLateAfternoon = totalStandardTimeAfternoon;
                            }
                            // 3.4 remote chieu va ko xin nghi sang
                            if (getValue.containsKey(REMOTE_AFTERNOON)
                                    && !(getValue.containsKey(OFF_MORNING) || getValue.containsKey(PERSONAL_MORNING)
                                            || getValue.containsKey(COMPENSATORY_MORNING)
                                            || getValue.containsKey(UNPAID_MORNING))) {
                                countTotalStandardWorkDay += 0.5;
                                countTotalRemoteTime += 0.5;
                                countTotalDayOffNoAccept += 0.5;
                                totalTimeCheckInLateMorning = totalStandardTimeMorning;
                            } else if (!getValue.containsKey(OFF_AFTERNOON) && !getValue.containsKey(OFF_MORNING)
                                    && !getValue.containsKey(REMOTE_AFTERNOON) && !getValue.containsKey(REMOTE_MORNING)
                                    && !getValue.containsKey(PERSONAL_AFTERNOON)
                                    && !getValue.containsKey(PERSONAL_MORNING)
                                    && !getValue.containsKey(COMPENSATORY_MORNING)
                                    && !getValue.containsKey(COMPENSATORY_AFTERNOON)
                                    && !getValue.containsKey(UNPAID_AFTERNOON)
                                    && !getValue.containsKey(UNPAID_MORNING)) {
                                countTotalDayOffNoAccept++;
                                totalTimeCheckInLateMorning = totalStandardTimeAfternoon + totalStandardTimeMorning;
                                timekeepingDetail.setStatus(Constants.STATUS_NONE_ACCEPT);
                            }
                            totalTimeLateByDay = totalTimeCheckInLateAfternoon + totalTimeCheckInLateMorning;
                            totalLateEarlyHourToday += totalTimeLateByDay;
                        }
                        totalForgotTimeKeeping += (getValue.containsKey(KEEPING_FORGET_IN) ? 1 : 0)
                                + (getValue.containsKey(KEEPING_FORGET_OUT) ? 1 : 0);
                    }
                    if (getValue.containsKey(AWAY_FROM_DESK)) {
                        totalAwayFromDeskByday = getValue.get(AWAY_FROM_DESK);
                    }
                }

// Khong phep
            } else {
                if (!checkWeekend) {
                    timekeepingDetail.setStatus(Constants.STATUS_NONE_ACCEPT);
                    if (timeKeeping.getCheckIn() != null) {
                        calendar.setTime(timeKeeping.getCheckIn());
                        totalTimeKeepingCheckIn = getSecond(calendar);

                        if (timeKeeping.getCheckOut() != null) {
                            calendar.setTime(timeKeeping.getCheckOut());
                            totalTimeKeepingCheckOut = getSecond(calendar);
                            if (totalTimeKeepingCheckIn <= totalStandardCheckOutMorning) {
                                countTotalStandardWorkDay += 0.5;
                                totalTimeCheckInLateMorning = Math
                                        .max((totalTimeKeepingCheckIn - totalStandardCheckInMorning), 0);
                            } else {
                                countTotalStandardWorkDay += 0.5;
                                totalTimeCheckInLateAfternoon = Math
                                        .max((totalTimeKeepingCheckIn - totalStandardCheckInAfternoon), 0)
                                        + totalStandardTimeMorning;
                            }
                            if (totalTimeKeepingCheckOut <= totalStandardCheckInAfternoon) {
                                countTotalStandardWorkDay += 0.5;
//								countTotalDayOffNoAccept += 0.5;
                                totalTimeCheckOutEarlyMorning = Math
                                        .max((totalStandardCheckOutMorning - totalTimeKeepingCheckOut), 0)
                                        + totalStandardTimeAfternoon;
                            } else {
                                countTotalStandardWorkDay += 0.5;
                                totalTimeCheckOutEarlyAfternoon = Math
                                        .max((totalStandardCheckOutAfternoon - totalTimeKeepingCheckOut), 0);
                            }
                            totalTimeLateByDay = totalTimeCheckInLateMorning + totalTimeCheckInLateAfternoon
                                    + totalTimeCheckOutEarlyMorning + totalTimeCheckOutEarlyAfternoon;
                            totalLateEarlyHourToday = totalTimeLateByDay;
                        } else {
                            if (totalTimeKeepingCheckIn < totalStandardCheckOutMorning) {
                                totalForgotTimeKeeping += 1;
//                                countTotalStandardWorkDay+=0.5;
                                totalTimeCheckInLateMorning = Math
                                        .max((totalTimeKeepingCheckIn - totalStandardCheckInMorning), 0);
                            } else {
//								countTotalStandardWorkDay += 0.5;
//								countTotalDayOffNoAccept += 0.5;
                                totalForgotTimeKeeping += 1;
                                totalTimeCheckInLateAfternoon = Math
                                        .max((totalTimeKeepingCheckIn - totalStandardCheckInAfternoon), 0)
                                        + totalStandardTimeMorning;
                            }
                            totalTimeLateByDay = totalTimeCheckInLateMorning + totalTimeCheckInLateAfternoon;
                            totalLateEarlyHourToday = totalTimeLateByDay;
                        }
                    } else {
                        countTotalDayOffNoAccept++;
                    }
                }
            }
            // none weekend
            if (!checkWeekend) {
                if (totalTimeLateByDay > 0
                        && (totalTimeLateByDay != totalStandardTimeAfternoon + totalStandardTimeMorning)) {
                    countTotalDayLate++;
                }
                totalTimeLate += totalTimeLateByDay;
                totalAllLateEarlyHour += totalLateEarlyHourToday;
                totalAwayFromDesk += totalAwayFromDeskByday;
            }
            timekeepingDetail.setCheckIn(timeKeeping.getCheckIn() == null ? "" : timeKeeping.getCheckIn().toString());
            timekeepingDetail
                    .setCheckOut(timeKeeping.getCheckOut() == null ? "" : timeKeeping.getCheckOut().toString());
            timekeepingDetail.setDateWorking(
                    timeKeeping.getDateWorking() == null ? "" : timeKeeping.getDateWorking().toString());
            timekeepingDetail.setCheckDayOff(checkWeekend);
            timekeepingDetail.setTotalTimeLateByDay(Utils.convertSecondsToDate(totalTimeLateByDay));
            timekeepingDetailList.add(timekeepingDetail);
        }
        timeKeepingResponse.setTimekeepingDetail(timekeepingDetailList);

        timeKeepingResponse.setTimeKeepingSummary(new TimeKeepingSummary(Utils.convertSecondsToDate(totalTimeLate),
                Utils.convertSecondsToDate(totalAllLateEarlyHour + totalAwayFromDesk),
                (float) (totalAllLateEarlyHour + totalAwayFromDesk), countTotalDayLate, countTotalDayOffNoAccept,
                countTotalDayOffAccept, countTotalStandardWorkDay, totalForgotTimeKeeping, countPersonalLeave,
                countCompensatoryLeave, (float) totalTimeLate, Utils.convertSecondsToDate(totalAwayFromDesk),
                TotalForgotTimeKeepingAccept, countTotalRemoteTime));
        return timeKeepingResponse;
    }

    public Map<Integer, Map<Integer, Integer>> calculatorBookingDateOffAccept(Calendar calendar, int currentMonth,
            int maxDayInMonth, int totalStandardCheckInMorning, int totalStandardCheckOutMorning,
            int totalStandardCheckInAfternoon, int totalStandardCheckOutAfternoon,
            List<BookingDayOff> bookingDayOffList, List<ConfigDayOff> configDayOffList) {

        Map<Integer, Map<Integer, Integer>> checkBookingDateAccept = new HashMap<>();
        int totalStandardTimeMorning = totalStandardCheckOutMorning - totalStandardCheckInMorning;
        int totalStandardTimeAfternoon = totalStandardCheckOutAfternoon - totalStandardCheckInAfternoon;
        int dateStartAccept;
        int dateBackAccept;
        int totalTimeCheckInLate;
        int totalTimeCheckOutEarly;
        for (BookingDayOff bookingDayOff : bookingDayOffList) {
            // time back
            calendar.setTime(bookingDayOff.getBackDay());
            int backDaySecond = getSecond(calendar);
            int backMonth = calendar.get(Calendar.MONTH);
            int backDate = calendar.get(Calendar.DATE);
            // time request
            calendar.setTime(bookingDayOff.getRequestDay());
            int requestDaySecond = getSecond(calendar);
            int requestMonth = calendar.get(Calendar.MONTH);
            int requestDate = calendar.get(Calendar.DATE);
            // di muon - ve som
            if (bookingDayOff.getStatus() == OFF_FULL_DAY) {
                totalTimeCheckInLate = 0;
                totalTimeCheckOutEarly = 0;
                Map<Integer, Integer> getValue = checkBookingDateAccept.get(requestDate);
                boolean checkBookingFullDay = false;
                boolean checkBookingAM = false;
                boolean checkBookingPM = false;
                if (getValue != null) {
                    if (getValue.containsKey(OFF_FULL_DAY)) {
                        checkBookingFullDay = true;
                    }
                    if (getValue.containsKey(OFF_MORNING)) {
                        checkBookingAM = true;
                    }
                    if (getValue.containsKey(OFF_AFTERNOON)) {
                        checkBookingPM = true;
                    }
                }
                // ko xin nghi sang
                if (!checkBookingAM || !checkBookingFullDay) {
                    if (requestDaySecond < totalStandardCheckOutMorning
                            && backDaySecond <= totalStandardCheckInAfternoon) {
                        totalTimeCheckOutEarly = Math.max((totalStandardCheckOutMorning - requestDaySecond), 0);
                        totalTimeCheckInLate = Math.max((backDaySecond - totalStandardCheckInMorning), 0);
                        if (totalTimeCheckInLate > 0) {
                            Map<Integer, Integer> value = new HashMap<>();
                            if (checkBookingDateAccept.get(requestDate) != null) {
                                value = checkBookingDateAccept.get(requestDate);
                            }
                            value.put(LATE_MORNING, totalTimeCheckInLate);
                            checkBookingDateAccept.put(requestDate, value);
                        }
                        if (totalTimeCheckOutEarly > 0) {
                            Map<Integer, Integer> value = new HashMap<>();
                            if (checkBookingDateAccept.get(requestDate) != null) {
                                value = checkBookingDateAccept.get(requestDate);
                            }
                            value.put(EARLY_MORNING, totalTimeCheckOutEarly);
                            checkBookingDateAccept.put(requestDate, value);
                        }
                    }
                }
                // ko xin nghi chieu
                if (!checkBookingPM || !checkBookingFullDay) {
                    if (requestDaySecond >= totalStandardCheckOutMorning
                            && backDaySecond <= totalStandardCheckOutAfternoon) {
                        totalTimeCheckOutEarly = Math.max((totalStandardCheckOutAfternoon - requestDaySecond), 0);
                        totalTimeCheckInLate = Math.max((backDaySecond - totalStandardCheckInAfternoon), 0);
                        if (totalTimeCheckInLate > 0) {
                            Map<Integer, Integer> value = new HashMap<>();
                            if (checkBookingDateAccept.get(requestDate) != null) {
                                value = checkBookingDateAccept.get(requestDate);
                            }

                            value.put(LATE_AFTERNOON, totalTimeCheckInLate);
                            checkBookingDateAccept.put(requestDate, value);
                        }
                        if (totalTimeCheckOutEarly > 0) {
                            Map<Integer, Integer> value = new HashMap<>();
                            if (checkBookingDateAccept.get(requestDate) != null) {
                                value = checkBookingDateAccept.get(requestDate);
                            }

                            value.put(EARLY_AFTERNOON, totalTimeCheckOutEarly);
                            checkBookingDateAccept.put(requestDate, value);
                        }
                    }
                }
            }
            // nghi phep nhieu ngay
            else if (bookingDayOff.getStatus() == 0 || bookingDayOff.getStatus() == 5 || bookingDayOff.getStatus() == 6
                    || bookingDayOff.getStatus() == 7) {
                if (currentMonth == requestMonth) {
                    dateStartAccept = requestDate;
                } else {
                    dateStartAccept = 1;// min day in month
                }
                calendar.setTime(bookingDayOff.getBackDay());
                if (currentMonth == backMonth) {
                    dateBackAccept = backDate;
                } else {
                    dateBackAccept = maxDayInMonth; // max day in month
                }
                if (dateStartAccept == dateBackAccept) {
                    // nghi sang
                    if (requestDaySecond <= totalStandardCheckOutMorning
                            && backDaySecond <= totalStandardCheckInAfternoon) {
                        Map<Integer, Integer> value = new HashMap<>();
                        if (checkBookingDateAccept.get(dateStartAccept) != null) {
                            value = checkBookingDateAccept.get(dateStartAccept);
                        }
                        if (bookingDayOff.getStatus() == 0) {
                            value.put(OFF_MORNING, totalStandardTimeMorning);
                        } else if (bookingDayOff.getStatus() == 5) {
                            value.put(PERSONAL_MORNING, totalStandardTimeMorning);
                        } else if (bookingDayOff.getStatus() == 6) {
                            value.put(COMPENSATORY_MORNING, totalStandardTimeMorning);
                        } else if (bookingDayOff.getStatus() == 7) {
                            value.put(UNPAID_MORNING, totalStandardTimeMorning);
                        }
                        checkBookingDateAccept.put(dateStartAccept, value);
                        // nghi chieu
                    } else if (requestDaySecond >= totalStandardCheckOutMorning
                            && backDaySecond <= totalStandardCheckOutAfternoon) {
                        Map<Integer, Integer> value = new HashMap<>();
                        if (checkBookingDateAccept.get(dateStartAccept) != null) {
                            value = checkBookingDateAccept.get(dateStartAccept);
                        }
                        if (bookingDayOff.getStatus() == 0) {
                            value.put(OFF_AFTERNOON, totalStandardTimeAfternoon);
                        } else if (bookingDayOff.getStatus() == 5) {
                            value.put(PERSONAL_AFTERNOON, totalStandardTimeAfternoon);
                        } else if (bookingDayOff.getStatus() == 6) {
                            value.put(COMPENSATORY_AFTERNOON, totalStandardTimeAfternoon);
                        } else if (bookingDayOff.getStatus() == 7) {
                            value.put(UNPAID_AFTERNOON, totalStandardTimeAfternoon);
                        }
                        checkBookingDateAccept.put(dateStartAccept, value);
                        // nghi ca ngay
                    } else if (requestDaySecond <= totalStandardCheckOutMorning
                            && backDaySecond <= totalStandardCheckOutAfternoon) {
                        checkBookingDateAccept.remove(dateStartAccept);
                        Map<Integer, Integer> value = new HashMap<>();
                        if (checkBookingDateAccept.get(dateStartAccept) != null) {
                            value = checkBookingDateAccept.get(dateStartAccept);
                        }
                        if (bookingDayOff.getStatus() == 0) {
                            value.put(OFF_FULL_DAY, totalStandardTimeAfternoon + totalStandardTimeMorning);
                        } else if (bookingDayOff.getStatus() == 5) {
                            value.put(PERSONAL_FULL_DAY, totalStandardTimeAfternoon + totalStandardTimeMorning);
                        } else if (bookingDayOff.getStatus() == 6) {
                            value.put(COMPENSATORY_FULL_DAY, totalStandardTimeAfternoon + totalStandardTimeMorning);
                        } else if (bookingDayOff.getStatus() == 7) {
                            value.put(UNPAID_FULL_DAY, totalStandardTimeAfternoon + totalStandardTimeMorning);
                        }
                        checkBookingDateAccept.put(dateStartAccept, value);
                    }
                    // nghi khac ngay
                } else {
                    // time dang ky < 12h -> nghi ca nay
                    if (requestDaySecond < totalStandardCheckOutMorning) {
                        checkBookingDateAccept.remove(dateStartAccept);
                        Map<Integer, Integer> value = new HashMap<>();
                        if (checkBookingDateAccept.get(dateStartAccept) != null) {
                            value = checkBookingDateAccept.get(dateStartAccept);
                        }
                        if (bookingDayOff.getStatus() == 0) {
                            value.put(OFF_FULL_DAY, totalStandardTimeAfternoon + totalStandardTimeMorning);
                        } else if (bookingDayOff.getStatus() == 5) {
                            value.put(PERSONAL_FULL_DAY, totalStandardTimeAfternoon + totalStandardTimeMorning);
                        } else if (bookingDayOff.getStatus() == 6) {
                            value.put(COMPENSATORY_FULL_DAY, totalStandardTimeAfternoon + totalStandardTimeMorning);
                        } else if (bookingDayOff.getStatus() == 7) {
                            value.put(UNPAID_FULL_DAY, totalStandardTimeAfternoon + totalStandardTimeMorning);
                        }
                        checkBookingDateAccept.put(dateStartAccept, value);
                    } else {
                        Map<Integer, Integer> value = new HashMap<>();
                        if (checkBookingDateAccept.get(dateStartAccept) != null) {
                            value = checkBookingDateAccept.get(dateStartAccept);
                        }
                        if (bookingDayOff.getStatus() == 0) {
                            value.put(OFF_AFTERNOON, totalStandardTimeAfternoon);
                        } else if (bookingDayOff.getStatus() == 5) {
                            value.put(PERSONAL_AFTERNOON, totalStandardTimeAfternoon);
                        } else if (bookingDayOff.getStatus() == 6) {
                            value.put(COMPENSATORY_AFTERNOON, totalStandardTimeAfternoon);
                        } else if (bookingDayOff.getStatus() == 7) {
                            value.put(UNPAID_FULL_DAY, totalStandardTimeAfternoon);
                        }
                        checkBookingDateAccept.put(dateStartAccept, value);
                    }
                    // time tro lai > 8h30 && <=13h30 -> nghi sang
                    if (backDaySecond > totalStandardCheckInMorning && backDaySecond <= totalStandardCheckInAfternoon) {
                        Map<Integer, Integer> value = new HashMap<>();
                        if (checkBookingDateAccept.get(dateBackAccept) != null) {
                            value = checkBookingDateAccept.get(dateBackAccept);
                        }
                        if (bookingDayOff.getStatus() == 0) {
                            value.put(OFF_MORNING, totalStandardTimeMorning);
                        } else if (bookingDayOff.getStatus() == 5) {
                            value.put(PERSONAL_MORNING, totalStandardTimeMorning);
                        } else if (bookingDayOff.getStatus() == 6) {
                            value.put(COMPENSATORY_MORNING, totalStandardTimeMorning);
                        } else if (bookingDayOff.getStatus() == 7) {
                            value.put(UNPAID_FULL_DAY, totalStandardTimeMorning);
                        }
                        checkBookingDateAccept.put(dateBackAccept, value);
                    } else if (backDaySecond >= totalStandardCheckInAfternoon) {
                        checkBookingDateAccept.remove(dateBackAccept);
                        Map<Integer, Integer> value = new HashMap<>();
                        if (checkBookingDateAccept.get(dateBackAccept) != null) {
                            value = checkBookingDateAccept.get(dateBackAccept);
                        }
                        if (bookingDayOff.getStatus() == 0) {
                            value.put(OFF_FULL_DAY, totalStandardTimeAfternoon + totalStandardTimeMorning);
                        } else if (bookingDayOff.getStatus() == 5) {
                            value.put(PERSONAL_FULL_DAY, totalStandardTimeAfternoon + totalStandardTimeMorning);
                        } else if (bookingDayOff.getStatus() == 6) {
                            value.put(COMPENSATORY_FULL_DAY, totalStandardTimeAfternoon + totalStandardTimeMorning);
                        } else if (bookingDayOff.getStatus() == 7) {
                            value.put(UNPAID_FULL_DAY, totalStandardTimeAfternoon + totalStandardTimeMorning);
                        }
                        checkBookingDateAccept.put(dateBackAccept, value);
                    }
                    for (int i = (dateStartAccept + 1); i < dateBackAccept; i++) {
                        checkBookingDateAccept.remove(i);
                        Map<Integer, Integer> value = new HashMap<>();
                        if (bookingDayOff.getStatus() == 0) {
                            value.put(OFF_FULL_DAY, totalStandardTimeAfternoon + totalStandardTimeMorning);
                        } else if (bookingDayOff.getStatus() == 5) {
                            value.put(PERSONAL_FULL_DAY, totalStandardTimeAfternoon + totalStandardTimeMorning);
                        } else if (bookingDayOff.getStatus() == 6) {
                            value.put(COMPENSATORY_FULL_DAY, totalStandardTimeAfternoon + totalStandardTimeMorning);
                        } else if (bookingDayOff.getStatus() == 7) {
                            value.put(UNPAID_FULL_DAY, totalStandardTimeAfternoon + totalStandardTimeMorning);
                        }
                        checkBookingDateAccept.put(i, value);
                    }
                }
            } else if (bookingDayOff.getStatus() == 8) {
                dateStartAccept = requestDate > 0 ? requestDate : backDate;
//				checkBookingDateAccept.remove(dateStartAccept);
                Map<Integer, Integer> value = new HashMap<>();
                if (checkBookingDateAccept.get(dateStartAccept) != null) {
                    value = checkBookingDateAccept.get(dateStartAccept);
                }
                if (requestDaySecond > 0) {
                    value.put(KEEPING_FORGET_IN, requestDaySecond);
                }
                if (backDaySecond > 0) {
                    value.put(KEEPING_FORGET_OUT, backDaySecond);
                }
                checkBookingDateAccept.put(dateStartAccept, value);
//                }
            } else if (bookingDayOff.getStatus() == 2) {
                if (currentMonth == requestMonth) {
                    dateStartAccept = requestDate;
                } else {
                    dateStartAccept = 1;// min day in month
                }
                if (currentMonth == backMonth) {
                    dateBackAccept = backDate;
                } else {
                    dateBackAccept = maxDayInMonth; // max day in month
                }
                // remote cung ngay
                if (dateStartAccept == dateBackAccept) {
                    // remote sang
                    if (requestDaySecond <= totalStandardCheckOutMorning
                            && backDaySecond <= totalStandardCheckInAfternoon) {
                        Map<Integer, Integer> value = new HashMap<>();
                        if (checkBookingDateAccept.get(dateStartAccept) != null) {
                            value = checkBookingDateAccept.get(dateStartAccept);
                        }
                        value.put(REMOTE_MORNING, totalStandardTimeMorning);
                        checkBookingDateAccept.put(dateStartAccept, value);
                        // remote chieu
                    } else if (requestDaySecond >= totalStandardCheckOutMorning
                            && backDaySecond <= totalStandardCheckOutAfternoon) {
                        Map<Integer, Integer> value = new HashMap<>();
                        if (checkBookingDateAccept.get(dateStartAccept) != null) {
                            value = checkBookingDateAccept.get(dateStartAccept);
                        }
                        value.put(REMOTE_AFTERNOON, totalStandardTimeAfternoon);
                        checkBookingDateAccept.put(dateStartAccept, value);
                        // remote ca ngay
                    } else if (requestDaySecond <= totalStandardCheckOutMorning
                            && backDaySecond <= totalStandardCheckOutAfternoon) {
                        checkBookingDateAccept.remove(dateStartAccept);
                        Map<Integer, Integer> value = new HashMap<>();
                        if (checkBookingDateAccept.get(dateStartAccept) != null) {
                            value = checkBookingDateAccept.get(dateStartAccept);
                        }
                        value.put(REMOTE_FULL_DAY, totalStandardTimeAfternoon + totalStandardTimeMorning);
                        checkBookingDateAccept.put(dateStartAccept, value);
                    }
                    // remote khac ngay
                } else {
                    // time dang ky <= 8h -> remote ca nay
                    if (requestDaySecond <= totalStandardCheckInMorning) {
                        checkBookingDateAccept.remove(dateStartAccept);
                        Map<Integer, Integer> value = new HashMap<>();
                        if (checkBookingDateAccept.get(dateStartAccept) != null) {
                            value = checkBookingDateAccept.get(dateStartAccept);
                        }
                        value.put(REMOTE_FULL_DAY, totalStandardTimeAfternoon + totalStandardTimeMorning);
                        checkBookingDateAccept.put(dateStartAccept, value);
                    } else {
                        Map<Integer, Integer> value = new HashMap<>();
                        if (checkBookingDateAccept.get(dateStartAccept) != null) {
                            value = checkBookingDateAccept.get(dateStartAccept);
                        }
                        value.put(REMOTE_AFTERNOON, totalStandardTimeAfternoon);
                        checkBookingDateAccept.put(dateStartAccept, value);
                    }
                    // time tro lai > 8h30 && <=13h30 -> remote sang
                    if (backDaySecond >= totalStandardCheckOutMorning
                            && backDaySecond < totalStandardCheckOutAfternoon) {
                        Map<Integer, Integer> value = new HashMap<>();
                        if (checkBookingDateAccept.get(dateBackAccept) != null) {
                            value = checkBookingDateAccept.get(dateBackAccept);
                        }
                        value.put(REMOTE_MORNING, totalStandardTimeMorning);
                        checkBookingDateAccept.put(dateBackAccept, value);
                    } else if (backDaySecond >= totalStandardCheckOutAfternoon) {
                        checkBookingDateAccept.remove(dateBackAccept);
                        Map<Integer, Integer> value = new HashMap<>();
                        if (checkBookingDateAccept.get(dateBackAccept) != null) {
                            value = checkBookingDateAccept.get(dateBackAccept);
                        }
                        value.put(REMOTE_FULL_DAY, totalStandardTimeAfternoon + totalStandardTimeMorning);
                        checkBookingDateAccept.put(dateBackAccept, value);
                    }
                    for (int i = (dateStartAccept + 1); i < dateBackAccept; i++) {
                        checkBookingDateAccept.remove(i);
                        Map<Integer, Integer> value = new HashMap<>();
                        value.put(REMOTE_FULL_DAY, totalStandardTimeAfternoon + totalStandardTimeMorning);
                        checkBookingDateAccept.put(i, value);
                    }
                }
            }
            // xin ra ngoai
            else if (bookingDayOff.getStatus() == 3) {
                if ((requestDaySecond < totalStandardCheckOutMorning && backDaySecond < totalStandardCheckOutMorning)
                        || (requestDaySecond >= totalStandardCheckInAfternoon
                                && backDaySecond > totalStandardCheckInAfternoon)) {
                    Map<Integer, Integer> value = new HashMap<>();
                    value.put(AWAY_FROM_DESK, backDaySecond - requestDaySecond);
                    checkBookingDateAccept.put(requestDate, value);
                } else if (requestDaySecond < totalStandardCheckOutMorning
                        && backDaySecond > totalStandardCheckInAfternoon) {
                    Map<Integer, Integer> value = new HashMap<>();
                    value.put(AWAY_FROM_DESK, (backDaySecond - totalStandardCheckInAfternoon)
                            + (totalStandardCheckOutMorning - requestDaySecond));
                    checkBookingDateAccept.put(requestDate, value);
                }
            }
        }
        for (ConfigDayOff configDayOff : configDayOffList) {
            // time back
            calendar.setTime(configDayOff.getDayTo());
            int backMonth = calendar.get(Calendar.MONTH);
            int backDate = calendar.get(Calendar.DATE);
            // time request
            calendar.setTime(configDayOff.getDayFrom());
            int requestMonth = calendar.get(Calendar.MONTH);
            int requestDate = calendar.get(Calendar.DATE);

            if (currentMonth == requestMonth) {
                dateStartAccept = requestDate;
            } else {
                dateStartAccept = 1;// min day in month
            }
            if (currentMonth == backMonth) {
                dateBackAccept = backDate;
            } else {
                dateBackAccept = maxDayInMonth; // max day in month
            }
            for (int i = dateStartAccept; i <= dateBackAccept; i++) {
                checkBookingDateAccept.remove(i);
                Map<Integer, Integer> value = new HashMap<>();
//                value.put(REMOTE_FULL_DAY, totalStandardTimeAfternoon + totalStandardTimeMorning);
                value.put(UNPAID_FULL_DAY, totalStandardTimeAfternoon + totalStandardTimeMorning);
                checkBookingDateAccept.put(i, value);
            }
        }
        return checkBookingDateAccept;
    }

    public int getSecond(Calendar calendar) {
        return calendar.get(Calendar.HOUR_OF_DAY) * 3600 + calendar.get(Calendar.MINUTE) * 60
                + calendar.get(Calendar.SECOND);
    }

    public int calculatorTimeLate(Map<Integer, Integer> getValue, int status, int totalTimeFirst, int totalTimeSecond,
            int totalStandardTimeFirst, int totalStandardTimeSecond) {
        int totalTimeCheck;
        if (getValue.get(status) != null) {
            totalTimeCheck = Math.max((totalTimeFirst - totalTimeSecond - getValue.get(status)), 0)
                    + totalStandardTimeFirst;
        } else {
            totalTimeCheck = Math.max((totalTimeFirst - totalTimeSecond), 0) + totalStandardTimeSecond;
        }
        return totalTimeCheck;
    }
}
