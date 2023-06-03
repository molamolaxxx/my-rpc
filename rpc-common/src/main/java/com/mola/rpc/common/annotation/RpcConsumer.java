package com.mola.rpc.common.annotation;

import com.mola.rpc.common.constants.LoadBalanceConstants;

import javax.annotation.Resource;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Resource
public @interface RpcConsumer {

    String version() default "1.0.0";

    String group() default "default";

    long timeout() default 10000L;

    String loadBalanceStrategy() default LoadBalanceConstants.RANDOM_STRATEGY;

    String[] asyncMethods() default {};

    /**
     * 反转模式
     * @return
     */
    boolean reverseMode() default false;

    /**
     * 指定调用地址，优先级最高
     * @return
     */
    String[] appointedAddress() default {};
}
