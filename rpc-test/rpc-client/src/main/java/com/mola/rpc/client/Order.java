package com.mola.rpc.client;

import com.google.common.base.Objects;
import com.mola.rpc.common.annotation.ConsistencyHashKey;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equal(id, order.id) && Objects.equal(gmtCreate, order.gmtCreate) && Objects.equal(desc, order.desc) && Objects.equal(code, order.code) && Objects.equal(operator, order.operator);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, gmtCreate, desc, code, operator);
    }


    @ConsistencyHashKey
    public String hash() {
        return id;
    }
}
