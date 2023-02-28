package com.mola.rpc.common.entity;

import com.mola.rpc.common.constants.BaseTypeConstants;

import java.util.Map;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 泛化调用参数
 * 支持类型：
 * 1、map（常用）
 * 2、对象
 * 3、基础类型（基础类型数组不支持，请误在provider中定义，会报找不到method）
 * @date : 2023-01-21 16:37
 **/
public class GenericParam {

    /**
     * 参数对象
     */
    private Object obj;

    /**
     * 参数类型（包括基础类型，如int）
     */
    private String paramTypeClazzName;

    private GenericParam(){
    }

    public Object getObj() {
        return obj;
    }

    public String getParamTypeClazzName() {
        return paramTypeClazzName;
    }

    /**
     * 创建map的包装
     * @param map
     * @param paramTypeClazzName
     * @return
     */
    public static GenericParam ofMap(Map map, String paramTypeClazzName) {
        GenericParam genericParam = new GenericParam();
        genericParam.obj = map;
        genericParam.paramTypeClazzName = paramTypeClazzName;
        return genericParam;
    }

    /**
     * 创建obj的包装
     * @param obj
     * @return
     */
    public static GenericParam ofObj(Object obj) {
        GenericParam genericParam = new GenericParam();
        genericParam.obj = obj;
        genericParam.paramTypeClazzName = obj.getClass().getName();
        return genericParam;
    }

    public static GenericParam ofObj(Object obj, String paramTypeClazzName) {
        GenericParam genericParam = new GenericParam();
        genericParam.obj = obj;
        genericParam.paramTypeClazzName = paramTypeClazzName;
        return genericParam;
    }

    /*
    * 基础类型
    */
    public static GenericParam ofInt(int param) {
        GenericParam genericParam = new GenericParam();
        genericParam.obj = param;
        genericParam.paramTypeClazzName = BaseTypeConstants.TYPE_INT;
        return genericParam;
    }

    public static GenericParam ofBoolean(boolean param) {
        GenericParam genericParam = new GenericParam();
        genericParam.obj = param;
        genericParam.paramTypeClazzName = BaseTypeConstants.TYPE_BOOLEAN;
        return genericParam;
    }

    public static GenericParam ofByte(byte param) {
        GenericParam genericParam = new GenericParam();
        genericParam.obj = param;
        genericParam.paramTypeClazzName = BaseTypeConstants.TYPE_BYTE;
        return genericParam;
    }

    public static GenericParam ofChar(char param) {
        GenericParam genericParam = new GenericParam();
        genericParam.obj = param;
        genericParam.paramTypeClazzName = BaseTypeConstants.TYPE_CHAR;
        return genericParam;
    }

    public static GenericParam ofDouble(double param) {
        GenericParam genericParam = new GenericParam();
        genericParam.obj = param;
        genericParam.paramTypeClazzName = BaseTypeConstants.TYPE_DOUBLE;
        return genericParam;
    }

    public static GenericParam ofFloat(float param) {
        GenericParam genericParam = new GenericParam();
        genericParam.obj = param;
        genericParam.paramTypeClazzName = BaseTypeConstants.TYPE_FLOAT;
        return genericParam;
    }

    public static GenericParam ofShort(short param) {
        GenericParam genericParam = new GenericParam();
        genericParam.obj = param;
        genericParam.paramTypeClazzName = BaseTypeConstants.TYPE_SHORT;
        return genericParam;
    }

    public static GenericParam ofLong(long param) {
        GenericParam genericParam = new GenericParam();
        genericParam.obj = param;
        genericParam.paramTypeClazzName = BaseTypeConstants.TYPE_LONG;
        return genericParam;
    }
}
