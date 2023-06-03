package com.mola.rpc.common.lifecycle;

import com.google.common.collect.Maps;
import com.mola.rpc.common.entity.RpcMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: consumer生命周期
 * @date : 2023-05-27 11:15
 **/
public class ConsumerLifeCycle implements ConsumerLifeCycleHandler {

    private static final Logger log = LoggerFactory.getLogger(ConsumerLifeCycle.class);

    private ConsumerLifeCycle(){}
    static class Singleton{
        private static ConsumerLifeCycle consumerLifeCycle = new ConsumerLifeCycle();
    }

    public static ConsumerLifeCycle fetch(){
        return Singleton.consumerLifeCycle;
    }

    private Map<String, ConsumerLifeCycleHandler> listeners = Maps.newHashMap();

    public void addListener(ConsumerLifeCycleHandler listener) {
        Assert.notNull(listener, "lo is null");
        Assert.notNull(listener.getName(), "ConsumerLifeCycleListener's name is null");
        if (listeners.containsKey(listener.getName())) {
            log.warn("ConsumerLifeCycleListener's name is duplicate, ignore " + listener.getName());
            return;
        }
        listeners.put(listener.getName(), listener);
    }

    public void removeListener(String name) {
        Assert.notNull(name, "ConsumerLifeCycleListener's name is null");
        if (!listeners.containsKey(name)) {
            throw new RuntimeException("can not find ConsumerLifeCycleListener by name " + name);
        }
        listeners.remove(name);
    }

    @Override
    public void afterAddressChange(RpcMetaData consumerMetaData) {
        for (ConsumerLifeCycleHandler listener : listeners.values()) {
            listener.afterAddressChange(consumerMetaData);
        }
    }

    @Override
    public void afterInitialize(RpcMetaData consumerMetaData) {
        for (ConsumerLifeCycleHandler listener : listeners.values()) {
            listener.afterInitialize(consumerMetaData);
        }
    }

    @Override
    public String getName() {
        return "publisher";
    }
}
