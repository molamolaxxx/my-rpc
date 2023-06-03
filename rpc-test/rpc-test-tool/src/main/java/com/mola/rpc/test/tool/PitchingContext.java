package com.mola.rpc.test.tool;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2023-05-27 19:51
 **/
public class PitchingContext {

    public static final ThreadLocal<PitchingContext> ctx = new ThreadLocal<>();

    private Map<String, List<PitchInvokeContext>> contextMap = Maps.newHashMap();

    private boolean start;

    public static void start() {
        if (ctx.get() == null) {
            ctx.set(new PitchingContext());
        }
        ctx.get().contextMap.clear();
        ctx.get().start = true;
    }


    public boolean isStart() {
        return start;
    }

    public static void finish() {
        ctx.get().contextMap.clear();
        ctx.get().start = false;
    }

    static void pitching(Method method, Object[] params, Object result) {
        if (ctx.get() == null) {
            ctx.set(new PitchingContext());
        }
        String key = method.getDeclaringClass().getName() + ":" + method.getName();
        PitchingContext pitchingContext = ctx.get();
        pitchingContext.contextMap.putIfAbsent(key, Lists.newArrayList());
        pitchingContext.contextMap.get(key).add(new PitchInvokeContext(params, result));
    }

    public static List<PitchInvokeContext> fetchInvokeContext(String clazzName, String methodName) {
        String key = clazzName + ":" + methodName;
        return ctx.get().contextMap.get(key);
    }
}
