package com.xeasy.noticefix.bean;

public class IconLibBean implements Comparable<IconLibBean> {
    public String appName;
    public String packageName;
    public String iconBitmap;
    public String iconColor;
    public String contributorName;
    @Deprecated
    public Boolean isEnabled;
    @Deprecated
    public Boolean isEnabledAll;

    @Override
    public int hashCode() {
        return appName.hashCode();
    }

    @Override
    public int compareTo(IconLibBean o) {
        return this.hashCode() - o.hashCode();
    }

}
