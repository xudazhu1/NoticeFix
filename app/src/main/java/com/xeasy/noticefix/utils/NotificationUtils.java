package com.xeasy.noticefix.utils;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.xeasy.noticefix.R;

/**
 * 单例通知类
 * 适配Android O
 */
public class NotificationUtils {

    private Context context;
    private NotificationManager notificationManager;
    private static int id = 0;

    private static class NotificationUtilsHolder {
        public static final NotificationUtils notificationUtils = new NotificationUtils();
    }

    private NotificationUtils(){
    }

    public static NotificationUtils getInstance() {
        return NotificationUtilsHolder.notificationUtils;
    }

    /**
     * 初始化
     * @param context 引用全局上下文
     */
    public void init(Context context) {
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
    }

    /**
     * 创建通知通道
     * @param channelId 通道id
     * @param channelName 通道名称
     * @param importance 通道级别
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createNotificationChannel(String channelId, String channelName, int importance) {

        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        notificationManager.createNotificationChannel(channel);
    }

    /**
     * 发送通知
     * @param channelId 通道id
     * @param title 标题
     * @param content 内容
     * @param intent 意图
     */
    public void sendNotification(String channelId, String title, String content, Intent intent) {

        PendingIntent pendingIntent = null;
        if (intent != null) {
            pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        }

        Notification notification = new NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();

        notificationManager.notify(id++, notification);
    }


    /**
     * 判断通知是否开启（非单个消息渠道）
     * @param context 上下文
     * @return true 开启
     * API19 以上可用
     */
    public static boolean checkNotificationsEnabled(Context context) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        boolean areNotificationsEnabled = notificationManagerCompat.areNotificationsEnabled();
        return areNotificationsEnabled;
    }

    /**
     * 判断通知渠道是否开启（单个消息渠道）
     * @param context 上下文
     * @param channelID 渠道 id
     * @return true 开启
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static boolean checkNotificationsChannelEnabled(Context context, String channelID) {
        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (manager == null) {
            return false;
        }
        NotificationChannel channel = manager.getNotificationChannel(channelID);
        return !(channel.getImportance() == NotificationManager.IMPORTANCE_NONE);
    }
}