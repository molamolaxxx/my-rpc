package com.mola.rpc.common.entity;

import com.alibaba.fastjson.JSONObject;
import com.mola.rpc.common.annotation.ConsumerSide;
import com.mola.rpc.common.annotation.ProviderSide;
import com.mola.rpc.common.constants.LoadBalanceConstants;

import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-07-30 21:52
 **/
public class RpcMetaData {

    /**
     * 服务提供组
     */
    @ProviderSide
    @ConsumerSide
    private String group = "default";

    /**
     * 服务提供版本
     */
    @ProviderSide
    @ConsumerSide
    private String version = "1.0.0";

    /**
     * 接口名
     */
    @ProviderSide
    @ConsumerSide
    private Class<?> interfaceClazz;

    /**
     * 是否使用纤程
     */
    @ProviderSide
    private Boolean inFiber = false;

    /**
     * 服务提供者ip:port
     */
    @ConsumerSide
    private List<AddressInfo> addressList;

    /**
     * 客户端超时时间
     */
    @ConsumerSide
    private long clientTimeout = 10000;

    /**
     * 负载均衡策略
     */
    @ConsumerSide
    private String loadBalanceStrategy = LoadBalanceConstants.LOAD_BALANCE_RANDOM_STRATEGY;

    /**
     * 服务提供者bean名称
     */
    @ProviderSide
    private String providerBeanName;

    /**
     * 服务提供者对象
     */
    @ProviderSide
    private Object providerObject;

    /**
     * 服务提供者类名
     */
    @ProviderSide
    private String providerBeanClazz;

    /**
     * 异步调用方法
     */
    @ConsumerSide
    private Set<String> asyncExecuteMethods;

    @ProviderSide
    private String host;

    /**
     * 是否泛化调用
     */
    @ConsumerSide
    private Boolean genericInvoke = Boolean.FALSE;

    /**
     * 泛化调用服务名
     */
    @ConsumerSide
    private String genericInterfaceName;

    /**
     * 哈希环，hash => ip#idx
     * 用于负载均衡
     */
    @ConsumerSide
    private SortedMap<Integer, String> virtualAddressNodeMap = new TreeMap<>();

    /**
     * 客户端指定的provider服务器地址
     */
    @ConsumerSide
    private List<String> appointedAddress;


    public static RpcMetaData of(String group, String version, Class<?> clazzType) {
        RpcMetaData rpcMetaData = new RpcMetaData();
        rpcMetaData.setGroup(group);
        rpcMetaData.setVersion(version);
        rpcMetaData.setInterfaceClazz(clazzType);
        return rpcMetaData;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<AddressInfo> getAddressList() {
        return addressList;
    }

    public void setAddressList(List<AddressInfo> addressList) {
        this.addressList = addressList;
    }

    public String getLoadBalanceStrategy() {
        return loadBalanceStrategy;
    }

    public void setLoadBalanceStrategy(String loadBalanceStrategy) {
        this.loadBalanceStrategy = loadBalanceStrategy;
    }

    public long getClientTimeout() {
        return clientTimeout;
    }

    public void setClientTimeout(long clientTimeout) {
        this.clientTimeout = clientTimeout;
    }

    public Class<?> getInterfaceClazz() {
        return interfaceClazz;
    }

    public void setInterfaceClazz(Class<?> interfaceClazz) {
        this.interfaceClazz = interfaceClazz;
    }

    public String getProviderBeanName() {
        return providerBeanName;
    }

    public void setProviderBeanName(String providerBeanName) {
        this.providerBeanName = providerBeanName;
    }

    public String getProviderBeanClazz() {
        return providerBeanClazz;
    }

    public void setProviderBeanClazz(String providerBeanClazz) {
        this.providerBeanClazz = providerBeanClazz;
    }

    public Boolean getInFiber() {
        return inFiber;
    }

    public void setInFiber(Boolean inFiber) {
        this.inFiber = inFiber;
    }

    public Set<String> getAsyncExecuteMethods() {
        return asyncExecuteMethods;
    }

    public void setAsyncExecuteMethods(Set<String> asyncExecuteMethods) {
        this.asyncExecuteMethods = asyncExecuteMethods;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }

    public SortedMap<Integer, String> getVirtualAddressNodeMap() {
        return virtualAddressNodeMap;
    }

    public void setVirtualAddressNodeMap(SortedMap<Integer, String> virtualAddressNodeMap) {
        this.virtualAddressNodeMap = virtualAddressNodeMap;
    }

    public Object getProviderObject() {
        return providerObject;
    }

    public void setProviderObject(Object providerObject) {
        this.providerObject = providerObject;
    }

    public void setGenericInvoke(Boolean genericInvoke) {
        this.genericInvoke = genericInvoke;
    }

    public Boolean getGenericInvoke() {
        return genericInvoke;
    }

    public String getGenericInterfaceName() {
        return genericInterfaceName;
    }

    public void setGenericInterfaceName(String genericInterfaceName) {
        this.genericInterfaceName = genericInterfaceName;
    }

    public List<String> getAppointedAddress() {
        return appointedAddress;
    }

    public void setAppointedAddress(List<String> appointedAddress) {
        this.appointedAddress = appointedAddress;
    }
}
