package com.dongzy.common.common.time;

/**
 * 表示时间差的类
 */
public class TimeSpan {

    public final static String YEAR = "y";
    public final static String MONTH = "M";
    public final static String DAY = "d";
    public final static String HOUR = "H";
    public final static String MINUTE = "m";
    public final static String SECOND = "s";
    public final static String MILLISECOND = "S";

    private int years;              //年数
    private int months;             //月数
    private int days;               //日数
    private int hours;              //小时数
    private int minutes;            //分钟数
    private int seconds;            //秒数
    private int milliseconds;       //毫秒数

    void setYears(int years) {
        this.years = years;
    }

    void setMonths(int months) {
        this.months = months;
    }

    void setDays(int days) {
        this.days = days;
    }

    void setHours(int hours) {
        this.hours = hours;
    }

    void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    void setMilliseconds(int milliseconds) {
        this.milliseconds = milliseconds;
    }

    public int getYears() {
        return years;
    }

    public int getMonths() {
        return months;
    }

    public int getDays() {
        return days;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public int getMilliseconds() {
        return milliseconds;
    }

}
