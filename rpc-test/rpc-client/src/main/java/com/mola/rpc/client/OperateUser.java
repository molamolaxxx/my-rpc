package com.mola.rpc.client;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2022-10-16 18:41
 **/
@Data
@AllArgsConstructor
public class OperateUser {

    private String id;

    private String userName;
}
