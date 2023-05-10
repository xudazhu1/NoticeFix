package com.xeasy.noticefix.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflexUtil {


    public static Object getField4Obj(Object object, String fieldName) {
        Object field4Obj = getField4Obj(object.getClass(), object,  fieldName);
        if ( field4Obj == null ) {
            field4Obj = getField4Obj(object.getClass().getSuperclass(), object, fieldName);
        }
        return field4Obj;
    }
    public static Object getField4Obj(Class<?> clazz, Object object, String fieldName) {
        try {
            Field declaredField = clazz.getDeclaredField(fieldName);
            declaredField.setAccessible(true);
            return declaredField.get(object);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object getField4ObjByClass(Class<?> clazz, Object object, Class<?> fieldClass) {
        try {
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                declaredField.setAccessible(true);
                if ( declaredField.getType() == fieldClass ) {
                    return declaredField.get(object);
                }
            }
            return null;
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

    public static Object runMethod(Object object, String methodName, Object[] params, Class<?>... paramTypes) {

        try {
            if (null == params || params.length == 0) {
                Method declaredMethod = object.getClass().getDeclaredMethod(methodName);
                declaredMethod.setAccessible(true);
                return declaredMethod.invoke(object);
            }

            Method method = object.getClass().getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            return method.invoke(object, params);
        } catch (Exception e) {
            return e;
        }
    }

    public static Object runStaticMethod(Class<?> clazz, String methodName, Object[] params, Class<?>... paramTypes) throws Exception {


        try {
            if (null == params || params.length == 0) {
                Method declaredMethod = clazz.getDeclaredMethod(methodName);
                declaredMethod.setAccessible(true);
                return declaredMethod.invoke(null);
            }

            Method method = clazz.getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            return method.invoke(null, params);
        } catch (Exception e) {
            return e;
        }
    }

}
