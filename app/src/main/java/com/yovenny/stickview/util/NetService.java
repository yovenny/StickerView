/**
 * Summary: 网络请求层封装
 * Version 1.0
 * Author: zhaomi@jugame.com.cn
 * Company: muji.com
 * Date: 13-11-5
 * Time: 下午12:38
 * Copyright: Copyright (c) 2013
 */

package com.yovenny.stickview.util;


import com.yovenny.stickview.interf.OnProgressUpdateListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetService {



    public static boolean downloadFile(String srcUrl, File destFile, int retry, OnProgressUpdateListener listener) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(destFile);
        } catch (Exception e) {
            e.printStackTrace();
            Ln.d("create file-output-stream failed");

        }
        boolean ret = downloadFile(srcUrl, outputStream, retry, listener);
        if (!ret) {
            FileUtil.deleteFile(destFile.getAbsolutePath());
        }
        return ret;
    }

    //httpUrlConnection
    private static boolean downloadFile(String srcUrl, OutputStream os, int retry, OnProgressUpdateListener listener) {
        if (os == null) {
            return false;
        }
        if (retry <= 0) {
            retry = 1;
        }
        while (--retry >= 0) {
            InputStream is = null;
            try {
                URL url = new URL(srcUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.connect();
                int status = conn.getResponseCode();
                if (status == 200) {
                    is = conn.getInputStream();
                    byte[] buffer = new byte[8192];
                    int lenRead;
                    long fileLength = 0;
                    long totalDownloadedBytes = 0;
                    fileLength=conn.getContentLength();
                    while ((lenRead = is.read(buffer)) != -1) {
                        totalDownloadedBytes += lenRead;
                        if (listener != null) {
                            listener.onProgressUpdate(totalDownloadedBytes, fileLength);
                        }
                        os.write(buffer, 0, lenRead);
                    }
                    Ln.d("download file(" + srcUrl + ") success");
                    if (listener != null) {
                        listener.onComplete();
                    }

                    return true;
                } else if (retry == 0 && listener != null) {
                    listener.onError(null);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (retry == 0 && listener != null)
                    listener.onError(e);
            } finally {
                StreamUtil.closeCloseable(is);
                StreamUtil.closeCloseable(os);
            }
        }
        Ln.d("download file(" + srcUrl + ") failed");
        return false;
    }
}