package com.dongzy.common;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author dongzy
 * @Desc
 * @date 2019/10/29.
 */
public class CommonUtil {
    public static final String dateStyle24 = "yyyy-MM-dd HH:mm:ss";// 24小时制格式

    /**
     * String\List\Set\Map为空判断 null "null" "" 都认为是true
     *
     * @param obj
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }

        if (obj instanceof String) {
            String str = (String) obj;
            return str.trim().length() == 0 || str.toLowerCase().equals("null");
        } else if (obj instanceof List) {
            List ls = (List) obj;
            return ls.size() == 0;
        } else if (obj instanceof Set) {
            Set ls = (Set) obj;
            return ls.size() == 0;
        } else if (obj instanceof Map) {
            Map ls = (Map) obj;
            return ls.size() == 0;
        } else if (obj instanceof String[]) {
            String[] ls = (String[]) obj;
            return ls.length == 0;
        }
        return false;
    }

    public static boolean isNotEmpty(Object objs) {
        return !isEmpty(objs);
    }

    /**
     * @param
     * @return String 返回类型
     * @throws
     * @Title: CreateNewGUID
     * @create_time 2014-12-12
     */
    public static String createGUID() {
        // 创建 GUID 对象
        UUID uuid = UUID.randomUUID();
        // 得到对象产生的ID
        return uuid.toString().toUpperCase();
    }

    /**
     * 格式化日期为字符串.
     *
     * @param obj     日期字符串
     * @param pattern 类型
     * @return 结果字符串
     */
    public static String formatDate(Object obj, String pattern) {
        if (isEmpty(obj)) {
            return null;
        }
        if (pattern == null) {
            pattern = dateStyle24;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        if (obj instanceof String) {
            String str = (String) obj;
            return sdf.format(getDate(str, pattern));
        } else if (obj instanceof java.sql.Timestamp) {
            java.sql.Timestamp d = (java.sql.Timestamp) obj;
            return sdf.format(d);
        } else if (obj instanceof java.sql.Date) {
            java.sql.Date d = (java.sql.Date) obj;
            return sdf.format(d);
        } else if (obj instanceof Date) {
            Date d = (Date) obj;
            return sdf.format(d);
        }
        return sdf.format(obj);
    }

    public static Date getDate(String dtStr, String pattern) {
        if (isEmpty(dtStr)) {
            return null;
        }
        try {
            if (dtStr.trim().length() == 10) {
                dtStr = dtStr + " 00:00:00";
            } else if (dtStr.trim().length() > 19) {
                dtStr = dtStr.substring(0, 19);
            }
            SimpleDateFormat df = new SimpleDateFormat(pattern);
            Date dateInstance = df.parse(dtStr);
            return dateInstance;
        } catch (Exception ex) {
            return null;
        }

    }

    /**
     * @return
     */
    @SuppressWarnings("deprecation")
    public static Integer formatBirthdayToAge(String birthday) {
        Integer age = null;
        try {
            String nowTime = formatDate(new Date(), "yyyy-MM-dd")
                    .replace('-', '/');
            String birthdayTime = birthday.replace('-', '/');

            Date dt1 = new Date(nowTime);
            Date dt2 = new Date(birthdayTime);
            long sui = (dt1.getTime() - dt2.getTime()) / (1000 * 60 * 60 * 24);
            age = (int) (sui / 365);
        } catch (Exception e) {
            age = null;
        }
        return age;
    }

    /**
     * 根据开始时间和结束时间计算出天数
     *
     * @param beginDateStr
     * @param endDateStr
     * @return
     */
    public static long formatBirthdayToDays(String beginDateStr,
                                            String endDateStr) {
        long day = 0;
        SimpleDateFormat format = new SimpleDateFormat(
                "yyyy-MM-dd");
        Date beginDate;
        Date endDate;
        try {
            beginDate = format.parse(beginDateStr);
            endDate = format.parse(endDateStr);
            day = (endDate.getTime() - beginDate.getTime())
                    / (24 * 60 * 60 * 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return day;
    }

    /**
     * 判断是否是数字（小数）
     *
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        Pattern pattern = Pattern.compile("-?[0-9]+\\.?[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    /**
     * 获取当前日期后past天的日期
     *
     * @param past 天数
     * @param type 类型 Calender.type
     * @return
     */
    public static Date getFetureDates(int past, int type) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(type, calendar.get(type) + past);
        Date today = calendar.getTime();
        return today;
    }

    /**
     * 把Integer转换成Boolean,如果为null直接返回，为0返回false，否则返回true；
     *
     * @param num
     * @return
     */
    public static Boolean transIntToBoolean(Integer num) {
        if (num == null) {
            return null;
        } else if (num == 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 保留有效数字
     *
     * @param number
     * @return
     */
    public static String toAvailString(Number number) {
        if (number == null) {
            return null;
        } else {
            String value = (new BigDecimal(Double.toString(number.doubleValue()))).toPlainString();
            if (!value.contains(".")) {
                return value;
            } else {
                int length = value.length() - 1;

                for (int i = length; i >= 0; --i) {
                    char c = value.charAt(i);
                    if (c != '0') {
                        length = i;
                        break;
                    }
                }

                value = value.substring(0, length + 1);
                return StringExt.trim(value, '.');
            }
        }
    }

    public static boolean isEmptyOrLessThanZero(Integer val) {
        if (null == val) {
            return true;
        }
        if (val <= 0) {
            return true;
        }
        return false;
    }

    public static boolean isEmptyOrLessThanZero(BigDecimal val) {
        if (null == val) {
            return true;
        }
        if (val.compareTo(new BigDecimal(0)) <= 0) {
            return true;
        }
        return false;
    }
}
