package com.mola.rpc.core.proto;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 获取bean的接口
 * @date : 2022-09-13 00:25
 **/
public interface ObjectFetcher {

    /**
     * 获取对象
     * @param objectName
     * @return
     */
    Object getObject(String objectName);
}
