package com.xeasy.noticefix.constant;

import com.xeasy.noticefix.R;

public class MyConstant {
    /**
     * 保存icon_func配置的文件名
     */
    public static final String ICON_FUNC_ORDER_CONFIG = "icon_func_order_config";
    /**
     * 保存 icon_lib 的文件名
     */
    public static final String LIBRARY_ICON_FILE = "library_icon_file";
    /**
     * 保存 customIcon 配置的文件名
     */
    public static final String CUSTOM_ICON_FILE = "custom_icon_file";
    /**
     * 保存 GlobalConfig 配置的文件名
     */
    public static final String GLOBAL_CONFIG_FILE = "global_config_file";

    public enum AppType {
        SYSTEM(1, R.string.app_filter_system),
        USER(2, R.string.app_filter_user),
        LIB_ICON(3, R.string.app_filter_icon_matched),
        CUSTOM_ICON(4, R.string.app_filter_custom_icon),
        WHITE_LIST(5, R.string.app_filter_white_list);

        AppType(Integer funcId, Integer typeName){
            this.typeId = funcId;
            this.typeName = typeName;
        }

        public final Integer typeId;
        public final Integer typeName;

        public static Integer getDescById(int id) {
            AppType[] values = AppType.values();
            for (AppType value : values) {
                if ( value.typeId == id ) {
                    return value.typeName;
                }
            }
            return null;
        }
    }

}
