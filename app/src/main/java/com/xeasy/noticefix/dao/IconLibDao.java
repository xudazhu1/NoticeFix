package com.xeasy.noticefix.dao;

import static com.xeasy.noticefix.constant.MyConstant.LIBRARY_ICON_FILE;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xeasy.noticefix.R;
import com.xeasy.noticefix.bean.AppInfo4View;
import com.xeasy.noticefix.bean.IconLibBean;
import com.xeasy.noticefix.utils.ImageTools;
import com.xeasy.noticefix.utils.TaskUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class IconLibDao {

    private static final Gson gson = new Gson();
//    public static HashSet<IconLibBean> IconLibBeans = new HashSet<>();
    public static Map<String, IconLibBean> libBeanHashMap = new HashMap<>();
    private static final String FILE_NAME = LIBRARY_ICON_FILE;

    
    public static void save(Context context, IconLibBean iconLibBean) {
        // 交换俩对象的order值 然后保存
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(iconLibBean.packageName, gson.toJson(iconLibBean));
        boolean commit = edit.commit();
        if ( commit ) {
            Log.d(IconLibDao.class.getName(), "保存配置信息成功");
            // 判断 IconLibBeans里是否有该缓存 没有就添加
//            IconLibBeans.add(iconLibBean);
            libBeanHashMap.put(iconLibBean.packageName, iconLibBean);
            AppInfo4View app4ViewByPackageName = AppUtil.getApp4ViewByPackageName(context, iconLibBean.packageName);
            if ( app4ViewByPackageName != null ) {
                app4ViewByPackageName.libIcon = ImageTools.base64ToBitmap(iconLibBean.iconBitmap);
            }
//            Toast.makeText(context, "保存配置信息成功", Toast.LENGTH_SHORT).show();
        }
    }

    public static IconLibBean getIconLibBean(Context context, String packageName) {
        IconLibBean iconLibBean = libBeanHashMap.get(packageName);
        if ( iconLibBean != null ) {
            return iconLibBean;
        }
        // 没有就从本地取
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        String string = sharedPreferences.getString(packageName, null);
        if ( string != null ) {
            iconLibBean = gson.fromJson(string, IconLibBean.class);
            save(context, iconLibBean);
            return iconLibBean;
        }
        return null;
    }

    public static FutureTask<Map<String,IconLibBean>> cacheTask = null;

    public static FutureTask<Map<String,IconLibBean>> cacheIconLibMap(Context context) {
        // 开个线程准备app列表缓存 以解决首次搜索慢的问题 处理
        cacheTask = TaskUtils.createTask(() -> getIconLib(context, true));
        return cacheTask;
    }

    public static Map<String, IconLibBean> getIconLib(Context context, boolean refresh) {
        // 没有 创建
        if ( libBeanHashMap.isEmpty() || refresh) {
            // 准备返回的数据
//            IconLibBeans = new ArrayList<>();
            // 先判断 app数据里有没有
            SharedPreferences sharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
            Map<String, ?> all = sharedPreferences.getAll();
            // 没有过 初始化
            if ( all == null || all.size() == 0) {
                if ( GlobalConfigDao.globalConfigDao.debugMode ) {
                    // 拿到本地仨文件 assets
                    readAndInitIconLib(context, R.raw.icon_lib);
                    readAndInitIconLib(context, R.raw.icon_lib_a);
                    readAndInitIconLib(context, R.raw.icon_lib_b);
                }
            } else {
                // 读取数据返回出去
                all.forEach((key, value) -> libBeanHashMap.put(key, gson.fromJson(value.toString(), IconLibBean.class)));
//                Toast.makeText(context, "读取配置信息成功", Toast.LENGTH_SHORT).show();
            }
        }
        return libBeanHashMap;
    }

    private static void readAndInitIconLib(Context context, int rawId) {
        InputStream inputStream = context.getResources().openRawResource(rawId);
        readAndInitIconLib(context, inputStream);
    }

    public static HashSet<IconLibBean> readAndInitIconLib(Context context, InputStream inputStream) {
        AtomicInteger addCount = new AtomicInteger();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder results = new StringBuilder();
        bufferedReader.lines().forEach(results::append);
        Type type = new TypeToken<HashSet<IconLibBean>>(){}.getType();
        HashSet<IconLibBean> iconLibBeanList = gson.fromJson(results.toString(), type);
        // 开线程保存到app数据
        iconLibBeanList.forEach((iconLibBean) -> {
            if ( getIconLibBean(context, iconLibBean.packageName) == null ) {
                addCount.getAndIncrement();
            }
            save(context, iconLibBean);
        });
//        TaskUtils.createTask(()->saveIconLibBeanList(context, iconLibBeanList));
        // 刷新成功, 此次新增图标(个):
        ((Activity)context).runOnUiThread(() ->
                Toast.makeText(context, context.getString(R.string.icon_lib_refresh_tips) + addCount.get(), Toast.LENGTH_SHORT).show());
        AppUtil.countRefresh();
        return iconLibBeanList;
    }

    public static boolean saveIconLibBeanList(Context context, HashSet<IconLibBean> iconLibBeanList) {
        iconLibBeanList.forEach((iconLibBean -> save(context, iconLibBean)));
        return true;
    }

    public static boolean refreshIconLibOnLine(Context context, ObjectAnimator objectAnimator) {
        AtomicInteger addCount = new AtomicInteger();
        try {
            onlineLibUrl.forEach((url)->{
                String dataNoAsync = getDataNoAsync(url);
                Type type = new TypeToken<HashSet<IconLibBean>>(){}.getType();
                HashSet<IconLibBean> iconLibBeanList = gson.fromJson(dataNoAsync, type);
                // 开线程保存到app数据
                if ( iconLibBeanList != null ) {
                    iconLibBeanList.forEach((iconLibBean) -> {
                        if ( getIconLibBean(context, iconLibBean.packageName) == null ) {
                            addCount.getAndIncrement();
                        }
                        save(context, iconLibBean);
                    });
                }
            });
            ((Activity)context).runOnUiThread(() -> Toast.makeText(context, context.getString(R.string.icon_lib_refresh_tips) + addCount.get(), Toast.LENGTH_SHORT).show());
            AppUtil.countRefresh();
            objectAnimator.pause();
            return true;
        } catch ( Exception e) {
            e.printStackTrace();
            ((Activity)context).runOnUiThread(() -> Toast.makeText(context, context.getString(R.string.icon_lib_refresh_tips) + addCount.get(), Toast.LENGTH_SHORT).show());
            AppUtil.countRefresh();
            objectAnimator.pause();
            return false;
        }
    }

    private static final List<String> onlineLibUrl = new ArrayList<String>(){{
       add("https://x-easy.cn/noticefix/icon_lib");
       add("https://x-easy.cn/noticefix/icon_lib_a");
       add("https://x-easy.cn/noticefix/icon_lib_b");
    }};

    private static String getDataNoAsync(String url) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        try {
            Response execute = call.execute();
//            String string = body.string();
            return Objects.requireNonNull(execute.body()).string();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
