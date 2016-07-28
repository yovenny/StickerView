package com.yovenny.stickview;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.yovenny.stickview.util.AssetsUtil;
import com.yovenny.stickview.util.PreferenceHelper;

/**
 * Desc：
 * User: fenzaichui (1312397605@qq.com)
 * Date:2015/10/12.
 * Copyright: yovenny.com
 */
public class StickApp extends Application{
    private static final Object gLock = new Object();
    private static StickApp gApp;

    public static final String STICK_PREF="STICK_PREF";
    public static float sScale;
    public static int sWidthDp;
    public static int sWidthPix;
    public static int sHeightPix;

    @Override
    public void onCreate() {
        super.onCreate();
        synchronized (gLock) {
            gApp = this;
            initConstant();
            initCopyWaterToSD(this);
        }
    }

    private void initConstant() {
        sScale = getResources().getDisplayMetrics().density;
        sWidthPix = getResources().getDisplayMetrics().widthPixels;
        sHeightPix = getResources().getDisplayMetrics().heightPixels;
        sWidthDp = (int) (sWidthPix / sScale);
    }

    public static synchronized StickApp ins() {
        synchronized (gLock) {
            return gApp;
        }
    }

    //初始化水印
    private void initCopyWaterToSD(final Context context) {
        boolean isRequestEver = PreferenceHelper.readBoolean(context, STICK_PREF, "isEverInitWater", false);
        if (!isRequestEver) {
            PreferenceHelper.writeBoolean(context, STICK_PREF, "isEverInitWater", true);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String arg_assetDir = "water";
                    String arg_destinationDir = Environment.getExternalStorageDirectory()+"/StickView/water";
                    try {
                        AssetsUtil.copyDirectoryFromAsset(arg_assetDir, arg_destinationDir, context);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}
