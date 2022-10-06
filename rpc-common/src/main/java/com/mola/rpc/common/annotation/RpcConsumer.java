package com.mola.rpc.common.annotation;

import com.mola.rpc.common.constants.LoadBalanceConstants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcConsumer {

    String version() default "1.0.0";

    String group() default "default";

    long timeout() default 10000L;

    String loadBalanceStrategy() default LoadBalanceConstants.LOAD_BALANCE_RANDOM_STRATEGY;

    String[] asyncMethods() default {};
}
