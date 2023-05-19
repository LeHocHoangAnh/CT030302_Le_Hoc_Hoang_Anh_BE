package com.hrm.service.equipment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hrm.common.CommonService;
import com.hrm.common.Constants;
import com.hrm.entity.BookingDayOff;
import com.hrm.entity.Employee;
import com.hrm.entity.Equipment;
import com.hrm.entity.EquipmentHistory;
import com.hrm.exception.RecordNotFoundException;
import com.hrm.model.ApiResponse;
import com.hrm.model.dao.EquipmentManageDao;
import com.hrm.model.dao.EquipmentRegistrationListDAO;
import com.hrm.model.request.employee.RegisterEquipmentRequest;
import com.hrm.model.request.hr.CreateOrEditEquipmentRequest;
import com.hrm.model.request.hr.EquipmentListRequest;
import com.hrm.model.request.hr.ListEquipmentRegistrationRequest;
import com.hrm.model.response.employee.EquipmentRegistrationListResponse;
import com.hrm.model.response.hr.EquipmentHistoryListResponse;
import com.hrm.repository.BookingDayOffRepository;
import com.hrm.repository.EmployeeRepository;
import com.hrm.repository.EquipmentHistoryRepository;
import com.hrm.repository.EquipmentRepository;
import com.hrm.utils.Utils;

@Service
@Transactional
public class EquipmentService {

    @Autowired
    private BookingDayOffRepository bookingDayOffRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private EquipmentHistoryRepository equipmentHistoryRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EquipmentManageDao equipmentManageDao;

    @Autowired
    private CommonService commonService;

    @Autowired
    private EquipmentRegistrationListDAO equipmentRegistrationListDAO;

    // **** HR: Management ****
    public ApiResponse getListEquipment(EquipmentListRequest request) {
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS,
                equipmentManageDao.searchListEquipment(request));
    }

    public ApiResponse detail(Integer id) {
        Optional<Equipment> optionalEquipment = equipmentRepository.findById(id);
        if (!optionalEquipment.isPresent() || optionalEquipment.get() == null) {
            throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
        }

        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, optionalEquipment.get());
    }

    public ApiResponse edit(CreateOrEditEquipmentRequest request) {
        Equipment equipment = new Equipment();
        Boolean changeStatus = false;
        Boolean discardOwnershipFlag = false;
        equipment.setDeleteFlag(false);
        // validate data
        if (request.getId() != null) {
            Optional<Equipment> optionalEquipment = equipmentRepository.findById(request.getId());
            if (!optionalEquipment.isPresent() || optionalEquipment.get() == null) {
                throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
            }
            equipment = optionalEquipment.get();
        }
        if (request.getEmployeeId() != null) {
            Optional<Employee> optionalEmployee = employeeRepository.findById(request.getEmployeeId());
            if (!optionalEmployee.isPresent() || optionalEmployee.get() == null) {
                return new ApiResponse(Constants.HTTP_CODE_500, "Nhân viên không tồn tại", null);
            }
        }
        // set equipment
        changeStatus = equipment.getStatus() != request.getStatus() || request.getStatus() == null;
        discardOwnershipFlag = Arrays.asList(Constants.NOT_YET_USED, Constants.STOP_USED, Constants.RETURNED_STORED)
                .indexOf(request.getStatus()) > -1;
        equipment.setName(request.getName());
        equipment.setSerialNumber(request.getSerialNumber());
        equipment.setCategory(request.getCategory());
        equipment.setDescription(request.getDescription());
        equipment.setImportDate(request.getImportDate());
        equipment.setVendor(request.getVendor());
        equipment.setWarrantyTime(request.getWarrantyTime());
        equipment.setEmployeeId(discardOwnershipFlag ? null : request.getEmployeeId());
        equipment.setStatus(request.getStatus() == null ? 0 : request.getStatus());
        equipment.setCommonUpdate();
        equipment = equipmentRepository.save(equipment);

        if (changeStatus) {
            editEquipmentHistory(equipment, request, discardOwnershipFlag);
        }

        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, null);
    }

    private void editEquipmentHistory(Equipment equipment, CreateOrEditEquipmentRequest request,
            boolean discardOwnershipFlag) {
        if (request.getEmployeeId() != null) {
            if (request.getId() == null) {
                createEquipmentHistory(equipment.getId(), request, discardOwnershipFlag);
            } else {
                EquipmentHistory equipmentHistory = equipmentHistoryRepository
                        .findCurrentHistoryByEquipmentIdAndEmployeeId(request.getId(), request.getEmployeeId());
                if (equipmentHistory != null) {
                    // if the status is changed to something that means discarding ownership of the
                    // current user
                    checkUpdateBackDate(equipmentHistory, discardOwnershipFlag);
                    equipmentHistory.setCommonUpdate();
                    equipmentHistoryRepository.save(equipmentHistory);
                } else {
                    if (request.getStatus() == 1) {
                        createEquipmentHistory(equipment.getId(), request, discardOwnershipFlag);
                    }
                }
            }
        }
    }

    private void checkUpdateBackDate(EquipmentHistory equipmentHistory, Boolean discardOwnershipFlag) {
        if (discardOwnershipFlag) {
            if (equipmentHistory.getBackDate() == null) {
                equipmentHistory.setBackDate(Utils.getTimestamp());
            }
        }
    }

    public void createEquipmentHistory(Integer equipmentId, CreateOrEditEquipmentRequest request,
            Boolean discardOwnershipFlag) {
        EquipmentHistory equipmentHistory = new EquipmentHistory();
        equipmentHistory.setEquipmentId(equipmentId);
        equipmentHistory.setEmployeeId(request.getEmployeeId());
        equipmentHistory.setRequestDate(Utils.getTimestamp());
        checkUpdateBackDate(equipmentHistory, discardOwnershipFlag);
        equipmentHistory.setCommonRegister();
        equipmentHistoryRepository.save(equipmentHistory);
    }

    public ApiResponse switchOwnership(Integer id, Integer employeeId) {
        Optional<Equipment> optionalEquipment = equipmentRepository.findById(id);
        if (!optionalEquipment.isPresent() || optionalEquipment.get() == null) {
            throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
        }
        // find current user that is owner of the equipment, save back_day to his
        // history equipment record
        EquipmentHistory currentEquipmentHistory = equipmentHistoryRepository.findCurrentHistoryByEquipmentId(id);
        if (currentEquipmentHistory != null) {
            currentEquipmentHistory.setCommonUpdate();
            if (currentEquipmentHistory.getBackDate() == null) {
                currentEquipmentHistory.setBackDate(Utils.getTimestamp());
            }
            equipmentHistoryRepository.save(currentEquipmentHistory);
        }

        // if next owner is chosen, switch user and create new history record
        if (employeeId != null) {
            EquipmentHistory nextEquipmentHistory = new EquipmentHistory();
            nextEquipmentHistory.setEquipmentId(id);
            nextEquipmentHistory.setEmployeeId(employeeId);
            nextEquipmentHistory.setRequestDate(Utils.getTimestamp());
            nextEquipmentHistory.setCommonRegister();
            equipmentHistoryRepository.save(nextEquipmentHistory);

            optionalEquipment.get().setEmployeeId(employeeId);
            optionalEquipment.get().setStatus(1); // Đang sử dụng
            equipmentRepository.save(optionalEquipment.get());
        }

        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, null);
    }

    public ApiResponse delete(Integer id) {
        Optional<Equipment> optionalEquipment = equipmentRepository.findById(id);
        if (!optionalEquipment.isPresent() || optionalEquipment.get() == null) {
            throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
        }
        optionalEquipment.get().setCommonDelete();
        equipmentRepository.save(optionalEquipment.get());

        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, null);
    }

    // **** Employee: Registration ****
    public ApiResponse getHistoryList(Integer id) {
        Optional<Equipment> optionalEquipment = equipmentRepository.findById(id);
        if (!optionalEquipment.isPresent() || optionalEquipment.get() == null) {
            throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
        }

        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS,
                equipmentHistoryRepository.getHistoryListByEquipmentId(id));
    }

    public ApiResponse getListEmployee() {
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS,
                bookingDayOffRepository.getAllEmployeeDropdown());
    }

    public ApiResponse getUserEquipmentList(Integer idUserAccountLogin) {
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS,
                equipmentRepository.getUserEquipmentList(idUserAccountLogin));
    }

    public ApiResponse getRegistrationList(Integer idUserAccountLogin) {
        List<EquipmentRegistrationListResponse> equipmentRegistrationList = bookingDayOffRepository
                .findEquipmentRegistrationListByEmployeeId(idUserAccountLogin);
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, equipmentRegistrationList);
    }

    public ApiResponse getDetailRegistration(Integer id) {
        Optional<BookingDayOff> registration = bookingDayOffRepository.findById(id);
        if (!registration.isPresent() || registration.get() == null) {
            throw new RecordNotFoundException(Constants.RECORD_DOES_NOT_EXIST);
        }

        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, registration.get());
    }

    public ApiResponse getAllApprover() {
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, equipmentRepository.getAllApprover());

    }

    public ApiResponse editRegister(RegisterEquipmentRequest request) {
        BookingDayOff registration = new BookingDayOff();
        if (request.getId() != null) {
            registration = bookingDayOffRepository.findById(request.getId()).get();
            if (registration == null) {
                throw new RecordNotFoundException(Constants.RECORD_DOES_NOT_EXIST);
            }
            registration.setCommonUpdate();
        } else {
            registration.setStatus(9);
            registration.setApproveProgress(Arrays.asList(""));
            registration.setApproveReason(Arrays.asList(""));
            registration.setEmployee(employeeRepository.findSingleRecord(commonService.idUserAccountLogin()));
            registration.setCommonRegister();
        }
        registration.setApprover(request.getCategory());
        registration.setSelectedTypeTime(request.getDescription());
        registration.setRequestDay(request.getRequestDate());
        registration.setBackDay(request.getRequestDate());
        registration.setReason(request.getReason());
        registration.setApproverIDs(Arrays.asList(request.getApprover() + ""));
        registration.setApproveProgress(Arrays.asList("0"));
        registration.setConfirm(0);

        Integer id = bookingDayOffRepository.save(registration).getId();
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, id);
    }

    public ApiResponse deleteRegistration(Integer id) {
        Optional<BookingDayOff> registration = bookingDayOffRepository.findById(id);
        if (!registration.isPresent() || registration.get() == null) {
            throw new RecordNotFoundException(Constants.RECORD_DOES_NOT_EXIST);
        }

        registration.get().setDeleteFlag(true);
        registration.get().setCommonDelete();
        bookingDayOffRepository.save(registration.get());

        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, null);
    }

    public ApiResponse getEquipmentRegistrationList(ListEquipmentRegistrationRequest request) {
        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, equipmentRegistrationListDAO
                .getEquipmentRegistrationList(request, String.valueOf(commonService.idUserAccountLogin())));
    }

    public ApiResponse editHistory(EquipmentHistoryListResponse request) {
        EquipmentHistory equipmentHistory = equipmentHistoryRepository.findById(request.getId())
                .orElseThrow(() -> new RecordNotFoundException(null));
        Boolean changeFlag = false;
        if (Objects.nonNull(request.getBackDate())) {
            Date backDate;
            try {
                backDate = new SimpleDateFormat("dd-MM-yyyy").parse(request.getBackDate());
                equipmentHistory.setBackDate(backDate);
                changeFlag = true;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (Objects.nonNull(request.getRequestDate())) {
            Date requestDate;
            try {
                requestDate = new SimpleDateFormat("dd-MM-yyyy").parse(request.getRequestDate());
                equipmentHistory.setRequestDate(requestDate);
                changeFlag = true;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (changeFlag) {
            equipmentHistory.setCommonUpdate();
            equipmentHistoryRepository.save(equipmentHistory);
        }

        return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, null);
    }

}
