package com.yovenny.stickview.util;

import android.os.Environment;

import java.io.File;

/**
 * Created by skyArraon on 16/4/30.
 */
public class FileUtil {
    public static String STORE_PATH = Environment.getExternalStorageDirectory() + "/StickView/";

    public static File createTempFile(String part, String ext) throws Exception {
        File tempDir = new File(STORE_PATH + "transit/");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        return File.createTempFile(part, ext, tempDir);
    }
}
