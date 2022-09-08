package com.xeasy.noticefix.utils;


import android.app.Activity;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * 简单的多线程任务
 * @author xudaz
 */
public class TaskUtils {


    /**
     * 使用lambda的方式 创建简单的多线程任务
     * @param callable 任务
     * @param <T> T
     * @return 返回FutureTask任务对象
     */
    @SuppressWarnings("AlibabaAvoidManuallyCreateThread")
    public static <T> FutureTask<T> createTask(Callable<T> callable) {
        // 创建线程任务
        FutureTask<T> task = new FutureTask<>(callable);
        // 开始任务
        new Thread(task).start();
        return task;
    }
    /**
     * 使用lambda的方式 创建简单的多线程任务 for android ui
     * @param callable 任务
     * @param <T> T
     * @return 返回FutureTask任务对象
     */
    @SuppressWarnings("AlibabaAvoidManuallyCreateThread")
    public static <T> FutureTask<T> createTask4UI(Activity context, Callable<T> callable) {
        // 创建线程任务
        FutureTask<T> task = new FutureTask<>(callable);
        context.runOnUiThread(new Thread(task));
        // 开始任务
//        new Thread(task).start();
        return task;
    }

}
