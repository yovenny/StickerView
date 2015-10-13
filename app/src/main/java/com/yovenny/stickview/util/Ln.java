package com.yovenny.stickview.util;

public class Ln {
    public static final String LOG_TAG = "Knife";
    public static final boolean DEBUG = true;

    private static void log (int type, String message) {
        StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[4];
        String className = stackTrace.getClassName();
        String fromCode = className.substring(className.lastIndexOf('.') + 1) + "." + stackTrace.getMethodName() + "#" + stackTrace.getLineNumber();
        message = "from code:" + fromCode + "\n" + message;
        switch (type) {
            case android.util.Log.DEBUG:
                android.util.Log.d( LOG_TAG, message);
                break;
            case android.util.Log.INFO:
                android.util.Log.i( LOG_TAG, message);
                break;
            case android.util.Log.WARN:
                android.util.Log.w( LOG_TAG, message);
                break;
            case android.util.Log.ERROR:
                android.util.Log.e( LOG_TAG, message);
                break;
            case android.util.Log.VERBOSE:
                android.util.Log.v( LOG_TAG, message);
                break;
        }
    }

    public static void d (String message) {
        if ( DEBUG) log(android.util.Log.DEBUG, message);
    }

    public static void i (String message) {
        if ( DEBUG) log(android.util.Log.INFO, message);
    }

    public static void w (String message) {
        if ( DEBUG) log(android.util.Log.WARN, message);
    }

    public static void e (String message) {
        log(android.util.Log.ERROR, message);
    }

    public static void v (String message) {
        if ( DEBUG) log(android.util.Log.VERBOSE, message);
    }
}
