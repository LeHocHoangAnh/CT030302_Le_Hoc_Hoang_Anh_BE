package com.hrm.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.multipart.MultipartFile;

public class FileUtil {
	private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

	public static Workbook getWorkbookByMultipartFile(MultipartFile multipartFile)
			throws IOException, InvalidFormatException {
		if (multipartFile == null)
			return null;

		String fName = multipartFile.getOriginalFilename();
		if (StringUtils.isBlank(fName))
			return null;

		return getWorkbookByInputStream(multipartFile.getInputStream(), fName);
	}

	public static Workbook getWorkbookByInputStream(InputStream is, String fName)
			throws IOException, InvalidFormatException {
		if (is == null)
			return null;
		ZipSecureFile.setMinInflateRatio(-1.0d);
		Workbook wb = null;
		try {
			wb = WorkbookFactory.create(is);
			return wb;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}

	public static String validateStringCell(Cell cell, String columnCode, String nullCode, String regexCode,
			String regex) {
		String bug = "";
		if (columnCode != null) {
			if (cell != null && cell.getCellType() != CellType.BLANK) {
				String patternCell = formatCell(cell);
				if (patternCell != null) {
					if (regex != null && !regex.equals("")) {
						Pattern pattern = Pattern.compile(regex);
						String patternCheck = patternCell.trim();
						if (pattern != null && pattern.matcher(patternCheck).matches()) {
							return null;
						} else {
							bug += regexCode + ", ";
						}
					} else {
						return null;
					}
				}
				if (formatCell(cell) == null && nullCode != null) {
					bug += nullCode + ", ";
				}
			} else {
				if (nullCode == null) {
					return null;
				} else {
					bug += nullCode + ", ";
				}
			}
		}
		return bug;
	}

	public static String formatCell(Cell c) {
		if (c == null) {
			return null;
		} else if (c.getCellType() == CellType.STRING) {
			if ("".equalsIgnoreCase(c.getStringCellValue())) {
				return null;
			}
			return c.getStringCellValue().trim();
		} else if (c.getCellType() == CellType.NUMERIC) {
			DataFormatter dataFormatter = new DataFormatter();
			return dataFormatter.formatCellValue(c).trim();
		} else if (c.getCellType() == CellType.BLANK) {
			return null;
		} else
			return null;
	}

	public static Date formatDate(Date dateInput) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		formatter.setLenient(false);
		/*
		 * Create Date object parse the string into date
		 */
		try {
			String strDate = formatter.format(dateInput);
			return formatter.parse(strDate);
		}
		/* Date format is invalid */
		catch (ParseException e) {
			logger.error("LOGGER - Exception {} is Invalid Date format", dateInput);
			return null;
		}
	}

	public static Date formatDateHHMMSS(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss");
		formatter.setLenient(false);
		try {
			String strDate = formatter.format(date);
			return formatter.parse(strDate);
		} catch (ParseException e) {
			logger.error("LOGGER - Exception {} is Invalid Date format", date);
			return null;
		}
	}

	public static boolean checkData(Cell cell, String input) {
		return cell != null && StringUtils.isNotBlank(input);
	}

	public static File convertMPFileToFile(final MultipartFile multipartFile) {
		final File file = new File(multipartFile.getOriginalFilename());
		try (final FileOutputStream outputStream = new FileOutputStream(file)) {
			outputStream.write(multipartFile.getBytes());
		} catch (final IOException ex) {
		}

		return file;
	}

}
