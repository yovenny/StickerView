
package com.yovenny.stickview.util;


import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import org.apache.http.HttpEntity;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class StreamUtil {
    public static void closeCloseable(Closeable obj) {
        try {
            if (obj != null)
                obj.close();
        } catch (IOException e) {
        }
    }

    public static void closeHttpEntity(HttpEntity en) {
        if (en != null) {
            try {
                en.consumeContent();
            } catch (IOException e) {
            }
        }
    }

    public static void CopyStream(InputStream is, OutputStream os) {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
                int count=is.read(bytes, 0, buffer_size);
                if(count==-1)
                    break;
                os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }

    public static ByteArrayOutputStream getByteArrayOSFromUri (Context context, Uri uri) {
        ContentResolver cr = context.getContentResolver();
        InputStream is = null;
        ByteArrayOutputStream baos = null;
        try{
            is = cr.openInputStream(uri);
            //生成复用输出流
            baos = new ByteArrayOutputStream();
            StreamUtil.CopyStream(is, baos);
        } catch (Exception e) {

        }  finally {
            StreamUtil.closeCloseable(is);
            return baos;
        }
    }

    public static ByteArrayOutputStream getByteArrayOSFromFile (File file) {
        InputStream is = null;
        ByteArrayOutputStream baos = null;
        try{
            if(!file.exists()) {
                file.createNewFile();
            }
            is = new FileInputStream(file);
            //生成复用输出流
            baos = new ByteArrayOutputStream();
            StreamUtil.CopyStream(is, baos);
        } catch (Exception e) {
            Ln.e("create file error:" + e.getMessage());
        }  finally {
            StreamUtil.closeCloseable(is);
            return baos;
        }
    }
}