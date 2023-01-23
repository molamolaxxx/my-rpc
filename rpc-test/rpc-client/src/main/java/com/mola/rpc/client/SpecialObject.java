package com.mola.rpc.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.nio.file.AccessMode;
import java.util.Date;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 特殊对象序列化
 * @date : 2023-01-23 22:45
 **/
@AllArgsConstructor
@Getter
public class SpecialObject {

    private BigDecimal bigDecimal;

    private Date date;

    /**
     * 枚举
     */
    private AccessMode accessMode;


}
