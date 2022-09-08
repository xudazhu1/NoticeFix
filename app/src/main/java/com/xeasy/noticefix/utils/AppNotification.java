package com.xeasy.noticefix.utils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * Author：SkySmile
 * Date：2019/2/28
 * Description：App的通知渠道配置
 */
//@TargetApi(Build.VERSION_CODES.S)
@SuppressWarnings("unused")
public class AppNotification {
    //通知ID
    //对于通知来说ID相同即为同一条通知，如果通知ID已存在，则更新通知内容，否则发送一条新的通知
    //这里为了每次都能发送一条新的通知，对ID进行累加
    private static int id = 1;

    //影视类通知渠道
    public final static String mediaChannelId = "0x1"; //通知渠道ID
    public final static String mediaChannelName = "影视"; //通知渠道名称，显示在手机上该APP的通知管理中
    public final static int mediaChannelImportance = NotificationManager.IMPORTANCE_HIGH; //通知渠道重要性

    //美食类通知渠道
    public final static String foodChannelId = "0x2"; //通知渠道ID
    public final static String foodChannelName = "美食"; //通知渠道名称，显示在手机上该APP的通知管理中
    public final static int foodChannelImportance = NotificationManager.IMPORTANCE_DEFAULT; //通知渠道重要性

    /**
     * 创建通知渠道
     *
     * @param applicationContext  上下文
     * @param channelId           渠道ID
     * @param channelIdName       渠道名称，显示在手机上该APP的通知管理中
     * @param channelIdImportance 渠道重要程度
     */
    public static void createNotificationChannel(Context applicationContext, String channelId,
                                                 String channelIdName, int channelIdImportance) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            NotificationManager notificationManager = (NotificationManager) applicationContext.getSystemService(
                    Context.NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelIdName,
                    channelIdImportance);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    /**
     * 发送通知，根据需要进行扩展
     *
     * @param context   上下文
     * @param channelId 渠道ID（必须对应已创建的渠道ID）
     * @param title     通知标题
     * @param text      通知内容
     * @param smallIcon 通知小图标（显示在状态栏中的）,必须设置
     * @param largeIcon 通知大图标（下拉状态栏可见，显示在通知栏中），
     *                  注意：这里的图片ID不能是mipmap文件夹下的，因为BitmapFactory.decodeResource方法只能
     *                  获取到 drawable, sound, and raw resources;
     * @param pi        点击通知打开的页面
     */
    public static void sendNotification(Context context, String channelId, String title,
                                        String text, int smallIcon, int largeIcon, PendingIntent pi) {
        //判断通知是否开启
        if (!isNotificationEnabled(context)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("提示");
            builder.setMessage("是否开启通知？");
            builder.setPositiveButton("确定", (dialogInterface, i) -> openNotification(context));
            builder.setNegativeButton("取消", null);
            builder.show();
        }
        //判断某个渠道的通知是否开启
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!isNotificationChannelEnabled(context, channelId)) {
                String message = "";
                if ( channelId.equals(mediaChannelId)) {
                    message = mediaChannelName;
                }
                if ( channelId.equals(foodChannelId)) {
                    message = foodChannelName;
                }
                // 创建通知渠道
                createNotificationChannel(context, channelId, message, NotificationManager.IMPORTANCE_DEFAULT );

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("提示");
                builder.setMessage("是否开启"+message+"通知？");
                builder.setPositiveButton("确定", (dialogInterface, i) -> openNotificationChannel(context, channelId));
                builder.setNegativeButton("取消", null);
                builder.show();
            }
        }
        //发送通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);
                builder.setContentTitle(title);
                builder.setContentText(text);
                builder.setWhen(System.currentTimeMillis());
                builder.setSmallIcon(smallIcon);
                builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), largeIcon));
                builder.setContentIntent(pi);
                builder.setAutoCancel(true);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);
        Notification notification = builder.build();

        notificationManager.notify(id++, notification);
    }

    /**
     * 判断App通知是否开启
     * 注意这个方法判断的是通知总开关，如果APP通知被关闭，则其下面的所有通知渠道也被关闭
     */
    public static Boolean isNotificationEnabled(Context context) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat
                .from(context);
        return notificationManagerCompat.areNotificationsEnabled();
    }


    /**
     * 判断APP某个通知渠道的通知是否开启
     */
    @RequiresApi(Build.VERSION_CODES.S)
    public static Boolean isNotificationChannelEnabled(Context context, String channelId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = notificationManager.getNotificationChannel(channelId);
        return null != channel && channel.getImportance() != NotificationManager.IMPORTANCE_NONE;
    }

    /**
     * 打开通知设置页面
     */
    @SuppressLint("ObsoleteSdkInt")
    public static void openNotification(Context context) {
        String packageName = context.getPackageName();
        int uid = context.getApplicationInfo().uid;
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName);
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, uid);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", packageName);
            intent.putExtra("app_uid", uid);
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:$packageName"));
        } else {
            intent.setAction(Settings.ACTION_SETTINGS);
        }
        context.startActivity(intent);
    }


    /**
     * 打开通知渠道设置页面
     */
    @RequiresApi(Build.VERSION_CODES.S)
    public static void openNotificationChannel(Context context, String channelId) {
        Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
        intent.putExtra(Settings.EXTRA_CHANNEL_ID, channelId);
        context.startActivity(intent);
    }
}