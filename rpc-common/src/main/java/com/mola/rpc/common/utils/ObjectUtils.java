package com.mola.rpc.common.utils;


import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import java.util.Map;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2023-02-27 21:02
 **/
public class ObjectUtils {

    public static Map<String, String> parseObject(Object o) {
        return JSONObject.parseObject(JSONObject.toJSONString(o), new TypeReference<Map<String, String>>() {
        });
    }

    public static <T> T parseMap(Map<String, String> map, Class<T> resultType) {
        return JSONObject.parseObject(JSONObject.toJSONString(map), resultType);
    }
}
