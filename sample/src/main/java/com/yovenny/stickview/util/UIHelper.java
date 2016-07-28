package com.yovenny.stickview.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.yovenny.stickview.ui.ResultActivity;
import com.yovenny.stickview.ui.WaterActivity;

import java.util.ArrayList;

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

    /**
     * 跳转发话题界面
     */
    public static void showResultActivity(Context context, String savePath,ArrayList categoryIds,ArrayList watermarkIds,ArrayList contents) {
            Intent intent = new Intent(context, ResultActivity.class);
            intent.putExtra(WaterActivity.ADD_TOPIC_PIC, savePath);
            intent.putExtra("watermarkCategoryIds",categoryIds);
            intent.putExtra("watermarkIds",watermarkIds);
            intent.putExtra("contents",contents);
            launchIntent(context, intent, false);
    }

    public static void launchIntent(Context context, Intent intent, boolean isNewTask) {
        if (isNewTask) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        context.startActivity(intent);
    }

}


