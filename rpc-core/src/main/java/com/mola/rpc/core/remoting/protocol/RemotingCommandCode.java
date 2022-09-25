package com.mola.rpc.core.remoting.protocol;

public class RemotingCommandCode {

    /************************request-code*************************/

    //普通线程池code
    public static final int NORMAL = 0;

    /************************response-code*************************/

    // 返回成功
    public static final int SUCCESS = 1;

    // 发生了未捕获异常
    public static final int SYSTEM_ERROR = -1;

    // 由于线程池拥堵，系统繁忙
    public static final int SYSTEM_BUSY = -2;

    // 请求代码不支持
    public static final int REQUEST_CODE_NOT_SUPPORTED = -3;

    public static String buildCodeInfo(RemotingCommand response) {
        String codeInfo;
        switch (response.getCode()) {
            case SYSTEM_ERROR:
                codeInfo = "SYSTEM_ERROR";
                break;
            case SYSTEM_BUSY:
                codeInfo = "SYSTEM_BUSY";
                break;
            case REQUEST_CODE_NOT_SUPPORTED:
                codeInfo = "REQUEST_CODE_NOT_SUPPORTED";
                break;
            default:
                codeInfo = String.valueOf(response.getCode());
                break;
        }
        return codeInfo;
    }


}
