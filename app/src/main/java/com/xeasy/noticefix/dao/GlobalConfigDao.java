package com.xeasy.noticefix.dao;

import static com.xeasy.noticefix.constant.MyConstant.GLOBAL_CONFIG_FILE;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.xeasy.noticefix.utils.AppNotification;

import de.robv.android.xposed.XSharedPreferences;

public class GlobalConfigDao {
    /**
     * 是否已被systemui读
     */
    public boolean read = false;
    /**
     * 测试模式 开启使用图标库
     */
    public boolean debugMode = false;
    /**
     * 始终处理推送通知
     */
    public boolean alwaysHandleProxyNotice = true;
    /**
     * 是否跳过灰度
     */
    public boolean skipGrayscale = false;
    /**
     * 解除原生安卓色彩
     */
    public boolean showColoredIcons = true;
    /**
     * 展开通知
     */
    public boolean expandAllNotice = false;
    /**
     * 解除原生安卓色彩
     */
    public boolean customIconHelper = true;


    public static GlobalConfigDao globalConfigDao = new GlobalConfigDao();

    public static final String FILE_NAME = GLOBAL_CONFIG_FILE;

    public static final Gson gson = new Gson();

    public static void initGlobalConfig(Context context) {
        SharedPreferences sharedPreferences;
        if ( context.getPackageName().equals("com.xeasy.noticefix") ) {
            sharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        } else {
            XSharedPreferences xSharedPreferences = new XSharedPreferences("com.xeasy.noticefix", FILE_NAME);
            xSharedPreferences.makeWorldReadable();
            sharedPreferences = xSharedPreferences;
        }
        String string = sharedPreferences.getString(FILE_NAME, null);
//        Toast.makeText(context, "initGlobalConfig ==>> " + string, Toast.LENGTH_SHORT).show();
        // 如果没有 生成默认配置
        if ( string != null) {
            globalConfigDao = gson.fromJson(string, GlobalConfigDao.class);
        }
    }


    public static void saveConfig(Context context, GlobalConfigDao globalConfigDao) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(FILE_NAME, gson.toJson(globalConfigDao)).apply();
        // 发送刷新通知
        AppNotification.sendFlashNoticeMessage(context, null);
    }

}
