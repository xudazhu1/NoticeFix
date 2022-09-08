package com.xeasy.noticefix.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflexUtil {


    public static Object getField4Obj(Object object, String fieldName) {
        try {
            Field declaredField = object.getClass().getDeclaredField(fieldName);
            declaredField.setAccessible(true);
            return declaredField.get(object);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void setField4Obj(String fieldName, Object object, Object value) {
        try {
            Field declaredField = object.getClass().getDeclaredField(fieldName);
            declaredField.setAccessible(true);
            declaredField.set(object, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Object runMethod(Object object, String methodName, Object... params) throws Exception {


        if (null == params || params.length == 0) {
            return object.getClass().getMethod(methodName).invoke(object);
        }

        Method[] declaredMethods = object.getClass().getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            if (declaredMethod.getName().equals(methodName)) {
                Class<?>[] parameterTypes = declaredMethod.getParameterTypes();
                if (params.length == parameterTypes.length) {
                    boolean isIt = true;
                    for (int i = 0; i < params.length; i++) {
                        Object param = params[i];
                        if (null != param && param.getClass() != parameterTypes[i]) {
                            isIt = false;
                            break;
                        }
                    }
                    if (isIt) {
                        return declaredMethod.invoke(params);
                    }
                }
            }
        }
        return null;
    }

}
