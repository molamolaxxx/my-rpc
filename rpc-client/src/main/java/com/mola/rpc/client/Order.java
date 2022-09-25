package com.mola.rpc.client;

import java.util.Date;

/**
 * @author : molamola
 * @Project: java-study
 * @Description:
 * @date : 2022-07-25 23:26
 **/
public class Order {

    private String id;

    private Date gmtCreate;

    private String desc;

    private String code;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
