package com.mola.rpc.common.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RpcProvider {

    String version() default "1.0.0";

    String group() default "default";

    String description() default "";

    Class<?> interfaceClazz();

    boolean inFiber() default false;

    boolean reverseMode() default false;

    String[] reverseModeConsumerAddress() default {};
}
