package com.xeasy.noticefix.bean;

public class AppIconConfig {
    public int id;
    public String label;//应用名称
    public String packageName;//应用包名
    public String libIcon;//内置库icon base64格式
    public String libIconColor;//内置库icon 背景颜色
    public String customIcon;//自定义icon base64格式
    public String customIconColor;//自定义icon 背景颜色
    public String funcOrder;// 处理优先级数组格式 例: [2,1,3,4]
}
