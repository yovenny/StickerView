package com.yovenny.stickview;

import android.app.Application;
import android.content.Context;

import com.yovenny.stickview.util.AssetsUtil;
import com.yovenny.stickview.util.FileUtil;
import com.yovenny.stickview.util.Ln;
import com.yovenny.stickview.util.PreferenceHelper;
import com.yovenny.stickview.util.TaskExecutor;

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
        boolean isRequestEver = PreferenceHelper.readBoolean(context, STICK_PREF, "isEverinitWater", false);
        if (!isRequestEver) {
            PreferenceHelper.writeBoolean(context, STICK_PREF, "isEverinitWater", true);
            TaskExecutor.executeTask(new Runnable() {
                @Override
                public void run() {
                    String arg_assetDir = Constant.WATER_SAVE_FILE;
                    String arg_destinationDir = FileUtil.STORE_PATH + Constant.WATER_SAVE_FILE;
                    try {
                        //不删除水印文件夹，避免用户再次下载
//                        File waterFile=new File(arg_destinationDir);
//                        if(waterFile.exists()){
//                            FileUtil.deleteDirectory(waterFile.getAbsolutePath());
//                        }
                        AssetsUtil.copyDirorfileFromAssetManager(arg_assetDir, arg_destinationDir, context);
                    } catch (Exception e) {
                        Ln.e("copy file fail !!!");
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
