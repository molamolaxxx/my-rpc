package com.mola.rpc.core.proxy;

import com.mola.rpc.common.entity.GenericParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.lang.reflect.Method;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 泛化调用代理
 * @date : 2022-07-30 21:44
 **/
public class GenericRpcProxyInvokeHandler extends RpcProxyInvokeHandler {

    private static final Logger log = LoggerFactory.getLogger(GenericRpcProxyInvokeHandler.class);

    /**
     * 泛化调用真实的接口名
     */
    private String actualInterfaceClazzName;

    /**
     * 组装InvokeMethodreturn beanName;
     * @return
     */
    @Override
    protected InvokeMethod assemblyInvokeMethod(Method method, Object[] args) {
        Assert.isTrue(args[0] instanceof String, "generic call params[0] must be String!");
        Assert.hasText(actualInterfaceClazzName, "actualInterfaceClazzName can not be empty!");
        // 方法名称
        String methodName = (String) args[0];
        // 变量对象（基础类型和map）
        GenericParam[] genericParams = (GenericParam[]) args[1];
        String[] parameterTypesString = new String[genericParams.length];
        Object[] params = new Object[genericParams.length];
        for (int i = 0; i < genericParams.length; i++) {
            GenericParam genericParam = genericParams[i];
            parameterTypesString[i] = genericParam.getParamTypeClazzName();
            params[i] = genericParam.getObj();
        }
        // 构建invokeMethod
        InvokeMethod invokeMethod = new InvokeMethod(methodName,
                parameterTypesString,
                params,
                method.getReturnType().getName(),
                actualInterfaceClazzName);
        return invokeMethod;
    }

    @Override
    protected String getConsumerClazzName(Method method, Object[] args) {
        return actualInterfaceClazzName;
    }

    public void setActualInterfaceClazzName(String actualInterfaceClazzName) {
        this.actualInterfaceClazzName = actualInterfaceClazzName;
    }
}
