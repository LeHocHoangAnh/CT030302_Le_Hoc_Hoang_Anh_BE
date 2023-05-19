package com.hrm.service.employee;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.hrm.common.CommonFilter;
import com.hrm.common.CommonFilter.Booking_OFF_TYPE;
import com.hrm.common.CommonService;
import com.hrm.common.Constants;
import com.hrm.entity.BookingDayOff;
import com.hrm.entity.ConfigDayOff;
import com.hrm.entity.DetailTimeKeeping;
import com.hrm.entity.Employee;
import com.hrm.entity.OtGeneral;
import com.hrm.entity.Profile;
import com.hrm.exception.RecordNotFoundException;
import com.hrm.model.ApiResponse;
import com.hrm.model.dao.ApproveGeneralDao;
import com.hrm.model.request.hr.ListBookingRequest;
import com.hrm.model.request.hr.ListDetailTimeKeepingRequest;
import com.hrm.model.request.leader.UpdateBookingRequest;
import com.hrm.model.response.DetailTimeKeepingDisplayResponse;
import com.hrm.model.response.DetailTimeKeepingWithEmployeeResponse;
import com.hrm.model.response.leader.DetailBookingEntityResponse;
import com.hrm.model.response.leader.DetailBookingResponse;
import com.hrm.repository.BookingDayOffRepository;
import com.hrm.repository.ConfigDayOffRepository;
import com.hrm.repository.DetailTimeKeepingRepository;
import com.hrm.repository.EmployeeRepository;
import com.hrm.repository.OtGeneralRepository;
import com.hrm.repository.ProfileRepository;
import com.hrm.service.RefuseOtService;
import com.hrm.service.importFile.TimeKeepService;
import com.hrm.utils.Utils;

@Service
@Transactional
public class ApproveGeneralService {
    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ApproveGeneralDao approveGeneralDao;

    @Autowired
    private BookingDayOffRepository bookingDayOffRepository;

    @Autowired
    private DetailTimeKeepingRepository detailTimeKeepingRepository;

    @Autowired
    private OtGeneralRepository otGeneralRepository;

    @Autowired
    private ConfigDayOffRepository configDayOffRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RefuseOtService refuseOtService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private TimeKeepService timeKeepService;

    @Autowired
    private BookingDayOffService bookingDayOffService;

    @Autowired
    private JavaMailSender emailSender;

    @Value("${sendMail.to}")
    private String mailTo;

    @Value("${spring.mail.username}")
    private String mailFrom;

    public ApiResponse getListBooking(ListBookingRequest request) {
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS,
                approveGeneralDao.getListBooking(request, String.valueOf(commonService.idUserAccountLogin())));
    }

    public ApiResponse getDetailBooking(Integer id) {
        Optional<BookingDayOff> bookingDayOff = bookingDayOffRepository.findById(id);
        if (bookingDayOff.get().getStatus() == 9) {
            return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, bookingDayOff.get());
        } else {
            DetailBookingResponse detail = bookingDayOffRepository.getDetailBookingById(id, Constants.DELETE_NONE);
            if (detail == null) {
                throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
            }
            // if dayOff application is OT type: calculate total OT time
            if (detail.getStatus().equalsIgnoreCase(Booking_OFF_TYPE.OT.name())) {
                // mapping entity
                DetailBookingEntityResponse detailEntity = new DetailBookingEntityResponse();
                detailEntity.setId(detail.getId());
                detailEntity.setEmployeeId(detail.getEmployeeId());
                detailEntity.setFullName(detail.getFullName());
                detailEntity.setDepartmentName(detail.getDepartmentName());
                detailEntity.setStatus(detail.getStatus());
                detailEntity.setRequestDay(detail.getRequestDay());
                detailEntity.setBackDay(detail.getBackDay());
                detailEntity.setReason(detail.getReason());
                detailEntity.setConfirm(detail.getConfirm());
                detailEntity.setApprover(detail.getApprover());
                detailEntity.setEvidenceImage(detail.getEvidenceImage());
                detailEntity.setProjectName(detail.getProjectName());
                detailEntity.setDeleteFlag(detail.getDeleteFlag());
                //
                Date requestDate = Utils.convertStringToDate(Constants.YYYY_MM_DD_HH_MM_SS, detail.getRequestDay());
                Date backDate = Utils.convertStringToDate(Constants.YYYY_MM_DD_HH_MM_SS, detail.getBackDay());
                Optional<Employee> employee = employeeRepository.findByIdAndDeleteFlag(detail.getEmployeeId(),
                        Constants.DELETE_NONE);

                if (!employee.isPresent() || employee.get() == null) {
                    return new ApiResponse(Constants.HTTP_CODE_500, "Nhân viên không tồn tại", null);
                }
                OtGeneral otGeneral = new OtGeneral();
                calculatorOT(new Timestamp(requestDate.getTime()), new Timestamp(backDate.getTime()), otGeneral,
                        employee.get());
                detailEntity.setTotalOtTime(otGeneral.getOtNormal() + otGeneral.getOtMorning7()
                        + otGeneral.getOtSatSun() + otGeneral.getOtHoliday());
                return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, detailEntity);
            }
            return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, detail);
        }
    }

    public ApiResponse getDetailMultipleBooking(List<Integer> multipleSelected) {
        List<DetailBookingResponse> detail = bookingDayOffRepository.getMultipleDetailBooking(multipleSelected,
                Constants.DELETE_NONE);
        if (detail == null) {
            throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
        }
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, detail);
    }

    public ApiResponse getDetailBookingStatus(Integer id) {
        Optional<BookingDayOff> bookingDetail = bookingDayOffRepository.findById(id);
        List<String> approverName = new ArrayList<>();
        if (bookingDetail.isPresent()) {
            if (approverName == null || approverName.size() == 0) {
                new ApiResponse(Constants.HTTP_CODE_500, Constants.ERROR, null);
            }
            for (String approverID : bookingDetail.get().getApproverIDs()) {
                approverName.add(profileRepository.findFullNameByEmployeeId(Integer.parseInt(approverID)));
            }
            return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, List.of(approverName,
                    bookingDetail.get().getApproveProgress(), bookingDetail.get().getApproveReason()));
        } else
            return new ApiResponse(Constants.HTTP_CODE_500, Constants.RECORD_NOT_FOUND, null);
    }

    public ApiResponse updateBookings(List<UpdateBookingRequest> req) {
        req.forEach((x) -> {
            updateBooking(x);
        });
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, null);

    }

    public ApiResponse updateBooking(UpdateBookingRequest req) {
        // flag marks the accept/reject state
        boolean confirmFlag = false;
        Optional<BookingDayOff> booking = bookingDayOffRepository.findById(req.getId());
        if (!booking.isPresent()) {
            throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
        } else {
            List<String> approverIDs = booking.get().getApproverIDs();
            if (approverIDs != null && approverIDs.size() > 0) {
                Integer approverID = commonService.idUserAccountLogin();
                Integer approverConfirm = req.getConfirm();
                Integer approverIndex = null;
                if (approverIDs.contains(String.valueOf(approverID))) {
                    approverIndex = approverIDs.indexOf(String.valueOf(approverID));
                } else
                    return new ApiResponse(Constants.HTTP_CODE_500, Constants.APPROVER_NOT_FOUND, null);
                List<String> approveProgress = booking.get().getApproveProgress();
                // update reason for confirming
                booking.get().getApproveReason().set(approverIndex, req.getConfirmReason());
                // calculate confirm status for approveProgress
                Integer confirmStatus = calculateConfirmStatus(approverIndex, approverConfirm, approveProgress);

                // accept or reject
                switch (confirmStatus) {
                case 1:
                    booking.get().setConfirm(Constants.CONFIRM_ACCEPT);
                    confirmFlag = true;
                    break;

                case 2:
                    booking.get().setConfirm(Constants.CONFIRM_REJECT);
                    confirmFlag = true;
                    break;
                }

                booking.get().setApproveProgress(approveProgress);
                booking.get().setCommonUpdate();
            } else {
                confirmFlag = true;
                booking.get().setConfirm(req.getConfirm());
                booking.get().setCommonUpdate();
                booking.get().setApprover(commonService.idUserAccountLogin());
            }
            // update application status
            bookingDayOffRepository.save(booking.get());
            // if application is accepted or rejected: update detail_time_keeping
            if (confirmFlag) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(booking.get().getRequestDay());
                String month = null;
                if (String.valueOf(cal.get(Calendar.MONTH) + 1).length() == 1) {
                    month = String.valueOf("0" + (cal.get(Calendar.MONTH) + 1));
                } else {
                    month = String.valueOf(cal.get(Calendar.MONTH) + 1);
                }
                // update OT
                if (req.getStatus().equals(CommonFilter.BOOKING_DAY_OFF.OT.toString())) {
                    req.setConfirm(booking.get().getConfirm());
                    updateOT(req, booking.get());
                }
                // update keeping detail
                Optional<DetailTimeKeeping> detail = detailTimeKeepingRepository.findByEmployeeIdAndTime(
                        booking.get().getEmployee().getId(), String.valueOf(cal.get(Calendar.YEAR) + "-" + month));
                if (detail.isPresent()) {
                    timeKeepService.insertDetailTimeKeepingAfterConfirm(
                            String.valueOf(cal.get(Calendar.YEAR) + "-" + month), booking.get().getEmployee().getId());
                }
            }
        }
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, booking.get().getId());
    }

    // calculate and return confirm status based on % approverConfirm of leaders
    private Integer calculateConfirmStatus(Integer approverIndex, Integer approverConfirm,
            List<String> approveProgress) {
        Boolean confirmFlag = false;
        Boolean rejectFlag = false;
        Boolean allApproved = true; // flag marks if all approver have made their decision
        Integer acceptCount = 0;
        Integer rejectCount = 0;
        Integer totalApprovers = approveProgress.size();

        for (int i = 0; i < totalApprovers; i++) {
            // update approver's confirm
            if (approverIndex == i) {
                approveProgress.set(i, String.valueOf(approverConfirm));
            }
            acceptCount += Integer.parseInt(approveProgress.get(i)) == Constants.CONFIRM_ACCEPT ? 1 : 0; // if approver
                                                                                                         // accept ->
                                                                                                         // increase
                                                                                                         // acceptCount
            rejectCount += Integer.parseInt(approveProgress.get(i)) == Constants.CONFIRM_REJECT ? 1 : 0; // if approver
                                                                                                         // rejecy ->
                                                                                                         // increase
                                                                                                         // rejectCount
            // if current (acceptCount/total) percentage >= threshold -> application is
            // accepted -> return accepted
            confirmFlag = Double.valueOf(acceptCount * 100 / totalApprovers) >= Constants.ACCPET_PERCENTAGE_THRESHOLD;
            // if current (rejectCount/total) percentage >= threshold -> application is
            // rejected -> return rejected
            rejectFlag = Double
                    .valueOf(rejectCount * 100 / totalApprovers) >= (100 - Constants.ACCPET_PERCENTAGE_THRESHOLD);
            if (confirmFlag)
                return Constants.CONFIRM_ACCEPT;
            if (rejectFlag)
                return Constants.CONFIRM_REJECT;

            if (allApproved)
                // If there is at least a approver not yet confirmed -> false
                allApproved = Integer.parseInt(approveProgress.get(i)) != Constants.CONFIRM_WAIT;
        }

        // Nothing happened -> return waiting
        // Nothing happend, but all approver have confirmed -> return rejected
        return allApproved ? Constants.CONFIRM_REJECT : Constants.CONFIRM_WAIT;
    }

    private void updateOT(UpdateBookingRequest req, BookingDayOff booking) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(booking.getRequestDay());
        Optional<Employee> employee = employeeRepository.findByIdAndDeleteFlag(booking.getEmployee().getId(),
                Constants.DELETE_NONE);
        String month = null;
        if (String.valueOf(cal.get(Calendar.MONTH) + 1).length() == 1) {
            month = String.valueOf("0" + (cal.get(Calendar.MONTH) + 1));
        } else {
            month = String.valueOf(cal.get(Calendar.MONTH) + 1);
        }
        if (req.getConfirm() != 1) {
            if (req.getConfirm() == 2) {
                OtGeneral ot = otGeneralRepository.getByMonthActionAndEmployee(booking.getEmployee().getId(),
                        String.valueOf(cal.get(Calendar.YEAR) + "-" + month));
                if (ot == null) {
                    throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
                }
                refuseOtService.refuseOT(booking.getRequestDay(), booking.getBackDay(), ot, employee.get());
                otGeneralRepository.save(ot);
            }
        } else {
            booking.setConfirm(req.getConfirm());
            OtGeneral otGeneral = otGeneralRepository.getByMonthActionAndEmployee(booking.getEmployee().getId(),
                    String.valueOf(cal.get(Calendar.YEAR) + "-" + month));
            if (otGeneral == null) {
                OtGeneral ot = new OtGeneral();
                calculatorOT(booking.getRequestDay(), booking.getBackDay(), ot, employee.get());
                ot.setEmployee(booking.getEmployee());
                ot.setMonthAction(String.valueOf((cal.get(Calendar.YEAR) + "-" + month)));
                ot.setCommonRegister();
                otGeneralRepository.save(ot);
            } else {
                List<BookingDayOff> otListInMonth = bookingDayOffRepository.findTotalOtInMonth(
                        booking.getEmployee().getId(), String.valueOf(cal.get(Calendar.YEAR) + "-" + month));
                OtGeneral ot = otGeneral;
                // reset value of OT general record
                float zeroFl = (float) 0;
                ot.setOtNormal(zeroFl);
                ot.setOtMorning7(zeroFl);
                ot.setOtSatSun(zeroFl);
                ot.setOtHoliday(zeroFl);
                ot.setSumOtMonth(zeroFl);
                ot.setCompensatoryLeave(zeroFl);
                ot.setCstLeaveRounding(zeroFl);

                // recalculate OT time and overwrite the old record
                calculatorOTAll(otListInMonth, ot, employee.get());

                ot.setCommonUpdate();
                otGeneralRepository.save(ot);
            }
        }
        booking.setCommonUpdate();
        booking.setApprover(commonService.idUserAccountLogin());
        bookingDayOffRepository.save(booking);
    }

    private void calculatorOT(Timestamp startDay, Timestamp backDay, OtGeneral ot, Employee employee) {
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
                long MiliSecond = backDay.getTime() - startDay.getTime();
                float hour = convertSecondsToHour(MiliSecond);
                if (hourStart.compareTo(timeStartMorning) >= 0 && hourStart.compareTo(timeEndMorning) < 0
                        && hourBack.compareTo(timeEndAfternoon) <= 0 && hourBack.compareTo(timeStartAfternoon) > 0) {
                    hour -= 1.5;
                } else if ((hourStart.compareTo(timeStartMorning) < 0 && hourBack.compareTo(timeStartAfternoon) >= 0)
                        || (hourStart.compareTo(timeEndMorning) <= 0 && hourBack.compareTo(timeEndAfternoon) > 0)) {
                    hour -= 1.5;
                }

                if (filterConfigDayOff(listConfig, startDay.getTime(), backDay.getTime()) == true) {
                    ot.setOtHoliday(ot.getOtHoliday() + hour);
                } else {
                    ot.setOtNormal(ot.getOtNormal() + hour);
//					LocalDate calStartStandardOut = startDay.toLocalDateTime().toLocalDate();
//					LocalTime startNormalOtTime = LocalTime.of(18, 30, 00);
//					LocalDateTime startNormalOtTimeLDT = LocalDateTime.of(calStartStandardOut, startNormalOtTime);
//					Timestamp startStandardOut = Timestamp.valueOf(startNormalOtTimeLDT);
//
//					long backDayTs = backDay.getTime();
//					long startDayTs = backDay.getTime();
//					if (startDay.getTime() < startStandardOut.getTime()
//							&& backDay.getTime() > startStandardOut.getTime()) {
//						startDayTs = startStandardOut.getTime();
//					}
//					long MiliSecond = backDayTs - startDayTs;
//					ot.setOtNormal(ot.getOtNormal() + convertSecondsToHour(MiliSecond));
                }
                // check thu 7
            } else if (checkDayOfWeek == 7) {
                if (hourStart.compareTo(timeStartMorning) >= 0 && hourStart.compareTo(timeEndMorning) < 0
                        && hourBack.compareTo(timeStartAfternoon) <= 0 && hourBack.compareTo(timeStartMorning) > 0) {
                    if (filterConfigDayOff(listConfig, startDay.getTime(), backDay.getTime()) == true) {
                        long MiliSecond = backDay.getTime() - startDay.getTime();
                        ot.setOtHoliday(ot.getOtHoliday() + convertSecondsToHour(MiliSecond));
                    } else {
                        long MiliSecond = backDay.getTime() - startDay.getTime();
                        ot.setOtMorning7(ot.getOtMorning7() + convertSecondsToHour(MiliSecond));
                    }
                } else if (hourStart.compareTo(timeStartAfternoon) >= 0 && hourStart.compareTo(timeEndAfternoon) < 0
                        && (hourBack.compareTo(timeStartNight) <= 0 && hourBack.compareTo(timeStartAfternoon) > 0)) {
                    if (filterConfigDayOff(listConfig, startDay.getTime(), backDay.getTime()) == true) {
                        long MiliSecond = backDay.getTime() - startDay.getTime();
                        ot.setOtHoliday(ot.getOtHoliday() + convertSecondsToHour(MiliSecond));
                    } else {
                        long MiliSecond = backDay.getTime() - startDay.getTime();
                        ot.setOtMorning7(ot.getOtMorning7() + convertSecondsToHour(MiliSecond));
                    }
                } else if (hourStart.compareTo(timeStartNight) == 0) {
                    if (filterConfigDayOff(listConfig, startDay.getTime(), backDay.getTime()) == true) {
                        long MiliSecond = backDay.getTime() - startDay.getTime();
                        ot.setOtHoliday(ot.getOtHoliday() + convertSecondsToHour(MiliSecond));
                    } else {
                        long MiliSecond = backDay.getTime() - startDay.getTime();
                        ot.setOtMorning7(ot.getOtMorning7() + convertSecondsToHour(MiliSecond));
                    }
                } else if (hourStart.compareTo(timeStartMorning) >= 0 && hourStart.compareTo(timeEndMorning) < 0
                        && hourBack.compareTo(timeEndAfternoon) <= 0 && hourBack.compareTo(timeStartAfternoon) > 0) {
                    Timestamp timeMedial = Utils.convertStringToTimestamp(Constants.YYYY_MM_DD_HH_MM_SS,
                            calendarStart.get(Calendar.YEAR) + "-" + (calendarStart.get(Calendar.MONTH) + 1) + "-"
                                    + calendarStart.get(Calendar.DATE) + " 12:00:0");
                    if (filterConfigDayOff(listConfig, startDay.getTime(), backDay.getTime()) == true) {
                        long MiliSecond = backDay.getTime() - startDay.getTime();
                        ot.setOtHoliday(ot.getOtHoliday() + ((float) (convertSecondsToHour(MiliSecond) - 1.5)));
                    } else {
                        long MiliSecondMorning = timeMedial.getTime() - startDay.getTime();
                        ot.setOtMorning7(ot.getOtMorning7() + convertSecondsToHour(MiliSecondMorning));
                        long MiliSecondApternoon = backDay.getTime() - timeMedial.getTime();
                        ot.setOtMorning7(
                                ot.getOtMorning7() + ((float) (convertSecondsToHour(MiliSecondApternoon) - 1.5)));
                    }
                } else if (hourStart.compareTo(timeStartAfternoon) >= 0 && hourBack.compareTo(timeEndAfternoon) >= 0) {
                    long MiliSecond = backDay.getTime() - startDay.getTime();
                    if (filterConfigDayOff(listConfig, startDay.getTime(), backDay.getTime()) == true) {
                        ot.setOtHoliday(ot.getOtHoliday() + convertSecondsToHour(MiliSecond));
                    } else {
                        ot.setOtMorning7(ot.getOtMorning7() + convertSecondsToHour(MiliSecond));
                    }
                } else if ((hourStart.compareTo(timeStartMorning) < 0 && hourBack.compareTo(timeStartAfternoon) >= 0)
                        || (hourStart.compareTo(timeEndMorning) <= 0 && hourBack.compareTo(timeEndAfternoon) > 0)) {
                    Timestamp timeMedial = Utils.convertStringToTimestamp(Constants.YYYY_MM_DD_HH_MM_SS,
                            calendarStart.get(Calendar.YEAR) + "-" + (calendarStart.get(Calendar.MONTH) + 1) + "-"
                                    + calendarStart.get(Calendar.DATE) + " 12:00:0");
                    if (filterConfigDayOff(listConfig, startDay.getTime(), backDay.getTime()) == true) {
                        long MiliSecond = backDay.getTime() - startDay.getTime();
                        ot.setOtHoliday(ot.getOtHoliday() + ((float) (convertSecondsToHour(MiliSecond) - 1.5)));
                    } else {
                        long MiliSecondMorning = timeMedial.getTime() - startDay.getTime();
                        ot.setOtMorning7(ot.getOtMorning7() + convertSecondsToHour(MiliSecondMorning));
                        long MiliSecondApternoon = backDay.getTime() - timeMedial.getTime();
                        ot.setOtMorning7(
                                ot.getOtMorning7() + ((float) (convertSecondsToHour(MiliSecondApternoon) - 1.5)));
                    }
                } else if (hourStart.compareTo(timeStartMorning) < 0 && hourBack.compareTo(timeEndMorning) <= 0) {
                    if (filterConfigDayOff(listConfig, startDay.getTime(), backDay.getTime()) == true) {
                        long MiliSecond = backDay.getTime() - startDay.getTime();
                        ot.setOtHoliday(ot.getOtHoliday() + convertSecondsToHour(MiliSecond));
                    } else {
                        long MiliSecond = backDay.getTime() - startDay.getTime();
                        ot.setOtMorning7(ot.getOtMorning7() + convertSecondsToHour(MiliSecond));
                    }
                }
                // check chu nhat
            } else if (checkDayOfWeek == 1) {
                if (hourStart.compareTo(timeStartMorning) >= 0 && hourStart.compareTo(timeEndMorning) < 0
                        && hourBack.compareTo(timeStartAfternoon) <= 0 && hourBack.compareTo(timeStartMorning) > 0) {
                    if (filterConfigDayOff(listConfig, startDay.getTime(), backDay.getTime()) == true) {
                        long MiliSecond = backDay.getTime() - startDay.getTime();
                        ot.setOtHoliday(ot.getOtHoliday() + ((float) (convertSecondsToHour(MiliSecond))));
                    } else {
                        long MiliSecond = backDay.getTime() - startDay.getTime();
                        ot.setOtSatSun(ot.getOtSatSun() + ((float) (convertSecondsToHour(MiliSecond))));
                    }
                } else if (hourStart.compareTo(timeStartAfternoon) >= 0 && hourStart.compareTo(timeEndAfternoon) < 0
                        && hourBack.compareTo(timeStartNight) <= 0 && hourBack.compareTo(timeStartAfternoon) > 0) {
                    if (filterConfigDayOff(listConfig, startDay.getTime(), backDay.getTime()) == true) {
                        long MiliSecond = backDay.getTime() - startDay.getTime();
                        ot.setOtHoliday(ot.getOtHoliday() + ((float) (convertSecondsToHour(MiliSecond))));
                    } else {
                        long MiliSecond = backDay.getTime() - startDay.getTime();
                        ot.setOtSatSun(ot.getOtSatSun() + ((float) (convertSecondsToHour(MiliSecond))));
                    }
                } else if (hourStart.compareTo(timeStartNight) >= 0) {
                    if (filterConfigDayOff(listConfig, startDay.getTime(), backDay.getTime()) == true) {
                        long MiliSecond = backDay.getTime() - startDay.getTime();
                        ot.setOtHoliday(ot.getOtHoliday() + convertSecondsToHour(MiliSecond));
                    } else {
                        long MiliSecond = backDay.getTime() - startDay.getTime();
                        ot.setOtSatSun(ot.getOtSatSun() + convertSecondsToHour(MiliSecond));
                    }
                } else if (hourStart.compareTo(timeStartMorning) >= 0 && hourStart.compareTo(timeEndMorning) < 0
                        && hourBack.compareTo(timeEndAfternoon) <= 0 && hourBack.compareTo(timeStartAfternoon) > 0) {
                    if (filterConfigDayOff(listConfig, startDay.getTime(), backDay.getTime()) == true) {
                        long MiliSecond = backDay.getTime() - startDay.getTime();
                        ot.setOtHoliday(ot.getOtHoliday() + ((float) (convertSecondsToHour(MiliSecond) - 1.5)));
                    } else {
                        long MiliSecondApternoon = backDay.getTime() - startDay.getTime();
                        ot.setOtSatSun(ot.getOtSatSun() + ((float) (convertSecondsToHour(MiliSecondApternoon) - 1.5)));
                    }
                } else if (hourStart.compareTo(timeStartAfternoon) >= 0 && hourBack.compareTo(timeEndAfternoon) >= 0) {
                    long MiliSecond = backDay.getTime() - startDay.getTime();
                    if (filterConfigDayOff(listConfig, startDay.getTime(), backDay.getTime()) == true) {
                        ot.setOtHoliday(ot.getOtHoliday() + convertSecondsToHour(MiliSecond));
                    } else {
                        ot.setOtSatSun(ot.getOtSatSun() + convertSecondsToHour(MiliSecond));
                    }
                } else if ((hourStart.compareTo(timeStartMorning) < 0 && hourBack.compareTo(timeStartAfternoon) >= 0)
                        || (hourStart.compareTo(timeEndMorning) <= 0 && hourBack.compareTo(timeEndAfternoon) > 0)) {
                    if (filterConfigDayOff(listConfig, startDay.getTime(), backDay.getTime()) == true) {
                        long MiliSecond = backDay.getTime() - startDay.getTime();
                        ot.setOtHoliday(ot.getOtHoliday() + ((float) (convertSecondsToHour(MiliSecond) - 1.5)));
                    } else {
                        long MiliSecondApternoon = backDay.getTime() - startDay.getTime();
                        ot.setOtSatSun(ot.getOtSatSun() + ((float) (convertSecondsToHour(MiliSecondApternoon) - 1.5)));
                    }
                } else if (hourStart.compareTo(timeStartMorning) < 0 && hourBack.compareTo(timeEndMorning) <= 0) {
                    if (filterConfigDayOff(listConfig, startDay.getTime(), backDay.getTime()) == true) {
                        long MiliSecond = backDay.getTime() - startDay.getTime();
                        ot.setOtHoliday(ot.getOtHoliday() + convertSecondsToHour(MiliSecond));
                    } else {
                        long MiliSecond = backDay.getTime() - startDay.getTime();
                        ot.setOtSatSun(ot.getOtSatSun() + convertSecondsToHour(MiliSecond));
                    }
                }
            }
            // khac ngay
        } else {
            cal.setTime(new Date(startDay.getTime()));
            Integer dayOfWeekStart = cal.get(Calendar.DAY_OF_WEEK);
            Boolean checkDayOfWeekStart = dayOfWeekStart == 1 || dayOfWeekStart == 7;
            Boolean checkHolidayStart = filterConfigDayOff(listConfig, startDay.getTime(), startDay.getTime());
            cal.setTime(new Date(backDay.getTime()));
            Integer dayOfWeekBack = cal.get(Calendar.DAY_OF_WEEK);
            Boolean checkDayOfWeekBack = dayOfWeekBack == 1 || dayOfWeekBack == 7;
            Boolean checkHolidayBack = filterConfigDayOff(listConfig, backDay.getTime(), backDay.getTime());
            // Start day is Sunday: calculate otSatSun by get the start of the next
            // day(09:00:00) - requested OT startTime
            LocalDateTime endOfStartDay = LocalDateTime.of(startDay.toLocalDateTime().plusDays(1).toLocalDate(),
                    LocalTime.of(9, 00, 00));
            long startDayMilisecond = Math.max((Timestamp.valueOf(endOfStartDay).getTime() - startDay.getTime()), 0);

            // Back day is Monday: calculate otNormal by get the requested OT backTime - the
            // start of the day(09:00:00)
            LocalDateTime startOfBackDay = LocalDateTime.of(backDay.toLocalDateTime().toLocalDate(),
                    LocalTime.of(9, 00, 00));
            long backDayMilisecond = Math.max((backDay.getTime() - Timestamp.valueOf(startOfBackDay).getTime()), 0);

            if (backDayMilisecond == 0) {
                startDayMilisecond = Math.max((backDay.getTime() - startDay.getTime()), 0);
            }

            if (checkDayOfWeekStart) {
                ot.setOtSatSun(ot.getOtSatSun() + convertSecondsToHour(startDayMilisecond));
            } else if (checkHolidayStart) {
                ot.setOtHoliday(ot.getOtHoliday() + convertSecondsToHour(startDayMilisecond));
            } else {
                ot.setOtNormal(ot.getOtNormal() + convertSecondsToHour(startDayMilisecond));
            }
            if (backDayMilisecond > 0) {
                if (checkDayOfWeekBack) {
                    if (dayOfWeekBack == 7) {
                        ot.setOtMorning7(ot.getOtMorning7() + convertSecondsToHour(backDayMilisecond));
                    } else if (dayOfWeekBack == 1) {
                        ot.setOtSatSun(ot.getOtSatSun() + convertSecondsToHour(backDayMilisecond));
                    }
                } else if (checkHolidayBack) {
                    ot.setOtHoliday(ot.getOtHoliday() + convertSecondsToHour(startDayMilisecond));
                } else {
                    ot.setOtNormal(ot.getOtNormal() + convertSecondsToHour(backDayMilisecond));
                }
            }
        }
        // sum ot
        // get total ot time at morning saturday then - 4(first saturday morning in
        // month is not counted)
//        float otMorning7 = ot.getOtMorning7() <= 4 ? 0 : ot.getOtMorning7()-4;
        float otMorning7 = ot.getOtMorning7();
        // total ot salary will be paid in month
        ot.setSumOtMonth(
                (float) (ot.getOtNormal() + (otMorning7) + (ot.getOtSatSun() * 1.5) + (ot.getOtHoliday() * 2)));
        // total ot compensatory day-off (50% total real ot time in month)
        ot.setCompensatoryLeave(
                (float) ((((ot.getOtNormal() + otMorning7 + ot.getOtSatSun()) * 0.5) + ot.getOtHoliday()) / 8));
        Integer sumOtRounding = (int) ((((ot.getOtNormal() + otMorning7 + ot.getOtSatSun()) * 0.5) + ot.getOtHoliday())
                / 8);
        Integer numberDivide = (int) ((((ot.getOtNormal() + otMorning7 + ot.getOtSatSun()) * 0.5) + ot.getOtHoliday())
                / 8);
        int floatPointDivide = (int) ((numberDivide - (int) Math.floor(numberDivide)) * 10);
        if (floatPointDivide < 3) {
            ot.setCstLeaveRounding((float) sumOtRounding);
        } else if (floatPointDivide >= 3 && numberDivide < 7) {
            ot.setCstLeaveRounding((float) (sumOtRounding + 0.5));
        } else if (floatPointDivide >= 7) {
            ot.setCstLeaveRounding((float) sumOtRounding + 1);
        }
        //
//		employee.setCompensatoryLeave(employee.getCompensatoryLeave() == null ? 0
//				: employee.getCompensatoryLeave() + ot.getCompensatoryLeave());
//		employee.setOtUnpaid(employee.getOtUnpaid() == null ? 0 : employee.getOtUnpaid() + ot.getSumOtMonth());
//		employeeRepository.save(employee);
    }

    public void calculatorOTAll(List<BookingDayOff> otListInMonth, OtGeneral ot, Employee employee) {
        for (BookingDayOff item : otListInMonth) {
            Timestamp startDay = item.getRequestDay();
            Timestamp backDay = item.getBackDay();

            Calendar cal = Calendar.getInstance();
            Calendar calendarStart = Calendar.getInstance();
            calendarStart.setTime(startDay);
            Calendar calendarBack = Calendar.getInstance();
            calendarBack.setTime(backDay);
            String start = Utils.convertDateToString(Constants.YYYY_MM_DD, new Date(startDay.getTime()));
            String back = Utils.convertDateToString(Constants.YYYY_MM_DD, new Date(backDay.getTime()));
            List<ConfigDayOff> listConfig = configDayOffRepository.getConfigByYear(calendarStart.get(Calendar.YEAR));
            LocalTime hourStart = LocalTime.of(calendarStart.get(Calendar.HOUR_OF_DAY),
                    calendarStart.get(Calendar.MINUTE), calendarStart.get(Calendar.SECOND));
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
                    long MiliSecond = backDay.getTime() - startDay.getTime();
                    float hour = convertSecondsToHour(MiliSecond);
                    if (hourStart.compareTo(timeStartMorning) >= 0 && hourStart.compareTo(timeEndMorning) < 0
                            && hourBack.compareTo(timeEndAfternoon) <= 0
                            && hourBack.compareTo(timeStartAfternoon) > 0) {
                        hour -= 1.5;
                    } else if ((hourStart.compareTo(timeStartMorning) < 0
                            && hourBack.compareTo(timeStartAfternoon) >= 0)
                            || (hourStart.compareTo(timeEndMorning) <= 0 && hourBack.compareTo(timeEndAfternoon) > 0)) {
                        hour -= 1.5;
                    }

                    if (filterConfigDayOff(listConfig, startDay.getTime(), backDay.getTime()) == true) {
                        ot.setOtHoliday(ot.getOtHoliday() + hour);
                    } else {
                        ot.setOtNormal(ot.getOtNormal() + hour);
//						LocalDate calStartStandardOut = startDay.toLocalDateTime().toLocalDate();
//						LocalTime startNormalOtTime = LocalTime.of(18, 30, 00);
//						LocalDateTime startNormalOtTimeLDT =  LocalDateTime.of(calStartStandardOut, startNormalOtTime);
//						Timestamp startStandardOut = Timestamp.valueOf(startNormalOtTimeLDT);
//						
//						long startDayTs = startDay.getTime();
//						long backDayTs = backDay.getTime();
//						if(startDay.getTime()<startStandardOut.getTime() && backDay.getTime() > startStandardOut.getTime()) {
//							startDayTs = startStandardOut.getTime();
//						}
//						long MiliSecond = backDayTs - startDayTs;
//						ot.setOtNormal(ot.getOtNormal() + convertSecondsToHour(MiliSecond));
                    }
                    // check thu 7
                } else if (checkDayOfWeek == 7) {
                    if (hourStart.compareTo(timeStartMorning) >= 0 && hourStart.compareTo(timeEndMorning) < 0
                            && hourBack.compareTo(timeStartAfternoon) <= 0
                            && hourBack.compareTo(timeStartMorning) > 0) {
                        if (filterConfigDayOff(listConfig, startDay.getTime(), backDay.getTime()) == true) {
                            long MiliSecond = backDay.getTime() - startDay.getTime();
                            ot.setOtHoliday(ot.getOtHoliday() + convertSecondsToHour(MiliSecond));
                        } else {
                            long MiliSecond = backDay.getTime() - startDay.getTime();
                            ot.setOtMorning7(ot.getOtMorning7() + convertSecondsToHour(MiliSecond));
                        }
                    } else if (hourStart.compareTo(timeStartAfternoon) >= 0 && hourStart.compareTo(timeEndAfternoon) < 0
                            && (hourBack.compareTo(timeStartNight) <= 0
                                    && hourBack.compareTo(timeStartAfternoon) > 0)) {
                        if (filterConfigDayOff(listConfig, startDay.getTime(), backDay.getTime()) == true) {
                            long MiliSecond = backDay.getTime() - startDay.getTime();
                            ot.setOtHoliday(ot.getOtHoliday() + convertSecondsToHour(MiliSecond));
                        } else {
                            long MiliSecond = backDay.getTime() - startDay.getTime();
                            ot.setOtMorning7(ot.getOtMorning7() + convertSecondsToHour(MiliSecond));
                        }
                    } else if (hourStart.compareTo(timeStartNight) == 0) {
                        if (filterConfigDayOff(listConfig, startDay.getTime(), backDay.getTime()) == true) {
                            long MiliSecond = backDay.getTime() - startDay.getTime();
                            ot.setOtHoliday(ot.getOtHoliday() + convertSecondsToHour(MiliSecond));
                        } else {
                            long MiliSecond = backDay.getTime() - startDay.getTime();
                            ot.setOtMorning7(ot.getOtMorning7() + convertSecondsToHour(MiliSecond));
                        }
                    } else if (hourStart.compareTo(timeStartMorning) >= 0 && hourStart.compareTo(timeEndMorning) < 0
                            && hourBack.compareTo(timeEndAfternoon) <= 0
                            && hourBack.compareTo(timeStartAfternoon) > 0) {
                        Timestamp timeMedial = Utils.convertStringToTimestamp(Constants.YYYY_MM_DD_HH_MM_SS,
                                calendarStart.get(Calendar.YEAR) + "-" + (calendarStart.get(Calendar.MONTH) + 1) + "-"
                                        + calendarStart.get(Calendar.DATE) + " 12:00:0");
                        if (filterConfigDayOff(listConfig, startDay.getTime(), backDay.getTime()) == true) {
                            long MiliSecond = backDay.getTime() - startDay.getTime();
                            ot.setOtHoliday(ot.getOtHoliday() + ((float) (convertSecondsToHour(MiliSecond) - 1.5)));
                        } else {
                            long MiliSecondMorning = timeMedial.getTime() - startDay.getTime();
                            ot.setOtMorning7(ot.getOtMorning7() + convertSecondsToHour(MiliSecondMorning));
                            long MiliSecondApternoon = backDay.getTime() - timeMedial.getTime();
                            ot.setOtMorning7(
                                    ot.getOtMorning7() + ((float) (convertSecondsToHour(MiliSecondApternoon) - 1.5)));
                        }
                    } else if (hourStart.compareTo(timeStartAfternoon) >= 0
                            && hourBack.compareTo(timeEndAfternoon) >= 0) {
                        long MiliSecond = backDay.getTime() - startDay.getTime();
                        if (filterConfigDayOff(listConfig, startDay.getTime(), backDay.getTime()) == true) {
                            ot.setOtHoliday(ot.getOtHoliday() + convertSecondsToHour(MiliSecond));
                        } else {
                            ot.setOtMorning7(ot.getOtMorning7() + convertSecondsToHour(MiliSecond));
                        }
                    } else if ((hourStart.compareTo(timeStartMorning) < 0
                            && hourBack.compareTo(timeStartAfternoon) >= 0)
                            || (hourStart.compareTo(timeEndMorning) <= 0 && hourBack.compareTo(timeEndAfternoon) > 0)) {
                        Timestamp timeMedial = Utils.convertStringToTimestamp(Constants.YYYY_MM_DD_HH_MM_SS,
                                calendarStart.get(Calendar.YEAR) + "-" + (calendarStart.get(Calendar.MONTH) + 1) + "-"
                                        + calendarStart.get(Calendar.DATE) + " 12:00:0");
                        if (filterConfigDayOff(listConfig, startDay.getTime(), backDay.getTime()) == true) {
                            long MiliSecond = backDay.getTime() - startDay.getTime();
                            ot.setOtHoliday(ot.getOtHoliday() + ((float) (convertSecondsToHour(MiliSecond) - 1.5)));
                        } else {
                            long MiliSecondMorning = timeMedial.getTime() - startDay.getTime();
                            ot.setOtMorning7(ot.getOtMorning7() + convertSecondsToHour(MiliSecondMorning));
                            long MiliSecondApternoon = backDay.getTime() - timeMedial.getTime();
                            ot.setOtMorning7(
                                    ot.getOtMorning7() + ((float) (convertSecondsToHour(MiliSecondApternoon) - 1.5)));
                        }
                    } else if (hourStart.compareTo(timeStartMorning) < 0 && hourBack.compareTo(timeEndMorning) <= 0) {
                        if (filterConfigDayOff(listConfig, startDay.getTime(), backDay.getTime()) == true) {
                            long MiliSecond = backDay.getTime() - startDay.getTime();
                            ot.setOtHoliday(ot.getOtHoliday() + convertSecondsToHour(MiliSecond));
                        } else {
                            long MiliSecond = backDay.getTime() - startDay.getTime();
                            ot.setOtMorning7(ot.getOtMorning7() + +convertSecondsToHour(MiliSecond));
                        }
                    }
                    // check chu nhat
                } else if (checkDayOfWeek == 1) {
                    if (hourStart.compareTo(timeStartMorning) >= 0 && hourStart.compareTo(timeEndMorning) < 0
                            && hourBack.compareTo(timeStartAfternoon) <= 0
                            && hourBack.compareTo(timeStartMorning) > 0) {
                        if (filterConfigDayOff(listConfig, startDay.getTime(), backDay.getTime()) == true) {
                            long MiliSecond = backDay.getTime() - startDay.getTime();
                            ot.setOtHoliday(ot.getOtHoliday() + ((float) (convertSecondsToHour(MiliSecond))));
                        } else {
                            long MiliSecond = backDay.getTime() - startDay.getTime();
                            ot.setOtSatSun(ot.getOtSatSun() + ((float) (convertSecondsToHour(MiliSecond))));
                        }
                    } else if (hourStart.compareTo(timeStartAfternoon) >= 0 && hourStart.compareTo(timeEndAfternoon) < 0
                            && hourBack.compareTo(timeStartNight) <= 0 && hourBack.compareTo(timeStartAfternoon) > 0) {
                        if (filterConfigDayOff(listConfig, startDay.getTime(), backDay.getTime()) == true) {
                            long MiliSecond = backDay.getTime() - startDay.getTime();
                            ot.setOtHoliday(ot.getOtHoliday() + ((float) (convertSecondsToHour(MiliSecond))));
                        } else {
                            long MiliSecond = backDay.getTime() - startDay.getTime();
                            ot.setOtSatSun(ot.getOtSatSun() + ((float) (convertSecondsToHour(MiliSecond))));
                        }
                    } else if (hourStart.compareTo(timeStartNight) >= 0) {
                        if (filterConfigDayOff(listConfig, startDay.getTime(), backDay.getTime()) == true) {
                            long MiliSecond = backDay.getTime() - startDay.getTime();
                            ot.setOtHoliday(ot.getOtHoliday() + convertSecondsToHour(MiliSecond));
                        } else {
                            long MiliSecond = backDay.getTime() - startDay.getTime();
                            ot.setOtSatSun(ot.getOtSatSun() + convertSecondsToHour(MiliSecond));
                        }
                    } else if (hourStart.compareTo(timeStartMorning) >= 0 && hourStart.compareTo(timeEndMorning) < 0
                            && hourBack.compareTo(timeEndAfternoon) <= 0
                            && hourBack.compareTo(timeStartAfternoon) > 0) {
                        if (filterConfigDayOff(listConfig, startDay.getTime(), backDay.getTime()) == true) {
                            long MiliSecond = backDay.getTime() - startDay.getTime();
                            ot.setOtHoliday(ot.getOtHoliday() + ((float) (convertSecondsToHour(MiliSecond) - 1.5)));
                        } else {
                            long MiliSecondApternoon = backDay.getTime() - startDay.getTime();
                            ot.setOtSatSun(
                                    ot.getOtSatSun() + ((float) (convertSecondsToHour(MiliSecondApternoon) - 1.5)));
                        }
                    } else if (hourStart.compareTo(timeStartAfternoon) >= 0
                            && hourBack.compareTo(timeEndAfternoon) >= 0) {
                        long MiliSecond = backDay.getTime() - startDay.getTime();
                        if (filterConfigDayOff(listConfig, startDay.getTime(), backDay.getTime()) == true) {
                            ot.setOtHoliday(ot.getOtHoliday() + convertSecondsToHour(MiliSecond));
                        } else {
                            ot.setOtSatSun(ot.getOtSatSun() + convertSecondsToHour(MiliSecond));
                        }
                    } else if ((hourStart.compareTo(timeStartMorning) < 0
                            && hourBack.compareTo(timeStartAfternoon) >= 0)
                            || (hourStart.compareTo(timeEndMorning) <= 0 && hourBack.compareTo(timeEndAfternoon) > 0)) {
                        if (filterConfigDayOff(listConfig, startDay.getTime(), backDay.getTime()) == true) {
                            long MiliSecond = backDay.getTime() - startDay.getTime();
                            ot.setOtHoliday(ot.getOtHoliday() + ((float) (convertSecondsToHour(MiliSecond) - 1.5)));
                        } else {
                            long MiliSecondApternoon = backDay.getTime() - startDay.getTime();
                            ot.setOtSatSun(
                                    ot.getOtSatSun() + ((float) (convertSecondsToHour(MiliSecondApternoon) - 1.5)));
                        }
                    } else if (hourStart.compareTo(timeStartMorning) < 0 && hourBack.compareTo(timeEndMorning) <= 0) {
                        if (filterConfigDayOff(listConfig, startDay.getTime(), backDay.getTime()) == true) {
                            long MiliSecond = backDay.getTime() - startDay.getTime();
                            ot.setOtHoliday(ot.getOtHoliday() + convertSecondsToHour(MiliSecond));
                        } else {
                            long MiliSecond = backDay.getTime() - startDay.getTime();
                            ot.setOtSatSun(ot.getOtSatSun() + convertSecondsToHour(MiliSecond));
                        }
                    }
                }
                // khac ngay
            } else {
                cal.setTime(new Date(startDay.getTime()));
                Integer dayOfWeekStart = cal.get(Calendar.DAY_OF_WEEK);
                Boolean checkDayOfWeekStart = dayOfWeekStart == 1 || dayOfWeekStart == 7;
                Boolean checkHolidayStart = filterConfigDayOff(listConfig, startDay.getTime(), startDay.getTime());
                cal.setTime(new Date(backDay.getTime()));
                Integer dayOfWeekBack = cal.get(Calendar.DAY_OF_WEEK);
                Boolean checkDayOfWeekBack = dayOfWeekBack == 1 || dayOfWeekBack == 7;
                Boolean checkHolidayBack = filterConfigDayOff(listConfig, backDay.getTime(), backDay.getTime());
                // Start day is Sunday: calculate otSatSun by get the start of the next
                // day(09:00:00) - requested OT startTime
                LocalDateTime endOfStartDay = LocalDateTime.of(startDay.toLocalDateTime().plusDays(1).toLocalDate(),
                        LocalTime.of(9, 00, 00));
                long startDayMilisecond = Math.max((Timestamp.valueOf(endOfStartDay).getTime() - startDay.getTime()),
                        0);

                // Back day is Monday: calculate otNormal by get the requested OT backTime - the
                // start of the day(09:00:00)
                LocalDateTime startOfBackDay = LocalDateTime.of(backDay.toLocalDateTime().toLocalDate(),
                        LocalTime.of(9, 00, 00));
                long backDayMilisecond = Math.max((backDay.getTime() - Timestamp.valueOf(startOfBackDay).getTime()), 0);

                if (backDayMilisecond == 0) {
                    startDayMilisecond = Math.max((backDay.getTime() - startDay.getTime()), 0);
                }

                if (checkDayOfWeekStart) {
                    ot.setOtSatSun(ot.getOtSatSun() + convertSecondsToHour(startDayMilisecond));
                } else if (checkHolidayStart) {
                    ot.setOtHoliday(ot.getOtHoliday() + convertSecondsToHour(startDayMilisecond));
                } else {
                    ot.setOtNormal(ot.getOtNormal() + convertSecondsToHour(startDayMilisecond));
                }
                if (backDayMilisecond > 0) {
                    if (checkDayOfWeekBack) {
                        if (dayOfWeekBack == 7) {
                            ot.setOtMorning7(ot.getOtMorning7() + convertSecondsToHour(backDayMilisecond));
                        } else if (dayOfWeekBack == 1) {
                            ot.setOtSatSun(ot.getOtSatSun() + convertSecondsToHour(backDayMilisecond));
                        }
                    } else if (checkHolidayBack) {
                        ot.setOtHoliday(ot.getOtHoliday() + convertSecondsToHour(startDayMilisecond));
                    } else {
                        ot.setOtNormal(ot.getOtNormal() + convertSecondsToHour(backDayMilisecond));
                    }
                }
            }
        }
        // sum ot
        // get total ot time at morning saturday then - 4(first saturday morning in
        // month is not counted)
        float otMorning7 = ot.getOtMorning7();
        // total ot salary will be paid in month
        ot.setSumOtMonth(
                (float) (ot.getOtNormal() + (otMorning7) + (ot.getOtSatSun() * 1.5) + (ot.getOtHoliday() * 2)));
        // total ot compensatory day-off (50% total real ot time in month)
        ot.setCompensatoryLeave(
                (float) ((((ot.getOtNormal() + otMorning7 + ot.getOtSatSun()) * 0.5) + ot.getOtHoliday()) / 8));
        Integer sumOtRounding = (int) ((((ot.getOtNormal() + otMorning7 + ot.getOtSatSun()) * 0.5) + ot.getOtHoliday())
                / 8);
        Float numberDivide = (float) ((((ot.getOtNormal() + otMorning7 + ot.getOtSatSun()) * 0.5) + ot.getOtHoliday())
                / 8);
        int floatPointDivide = (int) ((numberDivide - (int) Math.floor(numberDivide)) * 10);
        if (floatPointDivide < 3) {
            ot.setCstLeaveRounding((float) sumOtRounding);
        } else if (floatPointDivide >= 3 && floatPointDivide < 7) {
            ot.setCstLeaveRounding((float) (sumOtRounding + 0.5));
        } else if (floatPointDivide >= 7) {
            ot.setCstLeaveRounding((float) sumOtRounding + 1);
        }
        //
//		employee.setCompensatoryLeave(employee.getCompensatoryLeave() == null ? 0
//				: employee.getCompensatoryLeave() + ot.getCompensatoryLeave());
//		employee.setOtUnpaid(employee.getOtUnpaid() == null ? 0 : employee.getOtUnpaid() + ot.getSumOtMonth());
//		employeeRepository.save(employee);
    }

    public Float convertSecondsToHour(long MiliSecond) {
        Float second = (float) MiliSecond / 1000;
        Float hours = second / 3600;
        return (float) (Math.round(hours * Math.pow(10, 5)) / Math.pow(10, 5));
    }

    public Boolean filterConfigDayOff(List<ConfigDayOff> list, Long startTime, Long endTime) {
        Integer timeLocal = 0;
        for (ConfigDayOff config : list) {
            if (startTime >= config.getDayFrom().getTime() && endTime <= (config.getDayTo().getTime() + 86340000)) {
                timeLocal++;
            }
        }
        if (timeLocal > 0) {
            return true;
        } else {
            return false;
        }
    }

    public void replyMailBooking(Integer bookingDayOffId) {
        Optional<BookingDayOff> bookingDayOff = bookingDayOffRepository.findById(bookingDayOffId);
        String dashWithSpace = " - ";
        String newLine = "\n";
        // validate values
        if (!bookingDayOff.isPresent()) {
            throw new RecordNotFoundException("Booking day-off record not found");
        }
        if (bookingDayOff.get().getConfirm() == Constants.CONFIRM_WAIT) {
            throw new RecordNotFoundException("Application has not been approved");
        }
        if (bookingDayOff.get().getEmployee() == null) {
            throw new RecordNotFoundException("Registrator not found");
        }
        //
        Employee employee = bookingDayOff.get().getEmployee();
        Profile employeeProfile = employee.getProfile().stream().collect(Collectors.toList()).get(0);
        for (Profile item : employee.getProfile()) {
            employeeProfile = item;
        }
        // get list emails send to
        List<String> listMailTo = new ArrayList<>();
        listMailTo.add("anhlhh@its-global.vn");
        listMailTo.add(employee.getEmail());
//		listMailTo.add(mailTo);
//		if (bookingDayOff.get().getApproverIDs() != null && bookingDayOff.get().getApproverIDs().size() > 0) {
//			listMailTo.addAll(getEmailEmployee(Collections.singletonList(bookingDayOff.get().getEmployee().getEmail())));
//			listMailTo.addAll(null);
//		} else {
//			listMailTo.addAll(bookingDayOffRepository.getListEmailSendBooking(employee.getDepartment().getId()));

//		}
        // get list emails send to
//		List<String> listMailCC = new ArrayList<>();
//		if (bookingDayOff.get().getRelatedEmployeeIDs() != null
//				&& bookingDayOff.get().getRelatedEmployeeIDs().size() > 0) {
////			listMailCC.addAll(getEmailEmployee(bookingDayOff.get().getRelatedEmployeeIDs()));
//			listMailCC.addAll(null);
//		}
//		if (employee.getConfirmDayOffNotify()) {
//			listMailCC.add(employee.getEmail());
//		}
        if (bookingDayOff.get().getStatus() != 9 || bookingDayOff.get().getConfirm() == 1) {
            String status = bookingDayOff.get().getConfirm() == Constants.CONFIRM_ACCEPT ? "Xác Nhận" : "Từ Chối";
            String typeBooking = "";
            typeBooking = Utils.getLabelDayOffType(bookingDayOff.get().getStatus());
            // check department null
            String departmentPart = "";
            if (employee.getDepartment() != null) {
                departmentPart = dashWithSpace + employee.getDepartment().getName();
            }
            // email subject
            String emailSubject = employeeProfile.getFullName() + departmentPart + dashWithSpace + typeBooking
                    + dashWithSpace
                    + Utils.convertDateToString(Constants.DD_MM_YYYY, bookingDayOff.get().getRequestDay());
            // email content
            StringBuilder emailContent = new StringBuilder();
            if (bookingDayOff.get().getStatus() == 9) {
                String equipmentCategory = Utils.getLabelEquipmentCategory(bookingDayOff.get().getApprover());
                emailContent.append(" - Loại Thiết Bị: " + equipmentCategory);
                emailContent.append(newLine);
                //
                emailContent.append(" - Mô tả thiết bị: ");
                if (bookingDayOff.get().getSelectedTypeTime().contains(newLine)) {
                    emailContent.append(newLine);
                    emailContent.append("     ");
                }
                emailContent.append(bookingDayOff.get().getSelectedTypeTime().replace(newLine, newLine + "     "));
                //
                emailContent.append(newLine);
                emailContent.append(" - Ngày nhận: "
                        + Utils.convertDateToString(Constants.DD_MM_YYYY, bookingDayOff.get().getRequestDay()));
                //
                emailContent.append(newLine);
                emailContent.append(" - Lý do: ");
                if (bookingDayOff.get().getReason().contains(newLine)) {
                    emailContent.append(newLine);
                    emailContent.append("     ");
                }
                emailContent.append(bookingDayOff.get().getReason().replace(newLine, newLine + "     "));
            }
            emailContent.append(newLine);
            emailContent.append(" - Trạng Thái:  ");
            emailContent.append(" Đã " + status);

            try {
                SimpleMailMessage message = new SimpleMailMessage();
                // send from
                message.setFrom(mailFrom);
                // send to
                String[] arrayTo = (String[]) listMailTo.toArray(String[]::new);
                message.setTo(arrayTo);
                // cc to
//			String[] arrayCC = (String[]) listMailCC.toArray(String[]::new);
//			message.setCc(arrayCC);
                // subject
                message.setSubject(emailSubject);
                // content
                message.setText(emailContent.toString());

                emailSender.send(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // get email for mail sending
    public List<String> getEmailEmployee(List<String> idList) {
        List<Employee> employee = employeeRepository.getListEmployeeByStringListId(idList);
        employee = employee.stream().filter(item -> item.getConfirmDayOffNotify() == true).collect(Collectors.toList());

        return employee.stream().map(item -> item.getEmail()).collect(Collectors.toList());
    }

    public ApiResponse deleteBooking(Integer id) {
        BookingDayOff detail = bookingDayOffRepository.getById(id);
        if (detail == null) {
            throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
        }
        bookingDayOffRepository.delete(detail);
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, null);
    }

    public ApiResponse getListAggregate(ListDetailTimeKeepingRequest request) {
        // get employee list according to requested employeeCode & employeeName
        String requestCode = request.getEmployeeCode() == null ? "" : request.getEmployeeCode().trim();
        String requestName = request.getFullName() == null ? "" : request.getFullName().trim();
        List<Employee> requestEmployee = employeeRepository.findByEmployeeCodeAndName(requestCode, requestName);
        if (requestEmployee == null || requestEmployee.size() <= 0) {
            throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
        }
        int totalRecord = requestEmployee.size();

        // get sublist according to pageSize and pageNo
        int currentEndPage = request.getPageNo() * request.getPageSize();
        requestEmployee = requestEmployee.subList(request.getPageSize() * (request.getPageNo() - 1),
                totalRecord < currentEndPage ? totalRecord : currentEndPage);
        //
        List<DetailTimeKeepingWithEmployeeResponse> responseList = new ArrayList<>();
        requestEmployee.forEach(item -> {
            DetailTimeKeepingWithEmployeeResponse response = new DetailTimeKeepingWithEmployeeResponse();
            response.setDetailTimeKeeping((DetailTimeKeepingDisplayResponse) bookingDayOffService
                    .getLeavePaidRemainByEmployeeAndMonth(item.getId(), request.getTimeYear()).getItems());
            response.setId(item.getId());
            response.setEmployeeCode(item.getEmployeeCode());
            response.setFullName(item.getProfile().stream().collect(Collectors.toList()).get(0).getFullName());
            responseList.add(response);
        });
        responseList.get(0).setTotalRecord(totalRecord);

        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, responseList);
    }

    // * export functions //
    public ApiResponse exportAggregateData(ListDetailTimeKeepingRequest request, HttpServletResponse response)
            throws IOException {
        XSSFWorkbook workbook = null;
        ServletOutputStream outputStream = null;
        XSSFSheet sheet = null;

        // get all employee
        request.setEmployeeCode(null);
        request.setFullName(null);
        request.setPageSize(9999);
        request.setPageNo(1);
        ApiResponse listAggregateResponse = getListAggregate(request);
        List<DetailTimeKeepingWithEmployeeResponse> listAggregateItem = (List<DetailTimeKeepingWithEmployeeResponse>) listAggregateResponse
                .getItems();
        //

        try {
            workbook = new XSSFWorkbook();

            response.setContentType("application/octet-stream");
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=application_summary_" + request.getTimeYear() + ".xlsx";
            response.setHeader(headerKey, headerValue);

            sheet = workbook.createSheet("Tổng hợp");
            Row row = sheet.createRow(0);
            writeHeaderLine(row, workbook, sheet);
            writeDataLines(workbook, sheet, listAggregateItem);

            outputStream = response.getOutputStream();
            workbook.write(outputStream);
            workbook.close();
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, null);
    }

    private void writeDataLines(XSSFWorkbook workbook, XSSFSheet sheet,
            List<DetailTimeKeepingWithEmployeeResponse> items) {
        int rowCount = 1;
        int stt = 1;
        CellStyle cellStyle = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        font.setFontName("Arial");
        cellStyle.setFont(font);

        for (DetailTimeKeepingWithEmployeeResponse item : items) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            DetailTimeKeepingDisplayResponse detail = item.getDetailTimeKeeping();

            createCell(row, columnCount++, String.valueOf(stt++), cellStyle, sheet);
            createCell(row, columnCount++, item.getEmployeeCode() == null ? "" : String.valueOf(item.getEmployeeCode()),
                    cellStyle, sheet);
            createCell(row, columnCount++, item.getFullName() == null ? "" : String.valueOf(item.getFullName()),
                    cellStyle, sheet);
            createCell(row, columnCount++, detail.getLateHour() == null ? "" : String.valueOf(detail.getLateHour()),
                    cellStyle, sheet);
            createCell(row, columnCount++, detail.getLateTime() == null ? "" : String.valueOf(detail.getLateTime()),
                    cellStyle, sheet);
            createCell(row, columnCount++, detail.getAwdTime() == null ? "" : String.valueOf(detail.getAwdTime()),
                    cellStyle, sheet);
            createCell(row, columnCount++,
                    detail.getKeepingForget() == null ? "" : String.valueOf(detail.getKeepingForget()), cellStyle,
                    sheet);
            createCell(row, columnCount++,
                    detail.getLeaveDayAccept() == null ? "" : String.valueOf(detail.getLeaveDayAccept()), cellStyle,
                    sheet);
            createCell(row, columnCount++,
                    detail.getUnpaidLeave() == null ? "" : String.valueOf(detail.getUnpaidLeave()), cellStyle, sheet);
            createCell(row, columnCount++, detail.getRemoteTime() == null ? "" : String.valueOf(detail.getRemoteTime()),
                    cellStyle, sheet);
            createCell(row, columnCount++, detail.getOtNormal() == null ? "" : String.valueOf(detail.getOtNormal()),
                    cellStyle, sheet);
            createCell(row, columnCount++, detail.getOtMorning7() == null ? "" : String.valueOf(detail.getOtMorning7()),
                    cellStyle, sheet);
            createCell(row, columnCount++, detail.getOtSatSun() == null ? "" : String.valueOf(detail.getOtSatSun()),
                    cellStyle, sheet);
            createCell(row, columnCount++, detail.getOtHoliday() == null ? "" : String.valueOf(detail.getOtHoliday()),
                    cellStyle, sheet);
            createCell(row, columnCount++,
                    detail.getLeaveRemainNow() == null ? "" : String.valueOf(detail.getLeaveRemainNow()), cellStyle,
                    sheet);
            createCell(row, columnCount++,
                    detail.getCsrLeaveNow() == null ? "" : String.valueOf(detail.getCsrLeaveNow()), cellStyle, sheet);
        }
    }

    private void writeHeaderLine(Row row, XSSFWorkbook workbook, XSSFSheet sheet) {
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(10);
        font.setFontName("Arial");
        style.setFont(font);

        createCell(row, 0, "STT", style, sheet);
        createCell(row, 1, "Mã NV", style, sheet);
        createCell(row, 2, "Họ Tên", style, sheet);
        createCell(row, 3, "Đi Muộn/ Về Sớm/ Ra Ngoài(Giờ)", style, sheet);
        createCell(row, 4, "Đi Muộn/ Về Sớm(Lần)", style, sheet);
        createCell(row, 5, "Ra Ngoài(Lần)", style, sheet);
        createCell(row, 6, "Quên Chấm Công", style, sheet);
        createCell(row, 7, "Nghỉ Có Phép", style, sheet);
        createCell(row, 8, "Nghỉ Không Hưởng Lương", style, sheet);
        createCell(row, 9, "Remote", style, sheet);
        createCell(row, 10, "OT Ngày Thường", style, sheet);
        createCell(row, 11, "OT Thứ 7", style, sheet);
        createCell(row, 12, "OT Chủ nhật", style, sheet);
        createCell(row, 13, "OT Ngày Lễ", style, sheet);
        createCell(row, 14, "Phép Còn Đến Hiện Tại", style, sheet);
        createCell(row, 15, "Nghỉ Bù Còn Đến Hiện Tại", style, sheet);
    }

    private void createCell(Row row, int columnCount, Object value, CellStyle style, XSSFSheet sheet) {
        sheet.autoSizeColumn(columnCount);
        Cell cell = row.createCell(columnCount);
        if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue((String) value);
        }
        cell.setCellStyle(style);
    }
}