package com.mola.rpc.webmanager.handler;

import com.mola.rpc.common.entity.RpcMetaData;
import com.mola.rpc.core.properties.RpcProperties;
import com.mola.rpc.data.config.manager.RpcDataManager;
import com.mola.rpc.data.config.spring.RpcProviderDataInitBean;
import com.mola.rpc.webmanager.config.RpcWebConfigurationProperties;
import com.mola.rpc.webmanager.entity.ProviderInfoEntity;
import com.mola.rpc.webmanager.entity.RpcWatcher;
import com.mola.rpc.webmanager.service.ProviderMetaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import com.mola.rpc.common.utils.AssertUtil;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 应用启动时读取/rpc/provider下所有索引信息入库
 * @date : 2023-01-24 17:57
 **/
@Component
public class ProviderRefreshAllHandler implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(ProviderRefreshAllHandler.class);

    /**
     * 业务线程池
     */
    private ThreadPoolExecutor providerRefreshThreadPool;

    private LinkedBlockingDeque<Runnable> linkedBlockingDeque;

    @Resource
    private ProviderMetaService providerMetaService;

    @Resource
    private RpcProviderDataInitBean rpcProviderDataInitBean;

    @Resource
    private RpcProperties rpcProperties;

    @Resource
    private RpcWebConfigurationProperties rpcWebConfigurationProperties;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.linkedBlockingDeque = new LinkedBlockingDeque<>(100);
        this.providerRefreshThreadPool = new ThreadPoolExecutor(1,1
                ,200, TimeUnit.MILLISECONDS, linkedBlockingDeque, new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("refresh-provider-thread-%d", this.threadIndex.incrementAndGet()));
            }
        });
        asyncRunTask();
    }

    /**
     * 异步执行任务
     */
    public void asyncRunTask() {
        if (Boolean.FALSE.equals(rpcWebConfigurationProperties.getRefreshDatabase())) {
            log.warn("server will not refresh database");
            return;
        }
        AssertUtil.isTrue(linkedBlockingDeque.size() < 5, "队列处理任务过多，请稍后重试");
        providerRefreshThreadPool.submit(() -> {
            // 读取/rpc/provider下全部数据，更新db
            Map<String, RpcMetaData> allProviderMetaData = providerMetaService.queryAllMetaDataFromConfigServer();
            for (Map.Entry<String, RpcMetaData> e : allProviderMetaData.entrySet()) {
                // 更新db
                ProviderInfoEntity entity = providerMetaService.updateProviderInfoStorage(e.getKey(), e.getValue());
                if (entity == null) {
                    continue;
                }
                // 注册监听器
                RpcWatcher rpcWatcher = new RpcWatcher();
                rpcWatcher.setProviderInfoEntity(entity);
                RpcDataManager<RpcMetaData> rpcDataManager = rpcProviderDataInitBean.getRpcDataManager();
                rpcDataManager.registerProviderDataListener(
                        entity.getInterfaceClazzName(),
                        entity.getProviderGroup(),
                        entity.getProviderVersion(),
                        rpcProperties.getEnvironment(),
                        rpcWatcher

                );
            }
        });
    }
}
