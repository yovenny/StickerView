package com.yovenny.stickview.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.yovenny.stickview.ui.WaterActivity;

/**
 * 页面统一跳转帮助类
 */
public class UIHelper {
    /**
     * 显示指定Clazz.Activity
     *
     * @param context
     */
    public static void showActivity(Context context, Class<?> clazz) {
        Intent intent = new Intent(context, clazz);
        context.startActivity(intent);
    }

    /**
     * 显示指定Clazz.Activity For result
     */
    public static void showActivityForResult(Activity activity, Class<?> clazz, int requestCode, Bundle bundle) {
        Intent intent = new Intent(activity, clazz);
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        activity.startActivityForResult(intent, requestCode);
    }


    /**
     * 显示指定Clazz.Activity For result
     */
    public static void showActivityForResult(Activity activity, Class<?> clazz, int requestCode) {
        Intent intent = new Intent(activity, clazz);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void showWaterActivity(Context context, String path) {
        Intent intent = new Intent(context, WaterActivity.class);
        intent.putExtra("process_path", path);
        context.startActivity(intent);
    }
}


