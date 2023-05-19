package com.hrm.service.employee;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.hrm.common.CommonService;
import com.hrm.common.Constants;
import com.hrm.entity.Department;
import com.hrm.entity.Employee;
import com.hrm.entity.Profile;
import com.hrm.entity.RoleGroup;
import com.hrm.exception.RecordNotFoundException;
import com.hrm.model.ApiResponse;
import com.hrm.model.ResultPageResponse;
import com.hrm.model.dao.EmployeeDao;
import com.hrm.model.dao.RoleGroupDAO;
import com.hrm.model.request.LoginRequest;
import com.hrm.model.request.employee.ChangePasswordRequest;
import com.hrm.model.request.employee.EmailNotificationsRequest;
import com.hrm.model.request.employee.ListSearchRequest;
import com.hrm.model.request.hr.CreateOrEditEmployeeRequest;
import com.hrm.model.request.hr.ListEmployeeRequest;
import com.hrm.model.response.EmployeeToExcelResponse;
import com.hrm.model.response.employee.ProfilesGeneralResponse;
import com.hrm.model.response.hr.CreateOrEditEmployeeResponse;
import com.hrm.model.response.hr.EmployeeAndPasswordResponse;
import com.hrm.model.response.hr.ListEmployeeResponse;
import com.hrm.repository.DepartmentRepository;
import com.hrm.repository.EmployeeRepository;
import com.hrm.repository.ProfileRepository;
import com.hrm.repository.RoleGroupRepository;
import com.hrm.service.AwsS3Service;
import com.hrm.utils.Utils;
import com.hrm.utils.UtilsComponent;

import net.bytebuddy.utility.RandomString;

@Service
@Transactional
public class EmployeeService {

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private ProfileRepository profileRepository;

	@Autowired
	private RoleGroupRepository roleGroupRepository;

	@Autowired
	private CommonService commonService;

	@Autowired
	private EmployeeDao employeeDao;
	@Autowired
	private DepartmentRepository departmentRepository;

	@Autowired
	private RoleGroupDAO roleGroupDAO;

	@Autowired
	private AwsS3Service awsS3Service;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private UtilsComponent utilsComponent;

	@Autowired
	private JavaMailSender mailSender;

	public ApiResponse sendMailForgotPassword(String email) throws UnsupportedEncodingException, MessagingException {
		Optional<Employee> opEmployee = employeeRepository.findByEmailAndDeleteFlag(email.trim(),
				Constants.DELETE_NONE);
		if (opEmployee.isEmpty()) {
			throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
		}
//        String tokens = RandomString.make(30);
		Employee employee = opEmployee.get();
		String tokenForgot = RandomString.make(30);
		employee.setResetPasswordToken(tokenForgot);
		employee.setCommonUpdate();
		employee.setExpireTimeToken(Utils.getTimestamp());
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom("mailautobyhr@its-global.vn", "HRM Support");
		helper.setTo(employee.getEmail());
		String subject = "Đây là link thay đổi password của bạn.";
		String link = "http://hrm.its-global.vn/reset/password/" + tokenForgot;
		String content = "<p>Xin chào,</p>" + "<p>Bạn yêu cầu cài đặt lại mật khẩu.</p>"
				+ "<p>Nhấn vào link dưới đây để thay đổi mật khẩu của bạn:</p>" + "<p><a href=\"" + link
				+ "\">Thay đổi mật khẩu của tôi</a></p>" + "<br>"
				+ "<p>Bỏ qua email này nếu bạn nhớ mật khẩu, hoặc bạn không thực hiện yêu cầu này</p>";
		helper.setSubject(subject);
		helper.setText(content, true);

		mailSender.send(message);
		employeeRepository.save(employee);
		return new ApiResponse(Constants.HTTP_CODE_200, Constants.CREATE_SUCCESS, null);
	}

	public ApiResponse checkToken(String token) {
//        String email=Utils.decrypt(token, Constants.SECRET);
		Optional<Employee> opEmployee = employeeRepository.findByResetPasswordTokenAndDeleteFlag(token.trim(),
				Constants.DELETE_NONE);
		if (opEmployee.isEmpty()) {
			throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
		}
		LocalDateTime expireTime = opEmployee.get().getExpireTimeToken().toLocalDateTime();
		LocalDateTime currentDatetime = Utils.getTimestamp().toLocalDateTime();
		Duration dur = Duration.between(expireTime, currentDatetime);
		long minute = dur.toMinutes();
		if (minute > 5) {
			throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
		}
		String result = String.format("%d:%02d", dur.toHours(), dur.toMinutesPart());
		System.out.println(result);
		return new ApiResponse(Constants.HTTP_CODE_200, Constants.CREATE_SUCCESS, null);
	}

	public ApiResponse resetPassword(LoginRequest request) {
		Optional<Employee> opEmployee = employeeRepository
				.findByResetPasswordTokenAndDeleteFlag(request.getToken().trim(), Constants.DELETE_NONE);
		if (opEmployee.isEmpty()) {
			throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
		}
		LocalDateTime expireTime = opEmployee.get().getExpireTimeToken().toLocalDateTime();
		LocalDateTime currentDatetime = Utils.getTimestamp().toLocalDateTime();
		Duration dur = Duration.between(expireTime, currentDatetime);
		long minute = dur.toMinutes();
		if (minute > 5) {
			throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
		}
		opEmployee.get().setPassword(Utils.encrypt(request.getPassword(), Constants.SECRET));
		employeeRepository.save(opEmployee.get());
		return new ApiResponse(Constants.HTTP_CODE_200, Constants.CREATE_SUCCESS, null);

	}

	public ApiResponse changePassword(ChangePasswordRequest request) {
		Optional<Employee> optionalEmployee = employeeRepository.findByIdAndPassword(commonService.idUserAccountLogin(),
				Utils.encrypt(request.getCurrentPassword().trim(), Constants.SECRET));
		if (optionalEmployee.isEmpty()) {
			throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
		}
		optionalEmployee.get().setPassword(Utils.encrypt(request.getNewPassword(), Constants.SECRET));
		employeeRepository.save(optionalEmployee.get());
		return new ApiResponse(Constants.HTTP_CODE_200, Constants.CREATE_SUCCESS, null);
	}

	public ApiResponse getEmployeeProfile(Integer id) {
		id = id == 0 ? commonService.idUserAccountLogin() : commonService.idUserAccountLogin();

		ProfilesGeneralResponse profile = new ProfilesGeneralResponse();
		profile.setEmployee(employeeRepository.getEmployeeProfileById(id));
		profile.setHistory(employeeRepository.getListHistoryByEmployeeId(id));
		return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, profile);

	}

	public ApiResponse getEmployeeInformationById(Integer id) {
		if (id == null) {
			throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
		}
		// get optional employee value to check
		Optional<Employee> employeeOptional = employeeRepository.findById(id);
		if (!employeeOptional.isPresent()) {
			throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
		}
		// fetch mapping to response
		CreateOrEditEmployeeResponse response = employeeRepository.findEmployeeInformationById(id);

		// get current section user and his role
		Optional<Employee> currentEmployee = employeeRepository.findById(commonService.idUserAccountLogin());
		RoleGroup currentRole = currentEmployee.get().getRoleGroup();
		// if user is HR -> return response + decripted password
		if (currentRole != null && currentRole.getHrFlag()) {
			EmployeeAndPasswordResponse employee = new EmployeeAndPasswordResponse();
			employee.setResponse(response);
			employee.setPassword(Utils.decrypt(employeeOptional.get().getPassword(), Constants.SECRET));
			return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, employee);
		}
		// else -> return only response
		else {
			return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, response);
		}
	}

	public ApiResponse getEmployeeInformation(ListEmployeeRequest request) {
		ResultPageResponse result = employeeDao.getListEmployee(request);
		if (result == null) {
			return new ApiResponse(Constants.HTTP_CODE_400, Constants.DEPARTMENT_NOT_FOUND, null);
		}
		return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, result);
	}

	public ApiResponse updateEmployee(CreateOrEditEmployeeRequest request) {
		Optional<Employee> optionalEmployee = Optional.of(employeeRepository.findSingleRecord(request.getId()));
		if (!optionalEmployee.isPresent()) {
			throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
		}
		if (!optionalEmployee.get().getEmail().equals(request.getEmail().trim())) {
			Optional<Employee> employeeByEmail = employeeRepository.findByEmailAndDeleteFlag(request.getEmail(),
					Constants.DELETE_NONE);
			if (employeeByEmail.isPresent()) {
				return new ApiResponse(Constants.HTTP_CODE_405, Constants.ERROR, null);
			}
		}
		if (!optionalEmployee.get().getEmployeeCode().equals(request.getEmployeeCode().trim())) {
			Optional<Employee> employeeByCode = employeeRepository.findByEmployeeCode(request.getEmployeeCode(),
					Constants.DELETE_NONE);
			if (employeeByCode.isPresent()) {
				return new ApiResponse(Constants.HTTP_CODE_405, Constants.ERROR, null);
			}
		}

		Profile profile = profileRepository.findByEmployeeId(request.getId());
		setCreateOrUpdateEmployee(profile, request, optionalEmployee.get());
		employeeRepository.save(optionalEmployee.get());
		profileRepository.save(profile);
		return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, optionalEmployee.get().getId());
	}

	public ApiResponse createEmployee(CreateOrEditEmployeeRequest request) {
		Optional<Employee> employeeByCode = employeeRepository.findByEmployeeCode(request.getEmployeeCode(),
				Constants.DELETE_NONE);

		Optional<Employee> employeeByEmail = employeeRepository.findByEmailAndDeleteFlag(request.getEmail(),
				Constants.DELETE_NONE);

		if (employeeByCode.isPresent() || employeeByEmail.isPresent()) {
			return new ApiResponse(Constants.HTTP_CODE_405, Constants.RECORD_ALREADY_EXISTS, null);
		}
		Employee employee = new Employee();
		Profile profile = new Profile();
		setCreateOrUpdateEmployee(profile, request, employee);
		profileRepository.save(profile);

		// create a new detail time keeping record for new employee
		Date currentDate = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(currentDate);
		cal.add(Calendar.MONTH, -1);
		utilsComponent.initializeDetailKeeping(employee.getId(),
				Utils.convertDateToString(Constants.YYYY_MM, cal.getTime()));

		return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, employee.getId());
	}

	public void setCreateOrUpdateEmployee(Profile profile, CreateOrEditEmployeeRequest request, Employee employee) {
		Optional<Department> optionalDepartment = null;
		if (request.getDepartment() != null) {
			optionalDepartment = departmentRepository.findById(request.getDepartment());

			// ===== Update Department =====
			// If employee is created, not updated
//    		if(employee==null) {    			
			if (request.getId() == null) {
				// If department request is received, add 1 to the member of department
				if (optionalDepartment.isPresent()) {
					int newDepartmentMember = optionalDepartment.get().getNumberMember();
					optionalDepartment.get().setNumberMember(newDepartmentMember + 1);
					departmentRepository.save(optionalDepartment.get());
				}
			}
			// else if employee is updated, not created
			else {
				// Get current employee in database(not yet updated department)
				Optional<Employee> currentEmployee = employeeRepository.findById(request.getId());
				Profile employeeProfile = profileRepository.findByEmployeeId(request.getId());
				// If employee is still working in company
				if (employeeProfile.getDateOut() == null || request.getDateOut() == null) {
					// Get current employee's department
					Department currentDepartment = currentEmployee.get().getDepartment();
					// If department request is received
					if (optionalDepartment.isPresent()) {
						// If employee status is changed from notWorking to Working
						if (employeeProfile.getDateOut() != null && request.getDateOut() == null) {
							optionalDepartment.get().setNumberMember(optionalDepartment.get().getNumberMember() + 1);
							departmentRepository.save(optionalDepartment.get());
						} else {
							// If employee want to change department
							if (currentDepartment != null
									&& (currentDepartment.getId() != optionalDepartment.get().getId())) {
								int currentMember = currentDepartment.getNumberMember();
								currentDepartment.setNumberMember(currentMember - 1);
								optionalDepartment.get()
										.setNumberMember(optionalDepartment.get().getNumberMember() + 1);
								departmentRepository.save(currentDepartment);
								departmentRepository.save(optionalDepartment.get());
							}
						}
					}
					// Discard member from department
					else {
						int currentMember = currentDepartment.getNumberMember();
						currentDepartment.setNumberMember(currentMember - 1);
						departmentRepository.save(currentDepartment);
					}
				}
			}
		}
		// end
		Integer roleGroupId = getRoleGroupEmployee(request);
		Optional<RoleGroup> roleGroupOptional = null;
		if (roleGroupId != null) {
			roleGroupOptional = roleGroupRepository.findById(roleGroupId);
		}
		// employee
		employee.setEmployeeCode(request.getEmployeeCode());
		employee.setEmail(request.getEmail());
		employee.setDepartment(request.getDepartment() != null ? optionalDepartment.get() : null);
		employee.setPassword(Utils.encrypt(request.getPassword(), Constants.SECRET));
		employee.setTypeContract(request.getTypeContract());
		employee.setStatus(request.getStatus());
		employee.setPosition(request.getWorkName());
		employee.setRoleGroup(roleGroupId != null ? roleGroupOptional.get() : null);
		employee.setReviewDate(Utils.convertStringToDate("dd-MM-yyy", request.getReviewDate()));
		employee.setBookingDayOffNotify(
				employee.getBookingDayOffNotify() == null ? true : employee.getBookingDayOffNotify());
		employee.setConfirmDayOffNotify(
				employee.getConfirmDayOffNotify() == null ? true : employee.getConfirmDayOffNotify());
		employee.setBookingMeetingNotify(
				employee.getBookingMeetingNotify() == null ? true : employee.getBookingMeetingNotify());
		employee.setConfirmMeetingNotify(
				employee.getConfirmMeetingNotify() == null ? true : employee.getConfirmMeetingNotify());
		// profile
		profile.setFullName(request.getFullName());
		profile.setDateOfBirth(Utils.convertStringToDate("dd-MM-yyy", request.getDateOfBirth()));
		profile.setGender(request.getGender());
		profile.setAddress(request.getAddress());
		profile.setPhoneNumber(request.getPhoneNumber());
		profile.setDateEntry(Utils.convertStringToDate("dd-MM-yyyy", request.getDateEntry()));
		profile.setDateOut(Utils.convertStringToDate("dd-MM-yyyy", request.getDateOut()));
		profile.setTaxCode(String.valueOf(request.getTaxCode()));
		profile.setSafeCode(String.valueOf(request.getSafeCode()));
		profile.setAddress(request.getAddress());
		profile.setPhoneNumber(request.getPhoneNumber());
		profile.setSalaryBasic(request.getSalaryBasic());
		profile.setGender(request.getGender() != null ? request.getGender() : null);
		profile.setTaxCode(request.getTaxCode() != null ? String.valueOf(request.getTaxCode()) : null);
		profile.setSafeCode(request.getSafeCode() != null ? String.valueOf(request.getSafeCode()) : null);
		profile.setDateOfBirth(Utils.convertStringToDate("dd-MM-yyy", request.getDateOfBirth()) != null
				? Utils.convertStringToDate("dd-MM-yyy", request.getDateOfBirth())
				: null);
		profile.setDateEntry(Utils.convertStringToDate("dd-MM-yyy", request.getDateEntry()) != null
				? Utils.convertStringToDate("dd-MM-yyy", request.getDateEntry())
				: null);
		profile.setDateOut(Utils.convertStringToDate("dd-MM-yyy", request.getDateOut()) != null
				? Utils.convertStringToDate("dd-MM-yyy", request.getDateOut())
				: null);
		profile.setBankName(request.getBankName());
		profile.setBankAccount(request.getBankAccount());
        profile.setDiscordId(request.getDiscordId());
        profile.setPermAddress(request.getPermAddress());

		if (request.getId() == null) {
			employee.setCreateEmployeeEditByAndFlag(commonService.idUserAccountLogin(), Constants.DELETE_NONE);
			employeeRepository.save(employee);
			profile.setEmployee(employee);
			profile.setCommonRegister();
		} else {

			employee.setUpdateEmployeeEditByAndFlag(commonService.idUserAccountLogin(), Constants.DELETE_NONE);
			profile.setCommonUpdate();
		}

	}

	public Integer getRoleGroupEmployee(CreateOrEditEmployeeRequest request) {
		return roleGroupDAO.getRoleGroupEmployee(request);
	}

	public ApiResponse delete(Integer id) {
		Employee employ = employeeRepository.findSingleRecord(id);

		// substract 1 member from department's member when employee is deleted
		Department department = employeeRepository.findById(id).get().getDepartment();
		if (department != null) {
			int currentMember = department.getNumberMember();
			department.setNumberMember(currentMember - 1);
			departmentRepository.save(department);
		}
		if (employ == null) {
			throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
		}
		Profile pro = profileRepository.findByEmployeeId(employ.getId());
		if (pro == null) {
			throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
		}
		if (pro.getDateOut() == null) {
			pro.setDateOut(Utils.getTimestamp());
			pro.setCommonUpdate();
		} else {
//        	profileRepository.deleteById(pro.getId());
			employ.setDeleteEmployeeEditAndFlag(commonService.idUserAccountLogin(), Constants.DELETE_TRUE);
		}
		employeeRepository.save(employ);
		return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, null);
	}

	public ApiResponse getLastEmployee() {
		String lastestEmployeeCode = employeeRepository.findLastEmployee(Constants.DIRECTOR_CODE);
		if (lastestEmployeeCode == null || lastestEmployeeCode.length() == 0) {
			return null;
		} else {
			String[] splitCode = lastestEmployeeCode.split("-");
			// get number part
			int numberPart = Integer.parseInt(splitCode[1]);
			// get next number and reformat it to 5-digit string-number
			String reformNextCodeNumberPart = "0000" + String.valueOf(numberPart + 1);
			String nextCode = reformNextCodeNumberPart.substring(reformNextCodeNumberPart.length() - 5);

			return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, splitCode[0] + "-" + nextCode);
		}

	}

	public ApiResponse getListAutoEmployee() {
		return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS,
				employeeRepository.findListAutoEmployee(Constants.DELETE_NONE));
	}

	public ApiResponse uploadImage(MultipartFile file, String code) throws IOException {
		Optional<Employee> employee = employeeRepository.findByEmployeeCode(code, Constants.DELETE_NONE);
		if (!employee.isPresent()) {
			throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
		}
		try {
			String fileName = StringUtils.cleanPath(file.getOriginalFilename());
			String imageUrl = awsS3Service.uploadImage(file, fileName, Constants.AVATAR);
			employee.get().setPictureProfile(imageUrl);
			employee.get().setPictureName(fileName);
			employee.get().setPictureType(file.getContentType());
			employee.get().setCommonUpdate();
			return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, null);
		} catch (Exception awsErr) {
			return new ApiResponse(Constants.HTTP_CODE_500, awsErr.getMessage(), null);
		}
	}

	public ApiResponse deleteImage(String code) {
		Optional<Employee> employee = employeeRepository.findByEmployeeCode(code, Constants.DELETE_NONE);
		if (!employee.isPresent()) {
			throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
		}
		employee.get().setPictureProfile(null);
		employee.get().setPictureName(null);
		employee.get().setPictureType(null);
		employee.get().setCommonUpdate();
		return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, null);
	}

	// get resource file
	public InputStream inputStreamSource(String fileName) throws IOException {
		Resource res = resourceLoader.getResource("classpath:" + fileName);
		return res.getInputStream();
	}

	public ApiResponse exportEmployeesToExcel(ListSearchRequest request, boolean exportAllFlag,
			HttpServletResponse response) throws IOException {
		Optional<Employee> currentEmployee = employeeRepository.findById(commonService.idUserAccountLogin());
		RoleGroup currentRole = currentEmployee.get().getRoleGroup();
		if (currentRole != null && !currentRole.getHrFlag()) {
			request.getSearchValues().setIsLeader(true);
		}
		XSSFWorkbook workbook = null;
		ServletOutputStream outputStream = null;
		XSSFSheet sheet = null;
		List<Integer> listId = request.getIdList();
		if (exportAllFlag == true) {
			@SuppressWarnings("unchecked")
            List<ListEmployeeResponse> listSearchValues = (List<ListEmployeeResponse>) employeeDao
					.getListEmployee(request.getSearchValues()).getItems();
			listId = listSearchValues.stream().map(item -> item.getId()).collect(Collectors.toList());
		}

		try {
			response.setContentType("application/octet-stream");
			String headerKey = "Content-Disposition";
			String headerValue = "attachment; filename=Employee_Info.xlsx";
			response.setHeader(headerKey, headerValue);
			InputStream testFile = null;
			try {
				testFile = inputStreamSource("Employee_Info_Format.xlsx");
			} catch (Exception e) {
				e.printStackTrace();
			}
			workbook = new XSSFWorkbook(testFile);
			sheet = workbook.getSheet("Nhân Viên");
			List<EmployeeToExcelResponse> employeeInfoList = employeeRepository
					.findAllEmployeeInformationByListId(listId);

			writeLines(workbook, employeeInfoList, sheet);
			outputStream = response.getOutputStream();
//			FileOutputStream outputStream = new FileOutputStream("test.xlsx");
			workbook.write(outputStream);
			workbook.close();
			outputStream.close();
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
		//
		return new ApiResponse(Constants.HTTP_CODE_200, Constants.SECRET, currentEmployee);
	}

	public void createCell(Row row, int ColumnCount, Object value, CellStyle style, XSSFSheet sheet) {
		sheet.autoSizeColumn(ColumnCount);
		Cell cell = row.createCell(ColumnCount);
		if (value instanceof Integer) {
			cell.setCellValue((Integer) value);
		} else if (value instanceof Boolean) {
			cell.setCellValue((Boolean) value);
		} else {
			cell.setCellValue((String) value);
		}
		cell.setCellStyle(style);
	}

	public void writeLines(XSSFWorkbook workbook, List<EmployeeToExcelResponse> listExportFile, XSSFSheet sheet) {
		int rowCount = 1;
		int stt = 1;
		CellStyle style = workbook.createCellStyle();
		XSSFFont font = workbook.createFont();
		font.setFontHeight(10);
		font.setFontName("Arial");
		style.setFont(font);

		for (EmployeeToExcelResponse item : listExportFile) {
			Row row = sheet.createRow(rowCount++);
			int columnCount = 0;
			createCell(row, columnCount++, String.valueOf(stt++), style, sheet);
			createCell(row, columnCount++, String.valueOf(item.getEmployeeCode() == null ? "" : item.getEmployeeCode()),
					style, sheet);
			createCell(row, columnCount++, String.valueOf(item.getEmployeeName() == null ? "" : item.getEmployeeName()),
					style, sheet);
			createCell(row, columnCount++, String.valueOf(item.getGender() == null ? "" : item.getGender()), style,
					sheet);
			createCell(row, columnCount++, String.valueOf(item.getDateOfBirth() == null ? "" : item.getDateOfBirth()),
					style, sheet);
			createCell(row, columnCount++, String.valueOf(item.getAddress() == null ? "" : item.getAddress()), style,
					sheet);
			createCell(row, columnCount++, String.valueOf(item.getPhoneNumber() == null ? "" : item.getPhoneNumber()),
					style, sheet);
			createCell(row, columnCount++, String.valueOf(item.getEmail() == null ? "" : item.getEmail()), style,
					sheet);
			createCell(row, columnCount++, String.valueOf(item.getRole() == null ? "" : item.getRole()), style, sheet);
			createCell(row, columnCount++, String.valueOf(item.getTypeContract() == null ? "" : item.getTypeContract()),
					style, sheet);
			createCell(row, columnCount++, String.valueOf(item.getReviewDate() == null ? "" : item.getReviewDate()),
					style, sheet);
			createCell(row, columnCount++, String.valueOf(item.getStatus() == null ? "" : item.getStatus()), style,
					sheet);
			createCell(row, columnCount++, String.valueOf(item.getDateEntry() == null ? "" : item.getDateEntry()),
					style, sheet);
			createCell(row, columnCount++, String.valueOf(item.getDateOut() == null ? "" : item.getDateOut()), style,
					sheet);
			createCell(row, columnCount++,
					String.valueOf(item.getDepartmentName() == null ? "" : item.getDepartmentName()), style, sheet);
			createCell(row, columnCount++, String.valueOf(item.getPosition() == null ? "" : item.getPosition()), style,
					sheet);
			createCell(row, columnCount++, String.valueOf(item.getBankName() == null ? "" : item.getBankName()), style,
					sheet);
			createCell(row, columnCount++, String.valueOf(item.getBankAccount() == null ? "" : item.getBankAccount()),
					style, sheet);
			createCell(row, columnCount++, String.valueOf(item.getTaxCode() == null ? "" : item.getTaxCode()), style,
					sheet);
			createCell(row, columnCount++, String.valueOf(item.getSafeCode() == null ? "" : item.getSafeCode()), style,
					sheet);
			createCell(row, columnCount++, String.valueOf(item.getSalaryBasic() == null ? "" : item.getSalaryBasic()),
					style, sheet);
		}
	}

	public ApiResponse updateEmailNotifications(EmailNotificationsRequest emailRequest) {
		Optional<Employee> employee = employeeRepository.findById(emailRequest.getEmployeeId());
		if (!employee.isPresent()) {
			return new ApiResponse(Constants.HTTP_CODE_400, Constants.RECORD_NOT_FOUND, null);
		}
		employee.get().setBookingDayOffNotify(emailRequest.getBookingDayOffNotify());
		employee.get().setConfirmDayOffNotify(emailRequest.getConfirmDayOffNotify());
		employee.get().setBookingMeetingNotify(emailRequest.getBookingMeetingNotify());
		employee.get().setConfirmMeetingNotify(emailRequest.getConfirmMeetingNotify());

		return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, null);
	}

}
