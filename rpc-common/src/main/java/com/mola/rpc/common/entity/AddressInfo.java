package com.mola.rpc.common.entity;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-08-27 12:12
 **/
public class AddressInfo {

    /**
     * 地址信息，ip:port
     */
    private String address;

    /**
     * 服务端上报数据
     */
    private ProviderConfigData providerConfigData;

    public AddressInfo(String address, ProviderConfigData providerConfigData) {
        this.address = address;
        this.providerConfigData = providerConfigData;
    }

    public String getAddress() {
        return address;
    }

    public ProviderConfigData getProviderConfigData() {
        return providerConfigData;
    }


}
