package com.mola.rpc.test.tool;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2023-05-27 18:02
 **/
@Component
public class RpcPitchingAdvisor extends AbstractPointcutAdvisor {

    @Override
    public Pointcut getPointcut() {
        return new StaticMethodMatcherPointcut() {
            @Override
            public boolean matches(Method method, Class<?> targetClass) {
                if (method.getDeclaringClass().getName().startsWith("com.mola")) {
                    return true;
                }
                return false;
            }
        };
    }

    @Override
    public Advice getAdvice() {
        return new RpcPitchingInterceptor();
    }
}
