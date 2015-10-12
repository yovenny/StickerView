package com.yovenny.stickview.util;
/**
 * Summary: 保存图片,并显示到相册
 * Version 1.0
 * Author: chenbc@jugame.com.cn
 * Company: www.mjgame.cn
 * Date: 15-4-25
 * Time: 下午7:49
 * Copyright: Copyright (c) 2013
*/

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PhotoUtils {
    // 5
    public static Uri addToTouchActiveAlbum(Context context, String title, String filePath) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, title);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.BUCKET_ID, filePath.hashCode());
        values.put(MediaStore.Images.Media.BUCKET_DISPLAY_NAME, "football");

        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.DESCRIPTION, "football");
        values.put(MediaStore.MediaColumns.DATA, filePath);
        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        return uri;
    }

    public static void addImageToGallery(final String filePath, final Context context) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, filePath);
        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }


    public static void saveImage(String path, Bitmap bm) throws IOException {
        FileOutputStream out = new FileOutputStream(new File(path));//new File(path + ".jpg")
        try {
            bm.compress(Bitmap.CompressFormat.JPEG, 100, out); // Compress Image
            out.flush();
            out.close();
        } catch (Exception e) {
            throw new IOException();
        }
    }


    //扫描指定文件
    public static void fileScan(Context ctx, String filePath) {
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scanIntent.setData(Uri.fromFile(new File(filePath)));
        ctx.sendBroadcast(scanIntent);
    }


}