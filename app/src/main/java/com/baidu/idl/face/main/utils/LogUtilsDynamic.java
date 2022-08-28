package com.baidu.idl.face.main.utils;

import android.util.Log;

//打印日志并存储日志记录

public class LogUtilsDynamic {

    public static void i(String title, String msg) {
        String str = formatString(title, msg);
        if (LogVariateUtils.getInstance().getIsShowLog())
            Log.i(LogVariateUtils.getInstance().getTag(), str);
        if (LogVariateUtils.getInstance().getIsWriteLog())
            LogFileUtilsDynamic.writeLogFile(str);
    }

    public static void w(String title, String msg) {
        String str = formatString(title, msg);
        if (LogVariateUtils.getInstance().getIsShowLog())
            Log.w(LogVariateUtils.getInstance().getTag(), str);
        if (LogVariateUtils.getInstance().getIsWriteLog())
            LogFileUtilsDynamic.writeLogFile(str);
    }

    public static void e(String title, String msg) {
        String str = formatString(title, msg);
        if (LogVariateUtils.getInstance().getIsShowLog())
            Log.e(LogVariateUtils.getInstance().getTag(), str);
        if (LogVariateUtils.getInstance().getIsWriteLog())
            LogFileUtilsDynamic.writeLogFile(str);
    }

    public static String formatString(String title, String msg) {
        if (title == null) {
            return msg == null ? "" : msg;
        }
        return String.format("[%s]: %s", title, msg == null ? "" : msg);
    }

}
