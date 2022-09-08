package com.xeasy.noticefix.hook;

import static com.xeasy.noticefix.hook.SystemUIHooker.fixNotificationIcon;

import android.app.AndroidAppHelper;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.xeasy.noticefix.utils.ImageTools;
import com.xeasy.noticefix.utils.ReflexUtil;

import java.util.Arrays;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MyHookTest implements IXposedHookLoadPackage {

    static final String NOTICE_CLASS = "android.app.NotificationManager";
    static final String LOG_PREV = "NoticeFix---";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        XposedBridge.log(LOG_PREV + "NotificationManager.clazz:  " + loadPackageParam.packageName);
        hookNotice(loadPackageParam.classLoader);
        XposedHelpers.findAndHookMethod("android.app.Instrumentation", loadPackageParam.classLoader,
                "newActivity", ClassLoader.class, String.class, Intent.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        hookNotice(param.thisObject.getClass().getClassLoader());
//                        hookByConstructor(loadPackageParam.classLoader);
                    }
                });

    }

    private void hookByConstructor(ClassLoader classLoader) {
        XposedBridge.hookAllConstructors(
                NotificationManager.class,
                new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                XposedBridge.log(LOG_PREV + "hookByConstructor.对象:  " + param.thisObject);
                hookNotice(classLoader);
            }
        });
    }

    // com.android.systemui.statusbar.notification.NotificationEntryManager$1
    // 测试在 onAsyncInflationFinished 后再修改图标 看看对状态栏的影响效果 todo 继续测试
    private void onAsyncInflationFinished(ClassLoader classLoader) {
        final Class<?> clazz = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.NotificationEntryManager$1", classLoader);
        final Class<?> args0 = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.collection.NotificationEntry", classLoader);
        final Class<?> expandableNotificationRowClass = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.row.ExpandableNotificationRow", classLoader);
        final Class<?> arge2 = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.row.NotificationRowContentBinder.InflationCallback", classLoader);
        // XposedBridge.log(LOG_PREV + "尝试Hook.isGrayscale:  ");
        XposedHelpers.findAndHookMethod(clazz, "onAsyncInflationFinished"
                , args0
                , new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log(LOG_PREV + "hook NotificationEntryManager$1 onAsyncInflationFinished 开始:  ");
                        Object notificationEntry = param.args[0];
                        StatusBarNotification statusBarNotification = (StatusBarNotification) ReflexUtil.getField4Obj(notificationEntry, "mSbn");
                        Object mIcons = ReflexUtil.getField4Obj(notificationEntry, "mIcons");

                        ImageView mStatusBarIcon = (ImageView) ReflexUtil.getField4Obj(mIcons, "mStatusBarIcon");
                        if (mStatusBarIcon != null) {
                            if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(mStatusBarIcon.getDrawable()))) {
                                XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mStatusBarIcon");
                            } else {
                                XposedBridge.log(LOG_PREV + "不是灰度 mStatusBarIcon:  ");
                            }
                        }

                        ImageView mShelfIcon = (ImageView) ReflexUtil.getField4Obj(mIcons, "mShelfIcon");
                        if (mShelfIcon != null) {
                            if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(mShelfIcon.getDrawable()))) {
                                XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mShelfIcon");
                            } else {
                                XposedBridge.log(LOG_PREV + "不是灰度 mShelfIcon:  ");
                            }
                        }
                        ImageView mAodIcon = (ImageView) ReflexUtil.getField4Obj(mIcons, "mAodIcon");
                        if (mAodIcon != null) {
                            if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(mAodIcon.getDrawable()))) {
                                XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mAodIcon");
                            } else {
                                XposedBridge.log(LOG_PREV + "不是灰度 mAodIcon:  ");
                            }
                        }
                        ImageView mCenteredIcon = (ImageView) ReflexUtil.getField4Obj(mIcons, "mCenteredIcon");
                        if (mCenteredIcon != null) {
                            if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(mCenteredIcon.getDrawable()))) {
                                XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mCenteredIcon");
                            } else {
                                XposedBridge.log(LOG_PREV + "不是灰度 mCenteredIcon:  ");
                            }
                        }

                        Application application = AndroidAppHelper.currentApplication();
                        Context context = application.createPackageContext(statusBarNotification.getPackageName(), Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);

                        Object mSmallIconDescriptor = ReflexUtil.getField4Obj(mIcons, "mSmallIconDescriptor");
                        if (mSmallIconDescriptor != null) {
                            Icon mSmallIconDescriptorIcon = (Icon) ReflexUtil.getField4Obj(mSmallIconDescriptor, "icon");
                            if (mSmallIconDescriptorIcon != null) {
                                Drawable drawable = mSmallIconDescriptorIcon.loadDrawable(context);
                                if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(drawable))) {
                                    XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mSmallIconDescriptor");
                                } else {
                                    XposedBridge.log(LOG_PREV + "不是灰度 mSmallIconDescriptor:  ");
                                }
                            }
                        }

                        Object mPeopleAvatarDescriptor = ReflexUtil.getField4Obj(mIcons, "mPeopleAvatarDescriptor");
                        if (mPeopleAvatarDescriptor != null) {
                            Icon mPeopleAvatarDescriptorIcon = (Icon) ReflexUtil.getField4Obj(mPeopleAvatarDescriptor, "icon");
                            if (mPeopleAvatarDescriptorIcon != null) {
                                Drawable mPeopleDrawable = mPeopleAvatarDescriptorIcon.loadDrawable(context);
                                if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(mPeopleDrawable))) {
                                    XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mPeopleAvatarDescriptor");
                                } else {
                                    XposedBridge.log(LOG_PREV + "不是灰度 mPeopleAvatarDescriptor  ");
                                }
                            }
                        }

                        XposedBridge.log(LOG_PREV + "NotificationEntryManager$1 onAsyncInflationFinished 前 end");
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log(LOG_PREV + "hook NotificationEntryManager$1 onAsyncInflationFinished 开始:  ");
                        Object notificationEntry = param.args[0];
                        StatusBarNotification statusBarNotification = (StatusBarNotification) ReflexUtil.getField4Obj(notificationEntry, "mSbn");
                        Object mIcons = ReflexUtil.getField4Obj(notificationEntry, "mIcons");

                        ImageView mStatusBarIcon = (ImageView) ReflexUtil.getField4Obj(mIcons, "mStatusBarIcon");
                        if (mStatusBarIcon != null) {
                            if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(mStatusBarIcon.getDrawable()))) {
                                XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mStatusBarIcon");
                            } else {
                                XposedBridge.log(LOG_PREV + "不是灰度 mStatusBarIcon:  ");
                            }
                        }

                        ImageView mShelfIcon = (ImageView) ReflexUtil.getField4Obj(mIcons, "mShelfIcon");
                        if (mShelfIcon != null) {
                            if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(mShelfIcon.getDrawable()))) {
                                XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mShelfIcon");
                            } else {
                                XposedBridge.log(LOG_PREV + "不是灰度 mShelfIcon:  ");
                            }
                        }
                        ImageView mAodIcon = (ImageView) ReflexUtil.getField4Obj(mIcons, "mAodIcon");
                        if (mAodIcon != null) {
                            if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(mAodIcon.getDrawable()))) {
                                XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mAodIcon");
                            } else {
                                XposedBridge.log(LOG_PREV + "不是灰度 mAodIcon:  ");
                            }
                        }
                        ImageView mCenteredIcon = (ImageView) ReflexUtil.getField4Obj(mIcons, "mCenteredIcon");
                        if (mCenteredIcon != null) {
                            if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(mCenteredIcon.getDrawable()))) {
                                XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mCenteredIcon");
                            } else {
                                XposedBridge.log(LOG_PREV + "不是灰度 mCenteredIcon:  ");
                            }
                        }

                        Application application = AndroidAppHelper.currentApplication();
                        Context context = application.createPackageContext(statusBarNotification.getPackageName(), Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);

                        Object mSmallIconDescriptor = ReflexUtil.getField4Obj(mIcons, "mSmallIconDescriptor");
                        if (mSmallIconDescriptor != null) {
                            Icon mSmallIconDescriptorIcon = (Icon) ReflexUtil.getField4Obj(mSmallIconDescriptor, "icon");
                            if (mSmallIconDescriptorIcon != null) {
                                Drawable drawable = mSmallIconDescriptorIcon.loadDrawable(context);
                                if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(drawable))) {
                                    XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mSmallIconDescriptor");
                                } else {
                                    XposedBridge.log(LOG_PREV + "不是灰度 mSmallIconDescriptor:  ");
                                }
                            }
                        }

                        Object mPeopleAvatarDescriptor = ReflexUtil.getField4Obj(mIcons, "mPeopleAvatarDescriptor");
                        if (mPeopleAvatarDescriptor != null) {
                            Icon mPeopleAvatarDescriptorIcon = (Icon) ReflexUtil.getField4Obj(mPeopleAvatarDescriptor, "icon");
                            if (mPeopleAvatarDescriptorIcon != null) {
                                Drawable mPeopleDrawable = mPeopleAvatarDescriptorIcon.loadDrawable(context);
                                if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(mPeopleDrawable))) {
                                    XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mPeopleAvatarDescriptor");
                                } else {
                                    XposedBridge.log(LOG_PREV + "不是灰度 mPeopleAvatarDescriptor  ");
                                }
                            }
                        }

                        XposedBridge.log(LOG_PREV + "NotificationEntryManager$1 onAsyncInflationFinished 前 end");

                    }
                });
    }

    // com.android.systemui.statusbar.notification.row.RowInflaterTask#inflate
    // 测试在 onAsyncInflationFinished 后再修改图标 看看对状态栏的影响效果 todo 继续测试
    private void inflate(ClassLoader classLoader) {
        final Class<?> clazz = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.row.RowInflaterTask", classLoader);
        final Class<?> args0 = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.collection.NotificationEntry", classLoader);
        final Class<?> args1 = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.row.RowInflaterTask.RowInflationFinishedListener", classLoader);
        final Class<?> arge2 = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.row.NotificationRowContentBinder.InflationCallback", classLoader);
        // XposedBridge.log(LOG_PREV + "尝试Hook.isGrayscale:  ");
        XposedHelpers.findAndHookMethod(clazz, "inflate",
                Context.class, ViewGroup.class, args0, args1
                , new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {


                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log(LOG_PREV + "hook RowInflaterTask#inflate 后 开始:  ");
                        Object notificationEntry = param.args[2];

//                        ReflexUtil.setField4Obj("targetSdk", notificationEntry, 20);
//                        XposedBridge.log(LOG_PREV + "NotificationRowBinderImpl======targetSdk:  "
//                                + ReflexUtil.getField4Obj(notificationEntry, "targetSdk"));

                        StatusBarNotification statusBarNotification = (StatusBarNotification) ReflexUtil.getField4Obj(notificationEntry, "mSbn");
                        // 获取this
//                        Context mContext = (Context) ReflexUtil.getField4Obj(param.thisObject, "mContext");
                        Context mContext = AndroidAppHelper.currentApplication();

                        XposedBridge.log(LOG_PREV + "NotificationListenerService======mContext:  " + mContext);

                        Context context = mContext.createPackageContext(statusBarNotification.getPackageName(), Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);

                        fixNotificationIcon(statusBarNotification, context);


//                        StatusBarNotification statusBarNotification = (StatusBarNotification) ReflexUtil.getField4Obj(notificationEntry, "mSbn");
                        Object mIcons = ReflexUtil.getField4Obj(notificationEntry, "mIcons");

                        ImageView mStatusBarIcon = (ImageView) ReflexUtil.getField4Obj(mIcons, "mStatusBarIcon");
                        if (mStatusBarIcon != null) {
                            if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(mStatusBarIcon.getDrawable()))) {
                                XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mStatusBarIcon");
                            } else {
                                XposedBridge.log(LOG_PREV + "不是灰度 mStatusBarIcon:  ");
                            }
                        }

                        ImageView mShelfIcon = (ImageView) ReflexUtil.getField4Obj(mIcons, "mShelfIcon");
                        if (mShelfIcon != null) {
                            if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(mShelfIcon.getDrawable()))) {
                                XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mShelfIcon");
                            } else {
                                XposedBridge.log(LOG_PREV + "不是灰度 mShelfIcon:  ");
                            }
                        }
                        ImageView mAodIcon = (ImageView) ReflexUtil.getField4Obj(mIcons, "mAodIcon");
                        if (mAodIcon != null) {
                            if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(mAodIcon.getDrawable()))) {
                                XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mAodIcon");
                            } else {
                                XposedBridge.log(LOG_PREV + "不是灰度 mAodIcon:  ");
                            }
                        }
                        ImageView mCenteredIcon = (ImageView) ReflexUtil.getField4Obj(mIcons, "mCenteredIcon");
                        if (mCenteredIcon != null) {
                            if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(mCenteredIcon.getDrawable()))) {
                                XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mCenteredIcon");
                            } else {
                                XposedBridge.log(LOG_PREV + "不是灰度 mCenteredIcon:  ");
                            }
                        }

                        Application application = AndroidAppHelper.currentApplication();
//                        Context context = application.createPackageContext(statusBarNotification.getPackageName(), Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);

                        Object mSmallIconDescriptor = ReflexUtil.getField4Obj(mIcons, "mSmallIconDescriptor");
                        if (mSmallIconDescriptor != null) {
                            Icon mSmallIconDescriptorIcon = (Icon) ReflexUtil.getField4Obj(mSmallIconDescriptor, "icon");
                            if (mSmallIconDescriptorIcon != null) {
                                Drawable drawable = mSmallIconDescriptorIcon.loadDrawable(context);
                                if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(drawable))) {
                                    XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mSmallIconDescriptor");
                                } else {
                                    XposedBridge.log(LOG_PREV + "不是灰度 mSmallIconDescriptor:  ");
                                }
                            }
                        }

                        Object mPeopleAvatarDescriptor = ReflexUtil.getField4Obj(mIcons, "mPeopleAvatarDescriptor");
                        if (mPeopleAvatarDescriptor != null) {
                            Icon mPeopleAvatarDescriptorIcon = (Icon) ReflexUtil.getField4Obj(mPeopleAvatarDescriptor, "icon");
                            if (mPeopleAvatarDescriptorIcon != null) {
                                Drawable mPeopleDrawable = mPeopleAvatarDescriptorIcon.loadDrawable(context);
                                if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(mPeopleDrawable))) {
                                    XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mPeopleAvatarDescriptor");
                                } else {
                                    XposedBridge.log(LOG_PREV + "不是灰度 mPeopleAvatarDescriptor  ");
                                }
                            }
                        }

                        XposedBridge.log(LOG_PREV + " RowInflaterTask#inflate 后 end");

                    }
                });
    }

    // todo 替换后通知没了
    // com.android.systemui.statusbar.notification.row.ExpandableNotificationRow#updateShelfIconColor
    private void updateShelfIconColorReplace(ClassLoader classLoader) {
        final Class<?> clazz = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.row.ExpandableNotificationRow", classLoader);
        final Class<?> args0 = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.collection.NotificationEntry", classLoader);
        final Class<?> args1 = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.row.ExpandableNotificationRow", classLoader);
        final Class<?> arge2 = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.row.NotificationRowContentBinder.InflationCallback", classLoader);
        // XposedBridge.log(LOG_PREV + "尝试Hook.isGrayscale:  ");
        XposedHelpers.findAndHookMethod(clazz, "updateShelfIconColor"
                , new XC_MethodReplacement() {

                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        return null;
                    }
                });
        XposedBridge.log(LOG_PREV + "updateShelfIconColorReplace:  Hook完成!!!");
    }

    // com.android.systemui.statusbar.notification.row.NotifBindPipeline#getBindEntry
    private void requestPipelineRun(ClassLoader classLoader) {
        XposedBridge.log(LOG_PREV + "尝试Hook.requestRebind:  ");
        final Class<?> clazz = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.row.NotifBindPipeline", classLoader);
        final Class<?> args0 = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.collection.NotificationEntry", classLoader);
        final Class<?> args1 = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.row.NotifBindPipeline.BindCallback", classLoader);
        final Class<?> arge2 = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.row.NotificationRowContentBinder.InflationCallback", classLoader);

        XposedHelpers.findAndHookMethod(clazz, "requestPipelineRun"
                , args0
                , new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                        XposedBridge.log(LOG_PREV + "getBindEntry 内部方法 == " + param.thisObject);

                        Object notificationEntry = param.args[0];
//                        ReflexUtil.setField4Obj("targetSdk", notificationEntry, 20);
//                        XposedBridge.log(LOG_PREV + "NotificationRowBinderImpl======targetSdk:  "
//                                + ReflexUtil.getField4Obj(notificationEntry, "targetSdk"));

                        StatusBarNotification statusBarNotification = (StatusBarNotification) ReflexUtil.getField4Obj(notificationEntry, "mSbn");
                        // 获取this
//                        Context mContext = (Context) ReflexUtil.getField4Obj(param.thisObject, "mContext");
                        Context mContext = AndroidAppHelper.currentApplication();

                        XposedBridge.log(LOG_PREV + "NotificationListenerService======mContext:  " + mContext);

                        Context context = mContext.createPackageContext(statusBarNotification.getPackageName(), Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);

                        fixNotificationIcon(statusBarNotification, context);

                        XposedBridge.log(LOG_PREV + "fixNotificationIcon == " + context);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {


                    }
                });
        XposedBridge.log(LOG_PREV + "ExpandableNotificationRow.setLegacy:  Hook完成!!!");
    }


    // com.android.systemui.statusbar.notification.row.RowContentBindStage 这是实现类
    // com.android.systemui.statusbar.notification.row.BindRequester 有效
    private void requestRebind(ClassLoader classLoader) {
        XposedBridge.log(LOG_PREV + "尝试Hook.requestRebind:  ");
        final Class<?> clazz = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.row.BindRequester", classLoader);
        final Class<?> args0 = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.collection.NotificationEntry", classLoader);
        final Class<?> args1 = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.row.NotifBindPipeline.BindCallback", classLoader);
        final Class<?> arge2 = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.row.NotificationRowContentBinder.InflationCallback", classLoader);

        XposedHelpers.findAndHookMethod(clazz, "requestRebind"
                , args0, args1
                , new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {


                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log(LOG_PREV + "onBindFinished lambda 内部方法 == " + param.thisObject);

                        Object notificationEntry = param.args[0];
//                        ReflexUtil.setField4Obj("targetSdk", notificationEntry, 20);
//                        XposedBridge.log(LOG_PREV + "NotificationRowBinderImpl======targetSdk:  "
//                                + ReflexUtil.getField4Obj(notificationEntry, "targetSdk"));

                        StatusBarNotification statusBarNotification = (StatusBarNotification) ReflexUtil.getField4Obj(notificationEntry, "mSbn");
                        // 获取this
//                        Context mContext = (Context) ReflexUtil.getField4Obj(param.thisObject, "mContext");
                        Context mContext = AndroidAppHelper.currentApplication();

                        XposedBridge.log(LOG_PREV + "NotificationListenerService======mContext:  " + mContext);

                        Context context = mContext.createPackageContext(statusBarNotification.getPackageName(), Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);

                        fixNotificationIcon(statusBarNotification, context);

                        XposedBridge.log(LOG_PREV + "fixNotificationIcon == " + context);

                    }
                });
        XposedBridge.log(LOG_PREV + "ExpandableNotificationRow.setLegacy:  Hook完成!!!");
    }


    // com.android.systemui.statusbar.notification.collection.inflation.NotificationRowBinderImpl#inflateContentViews
    private void inflateContentViewsSuccess(ClassLoader classLoader) {
        final Class<?> clazz = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.collection.inflation.NotificationRowBinderImpl", classLoader);
        final Class<?> args0 = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.collection.NotificationEntry", classLoader);
        final Class<?> args1 = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.row.ExpandableNotificationRow", classLoader);
        final Class<?> arge2 = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.row.NotificationRowContentBinder.InflationCallback", classLoader);
        // XposedBridge.log(LOG_PREV + "尝试Hook.isGrayscale:  ");
        XposedHelpers.findAndHookMethod(clazz, "inflateContentViews"
                , args0, args1, arge2
                , new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Object notificationEntry = param.args[0];
//                        ReflexUtil.setField4Obj("targetSdk", notificationEntry, 20);
//                        XposedBridge.log(LOG_PREV + "NotificationRowBinderImpl======targetSdk:  "
//                                + ReflexUtil.getField4Obj(notificationEntry, "targetSdk"));

                        StatusBarNotification statusBarNotification = (StatusBarNotification) ReflexUtil.getField4Obj(notificationEntry, "mSbn");
                        // 获取this
                        Context mContext = (Context) ReflexUtil.getField4Obj(param.thisObject, "mContext");

//                        XposedBridge.log(LOG_PREV + "NotificationListenerService======mContext:  " + mContext);

                        Context context = mContext.createPackageContext(statusBarNotification.getPackageName(), Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);

                        fixNotificationIcon(statusBarNotification, context);

//                        XposedBridge.log(LOG_PREV + "setLegacy callback回调 == " + param.args[2]);
                        Object mRowContentBindStage = ReflexUtil.getField4Obj(param.thisObject, "mRowContentBindStage");

//                        XposedBridge.log(LOG_PREV + "这个 mRowContentBindStage == " + mRowContentBindStage);

                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {


                    }
                });
//        XposedBridge.log(LOG_PREV + "ExpandableNotificationRow.setLegacy:  Hook完成!!!");
    }

    // com.android.systemui.statusbar.notification.row.NotificationContentInflater$5.onViewApplied
    private void onViewApplied(ClassLoader classLoader) {
        final Class<?> clazz = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.row.NotificationContentInflater$5", classLoader);
        final Class<?> args0 = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.collection.NotificationEntry", classLoader);
        final Class<?> args1 = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.row.ExpandableNotificationRow", classLoader);
        final Class<?> arge2 = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.row.NotificationRowContentBinder.InflationCallback", classLoader);
        // XposedBridge.log(LOG_PREV + "尝试Hook.isGrayscale:  ");
        XposedHelpers.findAndHookMethod(clazz, "onViewApplied"
                , View.class
                , new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                        Object row =  param.args[0];
                        Object thisObject = param.thisObject;
                        Object mEntry = ReflexUtil.getField4Obj(thisObject, "val$entry");

//                        Object mEntry = param.args[0];
                        StatusBarNotification sbn = (StatusBarNotification) ReflexUtil.getField4Obj(mEntry, "mSbn");
//                        StatusBarNotification sbn = (StatusBarNotification) param.args[0];
                        Context context = AndroidAppHelper.currentApplication().createPackageContext(sbn.getPackageName(),
                                Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);

                        Icon smallIcon = sbn.getNotification().getSmallIcon();
                        Bitmap bitmap = ImageTools.toBitmap(smallIcon.loadDrawable(context));
                        XposedBridge.log(LOG_PREV + " 执行完成后 mIcon isGrayscaleIcon!!! === " +
                                ImageTools.isGrayscaleIcon(bitmap)
                        );

                        fixNotificationIcon(sbn, context);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {




                        /*Object thisObject = param.thisObject;
                        final Class<?> ImageTransformState = XposedHelpers.findClass(
                                "com.android.systemui.statusbar.notification.ImageTransformState", classLoader);
                        Field icon_tag = ImageTransformState.getDeclaredField("ICON_TAG");
                        icon_tag.setAccessible(true);
                        int ICON_TAG = (int) icon_tag.get(null);
//                        int image_icon_tag = 0x7f0b02b5;

                        ImageView mIcon = (ImageView) ReflexUtil.getField4Obj(thisObject, "mIcon");
                        if ( mIcon != null ) {
                            Icon tag = (Icon) mIcon.getTag(ICON_TAG);
                            XposedBridge.log(LOG_PREV + " 执行完成后 mIcon base64!!! === " +
                                    ImageTools.bitmapToBase64(ImageTools.toBitmap(tag.loadDrawable(context)))
                            );
                        }*/

                    }
                });
//        XposedBridge.log(LOG_PREV + "ExpandableNotificationRow.setLegacy:  Hook完成!!!");
    }

    // com.android.systemui.statusbar.notification.row.ExpandableNotificationRow#setLegacy
    // isGrayscale  --- ImageView  ContrastColorUtil
    private void hookSetLegacy(ClassLoader classLoader) {
        final Class<?> clazz = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.row.ExpandableNotificationRow", classLoader);
        // XposedBridge.log(LOG_PREV + "尝试Hook.isGrayscale:  ");
        XposedHelpers.findAndHookMethod(clazz, "setLegacy"
                , boolean.class
                , new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log(LOG_PREV + "setLegacy 参数是 == " + param.args[0]);
                        super.beforeHookedMethod(param);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                    }
                });
        XposedBridge.log(LOG_PREV + "ExpandableNotificationRow.setLegacy:  Hook完成!!!");
    }

    // com.android.systemui.statusbar.notification.NotificationUtils
    // isGrayscale  --- ImageView  ContrastColorUtil
    private void hookIsGrayscale(ClassLoader classLoader) {
        final Class<?> clazz = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.NotificationUtils", classLoader);
        final Class<?> contrastColorUtilClass = XposedHelpers.findClass(
                "com.android.internal.util.ContrastColorUtil", classLoader);
        // XposedBridge.log(LOG_PREV + "尝试Hook.isGrayscale:  ");
        XposedHelpers.findAndHookMethod(clazz, "isGrayscale"
                , ImageView.class, contrastColorUtilClass
                , new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);

                        ImageView imageView = (ImageView) param.args[0];
//                        imageView.setTag(R.id.icon_is_grayscale, );
                        Context context = AndroidAppHelper.currentApplication();
//                        int icon_is_grayscale = 0x7f0b02ac;
                        int icon_is_grayscale = context.getResources().getIdentifier("icon_is_grayscale", "id", "com.android.systemui");
                        imageView.setTag(icon_is_grayscale, false);
                        Bitmap bitmap = ImageTools.toBitmap(imageView.getDrawable());


                        // XposedBridge.log(LOG_PREV + "尝试Hook.isGrayscale:  drawable" + drawable);
                        param.setResult(false);
                    }
                });
        XposedBridge.log(LOG_PREV + "isGrayscale:  Hook完成!!!");
    }

    // com.android.systemui.statusbar.notification.NotificationEntryManager
    // addNotificationInternal   mContext
    private void hookNotificationAddNotificationInternal(ClassLoader classLoader) {
        final Class<?> clazz = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.NotificationEntryManager", classLoader);
        // XposedBridge.log(LOG_PREV + "尝试Hook.AddNotificationInternal:  ");
        XposedHelpers.findAndHookMethod(clazz, "addNotificationInternal"
                , StatusBarNotification.class, NotificationListenerService.RankingMap.class
                , new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        // XposedBridge.log(LOG_PREV + "hook通知开始.包名:  " + Arrays.toString(param.args));
                        StatusBarNotification statusBarNotification = (StatusBarNotification) param.args[0];
                        Notification notification = statusBarNotification.getNotification();
                        Icon smallIcon = notification.getSmallIcon();
                        // XposedBridge.log(LOG_PREV + "抓到通知.图标:  " + smallIcon);

                        Bitmap bitmap = null;

                        if (smallIcon.getType() == Icon.TYPE_RESOURCE) {
                            // XposedBridge.log(LOG_PREV + "抓到通知.包名:  " + smallIcon.getResPackage());
                            // XposedBridge.log(LOG_PREV + "抓到通知.资源id:  " + smallIcon.getResId());


//                            Resources resources = (Resources) ReflexUtil.getField4Obj(smallIcon, "mObj1");
                            Context mContext = (Context) ReflexUtil.getField4Obj(param.thisObject, "mContext");
                            bitmap = ImageTools.getBitmap(mContext, smallIcon.getResId());
//                            resources.getDrawable(smallIconIcon.getResId(), )
                            // XposedBridge.log(LOG_PREV + "抓到通知.mContext:  " + mContext);
//                            Drawable drawable = ResourcesCompat.getDrawable(resources, smallIcon.getResId(), AndroidAppHelper.currentApplication().getTheme());
//                            bitmap = BitmapFactory.decodeResource(resources, smallIcon.getResId());
                        }
                        if (smallIcon.getType() == Icon.TYPE_BITMAP ||
                                Icon.TYPE_ADAPTIVE_BITMAP == smallIcon.getType()) {
                            // 反射 获取 mObj1 转为bitmap
                            bitmap = (Bitmap) ReflexUtil.getField4Obj(smallIcon, "mObj1");
                        }
                        if (smallIcon.getType() == Icon.TYPE_DATA) {
//                            byte [] dataBytes = (byte[]) ReflexUtil.getField4Obj(smallIconIcon, "mObj1");
                            // XposedBridge.log(LOG_PREV + "Icon.TYPE_DATA.dataBytes:  " + ReflexUtil.getField4Obj(smallIcon, "mObj1"));
                        }
//                        Notification notification = (Notification) param.args[1];

//                        NotificationListenerService that = (NotificationListenerService) param.thisObject;
//                        Context context = that.createPackageContext(statusBarNotification.getPackageName(), 0);

                        Toast.makeText(AndroidAppHelper.currentApplication(), "hook到有人發通知了!!", Toast.LENGTH_SHORT).show();
                        // XposedBridge.log(LOG_PREV + "转换前:  " + ImageTools.bitmapToBase64(bitmap));
                        // 转换为单色位图
                        Bitmap bitmap1 = ImageTools.getSinglePic(bitmap);
                        // 加圆角
//                        Bitmap bitmap2 = ImageTools.toRoundCorner(bitmap1, bitmap1.getWidth()/2);
                        // XposedBridge.log(LOG_PREV + "转换后:  " + ImageTools.bitmapToBase64(bitmap1));
                        // 重新生成图标
                        Icon newIcon = Icon.createWithBitmap(bitmap1);
                        // 反射赋值
                        ImageTools.setSmallIcon(newIcon, notification);

                        super.beforeHookedMethod(param);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                    }
                });
        // XposedBridge.log(LOG_PREV + "NotificationListener.onNotificationPosted:  Hook完成!!!");
    }


    private void hookNotice(ClassLoader classLoader) {
        final Class<?> clazz = XposedHelpers.findClass(
                NOTICE_CLASS, classLoader);
        XposedHelpers.findAndHookMethod(clazz, "notify"
                , String.class, int.class, Notification.class
                , new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log(LOG_PREV + "hook通知开始.包名:  " + Arrays.toString(param.args));
                        Notification notification = (Notification) param.args[2];

                        Icon smallIconIcon = notification.getSmallIcon();
                        Context context = AndroidAppHelper.currentApplication();

                        // 获取图片
                        Bitmap bitmap = ImageTools.getBitmap(context, smallIconIcon.getResId());
                        XposedBridge.log(LOG_PREV + "转换前:  " + ImageTools.bitmapToBase64(bitmap));
                        // 转换为单色位图
                        Bitmap bitmap1 = ImageTools.getSinglePic(bitmap);
                        // 加圆角
//                        Bitmap bitmap2 = ImageTools.toRoundCorner(bitmap1, bitmap1.getWidth()/2);
                        XposedBridge.log(LOG_PREV + "转换后:  " + ImageTools.bitmapToBase64(bitmap1));
                        // 重新生成图标
                        Icon newIcon = Icon.createWithBitmap(bitmap1);
                        // 反射赋值
                        ImageTools.setSmallIcon(newIcon, notification);

                        super.beforeHookedMethod(param);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                    }
                });
        XposedBridge.log(LOG_PREV + "NotificationManager.findAndHookMethod:  Hook完成!!!");
    }

    // com.android.systemui.statusbar.notification.collection.inflation.NotificationRowBinderImpl#updateRow
    private void updateRow(ClassLoader classLoader) {
        final Class<?> clazz = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.collection.inflation.NotificationRowBinderImpl", classLoader);
        final Class<?> args0 = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.collection.NotificationEntry", classLoader);
        final Class<?> args1 = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.row.ExpandableNotificationRow", classLoader);
        //Hook有参构造函数，修改参数
        XposedHelpers.findAndHookMethod(clazz, "updateRow",
                args0, args1,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Object notificationEntry = param.args[0];
                        ReflexUtil.setField4Obj("targetSdk", notificationEntry, 20);
                    }
                });
    }

    // com.android.systemui.statusbar.notification.icon.IconManager#setIcon
    private void setIcon(ClassLoader classLoader) {
        final Class<?> clazz = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.icon.IconManager", classLoader);
        final Class<?> args0 = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.collection.NotificationEntry", classLoader);
        final Class<?> args1 = XposedHelpers.findClass(
                "com.android.internal.statusbar.StatusBarIcon", classLoader);
        final Class<?> args2 = XposedHelpers.findClass(
                "com.android.systemui.statusbar.StatusBarIconView", classLoader);
        //Hook有参构造函数，修改参数
        XposedHelpers.findAndHookMethod(clazz, "setIcon",
                args0, args1, args2,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Object notificationEntry = param.args[0];
                        ReflexUtil.setField4Obj("targetSdk", notificationEntry, 20);
                    }
                });
    }

    // com.android.systemui.statusbar.notification.row.ExpandableNotificationRow#updateLimitsForView
    private void updateLimitsForView(ClassLoader classLoader) {
        final Class<?> clazz = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.row.ExpandableNotificationRow", classLoader);
        final Class<?> args0 = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.row.NotificationContentView", classLoader);
//        final Class<?> args1 = XposedHelpers.findClass(
//                "com.android.internal.statusbar.StatusBarIcon", classLoader);
//        final Class<?> args2 = XposedHelpers.findClass(
//                "com.android.systemui.statusbar.StatusBarIconView", classLoader);
        //Hook有参构造函数，修改参数
        XposedHelpers.findAndHookMethod(clazz, "updateLimitsForView",
                args0,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Object notificationEntry = ReflexUtil.getField4Obj(param.thisObject, "mEntry");
                        ReflexUtil.setField4Obj("targetSdk", notificationEntry, 20);
                    }
                });
    }


    // com.android.systemui.statusbar.notification.row.ExpandableNotificationRow#updateShelfIconColor
    private void updateShelfIconColor(ClassLoader classLoader) {
        final Class<?> clazz = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.row.ExpandableNotificationRow", classLoader);
//        final Class<?> args0 = XposedHelpers.findClass(
//                "com.android.systemui.statusbar.notification.collection.NotificationEntry", classLoader);
//        final Class<?> args1 = XposedHelpers.findClass(
//                "com.android.internal.statusbar.StatusBarIcon", classLoader);
//        final Class<?> args2 = XposedHelpers.findClass(
//                "com.android.systemui.statusbar.StatusBarIconView", classLoader);
        //Hook有参构造函数，修改参数
        XposedHelpers.findAndHookMethod(clazz, "updateShelfIconColor",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Object notificationEntry = ReflexUtil.getField4Obj(param.thisObject, "mEntry");
                        Object mIcons = ReflexUtil.getField4Obj(notificationEntry, "mIcons");
                        ImageView mShelfIcon = (ImageView) ReflexUtil.getField4Obj(mIcons, "mShelfIcon");
                        Drawable drawable = mShelfIcon.getDrawable();
                        Bitmap bitmap = ImageTools.toBitmap(drawable);
                        XposedBridge.log(LOG_PREV + "hook updateShelfIconColor 前:  " + ImageTools.bitmapToBase64(bitmap));
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Object notificationEntry = ReflexUtil.getField4Obj(param.thisObject, "mEntry");
                        Object mIcons = ReflexUtil.getField4Obj(notificationEntry, "mIcons");
                        ImageView mShelfIcon = (ImageView) ReflexUtil.getField4Obj(mIcons, "mShelfIcon");
                        Drawable drawable = mShelfIcon.getDrawable();
                        Bitmap bitmap = ImageTools.toBitmap(drawable);
                        XposedBridge.log(LOG_PREV + "hook updateShelfIconColor 后:  " + ImageTools.bitmapToBase64(bitmap));
                    }
                });
        XposedBridge.log(LOG_PREV + "hook updateShelfIconColor 完成!!!  ");
    }


    // com.android.systemui.statusbar.notification.collection.inflation.NotificationRowBinderImpl#inflateViews
    // 此次修改单独对状态栏生效 通知面板没有生效
    private void inflateViews1(ClassLoader classLoader) {
        // XposedBridge.log(LOG_PREV + "尝试Hook.NotificationRowBinderImpl:  ");
        final Class<?> clazz = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.collection.inflation.NotificationRowBinderImpl", classLoader);
        final Class<?> args0 = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.collection.NotificationEntry", classLoader);
        final Class<?> args1 = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.row.NotificationRowContentBinder.InflationCallback", classLoader);
        XposedHelpers.findAndHookMethod(clazz, "inflateViews"
                , args0, args1
                , new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log(LOG_PREV + "hook inflateViews 开始:  ");
                        Object notificationEntry = param.args[0];
                        StatusBarNotification statusBarNotification = (StatusBarNotification) ReflexUtil.getField4Obj(notificationEntry, "mSbn");
                        Object mIcons = ReflexUtil.getField4Obj(notificationEntry, "mIcons");

                        ImageView mStatusBarIcon = (ImageView) ReflexUtil.getField4Obj(mIcons, "mStatusBarIcon");
                        if (mStatusBarIcon != null) {
                            if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(mStatusBarIcon.getDrawable()))) {
                                XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mStatusBarIcon");
                            } else {
                                XposedBridge.log(LOG_PREV + "不是灰度 mStatusBarIcon:  ");
                            }
                        }

                        ImageView mShelfIcon = (ImageView) ReflexUtil.getField4Obj(mIcons, "mShelfIcon");
                        if (mShelfIcon != null) {
                            if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(mShelfIcon.getDrawable()))) {
                                XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mShelfIcon");
                            } else {
                                XposedBridge.log(LOG_PREV + "不是灰度 mShelfIcon:  ");
                            }
                        }
                        ImageView mAodIcon = (ImageView) ReflexUtil.getField4Obj(mIcons, "mAodIcon");
                        if (mAodIcon != null) {
                            if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(mAodIcon.getDrawable()))) {
                                XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mAodIcon");
                            } else {
                                XposedBridge.log(LOG_PREV + "不是灰度 mAodIcon:  ");
                            }
                        }
                        ImageView mCenteredIcon = (ImageView) ReflexUtil.getField4Obj(mIcons, "mCenteredIcon");
                        if (mCenteredIcon != null) {
                            if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(mCenteredIcon.getDrawable()))) {
                                XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mCenteredIcon");
                            } else {
                                XposedBridge.log(LOG_PREV + "不是灰度 mCenteredIcon:  ");
                            }
                        }

                        Application application = AndroidAppHelper.currentApplication();
                        Context context = application.createPackageContext(statusBarNotification.getPackageName(), Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);

                        Object mSmallIconDescriptor = ReflexUtil.getField4Obj(mIcons, "mSmallIconDescriptor");
                        if (mSmallIconDescriptor != null) {
                            Icon mSmallIconDescriptorIcon = (Icon) ReflexUtil.getField4Obj(mSmallIconDescriptor, "icon");
                            if (mSmallIconDescriptorIcon != null) {
                                Drawable drawable = mSmallIconDescriptorIcon.loadDrawable(context);
                                if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(drawable))) {
                                    XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mSmallIconDescriptor");
                                } else {
                                    XposedBridge.log(LOG_PREV + "不是灰度 mSmallIconDescriptor:  ");
                                }
                            }
                        }

                        Object mPeopleAvatarDescriptor = ReflexUtil.getField4Obj(mIcons, "mPeopleAvatarDescriptor");
                        if (mPeopleAvatarDescriptor != null) {
                            Icon mPeopleAvatarDescriptorIcon = (Icon) ReflexUtil.getField4Obj(mPeopleAvatarDescriptor, "icon");
                            if (mPeopleAvatarDescriptorIcon != null) {
                                Drawable mPeopleDrawable = mPeopleAvatarDescriptorIcon.loadDrawable(context);
                                if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(mPeopleDrawable))) {
                                    XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mPeopleAvatarDescriptor");
                                } else {
                                    XposedBridge.log(LOG_PREV + "不是灰度 mPeopleAvatarDescriptor  ");
                                }
                            }
                        }

                        XposedBridge.log(LOG_PREV + "NotificationRowBinderImpl inflateViews 前 end");

                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log(LOG_PREV + "hook inflateViews afterHookedMethod 开始:  ");
                        Object notificationEntry = param.args[0];
                        StatusBarNotification statusBarNotification = (StatusBarNotification) ReflexUtil.getField4Obj(notificationEntry, "mSbn");
                        Object mIcons = ReflexUtil.getField4Obj(notificationEntry, "mIcons");

                        ImageView mStatusBarIcon = (ImageView) ReflexUtil.getField4Obj(mIcons, "mStatusBarIcon");
                        if (mStatusBarIcon != null) {
                            if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(mStatusBarIcon.getDrawable()))) {
                                XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mStatusBarIcon");
                            } else {
                                XposedBridge.log(LOG_PREV + "不是灰度 mStatusBarIcon:  ");
                            }
                        }

                        ImageView mShelfIcon = (ImageView) ReflexUtil.getField4Obj(mIcons, "mShelfIcon");
                        if (mShelfIcon != null) {
                            if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(mShelfIcon.getDrawable()))) {
                                XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mShelfIcon");
                            } else {
                                XposedBridge.log(LOG_PREV + "不是灰度 mShelfIcon:  ");
                            }
                        }
                        ImageView mAodIcon = (ImageView) ReflexUtil.getField4Obj(mIcons, "mAodIcon");
                        if (mAodIcon != null) {
                            if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(mAodIcon.getDrawable()))) {
                                XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mAodIcon");
                            } else {
                                XposedBridge.log(LOG_PREV + "不是灰度 mAodIcon:  ");
                            }
                        }
                        ImageView mCenteredIcon = (ImageView) ReflexUtil.getField4Obj(mIcons, "mCenteredIcon");
                        if (mCenteredIcon != null) {
                            if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(mCenteredIcon.getDrawable()))) {
                                XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mCenteredIcon");
                            } else {
                                XposedBridge.log(LOG_PREV + "不是灰度 mCenteredIcon:  ");
                            }
                        }

                        Application application = AndroidAppHelper.currentApplication();
                        Context context = application.createPackageContext(statusBarNotification.getPackageName(), Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);

                        Object mSmallIconDescriptor = ReflexUtil.getField4Obj(mIcons, "mSmallIconDescriptor");
                        if (mSmallIconDescriptor != null) {
                            Icon mSmallIconDescriptorIcon = (Icon) ReflexUtil.getField4Obj(mSmallIconDescriptor, "icon");
                            if (mSmallIconDescriptorIcon != null) {
                                Drawable drawable = mSmallIconDescriptorIcon.loadDrawable(context);
                                if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(drawable))) {
                                    XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mSmallIconDescriptor");
                                } else {
                                    XposedBridge.log(LOG_PREV + "不是灰度 mSmallIconDescriptor:  ");
                                }
                            }
                        }

                        Object mPeopleAvatarDescriptor = ReflexUtil.getField4Obj(mIcons, "mPeopleAvatarDescriptor");
                        if (mPeopleAvatarDescriptor != null) {
                            Icon mPeopleAvatarDescriptorIcon = (Icon) ReflexUtil.getField4Obj(mPeopleAvatarDescriptor, "icon");
                            if (mPeopleAvatarDescriptorIcon != null) {
                                Drawable mPeopleDrawable = mPeopleAvatarDescriptorIcon.loadDrawable(context);
                                if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(mPeopleDrawable))) {
                                    XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mPeopleAvatarDescriptor");
                                } else {
                                    XposedBridge.log(LOG_PREV + "不是灰度 mPeopleAvatarDescriptor  ");
                                }
                            }
                        }

                        XposedBridge.log(LOG_PREV + "NotificationRowBinderImpl inflateViews afterHookedMethod end");

                    }
                });
    }

    private void inflateContentViews(ClassLoader classLoader) {
        // XposedBridge.log(LOG_PREV + "尝试Hook.NotificationRowBinderImpl:  ");
        final Class<?> clazz = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.collection.inflation.NotificationRowBinderImpl", classLoader);
        final Class<?> args0 = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.collection.NotificationEntry", classLoader);
        final Class<?> args1 = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.row.ExpandableNotificationRow", classLoader);
        final Class<?> args2 = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.row.NotificationRowContentBinder.InflationCallback", classLoader);
        XposedHelpers.findAndHookMethod(clazz, "inflateContentViews"
                , args0, args1, args2
                , new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log(LOG_PREV + "hook inflateContentViews 开始:  ");
                        Object notificationEntry = param.args[0];
                        StatusBarNotification statusBarNotification = (StatusBarNotification) ReflexUtil.getField4Obj(notificationEntry, "mSbn");
                        Object mIcons = ReflexUtil.getField4Obj(notificationEntry, "mIcons");

                        ImageView mStatusBarIcon = (ImageView) ReflexUtil.getField4Obj(mIcons, "mStatusBarIcon");
                        if (mStatusBarIcon != null) {
                            if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(mStatusBarIcon.getDrawable()))) {
                                XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mStatusBarIcon");
                            } else {
                                XposedBridge.log(LOG_PREV + "不是灰度 mStatusBarIcon:  ");
                            }
                        }

                        ImageView mShelfIcon = (ImageView) ReflexUtil.getField4Obj(mIcons, "mShelfIcon");
                        if (mShelfIcon != null) {
                            if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(mShelfIcon.getDrawable()))) {
                                XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mShelfIcon");
                            } else {
                                XposedBridge.log(LOG_PREV + "不是灰度 mShelfIcon:  ");
                            }
                        }
                        ImageView mAodIcon = (ImageView) ReflexUtil.getField4Obj(mIcons, "mAodIcon");
                        if (mAodIcon != null) {
                            if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(mAodIcon.getDrawable()))) {
                                XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mAodIcon");
                            } else {
                                XposedBridge.log(LOG_PREV + "不是灰度 mAodIcon:  ");
                            }
                        }
                        ImageView mCenteredIcon = (ImageView) ReflexUtil.getField4Obj(mIcons, "mCenteredIcon");
                        if (mCenteredIcon != null) {
                            if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(mCenteredIcon.getDrawable()))) {
                                XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mCenteredIcon");
                            } else {
                                XposedBridge.log(LOG_PREV + "不是灰度 mCenteredIcon:  ");
                            }
                        }

                        Application application = AndroidAppHelper.currentApplication();
                        Context context = application.createPackageContext(statusBarNotification.getPackageName(), Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);

                        Object mSmallIconDescriptor = ReflexUtil.getField4Obj(mIcons, "mSmallIconDescriptor");
                        if (mSmallIconDescriptor != null) {
                            Icon mSmallIconDescriptorIcon = (Icon) ReflexUtil.getField4Obj(mSmallIconDescriptor, "icon");
                            if (mSmallIconDescriptorIcon != null) {
                                Drawable drawable = mSmallIconDescriptorIcon.loadDrawable(context);
                                if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(drawable))) {
                                    XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mSmallIconDescriptor");
                                } else {
                                    XposedBridge.log(LOG_PREV + "不是灰度 mSmallIconDescriptor:  ");
                                }
                            }
                        }

                        Object mPeopleAvatarDescriptor = ReflexUtil.getField4Obj(mIcons, "mPeopleAvatarDescriptor");
                        if (mPeopleAvatarDescriptor != null) {
                            Icon mPeopleAvatarDescriptorIcon = (Icon) ReflexUtil.getField4Obj(mPeopleAvatarDescriptor, "icon");
                            if (mPeopleAvatarDescriptorIcon != null) {
                                Drawable mPeopleDrawable = mPeopleAvatarDescriptorIcon.loadDrawable(context);
                                if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(mPeopleDrawable))) {
                                    XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mPeopleAvatarDescriptor");
                                } else {
                                    XposedBridge.log(LOG_PREV + "不是灰度 mPeopleAvatarDescriptor  ");
                                }
                            }
                        }

                        XposedBridge.log(LOG_PREV + "NotificationRowBinderImpl inflateContentViews 前 end");

                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log(LOG_PREV + "hook inflateContentViews afterHookedMethod 开始:  ");
                        Object notificationEntry = param.args[0];
                        StatusBarNotification statusBarNotification = (StatusBarNotification) ReflexUtil.getField4Obj(notificationEntry, "mSbn");
                        Object mIcons = ReflexUtil.getField4Obj(notificationEntry, "mIcons");

                        ImageView mStatusBarIcon = (ImageView) ReflexUtil.getField4Obj(mIcons, "mStatusBarIcon");
                        if (mStatusBarIcon != null) {
                            if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(mStatusBarIcon.getDrawable()))) {
                                XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mStatusBarIcon");
                            } else {
                                XposedBridge.log(LOG_PREV + "不是灰度 mStatusBarIcon:  ");
                            }
                        }

                        ImageView mShelfIcon = (ImageView) ReflexUtil.getField4Obj(mIcons, "mShelfIcon");
                        if (mShelfIcon != null) {
                            if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(mShelfIcon.getDrawable()))) {
                                XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mShelfIcon");
                            } else {
                                XposedBridge.log(LOG_PREV + "不是灰度 mShelfIcon:  ");
                            }
                        }
                        ImageView mAodIcon = (ImageView) ReflexUtil.getField4Obj(mIcons, "mAodIcon");
                        if (mAodIcon != null) {
                            if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(mAodIcon.getDrawable()))) {
                                XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mAodIcon");
                            } else {
                                XposedBridge.log(LOG_PREV + "不是灰度 mAodIcon:  ");
                            }
                        }
                        ImageView mCenteredIcon = (ImageView) ReflexUtil.getField4Obj(mIcons, "mCenteredIcon");
                        if (mCenteredIcon != null) {
                            if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(mCenteredIcon.getDrawable()))) {
                                XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mCenteredIcon");
                            } else {
                                XposedBridge.log(LOG_PREV + "不是灰度 mCenteredIcon:  ");
                            }
                        }

                        Application application = AndroidAppHelper.currentApplication();
                        Context context = application.createPackageContext(statusBarNotification.getPackageName(), Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);

                        Object mSmallIconDescriptor = ReflexUtil.getField4Obj(mIcons, "mSmallIconDescriptor");
                        if (mSmallIconDescriptor != null) {
                            Icon mSmallIconDescriptorIcon = (Icon) ReflexUtil.getField4Obj(mSmallIconDescriptor, "icon");
                            if (mSmallIconDescriptorIcon != null) {
                                Drawable drawable = mSmallIconDescriptorIcon.loadDrawable(context);
                                if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(drawable))) {
                                    XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mSmallIconDescriptor");
                                } else {
                                    XposedBridge.log(LOG_PREV + "不是灰度 mSmallIconDescriptor:  ");
                                }
                            }
                        }

                        Object mPeopleAvatarDescriptor = ReflexUtil.getField4Obj(mIcons, "mPeopleAvatarDescriptor");
                        if (mPeopleAvatarDescriptor != null) {
                            Icon mPeopleAvatarDescriptorIcon = (Icon) ReflexUtil.getField4Obj(mPeopleAvatarDescriptor, "icon");
                            if (mPeopleAvatarDescriptorIcon != null) {
                                Drawable mPeopleDrawable = mPeopleAvatarDescriptorIcon.loadDrawable(context);
                                if (ImageTools.isGrayscaleIcon(ImageTools.toBitmap(mPeopleDrawable))) {
                                    XposedBridge.log(LOG_PREV + "!!!!!! 已变成灰度 mPeopleAvatarDescriptor");
                                } else {
                                    XposedBridge.log(LOG_PREV + "不是灰度 mPeopleAvatarDescriptor  ");
                                }
                            }
                        }

                        XposedBridge.log(LOG_PREV + "NotificationRowBinderImpl inflateContentViews afterHookedMethod end");

                    }
                });
    }


    // com.android.systemui.statusbar.phone.NotificationIconAreaController#updateTintForIcon
    private void updateTintForIcon(ClassLoader classLoader) {
        XposedBridge.log(LOG_PREV + "尝试Hook.updateTintForIcon:  ");
        final Class<?> clazz = XposedHelpers.findClass(
                "com.android.systemui.statusbar.phone.NotificationIconAreaController", classLoader);
        final Class<?> args0 = XposedHelpers.findClass(
                "com.android.systemui.statusbar.StatusBarIconView", classLoader);
        final Class<?> args1 = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.row.ExpandableNotificationRow", classLoader);
        XposedHelpers.findAndHookMethod(clazz, "updateTintForIcon"
                , args0, int.class
                , new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                        View statusBarIconView = (View) param.args[0];
                        int icon_is_grayscale = 0x7f0b02ac;
                        ImageView imageView = (ImageView) param.args[0];
                        imageView.setTag(icon_is_grayscale, true);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                    }
                });
        XposedBridge.log(LOG_PREV + "updateTintForIcon:  Hook完成!!!");
    }


    // com.android.systemui.statusbar.phone.NotificationIconAreaController#updateTintForIcon
    // com.android.systemui.statusbar.notification.row.ExpandableNotificationRow#getOriginalIconColor
    // 这里进行状态栏通知图标的旧版彩色判断 todo 有用
    // android.app.Notification.Builder#createContentView(boolean)
    private void createContentView(ClassLoader classLoader) {
        XposedBridge.log(LOG_PREV + "尝试Hook.getOriginalIconColor:  ");
        final Class<?> clazz = XposedHelpers.findClass(
                "android.app.Notification.Builder", classLoader);
        final Class<?> statusBarIconViewClass = XposedHelpers.findClass(
                "com.android.systemui.statusbar.StatusBarIconView", classLoader);
        XposedHelpers.findAndHookMethod(clazz, "createContentView"
                , boolean.class
                , new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                        View statusBarIconView = (View) param.args[0];
                        int icon_is_grayscale = 0x7f0b02ac;
                        // android.view.View
//                        statusBarIconView.setTag(icon_is_grayscale, true);
//                        if ( (int)param.args[0] == icon_is_grayscale) {
//                            param.setResult(0);
//                        }
                        Object mStyle = ReflexUtil.getField4Obj(param.thisObject, "mStyle");
                        XposedBridge.log(LOG_PREV + "尝试Hook. mStyle: == " + mStyle);
                        Notification mN = (Notification) ReflexUtil.getField4Obj(param.thisObject, "mN");
                        Context mContext = (Context) ReflexUtil.getField4Obj(param.thisObject, "mContext");
//                        fixNotificationIcon(mN, mContext);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {


                        RemoteViews remoteViews = (RemoteViews) param.getResult();
                    }
                });
        // XposedBridge.log(LOG_PREV + "NotificationListener.onNotificationPosted:  Hook完成!!!");
    }

}
