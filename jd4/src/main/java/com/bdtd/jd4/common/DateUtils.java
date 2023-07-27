package com.bdtd.jd4.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    public static String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS);

    /**
     * 说明: 日期格式yyyyMMddHHmmss，转换为yyyy-MM-dd HH:mm:ss
     * @创建者 zqw
     * @日期 2018年10月25日 下午2:27:28
     * @param dateObj
     * @return
     */
    public static String getSouStrToDateStr(String dateObj) {
        if(dateObj.length()!=14){
            return "";
        }
        String year=dateObj.substring(0,4);
        String month=dateObj.substring(4,6);
        String day=dateObj.substring(6,8);
        String hour=dateObj.substring(8,10);
        String min=dateObj.substring(10,12);
        String second=dateObj.substring(12,14);
        return year+"-"+month+"-"+day+" "+hour+":"+min+":"+second;
    }

    /**
     * yyyy-MM-dd HH:mm:ss 字符串转localDateTime
     */
    public static LocalDateTime transformLocalDateTime(String date) {
        return LocalDateTime.parse(date, DATE_TIME_FORMATTER);
    }
}
