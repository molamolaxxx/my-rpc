package com.mola.rpc.test.tool;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2023-05-27 22:18
 **/
class RpcPitchingInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        Object result = invocation.proceed();
        PitchingContext pitchingContext = PitchingContext.ctx.get();
        if (pitchingContext != null && pitchingContext.isStart()) {
            PitchingContext.pitching(method, invocation.getArguments(), result);
        }
        return result;
    }
}
