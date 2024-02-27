package com.xeasy.noticefix.bean;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public class AppInfo4View {
    public String AppName;
    public String AppPkg;
    public Drawable AppIcon;
    public String version;
    public String versionAndType;
    public boolean isSystem;
    public Bitmap libIcon;
    public String libIconColor;
    public Bitmap customIcon;
    public boolean notHandle;
    /**
     * 是否 展开此app的 heads up
     */
    public boolean expandHeadsUp = false;
    /**
     * 是否 展开此app的 status bar
     */
    public boolean expandStatusBar = false;
    public Bitmap lastIcon;
}
