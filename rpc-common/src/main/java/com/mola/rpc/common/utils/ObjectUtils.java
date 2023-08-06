package com.mola.rpc.common.utils;


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
        return JSONUtil.parseObject(JSONUtil.toJSONString(o), new TypeReference<Map<String, String>>() {
        });
    }

    public static <T> T parseMap(Map<String, String> map, Class<T> resultType) {
        return JSONUtil.parseObject(JSONUtil.toJSONString(map), resultType);
    }
}
