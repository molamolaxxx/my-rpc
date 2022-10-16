package com.mola.rpc.client;

import lombok.Data;

import java.util.Date;

/**
 * @author : molamola
 * @Project: java-study
 * @Description:
 * @date : 2022-07-25 23:26
 **/
@Data
public class Order {

    private String id;

    private Date gmtCreate;

    private String desc;

    private String code;

    private String operator;

    public Order() {
    }

    public Order(String id, Date gmtCreate, String desc, String code) {
        this.id = id;
        this.gmtCreate = gmtCreate;
        this.desc = desc;
        this.code = code;
    }
}
