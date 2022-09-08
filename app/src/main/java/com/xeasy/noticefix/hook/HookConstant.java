package com.xeasy.noticefix.hook;

import com.google.gson.Gson;
import com.xeasy.noticefix.bean.CustomIconBean;
import com.xeasy.noticefix.bean.IconLibBean;
import com.xeasy.noticefix.dao.GlobalConfigDao;
import com.xeasy.noticefix.dao.IconFuncDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HookConstant {
    public static Gson gson = new Gson();
    public static GlobalConfigDao globalConfigDao = new GlobalConfigDao();
    public static List<IconFuncDao.IconFuncStatus> iconFuncStatuses = new ArrayList<>();
    public static Map<String, IconLibBean> iconLibBeanMap = new HashMap<>();
    public static Map<String, CustomIconBean> customIconBeanMap = new HashMap<>();
}
