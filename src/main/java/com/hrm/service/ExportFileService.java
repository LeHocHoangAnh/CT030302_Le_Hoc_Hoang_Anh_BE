package com.hrm.service;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import org.springframework.stereotype.Service;

import com.hrm.model.DropDownResponse;
import com.hrm.model.ExportFileResponse;
import com.hrm.model.response.DetailTimeKeepingForExportResponse;
import com.hrm.repository.DetailTimeKeepingRepository;
import com.hrm.repository.TimeKeepingRepository;

@Service
@Transactional
public class ExportFileService {

    @Autowired
    private TimeKeepingRepository timeKeepingRepository;

    @Autowired
    private DetailTimeKeepingRepository detailTimeKeepingRepository;

    public void exportFile(String year, HttpServletResponse response) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = null;
        ServletOutputStream outputStream = null;
        try {
            response.setContentType("application/octet-stream");
            DateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
            String currentDateTime = dateFormatter.format(new Date());
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=thongke_" + year + "/" + currentDateTime + ".xlsx";
            response.setHeader(headerKey, headerValue);

            List<ExportFileResponse> listExportFile = new ArrayList<>();
            sheet = workbook.createSheet("Thống Kê");
            Row row = sheet.createRow(0);
            if (year.length() == 0) {
                List<DropDownResponse> listTime = timeKeepingRepository.getListTime();
                String firstTime = listTime.get(0).getName();
                List<DetailTimeKeepingForExportResponse> listDetailTimeKeeping = detailTimeKeepingRepository
                        .findByListDetailTimeKeepingForExportFile(firstTime);
                for (DetailTimeKeepingForExportResponse detailTimeKeeping : listDetailTimeKeeping) {
                    ExportFileResponse exportFile = new ExportFileResponse();
                    createExport(detailTimeKeeping, exportFile);
                    listExportFile.add(exportFile);
                }
            } else {
                List<DetailTimeKeepingForExportResponse> listDetailTimeKeeping = detailTimeKeepingRepository
                        .findByListDetailTimeKeepingForExportFile(year);
                for (DetailTimeKeepingForExportResponse detailTimeKeeping : listDetailTimeKeeping) {
                    ExportFileResponse exportFile = new ExportFileResponse();
                    createExport(detailTimeKeeping, exportFile);
                    listExportFile.add(exportFile);
                }
            }
            writeHeaderLine(row, workbook, sheet);
            writeDataLines(workbook, listExportFile, sheet);
            outputStream = response.getOutputStream();
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        } finally {
            workbook.close();
            outputStream.close();
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
        createCell(row, 3, "Email", style, sheet);
        createCell(row, 4, "Hợp đồng", style, sheet);
        createCell(row, 5, "Đi muộn/ Về sớm/\r\n" + " Ra ngoài\r\n" + " (Lần)", style, sheet);
        createCell(row, 6, "Đi muộn/ Về sớm\r\n" + "Ra ngoài\r\n " + " (Giờ)", style, sheet);
        createCell(row, 7, "Quên chấm công\r\n" + " (Lần)", style, sheet);
        createCell(row, 8, "Công thực(Ngày)", style, sheet);
        createCell(row, 9, "Phép(Ngày)", style, sheet);
        createCell(row, 10, "Nghỉ việc riêng\r\n" + " hưởng lương(Ngày)", style, sheet);
        createCell(row, 11, "Nghỉ bù(Ngày)", style, sheet);
        createCell(row, 12, "Công tính lương\r\n" + " (Ngày)", style, sheet);
        createCell(row, 13, "OT ngày thường\r\n" + "1.0l, 0.5 bù", style, sheet);
        createCell(row, 14, "OT Thứ 7\r\n" + " (1.5l, 0.5 bù)", style, sheet);
        createCell(row, 15, "OT Chủ Nhật\r\n" + " (1.5l, 0.5 bù)", style, sheet);
        createCell(row, 16, "OT ngày lễ\r\n" + " (2.0l, 1 bù)", style, sheet);
        createCell(row, 17, "OT tính lương\r\n" + " quy đổi ra\r\n" + " hs 1", style, sheet);
        createCell(row, 18, "Nghỉ bù cộng\r\n" + " thêm của tháng\r\n" + " hiện tại (ngày)", style, sheet);
        createCell(row, 19, "Nghỉ bù cộng\r\n" + " thêm làm tròn", style, sheet);
        createCell(row, 20, "OT trả trong\r\n" + " tháng (max 60h hs1)", style, sheet);
        createCell(row, 21, "OT tồn chưa\r\n" + " thanh toán", style, sheet);
        createCell(row, 22, "Phép Lưu Trữ Tháng Hiện Tại", style, sheet);
        createCell(row, 23, "Nghỉ Bù OT Tháng Hiện Tại", style, sheet);
        createCell(row, 24, "Phép Lưu Trữ Tháng Tiếp Theo", style, sheet);
        createCell(row, 25, "Nghỉ Bù Lưu Trữ Tháng Tiếp Theo", style, sheet);
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

    private void writeDataLines(XSSFWorkbook workbook, List<ExportFileResponse> listExportFile, XSSFSheet sheet) {
        int rowCount = 1;
        int stt = 1;
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        font.setFontName("Arial");
        style.setFont(font);
        for (ExportFileResponse resp : listExportFile) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row, columnCount++, String.valueOf(stt++), style, sheet);
            createCell(row, columnCount++, String.valueOf(resp.getIdEmployee()), style, sheet);
            createCell(row, columnCount++, String.valueOf(resp.getName()), style, sheet);
            createCell(row, columnCount++, String.valueOf(resp.getEmail()), style, sheet);
            createCell(row, columnCount++, String.valueOf(resp.getContract()), style, sheet);
            createCell(row, columnCount++, String.valueOf(resp.getLateTime()), style, sheet);
            createCell(row, columnCount++, String.valueOf(resp.getLateHour()), style, sheet);
            createCell(row, columnCount++, String.valueOf(resp.getForgetTimeKeeping()), style, sheet);
            createCell(row, columnCount++, String.valueOf(resp.getKeepingReal()), style, sheet);
            createCell(row, columnCount++, String.valueOf(resp.getOnLeave()), style, sheet);
            createCell(row, columnCount++, String.valueOf(resp.getPaidLeave()), style, sheet);
            createCell(row, columnCount++, String.valueOf(resp.getPlusLeave()), style, sheet);
            createCell(row, columnCount++, String.valueOf(resp.getSalaryKeeping()), style, sheet);
            createCell(row, columnCount++, String.valueOf(resp.getOTNormal()), style, sheet);
            createCell(row, columnCount++, String.valueOf(resp.getOTMorningSaturday()), style, sheet);
            createCell(row, columnCount++, String.valueOf(resp.getOTAfternoonSaturdayAndSunDay()), style, sheet);
            createCell(row, columnCount++, String.valueOf(resp.getOTHoliday()), style, sheet);
            createCell(row, columnCount++, String.valueOf(resp.getOTConvertSalary1()), style, sheet);
            createCell(row, columnCount++, String.valueOf(resp.getPlusLeaveAddMonthNow()), style, sheet);
            createCell(row, columnCount++, String.valueOf(resp.getPlusLeaveAdd()), style, sheet);
            createCell(row, columnCount++, String.valueOf(resp.getOTPayWageInMonth()), style, sheet);
            createCell(row, columnCount++, String.valueOf(resp.getOTUnpaid()), style, sheet);
            createCell(row, columnCount++, String.valueOf(resp.getOnLeaveRemainOnLastMonth()), style, sheet);
            createCell(row, columnCount++, String.valueOf(resp.getPaidLeaveOTRemainOnOnLastMonth()), style, sheet);
            createCell(row, columnCount++, String.valueOf(resp.getOnLeaveRemainOnNow()), style, sheet);
            createCell(row, columnCount++, String.valueOf(resp.getPaidLeaveOTRemainOnNow()), style, sheet);
        }
    }

    public void createExport(DetailTimeKeepingForExportResponse detail, ExportFileResponse export) {
        export.setIdEmployee(detail.getEmployeeCode());
        export.setName(detail.getFullName());
        export.setEmail(detail.getEmail());
        export.setContract(detail.getTypeContract());
        export.setLateTime(detail.getLateTime());
        export.setLateHour(detail.getLateHour());
        export.setForgetTimeKeeping(detail.getKeepingForget());
        export.setKeepingReal(detail.getSalaryReal());
        export.setOnLeave(detail.getLeaveDayAccept());
        export.setPaidLeave(detail.getWelfareLeave());
        export.setPlusLeave(detail.getCompensatoryLeave());
        export.setSalaryKeeping(detail.getSalaryCount());
        export.setOTNormal(detail.getOtNormal());
        export.setOTMorningSaturday(detail.getOtMorning7());
        export.setOTAfternoonSaturdayAndSunDay(detail.getOtSatSun());
        export.setOTHoliday(detail.getOtHoliday());
        export.setOTConvertSalary1(detail.getSumOtMonth());
        export.setPlusLeaveAddMonthNow(detail.getCsrLeavePlus());
        export.setPlusLeaveAdd(detail.getCsrLeavePlusRound());
        export.setOTPayWageInMonth(detail.getOtPayInMonth());
        export.setOTUnpaid(detail.getOtUnpaid());
        export.setOnLeaveRemainOnNow(detail.getLeaveRemainNow());
        export.setPaidLeaveOTRemainOnNow(detail.getCsrLeaveNow());
        export.setOnLeaveRemainOnLastMonth(detail.getLeaveRemainLastMonth());
        export.setPaidLeaveOTRemainOnOnLastMonth(detail.getCsrLeaveLastMonth());
    }
}
