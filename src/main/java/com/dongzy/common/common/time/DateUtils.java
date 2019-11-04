package com.dongzy.common.common.time;

import com.dongzy.common.common.DataFormatException;
import com.dongzy.common.common.NumberUtils;
import com.dongzy.common.common.Validate;
import com.dongzy.common.common.text.StringUtils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 日期转换辅助类，支持日期类型与其他类型的互相转换
 * 如果无法识别指定的日期值，那么将会抛出IllegalArgumentException异常
 *
 * @author zouyong
 * @since JDK1.8
 */
public final class DateUtils {

    /**
     * Number of milliseconds in a standard second.
     *
     * @since 2.1
     */
    public static final long MILLIS_PER_SECOND = 1000;
    /**
     * Number of milliseconds in a standard minute.
     *
     * @since 2.1
     */
    public static final long MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;
    /**
     * Number of milliseconds in a standard hour.
     *
     * @since 2.1
     */
    public static final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;
    /**
     * Number of milliseconds in a standard day.
     *
     * @since 2.1
     */
    public static final long MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR;

    //小时分秒正则表达式
    private final static Pattern TIME_PATTERN = Pattern.compile(".*(\\d{2}):(\\d{2}):(\\d{2}).*");
    private final static Pattern M_SECOND_PATTERN = Pattern.compile(".*(\\.\\d+)$");

    /**
     * 将一个字符串转换为Timestamp类型,如果转换的过程中发现异常,会返回空值
     *
     * @param value             需要处理的值
     * @param simpleDateFormats 自定义的日志格式化类
     * @return Date对象
     */
    public static Timestamp tryToTimestamp(final Object value, final SimpleDateFormat... simpleDateFormats) {
        Date d = tryToDate(value, simpleDateFormats);
        return (d == null) ? null : new Timestamp(d.getTime());
    }

    /**
     * 将一个字符串转换为日期类型,如果转换的过程中发现异常,会返回空值
     *
     * @param value             需要处理的值
     * @param simpleDateFormats 自定义的日志格式化类
     * @return Date对象
     */
    public static Date tryToDate(final Object value, final SimpleDateFormat... simpleDateFormats) {
        try {
            return toDate(value, simpleDateFormats);
        } catch (DataFormatException ignored) {
        }
        return null;
    }

    /**
     * 将一个字符串转换为Timestamp类型,如果转换失败,会抛出DataFormatException异常
     *
     * @param value             需要处理的值
     * @param simpleDateFormats 自定义的日志格式化类
     * @return Date对象     *
     * @throws DataFormatException 如果传入的不是一个合法日期将抛出此异常
     */
    public synchronized static Timestamp toTimestamp(final Object value, final SimpleDateFormat... simpleDateFormats) throws DataFormatException {
        Date d = toDate(value, simpleDateFormats);
        return (d == null) ? null : new Timestamp(d.getTime());
    }

    /**
     * 将一个字符串转换为日期类型,如果转换失败,会抛出DataFormatException异常
     *
     * @param value             需要处理的值
     * @param simpleDateFormats 自定义的日志格式化类
     * @return Date对象     *
     * @throws DataFormatException 如果传入的不是一个合法日期将抛出此异常
     */
    public synchronized static Date toDate(final Object value, final SimpleDateFormat... simpleDateFormats) throws DataFormatException {

        if (value == null) {
            return null;
        }

        if (value instanceof LocalDateTime) {
            return Date.from(((LocalDateTime) value).atZone(ZoneId.systemDefault()).toInstant());
        } else if (value instanceof LocalDate) {
            return Date.from(((LocalDate) value).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        }

        String date = value.toString();

        // 如果是空串，那么返回null值
        if (StringUtils.isBlank(date)) {
            return null;
        }

        String dateString = date.trim();

        //判断日期是否包含小时分秒，如果不包含，那么添加时间部分内容
        if (!TIME_PATTERN.matcher(dateString).matches()) {
            dateString += " 00:00:00";
        }

        //判断日期是否包含毫秒数据，如果不包含，那么添加毫秒部分
        if (!M_SECOND_PATTERN.matcher(dateString).matches()) {
            dateString += ".000";
        }

        //如果传入的表达式，那么尝试采用传入的表达式进行匹配
        if (simpleDateFormats != null) {
            for (SimpleDateFormat format : simpleDateFormats) {
                try {
                    return format.parse(dateString);
                } catch (ParseException ignored) {
                }
            }
        }


        try {
            String[] values = dateString.split("[^0-9]");
            int times[] = new int[7];
            for (int i = 0, j = 0; i < values.length; i++) {
                if (StringUtils.notBlank(values[i])) {
                    times[j] = NumberUtils.toInt(values[i]);
                    j++;
                }
            }

            LocalDateTime localDateTime;
            if (times[0] >= 1900) {
                localDateTime = LocalDateTime.of(times[0], times[1], times[2], times[3], times[4], times[5], times[6] * 1000000);
            } else {
                localDateTime = LocalDateTime.of(times[2], times[0], times[1], times[3], times[4], times[5], times[6] * 1000000);
            }

            return DateUtils.toDate(localDateTime);
        } catch (Exception ex) {
            throw new DataFormatException("无法识别的日期值：" + date);      //如果转换失败，那么抛出异常。
        }
    }

    /**
     * 获取日期的年月日表示表示yyyy-MM-dd
     *
     * @param date Date对象
     * @return 年月日字符串
     */
    public static String toShortString(final Date date) {
        return (date == null) ? null : new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    /**
     * 获取日期的年月日表示表示yyyy-MM-dd
     *
     * @param timestamp Timestamp对象
     * @return 年月日字符串
     */
    public static String toShortString(final java.sql.Timestamp timestamp) {
        return (timestamp == null) ? null : new SimpleDateFormat("yyyy-MM-dd").format(timestamp);
    }

    /**
     * 获取日期的年月日 小时：分：秒表示 yyyy-MM-dd HH:mm:ss
     *
     * @param date Date对象
     * @return 日期的年月日 小时：分：秒表示
     */
    public static String toString(final Date date) {
        return (date == null) ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    /**
     * 获取日期的年月日 小时：分：秒表示 yyyy-MM-dd HH:mm:ss
     *
     * @param timestamp Timestamp对象
     * @return 日期的年月日 小时：分：秒表示
     */
    public static String toString(final java.sql.Timestamp timestamp) {
        return (timestamp == null) ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(timestamp);
    }

    /**
     * 获取日期的年月日 小时：分：秒表示 yyyy-MM-dd HH:mm:ss.SSS
     *
     * @param date Date对象
     * @return 日期的年月日 小时：分：秒表示
     */
    public static String toFullString(final Date date) {
        return (date == null) ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(date);
    }

    /**
     * 获取日期的年月日 小时：分：秒表示 yyyy-MM-dd HH:mm:ss.SSS
     *
     * @param timestamp Timestamp对象
     * @return 日期的年月日 小时：分：秒表示
     */
    public static String toFullString(final java.sql.Timestamp timestamp) {
        return (timestamp == null) ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(timestamp);
    }

    /**
     * 将日期类型转换为LocalDateTime类型
     *
     * @param date 需要转换的日期对象
     * @return LocalDateTime类型对象
     */
    public static LocalDateTime toLocalDateTime(final Date date) {
        return (date == null) ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    //-----------------------------------------------------------------------

    /**
     * Adds a number of years to a date returning a new object.
     * The original {@code Date} is unchanged.
     *
     * @param date   the date, not null
     * @param amount the amount to add, may be negative
     * @return the new {@code Date} with the amount added
     * @throws IllegalArgumentException if the date is null
     */
    public static Date addYears(final Date date, final int amount) {
        return add(date, Calendar.YEAR, amount);
    }

    //-----------------------------------------------------------------------

    /**
     * Adds a number of months to a date returning a new object.
     * The original {@code Date} is unchanged.
     *
     * @param date   the date, not null
     * @param amount the amount to add, may be negative
     * @return the new {@code Date} with the amount added
     * @throws IllegalArgumentException if the date is null
     */
    public static Date addMonths(final Date date, final int amount) {
        return add(date, Calendar.MONTH, amount);
    }

    //-----------------------------------------------------------------------

    /**
     * Adds a number of weeks to a date returning a new object.
     * The original {@code Date} is unchanged.
     *
     * @param date   the date, not null
     * @param amount the amount to add, may be negative
     * @return the new {@code Date} with the amount added
     * @throws IllegalArgumentException if the date is null
     */
    public static Date addWeeks(final Date date, final int amount) {
        return add(date, Calendar.WEEK_OF_YEAR, amount);
    }

    //-----------------------------------------------------------------------

    /**
     * Adds a number of days to a date returning a new object.
     * The original {@code Date} is unchanged.
     *
     * @param date   the date, not null
     * @param amount the amount to add, may be negative
     * @return the new {@code Date} with the amount added
     * @throws IllegalArgumentException if the date is null
     */
    public static Date addDays(final Date date, final int amount) {
        return add(date, Calendar.DAY_OF_MONTH, amount);
    }

    //-----------------------------------------------------------------------

    /**
     * Adds a number of hours to a date returning a new object.
     * The original {@code Date} is unchanged.
     *
     * @param date   the date, not null
     * @param amount the amount to add, may be negative
     * @return the new {@code Date} with the amount added
     * @throws IllegalArgumentException if the date is null
     */
    public static Date addHours(final Date date, final int amount) {
        return add(date, Calendar.HOUR_OF_DAY, amount);
    }

    //-----------------------------------------------------------------------

    /**
     * Adds a number of minutes to a date returning a new object.
     * The original {@code Date} is unchanged.
     *
     * @param date   the date, not null
     * @param amount the amount to add, may be negative
     * @return the new {@code Date} with the amount added
     * @throws IllegalArgumentException if the date is null
     */
    public static Date addMinutes(final Date date, final int amount) {
        return add(date, Calendar.MINUTE, amount);
    }

    //-----------------------------------------------------------------------

    /**
     * Adds a number of seconds to a date returning a new object.
     * The original {@code Date} is unchanged.
     *
     * @param date   the date, not null
     * @param amount the amount to add, may be negative
     * @return the new {@code Date} with the amount added
     * @throws IllegalArgumentException if the date is null
     */
    public static Date addSeconds(final Date date, final int amount) {
        return add(date, Calendar.SECOND, amount);
    }

    //-----------------------------------------------------------------------

    /**
     * Adds a number of milliseconds to a date returning a new object.
     * The original {@code Date} is unchanged.
     *
     * @param date   the date, not null
     * @param amount the amount to add, may be negative
     * @return the new {@code Date} with the amount added
     * @throws IllegalArgumentException if the date is null
     */
    public static Date addMilliseconds(final Date date, final int amount) {
        return add(date, Calendar.MILLISECOND, amount);
    }

    //-----------------------------------------------------------------------

    /**
     * Adds to a date returning a new object.
     * The original {@code Date} is unchanged.
     *
     * @param date          the date, not null
     * @param calendarField the calendar field to add to
     * @param amount        the amount to add, may be negative
     * @return the new {@code Date} with the amount added
     * @throws IllegalArgumentException if the date is null
     */
    private static Date add(final Date date, final int calendarField, final int amount) {
        Validate.notNull(date);
        final Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(calendarField, amount);
        return c.getTime();
    }

    //-----------------------------------------------------------------------

    /**
     * Converts a {@code Date} into a {@code Calendar}.
     *
     * @param date the date to convert to a Calendar
     * @return the created Calendar
     * @throws NullPointerException if null is passed in
     * @since 3.0
     */
    public static Calendar toCalendar(final Date date) {
        final Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c;
    }

    //-----------------------------------------------------------------------

    /**
     * Converts a {@code Date} of a given {@code TimeZone} into a {@code Calendar}
     *
     * @param date the date to convert to a Calendar
     * @param tz   the time zone of the {@code date}
     * @return the created Calendar
     * @throws NullPointerException if {@code date} or {@code tz} is null
     */
    public static Calendar toCalendar(final Date date, final TimeZone tz) {
        final Calendar c = Calendar.getInstance(tz);
        c.setTime(date);
        return c;
    }

    //-----------------------------------------------------------------------

    /**
     * 采用截断的方式来处理日期精度
     *
     * @param date     需要处理的日期
     * @param timeUnit 处理后保留的精度
     * @return the different rounded date, not null
     * @throws ArithmeticException if the year is over 280 million
     */
    public static Date round(final Date date, final TimeUnit timeUnit) {
        Validate.notNull(date);
        long time = date.getTime();

        switch (timeUnit) {
            case SECONDS:
                time = time / MILLIS_PER_SECOND * MILLIS_PER_SECOND;
                break;
            case MINUTES:
                time = time / MILLIS_PER_MINUTE * MILLIS_PER_MINUTE;
                break;
            case HOURS:
                time = time / MILLIS_PER_HOUR * MILLIS_PER_HOUR;
                break;
            case DAYS:
                time = time / MILLIS_PER_DAY * MILLIS_PER_DAY;
                break;
        }
        return new Date(time);
    }

    /**
     * 比较两个日期的大小
     *
     * @param date1 日期1
     * @param date2 日期2
     * @return 大小对比结果
     */
    public static int compareDate(final Date date1, final Date date2) {
        return Long.compare(date1.getTime(), date2.getTime());
    }

    /**
     * 比较两个日期的大小
     *
     * @param date1    日期1
     * @param date2    日期2
     * @param timeUnit 对比的精度
     * @return 大小对比结果
     */
    public static int compareDate(final Date date1, final Date date2, final TimeUnit timeUnit) {
        return Long.compare(round(date1, timeUnit).getTime(), round(date2, timeUnit).getTime());
    }

    /**
     * 获取指定月份的的最后一天
     *
     * @param year  年
     * @param month 月
     * @return 最后一天
     */
    public static Date getMonthLastDay(int year, int month) {
        return addDays(getNextMonthFirstDay(year, month), -1);
    }

    /**
     * 获取指定月份的的第一天
     *
     * @param year  年
     * @param month 月
     * @return 最后一天
     */
    public static Date getMonthFirstDay(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    /**
     * 获取下个月的第一天
     *
     * @param year  年
     * @param month 月
     * @return 第一天
     */
    public static Date getNextMonthFirstDay(int year, int month) {
        return addMonths(getMonthFirstDay(year, month), 1);
    }
}
