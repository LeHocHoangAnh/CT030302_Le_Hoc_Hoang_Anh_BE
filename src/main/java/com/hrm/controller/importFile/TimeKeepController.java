package com.hrm.controller.importFile;

import java.io.IOException;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hrm.common.Constants;
import com.hrm.model.ApiResponse;
import com.hrm.model.request.hr.ListDetailTimeKeepingRequest;
import com.hrm.model.response.hr.ListDetailTimeKeepingResponse;
import com.hrm.service.ExportFileService;
import com.hrm.service.importFile.TimeKeepService;
import com.hrm.utils.FileUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/api/hr/")
@Api(description = "Nhập/xuất file chấm công")
public class TimeKeepController {

    @Autowired
    private TimeKeepService timeKeepService;
    
    @Autowired
    private ExportFileService exportService;

    @ApiOperation(value = "Nhập file chấm công")
    @PostMapping("import/timekeep")
    public ResponseEntity<ApiResponse> timeKeep(@ApiParam(value="file chấm công") @RequestParam("file") MultipartFile file,@ApiParam(value="thời gian trong file chấm công có thể null") @RequestParam("time") String time) throws Exception {
    	// valid date
    	Workbook workbook = null;
        String excelDate=time;
        try {
        	// open excel file
            workbook = FileUtil.getWorkbookByMultipartFile(file);
            if (workbook == null)
                return null;  
            workbook.setMissingCellPolicy(Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            Sheet workSheet = workbook.getSheetAt(0);
            
            // get excel yyyy-MM date
            // because yyyy-MM is redquired, -dd is not nesscessary, 
            // so I get a date in records(first row of records) to check date valid between 
            // date in excel file and requested param 'time'
            Row row = workSheet.getRow(4); // Get first record in the table at the row index=5
            Cell cell = row.getCell(1); // Get cell data at column index=B "Ngày" 
            //
            if (cell != null && StringUtils.isNotBlank(cell.toString())) {
            	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        		excelDate = sdf.format(cell.getDateCellValue());
        	}
            
            // valid
            if(time.length()>0 && !excelDate.equals(time)) {
            	return ResponseEntity.ok().body(new ApiResponse(Constants.HTTP_CODE_500, "Dữ liệu ngày tháng không trùng khớp", null));
            }
            
            workbook.close();
        }
        catch(Exception e) {}
        finally {
            workbook.close();
        }
        //
    	timeKeepService.importFileTimeKeep(file, time);
        timeKeepService.saveDetailTimeKeeping(time);
        return ResponseEntity.ok().body(new ApiResponse(Constants.HTTP_CODE_200, Constants.CREATE_SUCCESS, null));
    }
    
    @ApiOperation(value = "chi tiết chấm công")
    @PostMapping("list/dataDetailTimeKeeping")
    public ResponseEntity<ApiResponse> getListTimeKeeping(@ApiParam(value="data search chi tiết chấm công")@RequestBody ListDetailTimeKeepingRequest request) {
        return ResponseEntity.ok().body(timeKeepService.getListData(request));
    }
    
    @ApiOperation(value = "Xuất file chấm công")
    @GetMapping("list/export/excel")
    public ResponseEntity<Void> exportFileExcel(@ApiParam(value="tháng-năm file cầm xuất")@RequestParam("year")String year, HttpServletResponse respose) throws IOException {
        exportService.exportFile(year,respose);
        return ResponseEntity.ok().build();
    }
    
    @ApiOperation(value = "Cập nhật chấm công nhân viên theo mã nhân viên")
    @PutMapping("list/editEmployeeKeeping")
    public ResponseEntity<ApiResponse> editEmployeeKeeping(@ApiParam(value="data search chi tiết chấm công")@RequestBody ListDetailTimeKeepingResponse editRequest, @ApiParam(value="tháng-năm của bản ghi chi tiết")@RequestParam() String time) {
    	timeKeepService.editEmployeeKeeping(editRequest, time);
    	return ResponseEntity.ok().body(new ApiResponse(Constants.HTTP_CODE_200, Constants.CREATE_SUCCESS, null));
    }
}
