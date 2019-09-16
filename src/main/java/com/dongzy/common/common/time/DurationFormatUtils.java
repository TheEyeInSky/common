package com.dongzy.common.common.time;

import com.dongzy.common.common.Validate;

import java.util.Calendar;
import java.util.Date;

/**
 * <p>Duration formatting utilities and constants. The following table describes the tokens
 * used in the pattern language for formatting. </p>
 * <table border="1" summary="Pattern Tokens">
 * <tr><th>character</th><th>duration element</th></tr>
 * <tr><td>y</td><td>years</td></tr>
 * <tr><td>M</td><td>months</td></tr>
 * <tr><td>d</td><td>days</td></tr>
 * <tr><td>H</td><td>hours</td></tr>
 * <tr><td>m</td><td>minutes</td></tr>
 * <tr><td>s</td><td>seconds</td></tr>
 * <tr><td>S</td><td>milliseconds</td></tr>
 * <tr><td>'text'</td><td>arbitrary text content</td></tr>
 * </table>
 *
 * <b>Note: It's not currently possible to include a single-quote in a format.</b>
 * <br>
 * Token values are printed using decimal digits.
 * A token character can be repeated to ensure that the field occupies a certain minimum
 * size. Values will be left-padded with 0 unless padding is disabled in the method invocation.
 *
 * @since 2.1
 */
public class DurationFormatUtils {

    private final static Date INIT_DATE = DateUtils.tryToDate("2000-01-01");

    /**
     * 两个日期之间的时间差，日期1必须小于日期2
     *
     * @param date1 日期1
     * @param date2 日期2
     * @return 相差时间对象
     */
    public static TimeSpan duration(final Date date1, final Date date2) {
        return duration(date1, date2, TimeSpan.YEAR);
    }

    /**
     * 两个日期之间的时间差，日期1必须小于日期2
     *
     * @param date1  日期1
     * @param date2  日期2
     * @param format 日期返回的精度，可选值yMdHms
     * @return 相差时间对象
     */
    public static TimeSpan duration(final Date date1, final Date date2, String format) {
        long diff = date2.getTime() - date1.getTime();
        return duration(diff, format);
    }

    /**
     * 两个日期之间的时间差，日期1必须小于日期2
     *
     * @param diff 时间间隔毫秒数
     * @return 相差时间对象
     */
    public static TimeSpan duration(final long diff) {
        return duration(diff, TimeSpan.YEAR);
    }

    /**
     * 两个日期之间的时间差，日期1必须小于日期2
     *
     * @param diff   时间间隔毫秒数
     * @param format 日期返回的精度，可选值yMdHms
     * @return 相差时间对象
     */
    public static TimeSpan duration(final long diff, String format) {
        Validate.isTrue(diff >= 0, "date2 must greater date1");

        TimeSpan times = new TimeSpan();
        int length;
        switch (format) {
            case TimeSpan.YEAR:
                length = 7;
                break;
            case TimeSpan.MONTH:
                length = 6;
                break;
            case TimeSpan.DAY:
                times.setMilliseconds((int) (diff % DateUtils.MILLIS_PER_SECOND));
                times.setSeconds((int) (diff / DateUtils.MILLIS_PER_SECOND % 60));
                times.setMinutes((int) (diff / DateUtils.MILLIS_PER_MINUTE % 60));
                times.setHours((int) (diff / DateUtils.MILLIS_PER_HOUR % 24));
                times.setDays((int) (diff / DateUtils.MILLIS_PER_HOUR / 24));
                return times;
            case TimeSpan.HOUR:
                times.setMilliseconds((int) (diff % DateUtils.MILLIS_PER_SECOND));
                times.setSeconds((int) (diff / DateUtils.MILLIS_PER_SECOND % 60));
                times.setMinutes((int) (diff / DateUtils.MILLIS_PER_MINUTE % 60));
                times.setHours((int) (diff / DateUtils.MILLIS_PER_MINUTE / 60));
                return times;
            case TimeSpan.MINUTE:
                times.setMilliseconds((int) (diff % DateUtils.MILLIS_PER_SECOND));
                times.setSeconds((int) (diff / DateUtils.MILLIS_PER_SECOND % 60));
                times.setMinutes((int) (diff / DateUtils.MILLIS_PER_SECOND / 60));
                return times;
            case TimeSpan.SECOND:
                times.setMilliseconds((int) (diff % DateUtils.MILLIS_PER_SECOND));
                times.setSeconds((int) (diff / DateUtils.MILLIS_PER_SECOND / 60));
                return times;
            default:
                throw new IllegalArgumentException("format:" + format + " invalid");
        }

        // timezones get funky around 0, so normalizing everything to GMT
        // stops the hours being off
        final Calendar start = Calendar.getInstance();
        start.setTime(INIT_DATE);
        final Calendar end = Calendar.getInstance();
        end.setTime(new Date(INIT_DATE.getTime() + diff));

        // initial estimates
        int milliseconds = end.get(Calendar.MILLISECOND) - start.get(Calendar.MILLISECOND);
        int seconds = end.get(Calendar.SECOND) - start.get(Calendar.SECOND);
        int minutes = end.get(Calendar.MINUTE) - start.get(Calendar.MINUTE);
        int hours = end.get(Calendar.HOUR_OF_DAY) - start.get(Calendar.HOUR_OF_DAY);
        int days = end.get(Calendar.DAY_OF_MONTH) - start.get(Calendar.DAY_OF_MONTH);
        int months = end.get(Calendar.MONTH) - start.get(Calendar.MONTH);
        int years = end.get(Calendar.YEAR) - start.get(Calendar.YEAR);

        // each initial estimate is adjusted in case it is under 0
        while (milliseconds < 0) {
            milliseconds += 1000;
            seconds -= 1;
        }
        times.setMilliseconds(milliseconds);
        while (seconds < 0) {
            seconds += 60;
            minutes -= 1;
        }
        times.setSeconds(seconds);
        while (minutes < 0) {
            minutes += 60;
            hours -= 1;
        }
        times.setMinutes(minutes);
        while (hours < 0) {
            hours += 24;
            days -= 1;
        }
        times.setHours(hours);
        while (days < 0) {
            days += start.getActualMaximum(Calendar.DAY_OF_MONTH);
            months -= 1;
            start.add(Calendar.MONTH, 1);
        }
        times.setDays(days);
        while (months < 0) {
            months += 12;
            years -= 1;
        }

        if (length == 6) {
            times.setMonths(months + years * 12);
        }
        if (length == 7) {
            times.setMonths(months);
            times.setYears(years);
        }

        return times;
    }

    /**
     * <p>Formats the time gap as a string.</p>
     *
     * <p>The format used is ISO 8601-like: {@code HH:mm:ss.SSS}.</p>
     *
     * @param durationMillis the duration to format
     * @return the formatted duration, not null
     * @throws java.lang.IllegalArgumentException if durationMillis is negative
     */
    public static String formatDurationHMS(final long durationMillis) {
        TimeSpan timeSpan = duration(durationMillis, TimeSpan.HOUR);
        return format(timeSpan);
    }

    private static String format(TimeSpan timeSpan) {
        StringBuilder builder = new StringBuilder();
        if (timeSpan.getYears() != 0) {
            builder.append(timeSpan.getYears());
            builder.append("-");
        }
        if (builder.length() > 0 || timeSpan.getMonths() != 0) {
            builder.append(timeSpan.getMonths());
            builder.append("-");
        }
        if (builder.length() > 0 || timeSpan.getDays() != 0) {
            builder.append(timeSpan.getDays());
            builder.append(" ");
        }
        if (builder.length() > 0 || timeSpan.getHours() != 0) {
            builder.append(timeSpan.getHours());
            builder.append(":");
        }
        if (builder.length() > 0 || timeSpan.getMinutes() != 0) {
            builder.append(timeSpan.getMinutes());
            builder.append(":");
        }
        if (builder.length() > 0 || timeSpan.getSeconds() != 0) {
            builder.append(timeSpan.getSeconds());
            builder.append(".");
        }
        builder.append(timeSpan.getMilliseconds());

        return builder.toString();
    }
}
