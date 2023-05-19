package com.hrm.utils;

import org.apache.poi.ss.formula.functions.T;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hrm.common.CommonFilter;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);
    public static SecretKeySpec secretKey;
    public static byte[] key;
    public static String ALGORITHM = "AES";

    public static void prepareSecreteKey(String myKey) {
        try {
            key = myKey.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest();
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            e.getStackTrace();
        }
    }

    public static String encrypt(String strToEncrypt, String secret) {
        try {
            prepareSecreteKey(secret);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        } catch (Exception e) {
            logger.info("Error while encrypting: " + e.getMessage());
        }
        return null;
    }

    public static String decrypt(String strToDecrypt, String secret) {
        try {
            prepareSecreteKey(secret);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (Exception e) {
            logger.info("Error while decrypting: {}", e.getMessage());
        }
        return null;
    }

    public static Date convertStringToDate(String format, String date) {
        try {
            return new SimpleDateFormat(format).parse(date);
        } catch (Exception e) {
            return null;
        }
    }

    public static String convertDateToString(String format, Date date) {
        DateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    public static Timestamp convertStringToTimestamp(String format, String date) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            Date parsedDate = dateFormat.parse(date);
            return new java.sql.Timestamp(parsedDate.getTime());
        } catch (Exception e) {
            return null;
        }
    }

    public static Timestamp getTimestamp() {
        return new Timestamp(new Date().getTime());
    }

    public static String convertSecondsToDate(int secondsInput) {
        int second = secondsInput % 60;
        int cMinutes = secondsInput / 60;
        int minutes = cMinutes % 60;
        int hours = cMinutes / 60;
        if (hours > 23) {
            int day = hours / 24;
            hours = hours % 24;
            return (day > 9 ? day : "0" + day) + ":" + (hours > 9 ? hours : "0" + hours) + ":"
                    + (minutes > 9 ? minutes : "0" + minutes) + ":" + (second > 9 ? second : "0" + second);
        }
        return (hours > 9 ? hours : "0" + hours) + ":" + (minutes > 9 ? minutes : "0" + minutes) + ":"
                + (second > 9 ? second : "0" + second);
    }

    @SuppressWarnings("deprecation")
    public static <T> void addScalr(NativeQuery<?> sqlQuery, Class<T> clazz) {
        if (clazz == null) {
            throw new NullPointerException("[clazz] could not be null!");
        }
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.getType() == long.class || field.getType() == Long.class) {
                sqlQuery.addScalar(field.getName(), StandardBasicTypes.LONG);
            }
            if (field.getType() == int.class || field.getType() == Integer.class) {
                sqlQuery.addScalar(field.getName(), StandardBasicTypes.INTEGER);
            }
            if (field.getType() == char.class || field.getType() == Character.class) {
                sqlQuery.addScalar(field.getName(), StandardBasicTypes.CHARACTER);
            }
            if (field.getType() == short.class || field.getType() == Short.class) {
                sqlQuery.addScalar(field.getName(), StandardBasicTypes.SHORT);
            }
            if (field.getType() == double.class || field.getType() == Double.class) {
                sqlQuery.addScalar(field.getName(), StandardBasicTypes.DOUBLE);
            }
            if (field.getType() == float.class || field.getType() == Float.class) {
                sqlQuery.addScalar(field.getName(), StandardBasicTypes.FLOAT);
            }
            if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                sqlQuery.addScalar(field.getName(), StandardBasicTypes.BOOLEAN);
            }
            if (field.getType() == String.class) {
                sqlQuery.addScalar(field.getName(), StandardBasicTypes.STRING);
            }
            if (field.getType() == Date.class) {
                sqlQuery.addScalar(field.getName(), StandardBasicTypes.DATE);
            }
            if (field.getType() == Timestamp.class) {
                sqlQuery.addScalar(field.getName(), StandardBasicTypes.TIMESTAMP);
            }
        }
        sqlQuery.setResultTransformer(Transformers.aliasToBean(clazz));
    }

    public static String typeBooking(String Booking) {
        String typeBooking = "";
        if (Booking.equals(CommonFilter.BOOKING_DAY_OFF.DAY_OFF.toString())) {
            typeBooking = "ĐĂNG KÝ NGHỈ PHÉP";
        } else if (Booking.equals(CommonFilter.BOOKING_DAY_OFF.WORKING_LATE.toString())
                || Booking.equals(CommonFilter.BOOKING_DAY_OFF.WORKING_EARLY.toString())) {
            typeBooking = "ĐĂNG KÝ ĐI MUỘN/VỀ SỚM";
        } else if (Booking.equals(CommonFilter.BOOKING_DAY_OFF.REMOTE.toString())) {
            typeBooking = "ĐĂNG KÝ LÀM REMOTE";
        } else if (Booking.equals(CommonFilter.BOOKING_DAY_OFF.GO_OUT.toString())) {
            typeBooking = "ĐĂNG KÝ RA NGOÀI";
        } else if (Booking.equals(CommonFilter.BOOKING_DAY_OFF.OT.toString())) {
            typeBooking = "ĐĂNG KÝ OT";
        } else if (Booking.equals(CommonFilter.BOOKING_DAY_OFF.PERSONAL_LEAVE.toString())) {
            typeBooking = "ĐĂNG KÝ NGHỈ PHÚC LỢI";
        } else if (Booking.equals(CommonFilter.BOOKING_DAY_OFF.COMPENSATORY_LEAVE.toString())) {
            typeBooking = "ĐĂNG KÝ NGHỈ BÙ";
        } else if (Booking.equals(CommonFilter.BOOKING_DAY_OFF.UNPAID_LEAVE.toString())) {
            typeBooking = "ĐĂNG KÝ NGHỈ KHÔNG LƯƠNG";
        } else if (Booking.equals(CommonFilter.BOOKING_DAY_OFF.KEEPING_FORGET.toString())) {
            typeBooking = "ĐĂNG KÝ QUÊN CHẤM CÔNG";
        }
        return typeBooking;
    }

    // get previous month of year in format(yyyy-MM)
    public static String getPreviousMonth(String time) {
        String[] timeSplit = time.split("-");
        Integer year = Integer.parseInt(timeSplit[0]);
        Integer month = Integer.parseInt(timeSplit[1]) - 1;
        if (month <= 0) {
            year -= 1;
            month = 12;
        }
        String monthstr = month.toString();
        if (month < 10) {
            monthstr = "0" + monthstr;
        }
        return year.toString() + "-" + monthstr;
    }

    // check null list
    public static Boolean isNullList(List<?> list) {
        return list == null || list.isEmpty();
    }

    // get string of booking day off type
    public static String getLabelDayOffType(Integer type) {
        String result = "";
        if (type == null) {
            return result;
        }
        switch (type) {
        case 0:
            result = "Đăng ký nghỉ phép";
            break;
        case 1:
            result = "Đăng ký đi muộn/ về sớm";
            break;
        case 2:
            result = "Đăng ký làm Remote";
            break;
        case 3:
            result = "Đăng ký ra ngoài";
            break;
        case 4:
            result = "Đăng ký OT";
            break;
        case 5:
            result = "Đăng ký nghỉ phúc lợi";
            break;
        case 6:
            result = "Đăng ký nghỉ bù";
            break;
        case 7:
            result = "Đăng ký nghỉ không lương";
            break;
        case 8:
            result = "Đăng ký quên chấm công";
            break;
        case 9:
            result = "Đăng ký thiết bị";
            break;
        }
        return result;
    }

    // get string of equipment category
    public static String getLabelEquipmentCategory(Integer type) {
        String result = "";
        if (type == null) {
            return result;
        }
        switch (type) {
        case 0:
            result = "Khác";
            break;
        case 1:
            result = "Máy tính (Laptop)";
            break;
        case 2:
            result = "Máy tính (PC)";
            break;
        case 3:
            result = "Máy tính (Tablet)";
            break;
        case 4:
            result = "Điện thoại";
            break;
        case 5:
            result = "Chuột";
            break;
        case 6:
            result = "Bàn phím";
            break;
        case 7:
            result = "Phụ kiện";
            break;
        }
        return result;
    }

    public static Integer getFirstWorkdayOfDate(Date date) {
        Integer dayOfMonth = 1;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        for (int i = 1; i <= cal.getMaximum(Calendar.DATE); i++) {
            cal.set(Calendar.DATE, i);
            if (!(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
                    || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)) {
                dayOfMonth = i;
                break;
            }
        }
        return dayOfMonth;
    }

    public static Date convertLocalDateToDate(LocalDateTime dateTime) {
        return java.util.Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
