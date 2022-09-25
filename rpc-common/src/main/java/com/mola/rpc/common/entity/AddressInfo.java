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

    public AddressInfo(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }
}
