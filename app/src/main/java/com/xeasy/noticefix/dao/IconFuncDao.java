package com.xeasy.noticefix.dao;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.xeasy.noticefix.bean.IconFunc;
import com.xeasy.noticefix.constant.MyConstant;
import com.xeasy.noticefix.utils.AppNotification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.robv.android.xposed.XSharedPreferences;

public class IconFuncDao {

    static Gson gson = new Gson();
    static List<IconFuncStatus> iconFuncStatuses;

    public static void saveSwap(Context context, int from, int to) {
        System.out.println(iconFuncStatuses);
        // 交换俩对象的order值 然后保存
        IconFuncStatus iconFuncStatusFrom = iconFuncStatuses.get(from);
        IconFuncStatus iconFuncStatusTo = iconFuncStatuses.get(to);
        int old = iconFuncStatusTo.order;
        iconFuncStatusTo.order = iconFuncStatusFrom.order;
        iconFuncStatusFrom.order = old;

        SharedPreferences sharedPreferences = context.getSharedPreferences(MyConstant.ICON_FUNC_ORDER_CONFIG, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(iconFuncStatusFrom.iconFuncId+"", gson.toJson(iconFuncStatusFrom));
        edit.putString(iconFuncStatusTo.iconFuncId+"", gson.toJson(iconFuncStatusTo));
        // 发送刷新通知
        AppNotification.sendFlashNoticeMessage(context, null);
        boolean commit = edit.commit();
        if ( commit ) {
            Log.d(IconFuncDao.class.getName(), "保存配置信息成功");
//            Toast.makeText(context, "保存配置信息成功", Toast.LENGTH_SHORT).show();
        }
    }
    public static void save(Context context, IconFuncStatus iconFuncStatus) {
        // 交换俩对象的order值 然后保存
        SharedPreferences sharedPreferences = context.getSharedPreferences(MyConstant.ICON_FUNC_ORDER_CONFIG, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(iconFuncStatus.iconFuncId+"", gson.toJson(iconFuncStatus));
        // 发送刷新通知
        AppNotification.sendFlashNoticeMessage(context, null);
        boolean commit = edit.commit();
        if ( commit ) {
            Log.d(IconFuncDao.class.getName(), "保存配置信息成功");
//            Toast.makeText(context, "保存配置信息成功", Toast.LENGTH_SHORT).show();
        }
    }

    public static class IconFuncStatus implements Comparable<IconFuncStatus> {
        public int iconFuncId;
        /**
         * 是否激活
         */
        public boolean active;
        /**
         * 排序
         */
        public int order;

        @Override
        public int compareTo(IconFuncStatus o) {
            return order - o.order;
        }
    }

    public static List<IconFuncStatus> getIconFunc(Context context) {
        if ( iconFuncStatuses == null ) {
//            SharedPreferences sharedPreferences = context.getSharedPreferences(MyConstant.ICON_FUNC_ORDER_CONFIG, Context.MODE_PRIVATE);
            SharedPreferences sharedPreferences;
            if ( context.getPackageName().equals("com.xeasy.noticefix") ) {
                sharedPreferences = context.getSharedPreferences(MyConstant.ICON_FUNC_ORDER_CONFIG, Context.MODE_PRIVATE);
            } else {
                sharedPreferences = new XSharedPreferences("com.xeasy.noticefix", MyConstant.ICON_FUNC_ORDER_CONFIG);
            }
            Map<String, ?> all = sharedPreferences.getAll();
            // 准备返回的数据
            iconFuncStatuses = new ArrayList<>();
            if ( all == null || all.size() == 0) {
                // 没有过 初始化
                SharedPreferences.Editor edit = sharedPreferences.edit();
                IconFunc[] values = IconFunc.values();
                for (IconFunc value : values) {
                    IconFuncStatus iconFuncStatus = new IconFuncStatus();
                    iconFuncStatus.iconFuncId = value.funcId;
                    iconFuncStatus.active = true;
                    // 默认顺序
                    iconFuncStatus.order = value.funcId;
                    // 持久化
                    edit.putString(value.funcId + "", gson.toJson(iconFuncStatus));
                    // 发送刷新通知
                    AppNotification.sendFlashNoticeMessage(context, null);
                    iconFuncStatuses.add(iconFuncStatus);
                }
                boolean commit = edit.commit();
                if (! commit ) {
                    Toast.makeText(context, "初始化配置信息错误", Toast.LENGTH_SHORT).show();
                }
            } else {
                // 获取开关状态和排序状态
                all.forEach((key, value) -> iconFuncStatuses.add(gson.fromJson(value.toString(), IconFuncStatus.class)));
//                Toast.makeText(context, "读取配置信息成功", Toast.LENGTH_SHORT).show();
            }
        }
        Collections.sort(iconFuncStatuses);
        return iconFuncStatuses;
    }

}
