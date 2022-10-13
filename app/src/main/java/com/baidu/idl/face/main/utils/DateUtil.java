package com.baidu.idl.face.main.utils;

import android.text.format.Time;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    /**
     * 时间戳转换成日期格式字符串
     *
     * @param seconds 精确到秒的字符串
     * @param format
     * @return
     */
    public static String timeStamp2Date(String seconds, String format) {
        if (seconds == null || seconds.isEmpty() || seconds.equals("null")) {
            return "";
        }
        if (format == null || format.isEmpty()) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(Long.valueOf(seconds + "000")));
    }

    /**
     * 日期格式字符串转换成时间戳
     *
     * @param date_str 字符串日期
     * @param format   如：yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String date2TimeStamp(String date_str, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return String.valueOf(sdf.parse(date_str).getTime() / 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 取得当前时间戳（精确到秒）
     *
     * @return
     */
    public static String timeStamp() {
        long time = System.currentTimeMillis();
        String t = String.valueOf(time / 1000);
        return t;
    }

    public static String getHHmmss() {
        SimpleDateFormat date = new SimpleDateFormat("HH:mm:ss");//获取时分秒
        return date.format(new Date());
    }

    /**
     * 获取时间 小时:分; HH:mm
     *
     * @return
     */
    public static String getTimeShort() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        Date currentTime = new Date();
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    /***
     *
     * @param beginHour 开始小时       比如   23
     * @param beginMin  开始小时的分钟  比如  00
     * @param endHour   结束小时        比如  5
     * @param endMin    结束小时的分钟   比如 00
     * @return         true表示范围内   否则false
     */
    public static boolean atTheCurrentTime(int beginHour, int beginMin, int endHour, int endMin) {
        boolean result = false;
        final long aDayInMillis = 1000 * 60 * 60 * 24;
        final long currentTimeMillis = System.currentTimeMillis();
        Time now = new Time();
        now.set(currentTimeMillis);
        Time startTime = new Time();
        startTime.set(currentTimeMillis);
        startTime.hour = beginHour;
        startTime.minute = beginMin;
        Time endTime = new Time();
        endTime.set(currentTimeMillis);
        endTime.hour = endHour;
        endTime.minute = endMin;
        /**跨天的特殊情况(比如23:00-2:00)*/
        if (!startTime.before(endTime)) {
            startTime.set(startTime.toMillis(true) - aDayInMillis);
            result = !now.before(startTime) && !now.after(endTime); // startTime <= now <= endTime
            Time startTimeInThisDay = new Time();
            startTimeInThisDay.set(startTime.toMillis(true) + aDayInMillis);
            if (!now.before(startTimeInThisDay)) {
                result = true;
            }
        } else {
            /**普通情况(比如5:00-10:00)*/
            result = !now.before(startTime) && !now.after(endTime); // startTime <= now <= endTime
        }
        return result;
    }

//    public static void main(String[] args) {
//        String timeStamp = timeStamp();
//        System.out.println("timeStamp="+timeStamp); //运行输出:timeStamp=1470278082
//        System.out.println(System.currentTimeMillis());//运行输出:1470278082980
//        //该方法的作用是返回当前的计算机时间，时间的表达格式为当前计算机时间和GMT时间(格林威治时间)1970年1月1号0时0分0秒所差的毫秒数
//
//        String date = timeStamp2Date(timeStamp, "yyyy-MM-dd HH:mm:ss");
//        System.out.println("date="+date);//运行输出:date=2016-08-04 10:34:42
//
//        String timeStamp2 = date2TimeStamp(date, "yyyy-MM-dd HH:mm:ss");
//        System.out.println(timeStamp2);  //运行输出:1470278082
//    }
}