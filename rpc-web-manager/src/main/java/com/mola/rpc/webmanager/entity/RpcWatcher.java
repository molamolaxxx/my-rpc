package com.mola.rpc.webmanager.entity;

import com.mola.rpc.common.entity.RpcMetaData;
import lombok.Data;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2023-03-25 23:46
 **/
@Data
public class RpcWatcher extends RpcMetaData {

    private ProviderInfoEntity providerInfoEntity;
}
