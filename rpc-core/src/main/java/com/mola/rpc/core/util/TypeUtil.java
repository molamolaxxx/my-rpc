package com.mola.rpc.core.util;

import com.mola.rpc.common.constants.BaseTypeConstants;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 类型工具类
 * @date : 2023-01-21 17:39
 **/
public class TypeUtil {

    /**
     * 获取基础类型
     * @param paramTypeClazzName
     * @return
     */
    public static Class getBaseTypeClazz(String paramTypeClazzName) {
        switch (paramTypeClazzName) {
            case BaseTypeConstants.TYPE_INT: {
                return int.class;
            }
            case BaseTypeConstants.TYPE_BYTE: {
                return byte.class;
            }
            case BaseTypeConstants.TYPE_CHAR: {
                return char.class;
            }
            case BaseTypeConstants.TYPE_DOUBLE: {
                return double.class;
            }
            case BaseTypeConstants.TYPE_FLOAT: {
                return float.class;
            }
            case BaseTypeConstants.TYPE_LONG: {
                return long.class;
            }
            case BaseTypeConstants.TYPE_SHORT: {
                return short.class;
            }
            case BaseTypeConstants.TYPE_BOOLEAN: {
                return boolean.class;
            }
            default:{
                return null;
            }
        }
    }

    /**
     * 获取基础类型
     * @param paramTypeClazzName
     * @return
     */
    public static Object getBaseTypeDefaultObject(String paramTypeClazzName) {
        switch (paramTypeClazzName) {
            case BaseTypeConstants.TYPE_INT: {
                return new Integer(0);
            }
            case BaseTypeConstants.TYPE_BYTE: {
                return new Byte("0");
            }
            case BaseTypeConstants.TYPE_CHAR: {
                return new Character('0');
            }
            case BaseTypeConstants.TYPE_DOUBLE: {
                return new Double(0.0);
            }
            case BaseTypeConstants.TYPE_FLOAT: {
                return new Float(0.0);
            }
            case BaseTypeConstants.TYPE_LONG: {
                return new Long(0);
            }
            case BaseTypeConstants.TYPE_SHORT: {
                return new Short((short) 0);
            }
            case BaseTypeConstants.TYPE_BOOLEAN: {
                return new Boolean(false);
            }
            default:{
                return null;
            }
        }
    }
}
