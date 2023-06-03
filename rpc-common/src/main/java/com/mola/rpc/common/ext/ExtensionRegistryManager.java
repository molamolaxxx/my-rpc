package com.mola.rpc.common.ext;

import com.google.common.collect.Lists;
import com.mola.rpc.common.interceptor.RpcInterceptor;
import org.springframework.util.Assert;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 扩展点注册中心
 * @date : 2023-06-03 12:47
 **/
public class ExtensionRegistryManager {

    /**
     * 反向代理服务注册拦截器
     */
    private List<RpcInterceptor> rpcInterceptors = Lists.newArrayList();

    /**
     * 添加拦截器
     * @param interceptor
     */
    public void addInterceptor(RpcInterceptor interceptor) {
        Assert.notNull(interceptor, "interceptor is null");
        this.rpcInterceptors.add(interceptor);
    }

    public <T extends RpcInterceptor> List<T> getInterceptors(Class<T> clazz) {
        Assert.notNull(clazz, "clazz is null");
        List<T> interceptors = rpcInterceptors.stream()
                .filter(rpcInterceptor -> clazz.isAssignableFrom(rpcInterceptor.getClass()))
                .map(rpcInterceptor -> (T) rpcInterceptor)
                .sorted(Comparator.comparingInt(rpcInterceptor -> rpcInterceptor.priority()))
                .collect(Collectors.toList());
        return interceptors;
    }
}
