package com.yovenny.stickview.util;


import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AssetsUtil {

    /**
     * 从Assets中读取图片
     * getImageFromAssetsFile("Cat_Blink/cat_blink0000.png");
     */
    private Bitmap getImageFromAssetsFile(Context context, String fileName) {
        Bitmap image = null;
        AssetManager am = context.getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    public void readByte(Context c, String name, int indexInt, String path) {
        byte[] b = null;
        int[] intArrat = c.getResources().getIntArray(indexInt);
        try {
            AssetManager am = c.getAssets();
            InputStream is = am.open(name);
            for (int i = 0; i < intArrat.length; i++) {
                b = new byte[intArrat[i]];
                // 读取数据
                is.read(b);
                saveBitmap(Bytes2Bimap(b), path);
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Bitmap Bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }

    public static boolean saveBitmap(Bitmap bmp, String path) {
        File f = new File(path);
        try {
            f.createNewFile();
            FileOutputStream fOut = new FileOutputStream(f);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
            return true;
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return false;
    }



    public static String copyDirorfileFromAssetManager(String arg_assetDir, String arg_destinationDir, Context context) throws IOException {
        String dest_dir_path = arg_destinationDir;
        File dest_dir = new File(dest_dir_path);
        createDir(dest_dir);
        AssetManager asset_manager = context.getAssets();
        String[] files = asset_manager.list(arg_assetDir);

        for (int i = 0; i < files.length; i++) {
            String abs_asset_file_path = addTrailingSlash(arg_assetDir) + files[i];
            String sub_files[] = asset_manager.list(abs_asset_file_path);
            if (sub_files.length == 0) {
                String dest_file_path = addTrailingSlash(dest_dir_path) + files[i];
                if(!new File(dest_file_path).exists()){
                    copyAssetFile(abs_asset_file_path, dest_file_path, context);
                }
            } else {
                copyDirorfileFromAssetManager(abs_asset_file_path, addTrailingSlash(arg_destinationDir) + files[i], context);
            }
        }
        return dest_dir_path;
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


    /**
     *  以下方法跟上面方法有重复
     */
    public static boolean copyAssetFolder(AssetManager assetManager, String fromAssetPath, String toPath) {
        try {
            String[] files = assetManager.list(fromAssetPath);
            new File(toPath).mkdirs();
            boolean res = true;
            for (String file : files)
                if (file.contains("."))//这种判断方式不合理
                    res &= copyAssetFile(assetManager,
                            fromAssetPath + File.separator + file,
                            toPath + File.separator + file);
                else
                    res &= copyAssetFolder(assetManager,
                            fromAssetPath + File.separator + file,
                            toPath + File.separator + file);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean copyAssetFile(AssetManager assetManager, String fromAssetPath, String toPath) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(assetManager.open(fromAssetPath));
        FileOutputStream fos = new FileOutputStream(toPath);
        byte[] arrayOfByte = new byte[4096];
        try {
            while (true) {
                int len = bis.read(arrayOfByte);
                if (len == -1) {
                    fos.flush();
                    break;
                }
                fos.write(arrayOfByte, 0, len);
            }
        } finally {
            fos.close();
            bis.close();
        }
        return true;
    }

    public static boolean copyAssetFile(String absPath, String toPath) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(absPath));
        FileOutputStream fos = new FileOutputStream(toPath);
        byte[] arrayOfByte = new byte[4096];
        try {
            while (true) {
                int len = bis.read(arrayOfByte);
                if (len == -1) {
                    fos.flush();
                    break;
                }
                fos.write(arrayOfByte, 0, len);
            }
        } finally {
            fos.close();
            bis.close();
        }
        return true;
    }

}
