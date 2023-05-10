package com.xeasy.noticefix.bean;

public class CustomIconBean {
    /**
     * 包名 (主键)
     */
    public String pkgName;
    /**
     * 应用名称
     */
    public String label;
    /**
     * 图标
     */
    public String iconBase64;
    /**
     * 图标背景颜色 (暂时没有使用)
     */
    public String iconColor;
    /**
     * 是否不处理此app
     */
    public boolean noHandle = false;
    /**
     * 是否 展开此app的 heads up
     */
    public boolean expandHeadsUp = false;
    /**
     * 是否 展开此app的 status bar
     */
    public boolean expandStatusBar = false;
}
