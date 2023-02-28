package com.mola.rpc.common.constants;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-08-06 10:51
 **/
public class CommonConstants {

    /**
     * beanDefinition 中传递的consumer元数据
     */
    public static final String BEAN_DEF_CONSUMER_META = "BEAN_DEF_CONSUMER_META";

    /**
     * beanDefinition 中传递的provider元数据
     */
    public static final String BEAN_DEF_PROVIDER_META = "BEAN_DEF_PROVIDER_META";

    /**
     * 未知应用
     */
    public static final String UNKNOWN_APP = "UNKNOWN_APP";

    /**
     * 未知环境
     */
    public static final String DEFAULT_ENVIRONMENT = "public";

    /**
     * zk根节点路径
     */
    public static final String PATH_MY_RPC = "/myRpc";

    /**
     * provider节点路径
     */
    public static final String PATH_MY_RPC_PROVIDER = "/myRpc/provider";

    /**
     * consumer节点路径
     */
    public static final String PATH_MY_RPC_CONSUMER = "/myRpc/consumer";

    /**
     * zookeeper
     */
    public static final String ZOOKEEPER = "zookeeper";

    /**
     * nacos
     */
    public static final String NACOS = "nacos";
}
