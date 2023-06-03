package com.mola.rpc.common.context;

import com.google.common.collect.Lists;

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
     * 指定服务端地址信息，ip:port
     */
    private List<String> addressList;

    public List<String> getAddressList() {
        return addressList;
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
        InvokeContext invokeContext = ctxHolder.get();
        if (invokeContext == null) {
            return;
        }
        invokeContext.addressList = null;
    }

    public static void appointProviderAddressList(List<String> addressList) {
        InvokeContext context = fetch();
        context.addressList = addressList;
    }

    public static void appointProviderAddress(String address) {
        InvokeContext context = fetch();
        context.addressList = Lists.newArrayList(address);
    }
}
