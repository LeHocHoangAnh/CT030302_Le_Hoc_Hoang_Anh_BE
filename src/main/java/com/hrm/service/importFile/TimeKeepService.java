package com.hrm.service.importFile;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.RecordFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.hrm.common.Constants;
import com.hrm.entity.BookingDayOff;
import com.hrm.entity.DetailTimeKeeping;
import com.hrm.entity.Employee;
import com.hrm.entity.OtGeneral;
import com.hrm.entity.TimeKeeping;
import com.hrm.model.ApiResponse;
import com.hrm.model.DropDownResponse;
import com.hrm.model.dao.DetailTimeKeepingDao;
import com.hrm.model.request.hr.ListDetailTimeKeepingRequest;
import com.hrm.model.response.employee.TimeKeepingResponse;
import com.hrm.model.response.hr.ListDetailTimeKeepingResponse;
import com.hrm.repository.BookingDayOffRepository;
import com.hrm.repository.DetailTimeKeepingRepository;
import com.hrm.repository.EmployeeRepository;
import com.hrm.repository.OtGeneralRepository;
import com.hrm.repository.TimeKeepingRepository;
import com.hrm.service.employee.ApproveGeneralService;
import com.hrm.service.employee.TimeKeepingService;
import com.hrm.utils.FileUtil;
import com.hrm.utils.Utils;

@Service
@Transactional
public class TimeKeepService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TimeKeepingRepository timeKeepingRepository;

    @Autowired
    private DetailTimeKeepingRepository detailTimeKeepingRepository;

    @Autowired
    private OtGeneralRepository otGeneralRepository;

    @Autowired
    private TimeKeepingService timeKeepingService;

    @Autowired
    private DetailTimeKeepingDao detailTimeKeepingDao;

    @Autowired
    private TimeKeepService timeKeepService;

    @Autowired
    private ApproveGeneralService approveGeneralService;

    @Autowired
    private BookingDayOffRepository bookingDayOffRepository;

    @SuppressWarnings("unused")
    public ApiResponse importFileTimeKeep(MultipartFile file, String time) throws Exception {
        Workbook workbook = null;
        Date workDate = null;
        try {
            workbook = FileUtil.getWorkbookByMultipartFile(file);
            if (workbook == null)
                return null;
            List<Employee> employees = employeeRepository.findAll();
            // validate data excel
            workbook.setMissingCellPolicy(Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            Sheet workSheet = workbook.getSheetAt(0);
            // validate du lieu dau vao
            List<TimeKeeping> timeKeepingList = new ArrayList<>();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            for (int i = 4; i <= workSheet.getLastRowNum(); i++) {
                TimeKeeping timeKeeping = new TimeKeeping();
                Cell cell;
                Timestamp checkIn = null;
                Timestamp checkOut = null;
                Row row = workSheet.getRow(i);
                // kiem tra cell co du lieu
                cell = row.getCell(1);
                // skip row that has no workDate value
                if (cell == null) {
                    continue;
                }
                String id = row.getCell(2).getStringCellValue();
                if (cell != null && StringUtils.isNotBlank(row.getCell(1).toString())) {
                    workDate = FileUtil.formatDate(cell.getDateCellValue());
                    if (workDate != null) {
                        timeKeeping.setDateWorking(workDate);
                    }
                }
                boolean registeredEmployee = false;
                cell = row.getCell(2);
                if (cell != null && StringUtils.isNotBlank(row.getCell(2).toString())) {
                    // kiem tra employee co ton tai k
                    String employeeCode;
                    String employeeFileCode = row.getCell(2).getStringCellValue().toUpperCase();
                    if (employeeFileCode.contains("ITS-") || employeeFileCode.contains("ITS")) {
                        employeeCode = row.getCell(2).getStringCellValue();
                    } else {
                        employeeCode = "ITS-" + row.getCell(2).getStringCellValue();
                    }
                    Optional<Employee> employee = employees.stream()
                            .filter(em -> employeeCode.equals(em.getEmployeeCode())).findAny();
                    if (!employee.isEmpty()) {
                        timeKeeping.setEmployee(employee.get());
                        registeredEmployee = true;
                    }
                }
                if (registeredEmployee) {
                    cell = row.getCell(4);
                    if (cell != null && StringUtils.isNotBlank(row.getCell(4).toString())) {
                        Date checkInDate = FileUtil.formatDateHHMMSS(cell.getDateCellValue());
                        if (checkInDate != null) {
                            checkIn = new Timestamp(checkInDate.getTime());
                            timeKeeping.setCheckIn(checkIn);
                        }
                    }
                    cell = row.getCell(5);
                    if (cell != null && StringUtils.isNotBlank(row.getCell(5).toString())) {
                        Date checkOutDate = FileUtil.formatDateHHMMSS(cell.getDateCellValue());
                        if (checkOutDate != null) {
                            checkOut = new Timestamp(checkOutDate.getTime());
                            timeKeeping.setCheckOut(checkOut);
                        }
                    }
                    if (timeKeeping.getEmployee() != null) {
                        timeKeepingList.add(timeKeeping);
                    }
                } else {
                    timeKeeping = null;
                }
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(workDate);
            String month = (cal.get(Calendar.MONTH) + 1) > 9 ? (cal.get(Calendar.MONTH) + 1) + ""
                    : "0" + (cal.get(Calendar.MONTH) + 1);
            timeKeepService.deleteTimeKeepingAndDetail(cal.get(Calendar.YEAR) + "-" + month);
            workbook.close();
            if (!timeKeepingList.isEmpty()) {
                timeKeepingRepository.saveAll(timeKeepingList);
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        } finally {
            workbook.close();
        }
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.CREATE_SUCCESS, null);
    }

    public void saveDetailTimeKeeping(String time) {
        try {
            if (time.length() == 0) {
                DropDownResponse listTime = timeKeepingRepository.getLastestUpdateTime();
                String firstTime = listTime.getName();
                insertDetailTimeKeeping(firstTime);
            } else {
                insertDetailTimeKeeping(time);
            }
        } catch (Exception e) {
            throw new RecordFormatException(e.getMessage());
        }
    }

    private void insertDetailTimeKeeping(String time) {
        List<Employee> list = employeeRepository.findAllByDeleteFlag(Constants.DELETE_NONE);
//    	  List<Employee> list = new ArrayList<>();
//    	  list.add(employeeRepository.getById(2));
        List<DetailTimeKeeping> listDetail = new ArrayList<DetailTimeKeeping>();
        List<Integer> listId = new ArrayList<Integer>();
        String respTime = time + "-13";
        DecimalFormat df = new DecimalFormat("#.#####");
        for (Employee emloyee : list) {
            processOtInMonth(emloyee, time);
            TimeKeepingResponse timeResp = timeKeepingService.getTimeKeepingByEmployee(respTime, emloyee.getId());
            if (timeResp == null) {
                continue;
            }
            DetailTimeKeeping detail = new DetailTimeKeeping();
            detail.setEmployee(emloyee);
            detail.setKeepingForget(timeResp.getTimeKeepingSummary().getTotalForgotTimeKeeping());
            detail.setLateTime(timeResp.getTimeKeepingSummary().getCountTotalDayLate());
            detail.setLateHour(timeResp.getTimeKeepingSummary().getSeriouslyTotalTimeLate());
            detail.setLeaveDayAccept(timeResp.getTimeKeepingSummary().getCountTotalDayOffAccept());
            detail.setSalaryReal(timeResp.getTimeKeepingSummary().getCountTotalStandardWorkDay()
                    + timeResp.getTimeKeepingSummary().getTotalForgotTimeKeepingAccept());
            detail.setCompensatoryLeave(timeResp.getTimeKeepingSummary().getCountCompensatoryLeave());
            detail.setWelfareLeave(timeResp.getTimeKeepingSummary().getCountPersonalLeave());
            detail.setRemoteTime(timeResp.getTimeKeepingSummary().getCountTotalRemoteTime());
            if (timeResp.getTimeKeepingSummary().getTotalNumberSeriouslyTimeLate() <= 3 * 3600) {

                if (timeResp.getTimeKeepingSummary().getCountTotalDayOffAccept() >= 0) {
                    detail.setSalaryCount(timeResp.getTimeKeepingSummary().getCountTotalStandardWorkDay()
                            + timeResp.getTimeKeepingSummary().getTotalForgotTimeKeepingAccept()
                            + timeResp.getTimeKeepingSummary().getCountPersonalLeave()
                            + timeResp.getTimeKeepingSummary().getCountTotalDayOffAccept()
                            + timeResp.getTimeKeepingSummary().getCountCompensatoryLeave());
                } else {
                    detail.setSalaryCount(timeResp.getTimeKeepingSummary().getCountTotalStandardWorkDay()
                            + timeResp.getTimeKeepingSummary().getTotalForgotTimeKeepingAccept()
                            + timeResp.getTimeKeepingSummary().getCountPersonalLeave()
                            + timeResp.getTimeKeepingSummary().getCountTotalDayOffAccept()
                            + timeResp.getTimeKeepingSummary().getCountCompensatoryLeave()
                            - (timeResp.getTimeKeepingSummary().getCountTotalDayOffAccept()));
                }
            } else {
                int totallatehour = (int) (timeResp.getTimeKeepingSummary().getTotalNumberSeriouslyTimeLate()
                        - 3 * 3600);
                int latehour = totallatehour / 3600;
                int latesecond = (totallatehour - 3600 * latehour);
                latesecond = latesecond / 60;
                int mod = latesecond % 15;
                if (mod < 8) {
                    latesecond = latesecond - mod;
                } else {
                    latesecond = latesecond + (15 - mod);
                }
                float latesecondof = (float) (latesecond) / 60;
                float latehourof = latehour + latesecondof;
                float lateday = latehourof / 8;
                if (emloyee.getPaidLeave() - timeResp.getTimeKeepingSummary().getCountTotalDayOffAccept() >= 0) {
                    detail.setSalaryCount(timeResp.getTimeKeepingSummary().getCountTotalStandardWorkDay()
                            + timeResp.getTimeKeepingSummary().getTotalForgotTimeKeepingAccept()
                            + timeResp.getTimeKeepingSummary().getCountPersonalLeave()
                            + timeResp.getTimeKeepingSummary().getCountTotalDayOffAccept()
                            + timeResp.getTimeKeepingSummary().getCountCompensatoryLeave() - lateday);
                } else {
                    detail.setSalaryCount(timeResp.getTimeKeepingSummary().getCountTotalStandardWorkDay()
                            + timeResp.getTimeKeepingSummary().getTotalForgotTimeKeepingAccept()
                            + timeResp.getTimeKeepingSummary().getCountPersonalLeave()
                            + timeResp.getTimeKeepingSummary().getCountTotalDayOffAccept()
                            + timeResp.getTimeKeepingSummary().getCountCompensatoryLeave()
                            - (timeResp.getTimeKeepingSummary().getCountTotalDayOffAccept() - emloyee.getPaidLeave())
                            - lateday);
                }
            }
            // values for OT-paying calculation
            float sumInMonth = 0;
            float otUnpaid = 0;
//            float totalCompensatoryTimeOTInHour = 0;
            // value for compensatory/paid leave calculation
            float currentMonthCompensatory = 0;
            // Get ot records of employee
            List<OtGeneral> empOT = otGeneralRepository.getOtFindByListId(List.of(emloyee.getId()), time);
            if (empOT.size() > 0) {
                for (OtGeneral item : empOT) {
                    if (item.getEmployee().getId() == emloyee.getId()) {
                        sumInMonth = item.getSumOtMonth();
                        currentMonthCompensatory = item.getCstLeaveRounding();
                        break;
                    }
                }
            }
            // get previous month
            String previousTime = Utils.getPreviousMonth(time);
            // Get previous month of employee's time-keeping record
            Optional<DetailTimeKeeping> previousKeeping = detailTimeKeepingRepository.getOtByTime(previousTime,
                    emloyee.getId());
            // Check if record exists
            if (previousKeeping.isPresent()) {
                // Calculate lateTime compensatory for OT
//                int salariedLateSecond = 10800;
//                int lateTimeSecond = stringTimeToSecond(detail.getLateHour());
//                int realLateTimeSecond = stringTimeToSecond(detail.getLateHour());
//                int afdTimeSecond = stringTimeToSecond(timeResp.getTimeKeepingSummary().getTotalAwayFromDesk());
//                totalCompensatoryTimeOTInHour = Math.max(((realLateTimeSecond + afdTimeSecond) - salariedLateSecond) / 3600, 0); // lateTime compensatory for OT
                // Calculate final paid OT and unpaid OT in month
                otUnpaid = previousKeeping.get().getOtUnpaid() == null ? 0 : previousKeeping.get().getOtUnpaid();
                //
                LocalDateTime systemDate = LocalDateTime.now();
                Integer firstWorkdayInMonth = Utils.getFirstWorkdayOfDate(Utils.convertLocalDateToDate(systemDate));
                Integer extra = (systemDate.getDayOfMonth() - firstWorkdayInMonth) > 0 ? 1 : 0;
                if ((previousKeeping.get().getLeaveRemainNow() == null ? 0 : previousKeeping.get().getLeaveRemainNow())
                        - timeResp.getTimeKeepingSummary().getCountTotalDayOffAccept() >= 0) {
                    detail.setLeaveRemainNow((previousKeeping.get().getLeaveRemainNow() == null ? 0
                            : previousKeeping.get().getLeaveRemainNow())
                            - timeResp.getTimeKeepingSummary().getCountTotalDayOffAccept() + extra);
                } else {
                    detail.setLeaveRemainNow((float) 0 + extra);
                }
                if ((previousKeeping.get().getCsrLeaveNow() == null ? 0 : previousKeeping.get().getCsrLeaveNow())
                        - timeResp.getTimeKeepingSummary().getCountCompensatoryLeave() >= 0) {
                    detail.setCsrLeaveNow((previousKeeping.get().getCsrLeaveNow() == null ? 0
                            : previousKeeping.get().getCsrLeaveNow())
                            - timeResp.getTimeKeepingSummary().getCountCompensatoryLeave());
                } else {
                    detail.setCsrLeaveNow((float) (0));
                }
            }
            // calculate final ot-time and the excess
            float finalSumInMonth = Math.max(otUnpaid + sumInMonth
//                    - totalCompensatoryTimeOTInHour
                    , (float) 0);
            detail.setOtPayInMonth(finalSumInMonth > 60 ? (float) 60 : finalSumInMonth);
            detail.setOtUnpaid(Math.max(finalSumInMonth - 60, (float) 0));
            detail.setCsrLeaveNow(detail.getCsrLeaveNow() + currentMonthCompensatory);

            detail.setTimeSave(time);
            detail.setCommonRegister();
            listDetail.add(detail);
            listId.add(emloyee.getId());
            emloyee.setCommonUpdate();
            employeeRepository.save(emloyee);
        }

        List<OtGeneral> listOtGeneral = otGeneralRepository.getOtFindByListId(listId, time);
        for (DetailTimeKeeping detail : listDetail) {
            for (OtGeneral ot : listOtGeneral) {
                if (detail.getEmployee().getId() == ot.getEmployee().getId()
                        && detail.getTimeSave().equals(ot.getMonthAction()) == true) {
                    detail.setOtNormal(Float.parseFloat(df.format(ot.getOtNormal())));
                    detail.setOtMorning7(Float.parseFloat(df.format(ot.getOtMorning7())));
                    detail.setOtSatSun(Float.parseFloat(df.format(ot.getOtSatSun())));
                    detail.setOtHoliday(Float.parseFloat(df.format(ot.getOtHoliday())));
                    detail.setSumOtMonth(Float.parseFloat(df.format(ot.getSumOtMonth())));
                    detail.setCsrLeavePlus(Float.parseFloat(df.format(ot.getCompensatoryLeave())));
                    detail.setCsrLeavePlusRound(ot.getCstLeaveRounding() == null ? 0 : ot.getCstLeaveRounding());
                }
            }
        }
        detailTimeKeepingRepository.saveAll(listDetail);
    }

    public void processOtInMonth(Employee employee, String time) {
        OtGeneral otGeneral = otGeneralRepository.getByMonthActionAndEmployee(employee.getId(), time);
        OtGeneral ot = new OtGeneral();
        if (otGeneral == null) {
            ot.setEmployee(employee);
            ot.setMonthAction(time);
            ot.setCommonRegister();
            otGeneral = otGeneralRepository.save(ot);
        }
        List<BookingDayOff> otListInMonth = bookingDayOffRepository.findTotalOtInMonth(employee.getId(), time);
        // reset value of OT general record
        float zeroFl = (float) 0;
        otGeneral.setOtNormal(zeroFl);
        otGeneral.setOtMorning7(zeroFl);
        otGeneral.setOtSatSun(zeroFl);
        otGeneral.setOtHoliday(zeroFl);
        otGeneral.setSumOtMonth(zeroFl);
        otGeneral.setCompensatoryLeave(zeroFl);
        otGeneral.setCstLeaveRounding(zeroFl);

        // recalculate OT time and overwrite the old record
        approveGeneralService.calculatorOTAll(otListInMonth, otGeneral, employee);

        otGeneral.setCommonUpdate();
        otGeneralRepository.save(otGeneral);
    }

    public void insertDetailTimeKeepingAfterConfirm(String time, int id) {
        Employee emloyee = employeeRepository.getById(id);
        List<DetailTimeKeeping> listDetail = new ArrayList<DetailTimeKeeping>();
        List<Integer> listId = new ArrayList<Integer>();
        String respTime = time + "-13";
        DecimalFormat df = new DecimalFormat("#.#####");
        TimeKeepingResponse timeResp = timeKeepingService.getTimeKeepingByEmployee(respTime, emloyee.getId());
        Optional<DetailTimeKeeping> detaill = detailTimeKeepingRepository.findByEmployeeIdAndTime(emloyee.getId(),
                time);
        DetailTimeKeeping detail = detaill.get();
        detail.setEmployee(emloyee);
        detail.setKeepingForget(timeResp.getTimeKeepingSummary().getTotalForgotTimeKeeping());
        detail.setLateTime(timeResp.getTimeKeepingSummary().getCountTotalDayLate());
        detail.setLateHour(timeResp.getTimeKeepingSummary().getSeriouslyTotalTimeLate());
        detail.setLeaveDayAccept(timeResp.getTimeKeepingSummary().getCountTotalDayOffAccept());
        detail.setSalaryReal(timeResp.getTimeKeepingSummary().getCountTotalStandardWorkDay()
                + timeResp.getTimeKeepingSummary().getTotalForgotTimeKeepingAccept());
        detail.setCompensatoryLeave(timeResp.getTimeKeepingSummary().getCountCompensatoryLeave());
        detail.setWelfareLeave(timeResp.getTimeKeepingSummary().getCountPersonalLeave());
        detail.setRemoteTime(timeResp.getTimeKeepingSummary().getCountTotalRemoteTime());
        if (timeResp.getTimeKeepingSummary().getTotalNumberSeriouslyTimeLate() <= 3 * 3600) {

            if (emloyee.getPaidLeave() - timeResp.getTimeKeepingSummary().getCountTotalDayOffAccept() >= 0) {
                detail.setSalaryCount(timeResp.getTimeKeepingSummary().getCountTotalStandardWorkDay()
                        + timeResp.getTimeKeepingSummary().getTotalForgotTimeKeepingAccept()
                        + timeResp.getTimeKeepingSummary().getCountPersonalLeave()
                        + timeResp.getTimeKeepingSummary().getCountTotalDayOffAccept()
                        + timeResp.getTimeKeepingSummary().getCountCompensatoryLeave());
            } else {
                detail.setSalaryCount(timeResp.getTimeKeepingSummary().getCountTotalStandardWorkDay()
                        + timeResp.getTimeKeepingSummary().getTotalForgotTimeKeepingAccept()
                        + timeResp.getTimeKeepingSummary().getCountPersonalLeave()
                        + timeResp.getTimeKeepingSummary().getCountTotalDayOffAccept()
                        + timeResp.getTimeKeepingSummary().getCountCompensatoryLeave()
                        - (timeResp.getTimeKeepingSummary().getCountTotalDayOffAccept() - emloyee.getPaidLeave()));
            }
        } else {
            int totallatehour = (int) (timeResp.getTimeKeepingSummary().getTotalNumberSeriouslyTimeLate() - 3 * 3600);
            int latehour = totallatehour / 3600;
            int latesecond = (totallatehour - 3600 * latehour);
            latesecond = latesecond / 60;
            int mod = latesecond % 15;
            if (mod < 8) {
                latesecond = latesecond - mod;
            } else {
                latesecond = latesecond + (15 - mod);
            }
            float latesecondof = (float) (latesecond) / 60;
            float latehourof = latehour + latesecondof;
            float lateday = latehourof / 8;
            if (emloyee.getPaidLeave() - timeResp.getTimeKeepingSummary().getCountTotalDayOffAccept() >= 0) {
                detail.setSalaryCount(timeResp.getTimeKeepingSummary().getCountTotalStandardWorkDay()
                        + timeResp.getTimeKeepingSummary().getTotalForgotTimeKeepingAccept()
                        + timeResp.getTimeKeepingSummary().getCountPersonalLeave()
                        + timeResp.getTimeKeepingSummary().getCountTotalDayOffAccept()
                        + timeResp.getTimeKeepingSummary().getCountCompensatoryLeave() - lateday);
            } else {
                detail.setSalaryCount(timeResp.getTimeKeepingSummary().getCountTotalStandardWorkDay()
                        + timeResp.getTimeKeepingSummary().getTotalForgotTimeKeepingAccept()
                        + timeResp.getTimeKeepingSummary().getCountPersonalLeave()
                        + timeResp.getTimeKeepingSummary().getCountTotalDayOffAccept()
                        + timeResp.getTimeKeepingSummary().getCountCompensatoryLeave()
                        - (timeResp.getTimeKeepingSummary().getCountTotalDayOffAccept() - emloyee.getPaidLeave())
                        - lateday);
            }

        }
        // values for OT-paying calculation
        float sumInMonth = 0;
        float otUnpaid = 0;
//        float totalCompensatoryTimeOTInHour = 0;
        // value for compensatory/paid leave calculation
        float currentMonthCompensatory = 0;
        // Get ot records of employee
        List<OtGeneral> empOT = otGeneralRepository.getOtFindByListId(List.of(emloyee.getId()), time);
        if (empOT.size() > 0) {
            for (OtGeneral item : empOT) {
                if (item.getEmployee().getId() == emloyee.getId()) {
                    sumInMonth = item.getSumOtMonth();
                    currentMonthCompensatory = item.getCstLeaveRounding();
                    break;
                }
            }
        }
        // get previous month
        String previousTime = Utils.getPreviousMonth(time);
        // Get previous month of employee's timekeeping record
        Optional<DetailTimeKeeping> previousKeeping = detailTimeKeepingRepository.getOtByTime(previousTime,
                emloyee.getId());
        // Check if record exists
        if (previousKeeping.isPresent()) {
            // Calculate lateTime compensatory for OT
//            int salariedLateSecond = 10800;
//            int lateTimeSecond = stringTimeToSecond(detail.getLateHour());
//            int afdTimeSecond = stringTimeToSecond(timeResp.getTimeKeepingSummary().getTotalAwayFromDesk());
//            totalCompensatoryTimeOTInHour = Math.max(((lateTimeSecond + afdTimeSecond) - salariedLateSecond) / 3600, 0);  // lateTime for OT compensatory

            // Calculate final paid OT and unpaid OT in month
            otUnpaid = previousKeeping.get().getOtUnpaid() == null ? 0 : previousKeeping.get().getOtUnpaid();
            //
            LocalDateTime systemDate = LocalDateTime.now();
            Integer firstWorkdayInMonth = Utils.getFirstWorkdayOfDate(Utils.convertLocalDateToDate(systemDate));
            Integer extra = (systemDate.getDayOfMonth() - firstWorkdayInMonth) > 0 ? 1 : 0;
            if ((previousKeeping.get().getLeaveRemainNow() == null ? 0 : previousKeeping.get().getLeaveRemainNow())
                    - timeResp.getTimeKeepingSummary().getCountTotalDayOffAccept() >= 0) {
                detail.setLeaveRemainNow((previousKeeping.get().getLeaveRemainNow() == null ? 0
                        : previousKeeping.get().getLeaveRemainNow())
                        - timeResp.getTimeKeepingSummary().getCountTotalDayOffAccept() + extra);
            } else {
                detail.setLeaveRemainNow((float) 0 + extra);
            }
            if ((previousKeeping.get().getCsrLeaveNow() == null ? 0 : previousKeeping.get().getCsrLeaveNow())
                    - timeResp.getTimeKeepingSummary().getCountCompensatoryLeave() >= 0) {
                detail.setCsrLeaveNow(
                        (previousKeeping.get().getCsrLeaveNow() == null ? 0 : previousKeeping.get().getCsrLeaveNow())
                                - timeResp.getTimeKeepingSummary().getCountCompensatoryLeave());
            } else {
                detail.setCsrLeaveNow((float) (0));
            }
        }
        // calculate final ot-time and the excess
        float finalSumInMonth = Math.max(otUnpaid + sumInMonth
//                - totalCompensatoryTimeOTInHour
                , (float) 0);
        detail.setOtPayInMonth(finalSumInMonth > 60 ? (float) 60 : finalSumInMonth);
        detail.setOtUnpaid(Math.max(finalSumInMonth - 60, (float) 0));
        detail.setCsrLeaveNow(detail.getCsrLeaveNow() + currentMonthCompensatory);

        detail.setTimeSave(time);
        detail.setCommonRegister();
        listDetail.add(detail);
        listId.add(emloyee.getId());
        emloyee.setCommonUpdate();
        employeeRepository.save(emloyee);

        List<OtGeneral> listOtGeneral = otGeneralRepository.getOtFindByListId(listId, time);
        for (DetailTimeKeeping detail1 : listDetail) {
            for (OtGeneral ot : listOtGeneral) {
                if (detail1.getEmployee().getId() == ot.getEmployee().getId()
                        && detail1.getTimeSave().equals(ot.getMonthAction()) == true) {
                    detail1.setOtNormal(Float.parseFloat(df.format(ot.getOtNormal())));
                    detail1.setOtMorning7(Float.parseFloat(df.format(ot.getOtMorning7())));
                    detail1.setOtSatSun(Float.parseFloat(df.format(ot.getOtSatSun())));
                    detail1.setOtHoliday(Float.parseFloat(df.format(ot.getOtHoliday())));
                    detail1.setSumOtMonth(Float.parseFloat(df.format(ot.getSumOtMonth())));
                    detail1.setCsrLeavePlus(Float.parseFloat(df.format(ot.getCompensatoryLeave())));
                    detail1.setCsrLeavePlusRound(ot.getCstLeaveRounding() == null ? 0 : ot.getCstLeaveRounding());
                }
            }
        }
        detailTimeKeepingRepository.saveAll(listDetail);
    }

    public ApiResponse getListData(ListDetailTimeKeepingRequest request) {
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.CREATE_SUCCESS,
                detailTimeKeepingDao.getListDetailTimeKeeping(request));
    }

    public void deleteTimeKeepingAndDetail(String time) {
        List<DetailTimeKeeping> listDetail = detailTimeKeepingRepository.findByTimeSave(time);
        detailTimeKeepingRepository.deleteAll(listDetail);
        List<TimeKeeping> listTimeKeeping = timeKeepingRepository.findByDateWorking(time);
        timeKeepingRepository.deleteAll(listTimeKeeping);
    }

    public ApiResponse getListTimeDropDown() {
        List<DropDownResponse> list = timeKeepingRepository.getListTime();
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.CREATE_SUCCESS, list);
    }

    public ApiResponse editEmployeeKeeping(ListDetailTimeKeepingResponse editRequest, String time) {
        Optional<DetailTimeKeeping> detail = detailTimeKeepingRepository
                .findByEmployeeCode(editRequest.getEmployeeCode(), time);
        if (!detail.isPresent()) {
            return new ApiResponse(Constants.HTTP_CODE_500, Constants.RECORD_NOT_FOUND, null);
        }

        // Công Thực
        detail.get().setSalaryReal(editRequest.getSalaryReal() == null ? 0 : editRequest.getSalaryReal());
        // Phép
        detail.get().setLeaveDayAccept(editRequest.getLeaveDayAccept() == null ? 0 : editRequest.getLeaveDayAccept());
        // Nghỉ Phúc Lợi
        detail.get().setWelfareLeave(editRequest.getWelfareLeave() == null ? 0 : editRequest.getWelfareLeave());
        // Công Tính Lương
        detail.get().setSalaryCount(editRequest.getSalaryCount() == null ? 0 : editRequest.getSalaryCount());
        // Nghỉ Bù
        detail.get().setCompensatoryLeave(
                editRequest.getCompensatoryLeave() == null ? 0 : editRequest.getCompensatoryLeave());
        // Quên Chấm Công
        detail.get().setKeepingForget(editRequest.getKeepingForget() == null ? 0 : editRequest.getKeepingForget());
        // Đi muộn/ Về sớm(Lần)
        detail.get().setLateTime(editRequest.getLateTime() == null ? 0 : editRequest.getLateTime());
        // Đi muộn/ Về sớm/ Ra ngoài (Giờ)
        detail.get().setLateHour(editRequest.getLateTimeHour() == null ? "0" : editRequest.getLateTimeHour());
        // OT Ngày Thường
        detail.get().setOtNormal(editRequest.getOtNormal() == null ? 0 : editRequest.getOtNormal());
        // OT Sáng Thứ 7
        detail.get().setOtMorning7(editRequest.getOtMorning7() == null ? 0 : editRequest.getOtMorning7());
        // OT Chiều thứ 7/CN
        detail.get().setOtSatSun(editRequest.getOtSatSun() == null ? 0 : editRequest.getOtSatSun());
        // OT Ngày Lễ
        detail.get().setOtHoliday(editRequest.getOtHoliday() == null ? 0 : editRequest.getOtHoliday());
        // OT Tính Lương
        detail.get().setSumOtMonth(editRequest.getSumOtMonth() == null ? 0 : editRequest.getSumOtMonth());
        // Nghỉ Bù Cộng Thêm Theo Tháng
        detail.get().setCsrLeavePlus(editRequest.getCsrLeavePlus() == null ? 0 : editRequest.getCsrLeavePlus());
        // Nghỉ Bù Cộng Thêm Làm Tròn
        detail.get().setCsrLeavePlusRound(
                editRequest.getCsrLeavePlusRound() == null ? 0 : editRequest.getCsrLeavePlusRound());
        // OT Trả Trong Tháng
        detail.get().setOtPayInMonth(editRequest.getOtPayInMonth() == null ? 0 : editRequest.getOtPayInMonth());
        // OT Tồn Chưa Thanh Toán
        detail.get().setOtUnpaid(editRequest.getOtUnpaid() == null ? 0 : editRequest.getOtUnpaid());
        // Phép Lưu Trữ Tháng Tiếp Theo
        detail.get().setLeaveRemainNow(editRequest.getLeaveRemainNow() == null ? 0 : editRequest.getLeaveRemainNow());
        // Nghỉ bù Lưu Trữ Tháng Tiếp Theo
        detail.get().setCsrLeaveNow(editRequest.getCsrLeaveNow() == null ? 0 : editRequest.getCsrLeaveNow());
        // Ngày Remote
        detail.get().setRemoteTime(editRequest.getRemoteTime() == null ? 0 : editRequest.getRemoteTime());

        // update previous month's values
//        Optional<DetailTimeKeeping> previousDetail = detailTimeKeepingRepository
//                .findByEmployeeCode(editRequest.getEmployeeCode(), Utils.getPreviousMonth(time));
//        if (previousDetail.isPresent() && previousDetail.get() != null) {
//            // Phép Tháng Hiện Tại
//            previousDetail.get().setLeaveRemainNow(
//                    editRequest.getLeaveRemainLastMonth() == null ? 0 : editRequest.getLeaveRemainLastMonth());
//            // Nghỉ bù OT Tháng Hiện Tại
//            previousDetail.get().setCsrLeaveNow(
//                    editRequest.getCsrLeaveLastMonth() == null ? 0 : editRequest.getCsrLeaveLastMonth());
//        }

        // Ngày cập nhật
        detail.get().setCommonUpdate();
//        previousDetail.get().setCommonUpdate();

        detailTimeKeepingRepository.save(detail.get());
//        detailTimeKeepingRepository.save(previousDetail.get());

        return new ApiResponse(Constants.HTTP_CODE_200, Constants.CREATE_SUCCESS, null);
    }

    // Convert string time to second
    Integer stringTimeToSecond(String time) {
        String[] timeSplit = time.split(":");
        int result = 0;
        List<Integer> convert = null;
        if (timeSplit.length == 4) {
            convert = List.of(86400, 3600, 60, 1);
        }
        if (timeSplit.length == 3) {
            convert = List.of(3600, 60, 1);
        }
        for (int i = 0; i < timeSplit.length; i++) {
            result += convert.get(i) * Integer.parseInt(timeSplit[i]);
        }
        return result;
    }
}
