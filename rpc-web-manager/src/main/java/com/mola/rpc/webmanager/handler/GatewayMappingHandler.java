package com.mola.rpc.webmanager.handler;

import com.mola.rpc.data.config.spring.RpcProviderDataInitBean;
import com.mola.rpc.webmanager.config.RpcWebConfigurationProperties;
import com.mola.rpc.webmanager.service.GatewayMappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import com.mola.rpc.common.utils.AssertUtil;

import javax.annotation.Resource;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 应用启动时读取所有网关信息到缓存
 * @date : 2023-01-24 17:57
 **/
@Component
public class GatewayMappingHandler implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(GatewayMappingHandler.class);

    private ThreadPoolExecutor threadPoolExecutor;

    private LinkedBlockingDeque<Runnable> linkedBlockingDeque;

    @Resource
    private RpcProviderDataInitBean rpcProviderDataInitBean;

    @Resource
    private RpcWebConfigurationProperties rpcWebConfigurationProperties;

    @Resource
    private GatewayMappingService gatewayMappingService;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.linkedBlockingDeque = new LinkedBlockingDeque<>(100);
        this.threadPoolExecutor = new ThreadPoolExecutor(1,1
                ,200, TimeUnit.MILLISECONDS, linkedBlockingDeque, new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("refresh-provider-thread-%d", this.threadIndex.incrementAndGet()));
            }
        });
        // 启动db加载
        loadGatewayMappingToCache();
    }

    /**
     * 异步执行任务
     */
    public void loadGatewayMappingToCache() {
        if (Boolean.FALSE.equals(rpcWebConfigurationProperties.getRefreshGatewayMapping())) {
            log.warn("server will not refresh gateway mapping");
            return;
        }
        AssertUtil.isTrue(linkedBlockingDeque.size() < 5, "队列处理任务过多，请稍后重试");
        threadPoolExecutor.submit(() -> {
            gatewayMappingService.loadGatewayMappingToCache();
        });
    }
}
