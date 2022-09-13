package com.xeasy.noticefix.hook;

import static com.xeasy.noticefix.hook.HookConstant.gson;

import android.annotation.SuppressLint;
import android.app.AndroidAppHelper;
import android.app.Notification;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.view.View;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.xeasy.noticefix.bean.CustomIconBean;
import com.xeasy.noticefix.bean.IconFunc;
import com.xeasy.noticefix.bean.IconLibBean;
import com.xeasy.noticefix.dao.GlobalConfigDao;
import com.xeasy.noticefix.dao.IconFuncDao;
import com.xeasy.noticefix.utils.ImageTools;
import com.xeasy.noticefix.utils.ImageUtils;
import com.xeasy.noticefix.utils.ReflexUtil;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class SystemUIHooker implements IXposedHookLoadPackage {

    private static final String LOG_PREV = "NoticeFix---";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {


        // 激活状态 com.xeasy.noticefix.activity.MainActivity.activeXposed
        if (loadPackageParam.packageName.equals("com.xeasy.noticefix")) {
            Class<?> aClass = XposedHelpers.findClass("com.xeasy.noticefix.activity.MainActivity", loadPackageParam.classLoader);
            XposedHelpers.findAndHookMethod(aClass, "activeXposed"
                    , boolean.class
                    , new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            System.out.println("beforeHookedMethod ====" + param);
                            param.args[0] = true;
                        }
                    });
        }


        if (loadPackageParam.packageName.equals("com.android.systemui")) {
            // hook sustemui的通知监听器
            try {
                inflateViews(loadPackageParam.classLoader);
            } catch (Exception e) {
                XposedBridge.log(LOG_PREV + "hook -- inflateViews 错误");
                XposedBridge.log(e);
            }
            try {
                setIcon(loadPackageParam.classLoader);
            } catch (Exception e) {
                XposedBridge.log(LOG_PREV + "hook -- resolveNotificationSdk 错误");
                XposedBridge.log(e);
            }

        }
    }


    // com.android.systemui.statusbar.notification.row.ExpandableNotificationRow#setSystemExpanded
    // todo 测试展开通知
    private void setSystemExpanded(ClassLoader classLoader) {
        final Class<?> clazz = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.row.ExpandableNotificationRow", classLoader);
        //Hook有参构造函数，修改参数
        XposedHelpers.findAndHookMethod(clazz, "setSystemExpanded",
                boolean.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        param.args[0] = true;
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
                args0, args1, args2
                , new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        if ( HookConstant.globalConfigDao.read && HookConstant.globalConfigDao.showColoredIcons ) {
                            View iconView = (View) param.args[2];
                            @SuppressLint("DiscouragedApi")
                            int preLTag = AndroidAppHelper.currentApplication().getResources().getIdentifier("icon_is_pre_L", "id", "com.android.systemui");
                            iconView.setTag(preLTag, true);
                        }
                    }
                });
    }


    // com.android.systemui.statusbar.notification.collection.inflation.NotificationRowBinderImpl#inflateViews
    private void inflateViews(ClassLoader classLoader) {



        final Class<?> clazz = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.collection.inflation.NotificationRowBinderImpl", classLoader);
        final Class<?> args0 = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.collection.NotificationEntry", classLoader);
        final Class<?> args1 = XposedHelpers.findClass(
                "com.android.systemui.statusbar.notification.row.NotificationRowContentBinder.InflationCallback", classLoader);
        XC_MethodHook xc_methodHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (!HookConstant.globalConfigDao.read) {
                    readConfig(AndroidAppHelper.currentApplication());
                }
                        /*XposedBridge.log(LOG_PREV + "看看全局变量 HookConstant.globalConfigDao:  " + gson.toJson(HookConstant.globalConfigDao));
                        XposedBridge.log(LOG_PREV + "看看全局变量 HookConstant.iconFuncStatuses:  " + gson.toJson(HookConstant.iconFuncStatuses));
                        XposedBridge.log(LOG_PREV + "看看全局变量 HookConstant.iconLibBeanMap:  " + HookConstant.iconLibBeanMap.size());
                        XposedBridge.log(LOG_PREV + "看看全局变量 HookConstant.customIconBeanMap:  " + HookConstant.customIconBeanMap.size());
                        XposedBridge.log(LOG_PREV + "hook通知开始.包名:  " + Arrays.toString(param.args));*/
                try {
                    Object notificationEntry = param.args[0];
                    StatusBarNotification statusBarNotification = (StatusBarNotification) ReflexUtil.getField4Obj(notificationEntry, "mSbn");
                    Context mContext = (Context) ReflexUtil.getField4Obj(param.thisObject, "mContext");
                    Context context = mContext.createPackageContext(statusBarNotification.getPackageName(), Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
                    fixNotificationIcon(statusBarNotification, context);
                } catch (Exception e) {
                    XposedBridge.log(e);
                }
            }

        };


        Object[] args;

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
            final Class<?> argsNew = XposedHelpers.findClass(
                    "com.android.systemui.statusbar.notification.collection.inflation.NotifInflater.Params", classLoader);
            args = new Object[]{args0, argsNew, args1, xc_methodHook};
        } else {
            args = new Object[]{args0, args1, xc_methodHook};
        }

        XposedHelpers.findAndHookMethod(clazz, "inflateViews", args);
    }

    private void readConfig(Context context) {
        try {
            ContentResolver contentResolver = context.getContentResolver();
            Uri uri = Uri.parse("content://com.xeasy.noticefix.provider.IconDataContentProvider");
            Cursor query = contentResolver.query(uri, null, null, null);
            query.moveToNext();
            String globalConfig = query.getString(0);
            String iconFunc = query.getString(1);
            String libIconList = query.getString(2);
            String customIconList = query.getString(3);
            query.close();
            // 解析赋值给全局变量
            HookConstant.globalConfigDao = gson.fromJson(globalConfig, GlobalConfigDao.class);
            // 2
            Type type = new TypeToken<List<IconFuncDao.IconFuncStatus>>() {
            }.getType();
            HookConstant.iconFuncStatuses = gson.fromJson(iconFunc, type);
            Collections.sort(HookConstant.iconFuncStatuses);
            // 3
            Type type2 = new TypeToken<Map<String, IconLibBean>>() {
            }.getType();
            HookConstant.iconLibBeanMap = gson.fromJson(libIconList, type2);
            // 4
            Type type3 = new TypeToken<Map<String, CustomIconBean>>() {
            }.getType();
            HookConstant.customIconBeanMap = gson.fromJson(customIconList, type3);
//            Toast.makeText(context, "读取图标成功!!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "读取图标资源失败!!", Toast.LENGTH_SHORT).show();
            XposedBridge.log(e);
        }
    }


    public static void fixNotificationIcon(StatusBarNotification statusBarNotification, Context context) {
        try {
            Notification notification = statusBarNotification.getNotification();
            Icon smallIcon = notification.getSmallIcon();
            Drawable drawable = smallIcon.loadDrawable(context);
            Bitmap bitmap = ImageTools.toBitmap(drawable);
            String packageName = statusBarNotification.getPackageName();
            // 跳过灰度图
            if (HookConstant.globalConfigDao.skipGrayscale && new ImageUtils().isGrayscale(bitmap)) {
                return;
            }
            for (IconFuncDao.IconFuncStatus iconFuncStatus : HookConstant.iconFuncStatuses) {
                if (iconFuncStatus.active) {
                    // 使用库
                    if (iconFuncStatus.iconFuncId == IconFunc.LIB_FIX.funcId) {
                        IconLibBean iconLibBean = HookConstant.iconLibBeanMap.get(packageName);
                        if (iconLibBean != null) {
                            // 重新生成图标
                            Icon newIcon = Icon.createWithBitmap(ImageTools.base64ToBitmap(iconLibBean.iconBitmap));
                            // 反射赋值
                            ImageTools.setSmallIcon(newIcon, notification);
                            return;
                        }

                    }
                    // 使用自定义
                    if (iconFuncStatus.iconFuncId == IconFunc.CUSTOM_FIX.funcId) {
                        CustomIconBean customIconBean = HookConstant.customIconBeanMap.get(packageName);
                        if (customIconBean != null) {
                            // 重新生成图标
                            Icon newIcon = Icon.createWithBitmap(ImageTools.base64ToBitmap(customIconBean.iconBase64));
                            // 反射赋值
                            ImageTools.setSmallIcon(newIcon, notification);
                            return;
                        }
                    }
                    // 使用 算法
                    if (iconFuncStatus.iconFuncId == IconFunc.AUTO_FIX.funcId) {
                        // 不是灰度才转换 不然算法么有意义
                        if (!new ImageUtils().isGrayscale(bitmap)) {
                            // 转换为单色位图
                            Bitmap bitmap1 = ImageTools.getSinglePic(bitmap);
                            // 重新生成图标
                            Icon newIcon = Icon.createWithBitmap(bitmap1);
                            // 反射赋值
                            ImageTools.setSmallIcon(newIcon, notification);
                            return;
                        }
                    }
                }
            }
        } catch (Exception e) {
            XposedBridge.log(LOG_PREV + "修改图标错误");
            XposedBridge.log(e);
        }
    }


}
