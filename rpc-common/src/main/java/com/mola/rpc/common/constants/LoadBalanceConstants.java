package com.mola.rpc.common.constants;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-08-06 10:51
 **/
public class LoadBalanceConstants {

    /**
     * 随机负载均衡策略
     */
    public static final String LOAD_BALANCE_RANDOM_STRATEGY = "RANDOM";

    /**
     * 轮询负载均衡策略
     */
    public static final String LOAD_BALANCE_ROUND_ROBIN_STRATEGY = "ROUND_ROBIN";

    /**
     * 一致性哈希负载均衡策略
     */
    public static final String CONSISTENCY_HASHING_STRATEGY = "CONSISTENCY_HASHING";

    /**
     * 在指定的地址中，执行随机负载均衡策略
     */
    public static final String LOAD_BALANCE_APPOINTED_RANDOM_STRATEGY = "APPOINTED_RANDOM";
}
