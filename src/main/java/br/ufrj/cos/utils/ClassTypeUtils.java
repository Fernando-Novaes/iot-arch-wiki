package br.ufrj.cos.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public class ClassTypeUtils {

    public static Class<?> getListElementType(List<?> list) {
        if (list.getClass().getGenericSuperclass() instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) list.getClass().getGenericSuperclass();
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (actualTypeArguments != null && actualTypeArguments.length > 0) {
                return (Class<?>) actualTypeArguments[0];
            }
        }
        return Object.class; // Default to Object if type cannot be determined
    }

}
