package com.xeasy.noticefix.dao;

import static com.xeasy.noticefix.constant.MyConstant.CUSTOM_ICON_FILE;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.gson.Gson;
import com.xeasy.noticefix.bean.AppInfo4View;
import com.xeasy.noticefix.bean.CustomIconBean;
import com.xeasy.noticefix.constant.MyConstant;
import com.xeasy.noticefix.utils.ImageTools;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CustomIconDao {

    private static final String FILE_NAME = CUSTOM_ICON_FILE;

    static Gson gson = new Gson();
    static Map<String, CustomIconBean> customIconBeanMap;

    /**
     * 持久化自定义通知图标
     * 注意: 此方法会更新 AppUtil.appCount 计数 和 com.xeasy.noticefix.dao.AppUtil#appInfo4ViewMap
     */
    public static boolean saveCustomIcon(Context context, String pkgName, Bitmap icon) {
        // 更新 com.xeasy.noticefix.dao.AppUtil#appInfo4ViewMap
        AppInfo4View app4ViewByPackageName = AppUtil.getApp4ViewByPackageName(context, pkgName);
        assert app4ViewByPackageName != null;
        app4ViewByPackageName.customIcon = icon;
        CustomIconBean oldBean = getCustomIcons(context, pkgName);
        if ( oldBean != null ) {
            oldBean.iconBase64 = ImageTools.bitmapToBase64(icon);
            return save(context, oldBean);
        }
        // new 保存
        CustomIconBean customIconBean = new CustomIconBean();
        customIconBean.pkgName = pkgName;
        customIconBean.label = app4ViewByPackageName.AppName;
        customIconBean.iconBase64 = ImageTools.bitmapToBase64(icon);
        //  自定义图标 计数 + 1
        AppUtil.appCount.put(MyConstant.AppType.CUSTOM_ICON.typeId,
                Objects.requireNonNull(AppUtil.appCount.get(MyConstant.AppType.CUSTOM_ICON.typeId)) + 1);
        return CustomIconDao.save(context, customIconBean);
    }

    public static boolean save(Context context, CustomIconBean customIconBean) {
        // 交换俩对象的order值 然后保存
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();

        edit.putString(customIconBean.pkgName, gson.toJson(customIconBean));
        boolean commit = edit.commit();
        if ( commit ) {
            customIconBeanMap.put(customIconBean.pkgName, customIconBean);
            Log.d(CustomIconDao.class.getName(), "保存自定义图标成功");
//            Toast.makeText(context, "保存配置信息成功", Toast.LENGTH_SHORT).show();
        }
        return commit;
    }

    /**
     * 删除自定义图标
     * 注意: 此方法会更新 AppUtil.appCount 计数 和 com.xeasy.noticefix.dao.AppUtil#appInfo4ViewMap
     */
    public static boolean delete(Context context, String pkgName) {
        // AppUtil.appInfo4ViewMap
        AppInfo4View app4ViewByPackageName = AppUtil.getApp4ViewByPackageName(context, pkgName);
        assert app4ViewByPackageName != null;
        if ( app4ViewByPackageName.customIcon != null ) {
            //  自定义图标 计数 - 1
            AppUtil.appCount.put(MyConstant.AppType.CUSTOM_ICON.typeId,
                    Objects.requireNonNull(AppUtil.appCount.get(MyConstant.AppType.CUSTOM_ICON.typeId)) - 1);
        }
        app4ViewByPackageName.customIcon = null;
        // 移除
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.remove(pkgName);
        boolean commit = edit.commit();
        if ( commit ) {
            customIconBeanMap.remove(pkgName);
            Log.d(CustomIconDao.class.getName(), "删除自定义图标成功");
//            Toast.makeText(context, "保存配置信息成功", Toast.LENGTH_SHORT).show();
        }
        return commit;
    }

    /**
     * 获取所有自定义图标
     * @param context c
     * @return r
     */
    public static Map<String, CustomIconBean> getAllCustomIcons(Context context) {
        if ( customIconBeanMap == null ) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
            Map<String, ?> all = sharedPreferences.getAll();
            // 准备返回的数据
            customIconBeanMap = new HashMap<>();
            if ( all != null && all.size() > 0) {
                all.forEach((k, v)->{
                    CustomIconBean customIconBean = gson.fromJson((String) v, CustomIconBean.class);
                    customIconBeanMap.put(k, customIconBean);
                });
            }
            return customIconBeanMap;
        }
        return customIconBeanMap;
    }
    /**
     * 根据包名获取自定义图标
     * @param context c
     * @return r
     */
    public static CustomIconBean getCustomIcons(Context context, String pkgName) {
        Map<String, CustomIconBean> allCustomIcons = getAllCustomIcons(context);
        return allCustomIcons.get(pkgName);
    }

}
