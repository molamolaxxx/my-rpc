package com.mola.rpc.common.enums;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum SystemInvokerClazzInterfaceEnum {
    REVERSE_INVOKER_CLAZZ_NAME("com.mola.rpc.core.system.SystemConsumer$ReverseInvokerCaller");

    private static final Set<String> ALL = new HashSet<>();
    static {
        Arrays.stream(SystemInvokerClazzInterfaceEnum.values())
                .forEach(e -> ALL.add(e.name));
    }

    private String name;

    SystemInvokerClazzInterfaceEnum(String name) {
        this.name = name;
    }

    public static boolean has(String interfaceName) {
        return ALL.contains(interfaceName);
    }

    public boolean is(String interfaceName) {
        return this.name.equals(interfaceName);
    }
}
