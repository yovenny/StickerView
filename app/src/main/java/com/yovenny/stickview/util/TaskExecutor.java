/**
 * Summary: 利用线程池重复利用资源，异步执行任务Runnable，减小创建线程开销
 * Version 1.0
 * Company: muji.com
 * Date: 13-11-5
 * Time: 上午10:31
 * Copyright: Copyright (c) 2013
 */

package com.yovenny.stickview.util;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class TaskExecutor {
    private static ScheduledThreadPoolExecutor gScheduledThreadPoolExecutor = null;
    private static Handler gMainHandler = null;

    private static List<Runnable> mRunnables = null;
    private static Handler gThreadHandler = null;


    //不包含网络传输处理过程的线程池执行对象
    private static ExecutorService gThreadPoolExecutor = null;

    //包含网络传输处理过程的线程池执行对象
    private static ExecutorService gNetProcessThreadPoolExecutor = null;

    //执行不包含网络传输处理过程的线程
    public static void executeTask(Runnable task) {
        ensureThreadPoolExecutor();
        gThreadPoolExecutor.execute(task);
    }

    //执行包含网络传输处理过程的线程，可能存在等待阻塞的状况
    public static void executeNetTask(Runnable task) {
        ensureNetProcessThreadPoolExecutor();
        gNetProcessThreadPoolExecutor.execute(task);
    }

    public static <T> Future<T> submitTask(Callable <T> task) {
        ensureThreadPoolExecutor();
        return gThreadPoolExecutor.submit(task);
    }

    public static void scheduleTask(long delay, Runnable task) {
        ensureScheduledThreadPoolExecutor();
        gScheduledThreadPoolExecutor.schedule(task, delay, TimeUnit.MILLISECONDS);
    }

    public static void scheduleTaskAtFixedRateIgnoringTaskRunningTime(long initialDelay, long period, Runnable task) {
        ensureScheduledThreadPoolExecutor();
        gScheduledThreadPoolExecutor.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.MILLISECONDS);
    }

    public static void scheduleTaskAtFixedRateIncludingTaskRunningTime(long initialDelay, long period, Runnable task) {
        ensureScheduledThreadPoolExecutor();
        gScheduledThreadPoolExecutor.scheduleWithFixedDelay(task, initialDelay, period, TimeUnit.MILLISECONDS);
    }

    private static void ensureThreadPoolExecutor() {
        if (gThreadPoolExecutor == null) {
            gThreadPoolExecutor = new ThreadPoolExecutor(5, 10,
                    60L, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(),
                    Executors.defaultThreadFactory());

        }
    }

    private static void ensureNetProcessThreadPoolExecutor() {
        if (gNetProcessThreadPoolExecutor == null) {
            gNetProcessThreadPoolExecutor = new ThreadPoolExecutor(10, 15,
                    60L, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(),
                    Executors.defaultThreadFactory());

        }
    }

    private static void ensureScheduledThreadPoolExecutor() {
        if (gScheduledThreadPoolExecutor == null) {
            gScheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(2);
        }
    }

    public static void shutdown() {
        if (gThreadPoolExecutor != null) {
            gThreadPoolExecutor.shutdown();
            gThreadPoolExecutor = null;
        }

        if (gScheduledThreadPoolExecutor != null) {
            // DelayedTask在shutdown后默认不会被取消，需要下面显式地设置为false
            gScheduledThreadPoolExecutor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
            gScheduledThreadPoolExecutor.shutdown();
            gScheduledThreadPoolExecutor = null;
        }
    }


    public static void scheduleTaskOnUiThread(long delay, Runnable task) {
        ensureMainHandler();
        gMainHandler.postDelayed(task, delay);
    }

    public static void runTaskOnUiThread(Runnable task) {
        ensureMainHandler();
        gMainHandler.post(task);
    }

    private static void ensureMainHandler() {
        if (gMainHandler == null) {
            gMainHandler = new Handler(Looper.getMainLooper());
        }
    }

    private static void ensureThreadHandler() {
        if (gThreadHandler == null) {
            Looper.prepare();
            //子线程中是不会自动创建looper的。
            gThreadHandler = new Handler();
            Looper.loop();
        }
    }

    private static void ensureRunnables() {
        if (mRunnables == null) {
            mRunnables = new ArrayList<Runnable>();
        }
    }

    public static void postDelayedRunnable(long delay, Runnable task) {
        ensureThreadHandler();
        ensureRunnables();
        mRunnables.add(task);
        gThreadHandler.postDelayed(task, delay);
    }

    public static void clearRunnables() {
        if (null != mRunnables && mRunnables.size() > 0) {
            for (int i = 0; i < mRunnables.size(); i++) {
                gThreadHandler.removeCallbacks(mRunnables.get(i));
                mRunnables.remove(mRunnables.get(i));
                i--;
            }
        }
    }
}
