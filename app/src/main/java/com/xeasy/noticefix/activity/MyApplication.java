package com.xeasy.noticefix.activity;

import android.app.Application;

import com.xeasy.noticefix.bean.AppInfo4View;
import com.xeasy.noticefix.bean.IconLibBean;
import com.xeasy.noticefix.dao.AppUtil;
import com.xeasy.noticefix.dao.GlobalConfigDao;
import com.xeasy.noticefix.dao.IconLibDao;

import java.util.List;
import java.util.Map;
import java.util.concurrent.FutureTask;

public class MyApplication extends Application {

    public static FutureTask<Map<String,IconLibBean>> cacheTask4IconLibBean;
    public static FutureTask<List<AppInfo4View>> cacheTask4AppInfo4View;

    @Override
    public void onCreate() {
        super.onCreate();
        // 缓存本机app情况
        IconLibDao.getIconLib(this, true);
//        cacheTask4IconLibBean = IconLibDao.cacheIconLibMap(this);
        GlobalConfigDao.initGlobalConfig(this);
        cacheTask4AppInfo4View = AppUtil.cacheInfoMap(this);
    }
}
