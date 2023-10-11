package com.mola.rpc.common.context;

import java.util.List;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 客户端运行时参数
 * @date : 2023-05-14 17:07
 **/
public class InvokeContext {

    private static final ThreadLocal<InvokeContext> ctxHolder = new ThreadLocal<>();

    /**
     * 指定provider地址信息，ip:port
     */
    private List<String> addressList;

    /**
     * 指定provider路由标示，不为空时，永远只会路由到相同routeTag的服务
     * beta:目前只对反向代理生效，正向没有tag路由功能，且不支持注解接入
     */
    private String routeTag;

    public List<String> getAddressList() {
        return addressList;
    }

    public String getRouteTag() {
        return routeTag;
    }

    /*
         static method
         */
    public static InvokeContext fetch() {
        InvokeContext ctx = ctxHolder.get();
        if (ctx == null) {
            ctxHolder.set((ctx = new InvokeContext()));
        }
        return ctx;
    }

    public static void clear() {
        ctxHolder.remove();
    }

    public static void appointProviderAddressList(List<String> addressList) {
        InvokeContext context = fetch();
        context.addressList = addressList;
    }

    public static void routeTag(String routeTag) {
        InvokeContext context = fetch();
        context.routeTag = routeTag;
    }
}
