package com.baidu.idl.face.main.utils;

import android.os.Environment;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

//存储日志

public class LogFileUtilsStatic {
    private static Object obj = new Object();

    //文件名称
    private static String fileName = "人脸Log日志（静态识别）" + ".log";
//    private static String fileName = "operationLog-" + "人脸Log对比日志" + ".log";

    //文件路径
    private static String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Log日志/";

    /**
     * 写入文件
     *
     * @param msg
     */
    public static void writeLogFile(String msg) {
        synchronized (obj) {
            try {
                createFile();
                File file = new File(filePath + fileName);
                FileWriter fw = null;
                if (file.exists()) {
                    if (file.length() > LogVariateUtils.getInstance().getFileSize())
                        fw = new FileWriter(file, false);
                    else
                        fw = new FileWriter(file, true);
                } else
                    fw = new FileWriter(file, false);

                Date d = new Date();
                SimpleDateFormat s = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");
                String dateStr = s.format(d);

                fw.write(String.format("[%s] %s", dateStr, msg));
                fw.write(13);
                fw.write(10);
                fw.flush();
                fw.close();
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 取出文件
     *
     * @return
     */
    public static String readLogText() {
        FileReader fr = null;
        try {
            File file = new File(filePath + fileName);
            if (!file.exists()) {
                return "";
            }
            long n = LogVariateUtils.getInstance().getFileSize();
            long len = file.length();
            long skip = len - n;
            fr = new FileReader(file);
            fr.skip(Math.max(0, skip));
            char[] cs = new char[(int) Math.min(len, n)];
            fr.read(cs);
            return new String(cs).trim();
        } catch (Throwable ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (fr != null)
                    fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    //创建文件夹
    public static void createFile() {
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdir();
        }
    }

}
