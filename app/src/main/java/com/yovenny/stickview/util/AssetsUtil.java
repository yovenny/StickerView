package com.yovenny.stickview.util;


import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

//copy from stackoverflow

public class AssetsUtil {

    public static String copyDirectoryFromAsset(String argAssetDir, String argDestDir, Context context) throws IOException {
        String destDir = argDestDir;
        File dest_dir = new File(destDir);
        createDir(dest_dir);
        AssetManager assetManager = context.getAssets();
        String[] files = assetManager.list(argAssetDir);

        for (int i = 0; i < files.length; i++) {
            String absAssetFilePath = addTrailingSlash(argAssetDir) + files[i];
            String subFiles[] = assetManager.list(absAssetFilePath);
            if (subFiles.length == 0) {
                String destFilePath = addTrailingSlash(destDir) + files[i];
                if(!new File(destFilePath).exists()){
                    copyAssetFile(absAssetFilePath, destFilePath, context);
                }
            } else {
                copyDirectoryFromAsset(absAssetFilePath, addTrailingSlash(argDestDir) + files[i], context);
            }
        }
        return destDir;
    }


    public static void copyAssetFile(String assetFilePath, String destinationFilePath, Context context) throws IOException {
        InputStream in = context.getAssets().open(assetFilePath);
        OutputStream out = new FileOutputStream(destinationFilePath);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0)
            out.write(buf, 0, len);
        in.close();
        out.close();
    }

    public static String addTrailingSlash(String path) {
        if (path.charAt(path.length() - 1) != '/') {
            path += "/";
        }
        return path;
    }

    public static String addLeadingSlash(String path) {
        if (path.charAt(0) != '/') {
            path = "/" + path;
        }
        return path;
    }

    public static void createDir(File dir) throws IOException {
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                throw new IOException("Can't create directory, a file is in the way");
            }
        } else {
            dir.mkdirs();
            if (!dir.isDirectory()) {
                throw new IOException("Unable to create directory");
            }
        }
    }

}
