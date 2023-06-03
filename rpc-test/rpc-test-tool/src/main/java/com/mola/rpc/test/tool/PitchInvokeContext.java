package com.mola.rpc.test.tool;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description:
 * @date : 2023-05-27 22:17
 **/
public class PitchInvokeContext {

    private Object[] params;

    private Object result;

    public PitchInvokeContext(Object[] params, Object result) {
        this.params = params;
        this.result = result;
    }

    public Object[] getParams() {
        return params;
    }

    public Object getResult() {
        return result;
    }
}
