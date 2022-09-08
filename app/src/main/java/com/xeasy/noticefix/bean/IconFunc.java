package com.xeasy.noticefix.bean;

import com.xeasy.noticefix.R;

/**
 * 转换图标的方式
 */
public enum IconFunc {

    CUSTOM_FIX(0, R.string.icon_func_custom),
    LIB_FIX(1, R.string.icon_func_lib),
    AUTO_FIX(2, R.string.icon_func_auto);

    IconFunc(Integer funcId, Integer funcDesc){
        this.funcId = funcId;
        this.funcDesc = funcDesc;
    }

    public final Integer funcId;
    public final Integer funcDesc;

    public static Integer getDescById(int id) {
        IconFunc[] values = IconFunc.values();
        for (IconFunc value : values) {
            if ( value.funcId == id ) {
                return value.funcDesc;
            }
        }
        return null;
    }

}
