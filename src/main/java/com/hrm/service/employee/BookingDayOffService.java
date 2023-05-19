package com.hrm.service.employee;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

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
import com.hrm.entity.StandardTime;
import com.hrm.exception.RecordNotFoundException;
import com.hrm.model.ApiResponse;
import com.hrm.model.DropDownResponse;
import com.hrm.model.request.employee.CreateOrUpdateBookingRequest;
import com.hrm.model.response.DetailBookingDayOffResponse;
import com.hrm.model.response.DetailTimeKeepingDisplayResponse;
import com.hrm.model.response.ListConfigDayOffResponse;
import com.hrm.repository.BookingDayOffRepository;
import com.hrm.repository.ConfigDayOffRepository;
import com.hrm.repository.DetailTimeKeepingRepository;
import com.hrm.repository.EmployeeRepository;
import com.hrm.repository.ProfileRepository;
import com.hrm.repository.ProjectsRepository;
import com.hrm.repository.StandardTimeRepository;
import com.hrm.service.AwsS3Service;
import com.hrm.utils.Utils;
import com.hrm.utils.UtilsComponent;

@Service
@Transactional
public class BookingDayOffService {

    @Autowired
    private BookingDayOffRepository bookingDayOffRepository;

    @Autowired
    private StandardTimeRepository standardTimeRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ConfigDayOffRepository configDayOffRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ProjectsRepository projectsRepository;

    @Autowired
    private DetailTimeKeepingRepository detailTimeKeepingRepository;

    @Autowired
    private CommonService commonService;

    @Autowired
    private ApproveGeneralService approveGeneralService;

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private AwsS3Service awsS3Service;

    @Autowired
    private UtilsComponent utilsComponent;

    @Value("${sendMail.to}")
    private String mailTo;

    @Value("${spring.mail.username}")
    private String mailFrom;

    public ApiResponse onChangeBooking(CreateOrUpdateBookingRequest request) {
        Integer currentSectionUserId = commonService.idUserAccountLogin();
        // check if request is paid-leave type
        boolean checkPaidLeave = request.getRegistrationType().equals(CommonFilter.BOOKING_DAY_OFF.DAY_OFF.toString());
        // check if request is OT-compensatory type
        boolean checkCompensatoryLeave = request.getRegistrationType()
                .equals(CommonFilter.BOOKING_DAY_OFF.COMPENSATORY_LEAVE.toString());
        //
        boolean previousMonthExists = true;
        if (request.getId() != null) {
            Optional<BookingDayOff> bookingDayOff = bookingDayOffRepository.findById(request.getId());
            if (bookingDayOff.isPresent() && bookingDayOff.get() != null) {
                if (bookingDayOff.get().getConfirm() == Constants.CONFIRM_ACCEPT) {
                    return new ApiResponse(Constants.HTTP_CODE_500, Constants.APPROVER_NOT_FOUND, null);
                }
            }
        }
        // if booking request is paid-day-off types
        // validate the remain day-off amount
        if (checkPaidLeave || checkCompensatoryLeave) {
            Calendar cal = Calendar.getInstance();
            final int nullNumber = -1;
            cal.setTime(Utils.convertStringToTimestamp(Constants.YYYY_MM_DD_HH_MM_SS, request.getDateStart()));
            // get current month
            String currentTime = Utils.convertDateToString(Constants.YYYY_MM, cal.getTime());
            Integer currentMonth = cal.get(Calendar.MONTH);
            // get previous month
            cal.add(Calendar.MONTH, -1);
            String previousTime = Utils.convertDateToString(Constants.YYYY_MM, cal.getTime());
            Integer previousMonth = cal.get(Calendar.MONTH);
            // get previous month
            cal.add(Calendar.MONTH, -1);
            String doublePreviousTime = Utils.convertDateToString(Constants.YYYY_MM, cal.getTime());
            // get previous remain day-off/OT compensatory
            Optional<DetailTimeKeeping> previousKeeping = detailTimeKeepingRepository.getOtByTime(previousTime,
                    currentSectionUserId);
            if (!previousKeeping.isPresent() || previousKeeping.get() == null) {
                previousMonthExists = false;
                previousKeeping = detailTimeKeepingRepository.getOtByTime(doublePreviousTime, currentSectionUserId);
                // if the previous or double previous month value does not exists, it means user
                // has been registered recently
                if (!previousKeeping.isPresent()) {
                    // -> register a new detailTimeKeeping record of previous month for user
                    previousKeeping = Optional
                            .of(utilsComponent.initializeDetailKeeping(currentSectionUserId, previousTime));
                }
            }
            //
            // request type is paid day-off/OT compensatory
            int bookingType = checkPaidLeave ? Booking_OFF_TYPE.DAY_OFF.getValue()
                    : checkCompensatoryLeave ? Booking_OFF_TYPE.COMPENSATORY_LEAVE.getValue() : nullNumber;
            // get remain paidOff/compensatory
            float remainLeave = checkPaidLeave ? previousKeeping.get().getLeaveRemainNow()
                    : checkCompensatoryLeave ? previousKeeping.get().getCsrLeaveNow() : (float) nullNumber;
            // if the request is valid
            if (bookingType != nullNumber) {
                // get total paid day-off booking in current month
                List<BookingDayOff> listBookingDayOff = bookingDayOffRepository
                        .findTotalDayOffInMonth(currentSectionUserId, bookingType, currentTime);
                if (request.getId() != null) {
                    listBookingDayOff = listBookingDayOff.stream().filter(item -> item.getId() != request.getId())
                            .collect(Collectors.toList());
                }
                float totalDateOff = getTotalTimeOff(listBookingDayOff, request.getDateStart(), request.getDateEnd(),
                        currentMonth);
                if (!previousMonthExists) {
                    totalDateOff += getTotalTimeOff(bookingDayOffRepository.findTotalDayOffInMonth(currentSectionUserId,
                            bookingType, previousTime), null, null, previousMonth);
                }

                //kiểm tra xem còn ngày pheps ko

//                if (remainLeave < totalDateOff) {
//                    return new ApiResponse(Constants.HTTP_CODE_500, Constants.REMAIN_DAY_OFF_NOT_ENOUGH, null);
//                }
            }
        }
        //
        if (request.getId() == null) {
            return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, createBooking(request));
        }
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, updateBooking(request));
    }

    public int createBooking(CreateOrUpdateBookingRequest request) {
        Optional<Employee> optionalEmployee = employeeRepository.findById(commonService.idUserAccountLogin());
        BookingDayOff bookingDayOff = new BookingDayOff();
        request.setIdUser(optionalEmployee.get().getId());
        setBooking(bookingDayOff, request);
        bookingDayOff.setEmployee(optionalEmployee.get());
        bookingDayOff.setCommonRegister();
        bookingDayOffRepository.save(bookingDayOff);
        return bookingDayOff.getId();
    }

    public int updateBooking(CreateOrUpdateBookingRequest request) {
        Optional<BookingDayOff> optionalBookingDayOff = bookingDayOffRepository.findById(request.getId());
        if (optionalBookingDayOff.isEmpty()) {
            throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
        }
        setBooking(optionalBookingDayOff.get(), request);
        optionalBookingDayOff.get().setCommonUpdate();
        bookingDayOffRepository.save(optionalBookingDayOff.get());
        return optionalBookingDayOff.get().getId();
    }

    public ApiResponse getBookingById(Integer id) {
        Optional<BookingDayOff> optionalBookingDayOff = bookingDayOffRepository.findById(id);
        if (optionalBookingDayOff.isEmpty()) {
            throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
        }
        DetailBookingDayOffResponse detailDayOff = new DetailBookingDayOffResponse();
        detailDayOff.setBookingDayOff(optionalBookingDayOff.get());
        List<String> approverIDs = detailDayOff.getBookingDayOff().getApproverIDs();
        if (approverIDs != null && approverIDs.size() > 0) {
            try {
                List<String> approverFullName = new ArrayList<>();
                for (int i = 0; i < approverIDs.size(); i++) {
                    approverFullName
                            .add((profileRepository.findFullNameByEmployeeId(Integer.parseInt(approverIDs.get(i)))));
                }
                detailDayOff.setApproverFullName(approverFullName);
            } catch (Exception e) {
            }

        }
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, detailDayOff);
    }

    public ApiResponse getAllBookingByEmployeeAndTime(String calculatorDate, Integer employeeId) {
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
        List<BookingDayOff> bookingResponseList = bookingDayOffRepository.findByEmployeeIdAndTime(employeeId, startDate,
                endDate);
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, bookingResponseList);
    }

    public ApiResponse deleteBooking(Integer id) {
        Optional<BookingDayOff> optionalBookingDayOff = bookingDayOffRepository.findById(id);
        if (optionalBookingDayOff.isEmpty()) {
            throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
        }
        optionalBookingDayOff.get().setDeleteFlag(true);
        bookingDayOffRepository.save(optionalBookingDayOff.get());
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, null);
    }

    private void setBooking(BookingDayOff bookingDayOff, CreateOrUpdateBookingRequest request) {
        bookingDayOff
                .setRequestDay(Utils.convertStringToTimestamp(Constants.YYYY_MM_DD_HH_MM_SS, request.getDateStart()));
        bookingDayOff.setBackDay(Utils.convertStringToTimestamp(Constants.YYYY_MM_DD_HH_MM_SS, request.getDateEnd()));
        bookingDayOff.setReason(request.getReason());
        if (request.getRegistrationType().equals(CommonFilter.BOOKING_DAY_OFF.DAY_OFF.toString())) {
            bookingDayOff.setStatus(0);
        } else if (request.getRegistrationType().equals(CommonFilter.BOOKING_DAY_OFF.WORKING_LATE.toString())
                || request.getRegistrationType().equals(CommonFilter.BOOKING_DAY_OFF.WORKING_EARLY.toString())) {
            bookingDayOff.setStatus(1);
        } else if (request.getRegistrationType().equals(CommonFilter.BOOKING_DAY_OFF.REMOTE.toString())) {
            bookingDayOff.setStatus(2);
        } else if (request.getRegistrationType().equals(CommonFilter.BOOKING_DAY_OFF.GO_OUT.toString())) {
            bookingDayOff.setStatus(3);
        } else if (request.getRegistrationType().equals(CommonFilter.BOOKING_DAY_OFF.OT.toString())) {
            bookingDayOff.setStatus(4);
        } else if (request.getRegistrationType().equals(CommonFilter.BOOKING_DAY_OFF.PERSONAL_LEAVE.toString())) {
            bookingDayOff.setStatus(5);
        } else if (request.getRegistrationType().equals(CommonFilter.BOOKING_DAY_OFF.COMPENSATORY_LEAVE.toString())) {
            bookingDayOff.setStatus(6);
        } else if (request.getRegistrationType().equals(CommonFilter.BOOKING_DAY_OFF.UNPAID_LEAVE.toString())) {
            bookingDayOff.setStatus(7);
        } else if (request.getRegistrationType().equals(CommonFilter.BOOKING_DAY_OFF.KEEPING_FORGET.toString())) {
            bookingDayOff.setStatus(8);
        } else if (request.getRegistrationType().equals(CommonFilter.BOOKING_DAY_OFF.WORKUP_FOR_LATE.toString())) {
            bookingDayOff.setStatus(10); // 9 is for equipment registering application
        }
        // set bookingDayOff approver_ids and initialize value of approve_progress
        List<String> approverIDs = request.getApprover();
        // Create/overwrite a '0' status for approve_progress
        if (approverIDs.size() > 0) {
            // create a '0' status list
            List<String> approveProgress = new ArrayList<>();
            List<String> approveReason = new ArrayList<>();
            for (int i = 0; i < approverIDs.size(); i++) {
                approveProgress.add("0");
                approveReason.add("");
            }

            // set to bookingDayOff
            bookingDayOff.setApproverIDs(approverIDs);
            bookingDayOff.setApproveProgress(approveProgress);
            bookingDayOff.setApproveReason(approveReason);
            bookingDayOff.setSelectedTypeTime(request.getSelectedTypeTime());
        }
        bookingDayOff.setConfirm(0);
        bookingDayOff.setApprover(0);

        //
        if (!request.getRelatedEmployee().isEmpty()) {
            bookingDayOff.setRelatedEmployeeIDs(request.getRelatedEmployee());
        }
        if (request.getProjectId() != null) {
            bookingDayOff.setProjectId(request.getProjectId());
        }
    }

    public ApiResponse getListConfigDayOff(Date requestDate) {
//        ListConfigDayOffResponse
        Calendar calendar = Calendar.getInstance();
        if (Objects.isNull(requestDate)) {
            throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
        }
        calendar.setTime(requestDate);
        int calculatorMonth = calendar.get(Calendar.MONTH);
        calendar.set(Calendar.MONTH, calculatorMonth);
        calendar.set(Calendar.DATE, 1);
        String startDate = Utils.convertDateToString(Constants.YYYY_MM_DD, calendar.getTime());
        int maxDayInMonth = calendar.getActualMaximum(Calendar.DATE);
        calendar.set(Calendar.DATE, maxDayInMonth);
        String endDate = Utils.convertDateToString(Constants.YYYY_MM_DD, calendar.getTime());
        List<ListConfigDayOffResponse> configDayOffList = configDayOffRepository.getListConfigDtoByTime(startDate,
                endDate);

        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, configDayOffList);
    }

    @Async
    public void sendMailBooking(Integer bookingDayOffId, Integer userId) {
        Optional<Employee> employee = employeeRepository.findByIdAndDeleteFlag(userId, Constants.DELETE_NONE);
        Profile profile = employee.get().getProfile().stream().collect(Collectors.toList()).get(0);
        BookingDayOff request = bookingDayOffRepository.findById(bookingDayOffId).get();
        String newLine = "\n";
        String dashWithSpace = " - ";
        if (request == null) {
            throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
        }
        // ** get list emails send to
        List<String> listMailTo = new ArrayList<>();
//        listMailTo.add(mailTo);
        listMailTo.add("anhlhh@its-global.vn");
//        listMailTo.add("admin@its-global.vn");
        if (request.getApproverIDs() != null && request.getApproverIDs().size() > 0) {
            listMailTo.addAll(getEmailEmployee(request.getApproverIDs()));
        } else {
            listMailTo.addAll(bookingDayOffRepository.getListEmailSendBooking(employee.get().getDepartment().getId()));
        }
        // ** get list emails cc to
        List<String> listMailCC = new ArrayList<>();
        if (request.getRelatedEmployeeIDs() != null && request.getRelatedEmployeeIDs().size() > 0) {
            listMailCC.addAll(getEmailEmployee(request.getRelatedEmployeeIDs()));
        }
        if (employee.get().getBookingDayOffNotify()) {
            listMailCC.add(employee.get().getEmail());
        }

        if (listMailTo.size() == 0) {
            if (listMailCC.size() > 0) {
                listMailTo.addAll(listMailCC);
                listMailCC.clear();
            } else {
                return;
            }
        }
        String typeBooking = "";
        typeBooking = Utils.getLabelDayOffType(request.getStatus());

        // ** email subject
        String departmentPart = "";
        if (employee.get().getDepartment() != null) {
            departmentPart = dashWithSpace + employee.get().getDepartment().getName();
        }
        String emailSubject = profile.getFullName() + departmentPart + dashWithSpace + typeBooking + dashWithSpace
                + Utils.convertDateToString(Constants.DD_MM_YYYY, request.getRequestDay());

        // ** email content
        StringBuilder emailContent = new StringBuilder();
        // Append full name
        emailContent.append("Họ tên: ");
        emailContent.append(profile.getFullName());
        // Append register type
        emailContent.append(newLine);
        emailContent.append("Loại đăng ký: ");
        emailContent.append(typeBooking);
        // Append Time
        emailContent.append(newLine);
        emailContent.append("Thời gian: ");

        LocalTime zeroTime = LocalTime.of(0, 0, 0);
        LocalDate requestDate = request.getRequestDay().toLocalDateTime().toLocalDate();
        LocalDate backDate = request.getBackDay().toLocalDateTime().toLocalDate();
        LocalTime requestTime = request.getRequestDay().toLocalDateTime().toLocalTime();
        LocalTime backTime = request.getBackDay().toLocalDateTime().toLocalTime();

        emailContent.append(requestTime.toString() + dashWithSpace + backTime.toString());
        emailContent.append(" ngày");
        Long daysBetween = Duration
                .between(LocalDateTime.of(requestDate, zeroTime), LocalDateTime.of(backDate, zeroTime)).toDays();
        if (daysBetween == 0) {
            emailContent.append(" " + Utils.convertDateToString(Constants.DD_MM_YYYY, request.getRequestDay()));
        } else {
            Calendar cal = Calendar.getInstance();
            for (int i = 0; i <= daysBetween.intValue(); i++) {
                cal.setTime(request.getRequestDay());
                cal.add(Calendar.DATE, i);
                emailContent.append(" " + Utils.convertDateToString(Constants.DD_MM, cal.getTime()));
                emailContent.append(",");
            }
            emailContent.deleteCharAt(emailContent.length() - 1);
        }
        // Append reason
        emailContent.append(newLine);
        emailContent.append("Lý do: ");
        emailContent.append(request.getReason());
        // Append confirm link
        emailContent.append(newLine);
        emailContent.append("Link xác nhận: http://localhost:4200/confirm-day-off");
        emailContent.append(newLine);
        emailContent.append("(Chỉ Leader có quyền phê duyệt)");

        // ** configure message and send mail
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            // send from
            message.setFrom(mailFrom);
            // send to
            String[] arrayTo = (String[]) listMailTo.toArray(String[]::new);
            message.setTo(arrayTo);
            // cc to
            String[] arrayCC = (String[]) listMailCC.toArray(String[]::new);
            message.setCc(arrayCC);
            // subject
            message.setSubject(emailSubject);
            // content
            message.setText(emailContent.toString());

            emailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getEmailEmployee(List<String> idList) {
        List<Employee> employee = employeeRepository.getListEmployeeByStringListId(idList);
        employee = employee.stream().filter(item -> item.getBookingDayOffNotify() == true).collect(Collectors.toList());

        return employee.stream().map(item -> item.getEmail()).collect(Collectors.toList());
    }

    //
    public ApiResponse getStandardTime() {
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS,
                standardTimeRepository.findTop1ByStandardTimeOrderByIdDesc());
    }

    // get leader list for approver choosing
    public ApiResponse getAllEmployeeListDropdown() {
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, Arrays
                .asList(bookingDayOffRepository.getAllApprover(), bookingDayOffRepository.getAllEmployeeDropdown()));
    }

    // calculate total time off
    public float getTotalTimeOff(List<BookingDayOff> list, String dateStart, String dateEnd, Integer currentMonth) {
        Calendar cal = Calendar.getInstance();
        float totalDateOffs = 0;
        // get standard time checkin-checkout
        Optional<StandardTime> optionalStandardTime = standardTimeRepository.findTop1ByStandardTimeOrderByIdDesc();
        cal.setTime(optionalStandardTime.get().getCheckInMorning());
        int inAM = getSecond(cal);
        cal.setTime(optionalStandardTime.get().getCheckOutMorning());
        int outAM = getSecond(cal);
        cal.setTime(optionalStandardTime.get().getCheckInAfternoon());
        int inPM = getSecond(cal);
        cal.setTime(optionalStandardTime.get().getCheckOutAfternoon());
        int outPM = getSecond(cal);

        // initialize the request date-off if it exists
        if (dateStart != null && dateEnd != null) {
            Date startDate = Utils.convertStringToTimestamp(Constants.YYYY_MM_DD_HH_MM_SS, dateStart);
            Date endDate = Utils.convertStringToTimestamp(Constants.YYYY_MM_DD_HH_MM_SS, dateEnd);
            LocalDateTime startDateLocal = new Timestamp(startDate.getTime()).toLocalDateTime();
            int daysBetween = daysBetweenDropWeekends(new Timestamp(startDate.getTime()),
                    new Timestamp(endDate.getTime()), currentMonth);
            if (daysBetween > 1) {
                cal.setTime(endDate);
                cal.set(Calendar.DATE, startDateLocal.getDayOfMonth());
                cal.set(Calendar.MONTH, startDateLocal.getMonthValue() - 1);
                cal.set(Calendar.YEAR, startDateLocal.getYear());
                totalDateOffs += getEachTimeOff(startDate, cal.getTime(), inAM, outAM, inPM, outPM) * (daysBetween);
            } else {
                totalDateOffs += getEachTimeOff(startDate, endDate, inAM, outAM, inPM, outPM);
            }
        }

        // add all day-offs from booking history in month
        for (BookingDayOff bookingDayOff : list) {
            int daysBetween = daysBetweenDropWeekends(bookingDayOff.getRequestDay(), bookingDayOff.getBackDay(),
                    currentMonth);
            if (daysBetween > 1) {
                cal.setTime(bookingDayOff.getBackDay());
                cal.set(Calendar.DATE, bookingDayOff.getRequestDay().toLocalDateTime().getDayOfMonth());
                cal.set(Calendar.MONTH, bookingDayOff.getRequestDay().toLocalDateTime().getMonthValue() - 1);
                cal.set(Calendar.YEAR, bookingDayOff.getRequestDay().toLocalDateTime().getYear());
                totalDateOffs += getEachTimeOff(bookingDayOff.getRequestDay(), cal.getTime(), inAM, outAM, inPM, outPM)
                        * (daysBetween);
            } else {
                totalDateOffs += getEachTimeOff(bookingDayOff.getRequestDay(), bookingDayOff.getBackDay(), inAM, outAM,
                        inPM, outPM);
            }
        }

        return totalDateOffs;
    }

    public int daysBetweenDropWeekends(Date startDate, Date endDate, Integer currentMonth) {
        long daysExceptWeekends = (long) 0;
        Calendar cal = Calendar.getInstance();
        while (startDate.compareTo(endDate) <= 0) {
            cal.setTime(startDate);
            Integer currentDay = cal.get(Calendar.DAY_OF_WEEK);
            if (!(currentDay == Calendar.SATURDAY || currentDay == Calendar.SUNDAY
                    || cal.get(Calendar.MONTH) != currentMonth)) {
                daysExceptWeekends += 1;
            }
            cal.add(Calendar.DATE, 1);
            startDate = cal.getTime();
        }
        return Math.toIntExact(daysExceptWeekends);
    }

    public float getEachTimeOff(Date startDate, Date endDate, int inAM, int outAM, int inPM, int outPM) {
        Calendar cal = Calendar.getInstance();
        // check request-back time duration in day
        boolean requestMorning = false;
        boolean requestAfternoon = false;
        boolean backMorning = false;
        boolean backAfternoon = false;
        // get total all days off between request date and end date
        @SuppressWarnings("deprecation")
        float totalDateOff = new Long(
                Math.max(((endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24) - 1), 0)).floatValue();

        // get timestamp of startDate's time hh:mm:ss
        cal.setTime(startDate);
        int requestStartHour = getSecond(cal);
        // get timestamp of endDate's time hh:mm:ss
        cal.setTime(endDate);
        int requestEndHour = getSecond(cal);

        // check if request date is from morning
        if (requestStartHour >= inAM && requestStartHour <= outAM) {
            requestMorning = true;
        }
        // else if it starts from afternoon
        else if (requestStartHour >= inPM && requestStartHour <= outPM) {
            requestAfternoon = true;
        }
        // check if end date ends at morning
        if (requestEndHour >= inAM && requestEndHour <= outAM) {
            backMorning = true;
        }
        // else if it ends at afternoon
        else if (requestEndHour >= inPM && requestEndHour <= outPM) {
            backAfternoon = true;
        }

        if (totalDateOff == 0) {// if leave in a day
            // if in a day, leave only half a day
            if ((requestMorning && backMorning) || (requestAfternoon && backAfternoon))
                totalDateOff += 0.5;
            else
                totalDateOff += 1;
        } else { // if leave in many days
            // dates between startdate and enddate always = 1
            // then, check date at the start if:
            if (requestMorning)
                totalDateOff += 1; // request from morning -> full day-off
            if (requestAfternoon)
                totalDateOff += 0.5; // request from afternoon -> half day-off

            // and, check date at the end if:
            if (backMorning)
                totalDateOff += 0.5; // back at morning -> half day-off
            if (backAfternoon)
                totalDateOff += 1; // back at afternoon -> full day-off
        }

        return totalDateOff;
    }

    //
    public int getSecond(Calendar calendar) {
        return calendar.get(Calendar.HOUR_OF_DAY) * 3600 + calendar.get(Calendar.MINUTE) * 60
                + calendar.get(Calendar.SECOND);
    }

    public ApiResponse getLeavePaidRemainByEmployeeAndMonth(Integer employeeId, String timeSave) {
        boolean hasLastMonth = true;
        Optional<Employee> employee = employeeRepository.findByIdAndDeleteFlag(employeeId, Constants.DELETE_NONE);
        Calendar calendar = Calendar.getInstance();
        if (!employee.isPresent()) {
            throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
        }
        // get and check data of last month
        String previousMonth = Utils.getPreviousMonth(timeSave);
        DetailTimeKeeping detailTimeKeeping = detailTimeKeepingRepository.findRemainLeavesByIdAndMonth(employeeId,
                previousMonth);
        if (detailTimeKeeping == null) {
            hasLastMonth = false;
        }
        // get standard in/out
        Optional<StandardTime> optionalStandardTime = standardTimeRepository.findTop1ByStandardTimeOrderByIdDesc();
        calendar.setTime(optionalStandardTime.get().getCheckInMorning());
        int inAM = getSecond(calendar);
        calendar.setTime(optionalStandardTime.get().getCheckOutMorning());
        int outAM = getSecond(calendar);
        calendar.setTime(optionalStandardTime.get().getCheckInAfternoon());
        int inPM = getSecond(calendar);
        calendar.setTime(optionalStandardTime.get().getCheckOutAfternoon());
        int outPM = getSecond(calendar);

        // get current month
        Calendar cal = Calendar.getInstance();
        cal.setTime(Utils.convertStringToDate(Constants.YYYY_MM_DD, timeSave));
        String currentMonth = Utils.convertDateToString(Constants.YYYY_MM, cal.getTime());
        Integer intCurrentMonth = cal.get(Calendar.MONTH);

        // booking day off records of current month
        List<BookingDayOff> allBookingDayOffInMonth = bookingDayOffRepository.findTotalAllDayOffInMonth(employeeId,
                currentMonth);
        List<BookingDayOff> paidLeavesInMonth = allBookingDayOffInMonth.stream().filter(item -> item.getStatus() == 0)
                .collect(Collectors.toList());
        List<BookingDayOff> earlyLateInMonth = allBookingDayOffInMonth.stream().filter(item -> item.getStatus() == 1)
                .collect(Collectors.toList());
        List<BookingDayOff> remoteTimesInMonth = allBookingDayOffInMonth.stream().filter(item -> item.getStatus() == 2)
                .collect(Collectors.toList());
        List<BookingDayOff> otListInMonth = allBookingDayOffInMonth.stream()
                .filter(item -> item.getStatus() == 4 && item.getConfirm() == Constants.CONFIRM_ACCEPT)
                .collect(Collectors.toList());
        List<BookingDayOff> awayFromDeskInMonth = allBookingDayOffInMonth.stream().filter(item -> item.getStatus() == 3)
                .collect(Collectors.toList());
        List<BookingDayOff> personalLeavesInMonth = allBookingDayOffInMonth.stream()
                .filter(item -> item.getStatus() == 5).collect(Collectors.toList());
        List<BookingDayOff> compensatoryLeavesInMonth = allBookingDayOffInMonth.stream()
                .filter(item -> item.getStatus() == 6).collect(Collectors.toList());
        List<BookingDayOff> unpaidLeavesInMonth = allBookingDayOffInMonth.stream().filter(item -> item.getStatus() == 7)
                .collect(Collectors.toList());
        List<BookingDayOff> forgotTimeKeepingInMonth = allBookingDayOffInMonth.stream()
                .filter(item -> item.getStatus() == 8).collect(Collectors.toList());

        // previous month's leaves remain
        Float totalPaidLeavesPreviousMonth = (float) 0;
        Float totalCompensatoryLeavesPreviousMonth = (float) 0;
        if (!hasLastMonth) {
            String antepenultimateMonth = Utils.getPreviousMonth(previousMonth);
            detailTimeKeeping = detailTimeKeepingRepository.findRemainLeavesByIdAndMonth(employeeId,
                    antepenultimateMonth);
            List<BookingDayOff> allBookingDayOffPreviousMonth = bookingDayOffRepository
                    .findTotalAllDayOffInMonth(employeeId, previousMonth);
            //
            List<BookingDayOff> paidLeavesPreviousMonth = allBookingDayOffPreviousMonth.stream()
                    .filter(item -> item.getStatus() == 0).collect(Collectors.toList());
            totalPaidLeavesPreviousMonth = calculateTotalLeaves(paidLeavesPreviousMonth, intCurrentMonth);
            //
            List<BookingDayOff> compensatoryLeavesPreviousMonth = allBookingDayOffPreviousMonth.stream()
                    .filter(item -> item.getStatus() == 6).collect(Collectors.toList());
            totalCompensatoryLeavesPreviousMonth = calculateTotalLeaves(compensatoryLeavesPreviousMonth,
                    intCurrentMonth);
        }
        Float remainPaidLeaves = detailTimeKeeping == null ? 0 : detailTimeKeeping.getLeaveRemainNow();
        Float remainCompensatoryLeaves = detailTimeKeeping == null ? 0 : detailTimeKeeping.getCsrLeaveNow();

        // calculate paid leave remains
        Float totalPaidLeavesInMonth = calculateTotalLeaves(paidLeavesInMonth, intCurrentMonth);
        remainPaidLeaves -= (totalPaidLeavesInMonth + totalPaidLeavesPreviousMonth);

        // calculate compensatory leave remains
        Float totalCompensatoryLeavesInMonth = calculateTotalLeaves(compensatoryLeavesInMonth, intCurrentMonth);
        remainCompensatoryLeaves -= (totalCompensatoryLeavesInMonth + totalCompensatoryLeavesPreviousMonth);

        // calculate unpaid leaves in month
        Float totalUnpaidLeavesInMonth = calculateTotalLeaves(unpaidLeavesInMonth, intCurrentMonth);

        // calculate remote times in month
        Float totalRemoteTimesInMonth = calculateTotalLeaves(remoteTimesInMonth, intCurrentMonth);

        // calculate personal leaves in month
        Float totalPersonalLeavesInMonth = calculateTotalLeaves(personalLeavesInMonth, intCurrentMonth);

        // calculate keeping forgot times
        Integer totalForgotTimeKeeping = forgotTimeKeepingInMonth != null && forgotTimeKeepingInMonth.size() > 0
                ? forgotTimeKeepingInMonth.size()
                : 0;

        // calculate early/late times
        Integer lateTimes = earlyLateInMonth != null && earlyLateInMonth.size() > 0 ? earlyLateInMonth.size() : 0;

        // calculate early/late/awd total hour
        List<BookingDayOff> earlyLateAwdInMonth = Stream.concat(earlyLateInMonth.stream(), awayFromDeskInMonth.stream())
                .collect(Collectors.toList());
        int totalTimeCheckInLate = 0;
        int totalTimeCheckOutEarly = 0;
        for (BookingDayOff item : earlyLateAwdInMonth) {
            // time back
            calendar.setTime(item.getBackDay());
            int backDaySecond = getSecond(calendar);
            // time request
            calendar.setTime(item.getRequestDay());
            int requestDaySecond = getSecond(calendar);
            //
            if (requestDaySecond < outAM && backDaySecond <= inPM) {
                if (requestDaySecond != inAM && backDaySecond == outAM) {
                    totalTimeCheckOutEarly += Math.max((outAM - requestDaySecond), 0);
                }
                if (requestDaySecond == inAM && backDaySecond != outAM) {
                    totalTimeCheckInLate += Math.max((backDaySecond - inAM), 0);
                }
            } else if (requestDaySecond >= outAM && backDaySecond <= outPM) {
                if (requestDaySecond != inPM && backDaySecond == outPM) {
                    totalTimeCheckOutEarly += Math.max((outPM - requestDaySecond), 0);
                }
                if (requestDaySecond == inPM && backDaySecond != outPM) {
                    totalTimeCheckInLate += Math.max((backDaySecond - inPM), 0);
                }
            }
        }

        // calculate away from desk times
        Integer awayFromDeskTimes = awayFromDeskInMonth != null && awayFromDeskInMonth.size() > 0
                ? awayFromDeskInMonth.size()
                : 0;

        // calculate OT times in month
        OtGeneral otGeneral = new OtGeneral();
        approveGeneralService.calculatorOTAll(otListInMonth, otGeneral, null);

        // update to return result
        DetailTimeKeepingDisplayResponse detailTimeKeepingReturn = new DetailTimeKeepingDisplayResponse();
        detailTimeKeepingReturn.setLeaveDayAccept(
                totalPaidLeavesInMonth + totalCompensatoryLeavesInMonth + totalPersonalLeavesInMonth);
        detailTimeKeepingReturn.setRemoteTime(totalRemoteTimesInMonth);
        detailTimeKeepingReturn.setUnpaidLeave(totalUnpaidLeavesInMonth);
        detailTimeKeepingReturn.setLeaveRemainNow(remainPaidLeaves);
        detailTimeKeepingReturn.setCsrLeaveNow(remainCompensatoryLeaves);
        detailTimeKeepingReturn.setKeepingForget(totalForgotTimeKeeping);
        detailTimeKeepingReturn.setLateTime(lateTimes + awayFromDeskTimes);
        detailTimeKeepingReturn.setLateHour(Utils.convertSecondsToDate(totalTimeCheckInLate + totalTimeCheckOutEarly));
        detailTimeKeepingReturn.setOtNormal(otGeneral.getOtNormal());
        detailTimeKeepingReturn.setOtMorning7(otGeneral.getOtMorning7());
        detailTimeKeepingReturn.setOtSatSun(otGeneral.getOtSatSun());
        detailTimeKeepingReturn.setOtHoliday(otGeneral.getOtHoliday());
        detailTimeKeepingReturn.setAwdTime(awayFromDeskTimes);

        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, detailTimeKeepingReturn);
    }

    public Float calculateTotalLeaves(List<BookingDayOff> listDayOff, Integer currentMonth) {
        float totalDayOff = 0;
        if (listDayOff != null && listDayOff.size() > 0) {
            for (BookingDayOff item : listDayOff) {
                int daysBetween = daysBetweenDropWeekends(item.getRequestDay(), item.getBackDay(), currentMonth);
                if (item.getSelectedTypeTime() != null) {
                    totalDayOff += (float) (item.getSelectedTypeTime().equals("full") ? 1 : 0.5)
                            * (daysBetween == 0 ? 1 : daysBetween);
                }
            }
        }
        return totalDayOff;
    }

    // upload evidence image to S3
    public ApiResponse uploadEvidenceImg(Integer id, MultipartFile evidenceImg) {
        // check variables
        if (id == null) {
            return new ApiResponse(Constants.HTTP_CODE_400, Constants.RECORD_DOES_NOT_EXIST, null);
        }
        Optional<BookingDayOff> bookingDayoff = bookingDayOffRepository.findById(id);
        if (!bookingDayoff.isPresent()) {
            return new ApiResponse(Constants.HTTP_CODE_400, Constants.RECORD_NOT_FOUND, null);
        }
        if (evidenceImg == null || evidenceImg.isEmpty()) {
            return new ApiResponse(Constants.HTTP_CODE_400, Constants.FIELD_INVALID, null);
        }
        // start upload function
        try {
            String fileName = StringUtils.cleanPath(evidenceImg.getOriginalFilename());
            String imageUrl = awsS3Service.uploadImage(evidenceImg, fileName, Constants.EVIDENCE);
            bookingDayoff.get().setEvidenceImage(imageUrl);
            bookingDayoff.get().setCommonUpdate();
        } catch (Exception awsErr) {
            return new ApiResponse(Constants.HTTP_CODE_500, awsErr.getMessage(), null);
        }

        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, null);
    }

    public ApiResponse getProjectDropdown() {
        List<DropDownResponse> projectDropdown = projectsRepository.getProjectDropdown();
        if (projectDropdown.isEmpty()) {
            return new ApiResponse(Constants.HTTP_CODE_400, Constants.RECORD_NOT_FOUND, null);
        }
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, projectDropdown);
    }

//    public ApiResponse buildSlackMessage(Integer bookingId, String registType) {
//        BookingDayOff request = bookingDayOffRepository.findById(bookingId).get();
//        StringBuilder messageContent = new StringBuilder();
//        String space = " ";
//        messageContent.append("<!here>");
//        messageContent.append(space);
//        messageContent.append(messageContentBody(request, registType));
//
//        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, messageContent.toString());
//    }

//    public ApiResponse buildDiscordMessage(Integer bookingId, String registType) {
//        BookingDayOff request = bookingDayOffRepository.findById(bookingId).get();
//        StringBuilder messageContent = new StringBuilder();
//        String space = " ";
//
//        if (registType.equalsIgnoreCase("đăng ký cấp thiết bị")) {
//            Profile adminEmployee = profileRepository.findByEmployeeId(80); // id 17: Khuong Thi Hao(admin)
////            if (adminEmployee.getDiscordId() != null) {
////                messageContent.append("<@" + adminEmployee.getDiscordId() + ">");
////                messageContent.append(space);
////            }
//        } else {
//            // mention approvers
//            if (request.getApproverIDs() != null && request.getApproverIDs().size() > 0) {
//                List<String> approverDiscordIds = employeeRepository
//                        .getListDiscordIdByStringListId(request.getApproverIDs());
//                approverDiscordIds.forEach(item -> {
//                    if (item != null) {
//                        messageContent.append("<@" + item + ">");
//                        messageContent.append(space);
//                    }
//                });
//            }
//            // mention related employees
//            if (request.getRelatedEmployeeIDs() != null && request.getRelatedEmployeeIDs().size() > 0) {
//                List<String> relatedDiscordIds = employeeRepository
//                        .getListDiscordIdByStringListId(request.getRelatedEmployeeIDs());
//                relatedDiscordIds.forEach(item -> {
//                    if (item != null) {
//                        messageContent.append("<@" + item + ">");
//                        messageContent.append(space);
//                    }
//                });
//            }
//        }
//
//        // message body
//        messageContent.append(messageContentBody(request, registType));
//
//        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, messageContent.toString());
//    }

//    public String messageContentBody(BookingDayOff request, String registType) {
//        String space = " ";
//        Employee employee = employeeRepository.findSingleRecord(request.getEmployee().getId());
//
//        StringBuilder messageContent = new StringBuilder();
//        // name
//        messageContent.append(employee.getEmployeeCode());
//        messageContent.append(space);
//        messageContent.append(employee.getProfile().stream().collect(Collectors.toList()).get(0).getFullName());
//        if (employee.getDepartment() != null) {
//            messageContent.append("(" + employee.getDepartment().getName() + ")");
//        }
//        // registration type
//        messageContent.append(space);
//        messageContent.append(registType);
//
//        if (registType.equalsIgnoreCase("đăng ký cấp thiết bị")) {
//            if (request.getConfirm() != Constants.CONFIRM_ACCEPT) {
//                throw new RecordNotFoundException("Đơn chưa được duyệt");
//            }
//            Map<Integer, String> equipmentCategory = Constants.EQUIPMENT_CATEGORY;
//            // equipment category
//            messageContent.append(space);
//            messageContent.append(equipmentCategory.get(request.getApprover()));
//            // date
//            messageContent.append(space);
//            messageContent.append("ngày");
//            messageContent.append(space);
//            messageContent.append(Utils.convertDateToString(Constants.DD_MM_YYYY, request.getRequestDay()));
//            // footer
//            messageContent.append(space);
//            messageContent.append("(Đã duyệt)");
//        } else {
//
//            // time
//            LocalTime zeroTime = LocalTime.of(0, 0, 0);
//            LocalDate requestDate = request.getRequestDay().toLocalDateTime().toLocalDate();
//            LocalDate backDate = request.getBackDay().toLocalDateTime().toLocalDate();
//            LocalTime requestTime = request.getRequestDay().toLocalDateTime().toLocalTime();
//            LocalTime backTime = request.getBackDay().toLocalDateTime().toLocalTime();
//            if (request.getStatus() == 1) {
//                messageContent.append(space);
//                messageContent.append("lúc "
//                        + (registType.trim().equals("xin đi muộn") ? backTime.toString() : requestTime.toString()));
//            } else if (request.getStatus() == 0 || request.getStatus() == 2 || request.getStatus() == 5
//                    || request.getStatus() == 6 || request.getStatus() == 7) {
//                messageContent.append(request.getSelectedTypeTime().equals("am") ? " sáng"
//                        : request.getSelectedTypeTime().equals("pm") ? " chiều" : "");
//            } else {
//                messageContent.append(space);
//                messageContent.append("từ");
//                messageContent.append(space);
//                messageContent.append(requestTime.toString() + " - " + backTime.toString());
//            }
//
//            // date
//            messageContent.append(space);
//            messageContent.append("ngày");
//            Long daysBetween = Duration
//                    .between(LocalDateTime.of(requestDate, zeroTime), LocalDateTime.of(backDate, zeroTime)).toDays();
//            if (daysBetween == 0) {
//                messageContent.append(" " + Utils.convertDateToString(Constants.DD_MM_YYYY, request.getRequestDay()));
//            } else {
//                Calendar cal = Calendar.getInstance();
//                for (int i = 0; i <= daysBetween.intValue(); i++) {
//                    cal.setTime(request.getRequestDay());
//                    cal.add(Calendar.DATE, i);
//                    messageContent.append(" " + Utils.convertDateToString(Constants.DD_MM, cal.getTime()));
//                    messageContent.append(",");
//                }
//                messageContent.deleteCharAt(messageContent.length() - 1);
//            }
//        }
//
//        return messageContent.toString();
//    }
}