package com.xeasy.noticefix.dao;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.xeasy.noticefix.R;
import com.xeasy.noticefix.bean.AppInfo4View;
import com.xeasy.noticefix.bean.CustomIconBean;
import com.xeasy.noticefix.bean.IconLibBean;
import com.xeasy.noticefix.constant.MyConstant;
import com.xeasy.noticefix.utils.ImageTools;
import com.xeasy.noticefix.utils.TaskUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.FutureTask;

public class AppUtil {

    public static Map<Integer, Integer> appCount;

    private static List<AppInfo4View> appInfo4ViewList;
    public static final Map<String, AppInfo4View> appInfo4ViewMap = new HashMap<>();

    static {
        resetCountMap();
    }

    public static FutureTask<List<AppInfo4View>> cacheTask = null;

    public static FutureTask<List<AppInfo4View>> cacheInfoMap(Context context) {
        // 开个线程准备app列表缓存 以解决首次搜索慢的问题 处理
        cacheTask = TaskUtils.createTask(() -> AppUtil.getApps4View(context, true));
        return cacheTask;
    }


    public static Map<Integer, Integer> resetCountMap(){
        appCount = new HashMap<>();
        for (MyConstant.AppType value : MyConstant.AppType.values()) {
            appCount.put(value.typeId, 0);
        }
        return appCount;
    }

    /**
     * 获取手机已安装应用列表
     *
     * @param ctx c
     * @return list
     */
    public static List<PackageInfo> getAllAppInfo(Context ctx) {
        PackageManager packageManager = ctx.getPackageManager();
        return packageManager.getInstalledPackages(0);
    }

    /**
     * 获取手机已安装应用列表
     *
     * @param ctx c
     * @return list
     */
    public static PackageInfo getAppInfo(Context ctx, String packageName) {
        try {
            PackageManager packageManager = ctx.getPackageManager();
            return packageManager.getPackageInfo(packageName, 0);
        } catch ( Exception e) {
            return null;
        }
    }

    public static List<AppInfo4View> getApps4View(Context context, boolean refresh) {
        if ( refresh || appInfo4ViewList == null || appInfo4ViewList.isEmpty()) {
            List<PackageInfo> allAppInfo = getAllAppInfo(context);
            List<AppInfo4View> result = new ArrayList<>();
            appInfo4ViewMap.clear();
            resetCountMap();
            int systemApp = 0;
            int userApp = 0;
            int libIconApp = 0;
            int customIconApp = 0;
            int whiteListApp = 0;
            for (PackageInfo packageInfo : allAppInfo) {
                AppInfo4View appInfo4View = getApp4ViewByPackageInfo(context, packageInfo);
                result.add(appInfo4View);
                // 开始计数
                if ( appInfo4View.isSystem ) systemApp++;
                if ( ! appInfo4View.isSystem ) userApp++;
                if ( appInfo4View.libIcon != null  ) libIconApp++;
                if ( appInfo4View.customIcon != null  ) customIconApp++;
                if ( appInfo4View.notHandle ) whiteListApp++;
            }
            appCount.put(MyConstant.AppType.SYSTEM.typeId, systemApp);
            appCount.put(MyConstant.AppType.USER.typeId, userApp);
            appCount.put(MyConstant.AppType.LIB_ICON.typeId, libIconApp);
            appCount.put(MyConstant.AppType.CUSTOM_ICON.typeId, customIconApp);
            appCount.put(MyConstant.AppType.WHITE_LIST.typeId, whiteListApp);
            appInfo4ViewList = result;
        }
        return appInfo4ViewList;
    }

    public static void countRefresh() {
        resetCountMap();
        int systemApp = 0;
        int userApp = 0;
        int libIconApp = 0;
        int customIconApp = 0;
        int whiteListApp = 0;
        for (AppInfo4View appInfo4View : appInfo4ViewList) {
            // 开始计数
            if ( appInfo4View.isSystem ) systemApp++;
            if ( ! appInfo4View.isSystem ) userApp++;
            if ( appInfo4View.libIcon != null  ) libIconApp++;
            if ( appInfo4View.customIcon != null  ) customIconApp++;
            if ( appInfo4View.notHandle ) whiteListApp++;
        }
        appCount.put(MyConstant.AppType.SYSTEM.typeId, systemApp);
        appCount.put(MyConstant.AppType.USER.typeId, userApp);
        appCount.put(MyConstant.AppType.LIB_ICON.typeId, libIconApp);
        appCount.put(MyConstant.AppType.CUSTOM_ICON.typeId, customIconApp);
        appCount.put(MyConstant.AppType.WHITE_LIST.typeId, whiteListApp);
    }

    public static AppInfo4View getApp4ViewByPackageInfo(Context context, PackageInfo packageInfo) {
        PackageManager packageManager = context.getPackageManager();
        AppInfo4View cache = appInfo4ViewMap.get(packageInfo.applicationInfo.packageName);
        if ( cache != null ) {
            return cache;
        }
//        System.out.println("cache没有命中=" + packageInfo.applicationInfo.packageName);
        AppInfo4View appInfo4View = new AppInfo4View();
        appInfo4View.AppName = (packageManager.getApplicationLabel(packageInfo.applicationInfo).toString());
        appInfo4View.AppPkg = (packageInfo.applicationInfo.packageName);
        appInfo4View.version = (packageInfo.versionName);
        int flags = packageInfo.applicationInfo.flags;
        appInfo4View.isSystem = (flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        appInfo4View.versionAndType = appInfo4View.isSystem ?
                context.getResources().getString(R.string.app_type_system, appInfo4View.version)
                : context.getResources().getString(R.string.app_type_user, appInfo4View.version);
        appInfo4View.AppIcon = (packageInfo.applicationInfo.loadIcon(packageManager));
        // todo
        appInfo4View.lastIcon = null;
        // 匹配 lib_icon
        IconLibBean iconLib = IconLibDao.getIconLibBean(context, appInfo4View.AppPkg);
        if ( iconLib != null ) {
            appInfo4View.libIcon = ImageTools.base64ToBitmap(iconLib.iconBitmap);
        }
        CustomIconBean customIcons = CustomIconDao.getCustomIcons(context, appInfo4View.AppPkg);
        if ( customIcons != null ) {
            if ( customIcons.iconBase64 != null && ! customIcons.iconBase64.isEmpty() ) {
                appInfo4View.customIcon = (ImageTools.base64ToBitmap(customIcons.iconBase64));
            }
            appInfo4View.notHandle = customIcons.noHandle;
            appInfo4View.expandStatusBar = customIcons.expandStatusBar;
            appInfo4View.expandHeadsUp = customIcons.expandHeadsUp;
        }
        appInfo4ViewMap.put(appInfo4View.AppPkg, appInfo4View);
        return appInfo4View;
    }
    public static AppInfo4View getApp4ViewByPackageName(Context context, String packageName) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            return getApp4ViewByPackageInfo(context, packageInfo);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 符合任意一项 返回true 否则 false
     * @param appInfo4View bean
     * @param filter 条件
     * @return yes | no
     */
    public static boolean appInFilter(AppInfo4View appInfo4View, Set<MyConstant.AppType> filter) {
        for (MyConstant.AppType appType : filter) {
            // An enum switch case label must be the unqualified name of an enumeration constant
            switch (appType){
                case SYSTEM:
                    if ( appInfo4View.isSystem ) return true;
                    break;
                case USER:
                    if ( ! appInfo4View.isSystem ) return true;
                    break;
                case LIB_ICON:
                    if ( appInfo4View.libIcon != null ) return true;
                    break;
                case CUSTOM_ICON:
                    if ( appInfo4View.customIcon != null ) return true;
                    break;
                case WHITE_LIST:
                    if ( appInfo4View.notHandle ) return true;
                    break;
                default:
                    return false;
            }
        }
        return false;
    }

}
