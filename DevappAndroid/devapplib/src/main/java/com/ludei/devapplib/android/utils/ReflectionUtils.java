package com.ludei.devapplib.android.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by imanolmartin on 21/01/16.
 */
public class ReflectionUtils {

    public static <T> T getStaticField(String className, String fieldName) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        T result = null;

        Class<?> clazz = Class.forName(className);
        Field f = clazz.getDeclaredField(fieldName);
        f.setAccessible(true);
        if (f.isAccessible()){
            result = (T) f.get(null);
        }

        return result;
    }

    public static <T> T invokeStaticMethod(String className, String methodName, Class[] argumentsTypes, Object... arguments) throws NoSuchMethodException, ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        Class<?> clazz = Class.forName(className);
        Method method = clazz.getMethod(methodName, argumentsTypes);
        return (T)method.invoke(null, arguments);
    }
}
