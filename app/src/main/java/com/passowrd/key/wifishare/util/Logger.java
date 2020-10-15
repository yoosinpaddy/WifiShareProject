package com.passowrd.key.wifishare.util;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {

    /*Log types*/
    public static final String LOG_E = "ERROR";
    public static final String LOG_W = "WARNING";
    public static final String LOG_I = "INFO";
    public static final String LOG_D = "DEBUG";
    public static final String LOG_V = "VERBOSE";

    public static void logThis(String tag, String methodName, String message, Integer line) {
        Log.d(tag, methodName + " " + message + "  " + line);
        appendToLogFile(tag, methodName, message, line);
    }

    private static void appendToLogFile(String tag, String methodName, String message, Integer line) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/.KenyaSihami");

        if (!myDir.exists())
            myDir.mkdirs();

        File logFile = new File(myDir + "/kenya_sihami.txt");
        String mLine = String.valueOf(line);
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append("---- Begin Entry ----" + "\n");
            buf.append("Log " + System.currentTimeMillis() + ":" + "\n");
            buf.append("Method Name: " + methodName + "\n");
            buf.append(tag + ": message: " + message + " on line: " + mLine + "\n");
            buf.append("---- End Entry ----" + "\n\n");
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
