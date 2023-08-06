package com.mola.rpc.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 客户端运行时参数
 * @date : 2023-08-06
 **/
public class JSONUtil {

    public static String toJSONString(Object object) {
        return JSONObject.toJSONString(object);
    }

    public static String toJSONString(Object object, boolean prettyFormat) {
        return JSONObject.toJSONString(object, prettyFormat);
    }

    public static <T> T parseObject(String text, TypeReference<T> type, Feature... features) {
        return JSONObject.parseObject(text, type, features);
    }


    public static <T> T parseObject(String text, Class<T> clazz) {
        return JSONObject.parseObject(text, clazz, new Feature[0]);
    }
}
