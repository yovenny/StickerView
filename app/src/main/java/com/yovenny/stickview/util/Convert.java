
package com.yovenny.stickview.util;
import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Convert {
    private static float mDensity = -1;

    /**
     * convert px from dp
     */
    public static int dip2px(Context context, float dpValue) {
        float scale = getScreenDensity(context);
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * convert dp from px
     */
    public static int px2dip(Context context, float pxValue) {
        float scale = getScreenDensity(context);
        return (int) (pxValue /scale+ 0.5f);
    }

    /**
     * get screen density
     *
     * @param context
     * @return
     */
    public static float getScreenDensity(Context context) {
        if (mDensity == -1) {
            mDensity = context.getResources().getDisplayMetrics().density;
        }

        return mDensity;
    }

    public static String getCurrTimeStamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public static int getDeviceDpi(Context context) {
        return (int) (getScreenDensity(context) * 160f);
    }



}