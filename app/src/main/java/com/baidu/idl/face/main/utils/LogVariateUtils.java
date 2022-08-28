package com.baidu.idl.face.main.utils;

//变量存储

public class LogVariateUtils {

    private boolean SHOW_LOG = false;//是否在logcat中打印log，true打印，false不打印
    private boolean WRITE_LOG = true;//是否在文件中记录，true记录，false不记录

    private long fileSize = 1000000 * 2;//日志文件的大小，默认0.1M   1000000

    private String TAG = "logging";//Logcat中显示的tag

    private static LogVariateUtils instance;

    public static LogVariateUtils getInstance() {
        if (instance == null) {
            synchronized (LogVariateUtils.class) {
                if (instance == null) {
                    instance = new LogVariateUtils();
                }
            }
        }
        return instance;
    }

    public LogVariateUtils isShowLog(boolean isShowLog) {
        SHOW_LOG = isShowLog;
        return this;
    }

    public boolean getIsShowLog() {
        return SHOW_LOG;
    }

    public LogVariateUtils isWriteLog(boolean isWriteLog) {
        WRITE_LOG = isWriteLog;
        return this;
    }

    public boolean getIsWriteLog() {
        return WRITE_LOG;
    }

    public LogVariateUtils fileSize(long size) {
        this.fileSize = size;
        return this;
    }

    public long getFileSize() {
        return this.fileSize;
    }

    public LogVariateUtils tag(String tag) {
        TAG = tag;
        return this;
    }

    public String getTag() {
        return TAG;
    }

}
