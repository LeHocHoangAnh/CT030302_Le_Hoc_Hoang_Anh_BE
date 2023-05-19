package com.hrm.service.dayoff;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hrm.common.Constants;
import com.hrm.entity.ConfigDayOff;
import com.hrm.exception.RecordNotFoundException;
import com.hrm.model.ApiResponse;
import com.hrm.model.request.hr.ConfigDayoffRequest;
import com.hrm.repository.ConfigDayOffRepository;

@Service
@Transactional
public class DayoffService {
    @Autowired
    ConfigDayOffRepository configDayOffRepo;
    // Get range of year
	public ApiResponse getYearRange() {
		return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, configDayOffRepo.getYearRange());
	}
	public ApiResponse getDayOffs(String year) {
		if(year.equals("all")) {		
			return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, configDayOffRepo.findAllByOrderByIdAsc());
		}
		return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, configDayOffRepo.findByYearApply(Integer.parseInt(year)));
	}
	public ApiResponse getDayOffDetail(String id) {
		if(id==null || id.length()<=0) {
			return new ApiResponse(Constants.HTTP_CODE_500, "Id is required", null);
		}
		Optional<ConfigDayOff> configDayOff = configDayOffRepo.findById(Integer.parseInt(id));
		if(!configDayOff.isPresent()) 
			throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
		
		return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, configDayOff.get());
	}
	public ApiResponse saveDayOffConfig(ConfigDayoffRequest req) {
		ConfigDayOff cdo = new ConfigDayOff();
		Date dayFrom = req.getDayFrom();
		@SuppressWarnings("deprecation")
		Integer monthApply = dayFrom.getMonth()+1;
		@SuppressWarnings("deprecation")
		Integer yearApply = dayFrom.getYear()+1900;
		Date dayTo = req.getDayTo();
		String reasonApply = req.getReasonApply();
		Timestamp now = new Timestamp(System.currentTimeMillis());
		
		if(req.getId()==null) {
			cdo.setMonthApply(monthApply);
			cdo.setDayFrom(dayFrom);
			cdo.setDayTo(dayTo);
			cdo.setReasonApply(reasonApply);
			cdo.setYearApply(yearApply);
			
			this.configDayOffRepo.save(cdo);
		}
		else {
			Optional<ConfigDayOff> optionalDayOff = this.configDayOffRepo.findById(req.getId());
			if(!optionalDayOff.isPresent())
				throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
			optionalDayOff.get().setMonthApply(monthApply);
			optionalDayOff.get().setDayFrom(dayFrom);
			optionalDayOff.get().setDayTo(dayTo);
			optionalDayOff.get().setReasonApply(reasonApply);
			optionalDayOff.get().setYearApply(yearApply);
			optionalDayOff.get().setUpdatedAt(now);
			
			this.configDayOffRepo.save(optionalDayOff.get());
		}
		return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, null);
	}
	public ApiResponse deleteDayOff(Integer id) {
		if(id==null) {
			return new ApiResponse(Constants.HTTP_CODE_500, "ID is null", null);
		}
		Optional<ConfigDayOff> optionalDayOff = this.configDayOffRepo.findById(id);
		if(!optionalDayOff.isPresent())
			throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
		else
			this.configDayOffRepo.deleteById(id);
		
		return new ApiResponse(Constants.HTTP_CODE_200, Constants.SUCCESS, null);
	}
}
