package com.mola.rpc.core.proxy;

/**
 * @author : molamola
 * @Project: InvincibleSchedulerEngine
 * @Description:
 * @date : 2020-08-30 20:42
 **/
public class MethodKey {

    /** 对象 */
    private Object object;

    /** 方法名称 */
    private String methodName;

    /** 类型 */
    private Class<?>[] classType;

    public MethodKey(Object object, String methodName, Class<?>[] classType) {
        this.object = object;
        this.methodName = methodName;
        this.classType = classType;
    }

    /**
     * 重写equals方法
     */
    @Override
    public boolean equals(Object object) {
        if(object == null) {
            return false;
        }
        if(!(object instanceof MethodKey)) {
            return false;
        }
        MethodKey methodKey = (MethodKey) object;
        return methodKey.toString().equals(this.toString());
    }

    /**
     * 重写hashCode方法
     */
    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    /**
     * 重写toString方法
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(object.toString() + " ");
        stringBuilder.append(methodName + " ");
        for(int i = 0 ; i < classType.length ; i ++) {
            stringBuilder.append(classType[i].getName() + " ");
        }
        return stringBuilder.toString();
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getClassType() {
        return classType;
    }

    public void setClassType(Class<?>[] classType) {
        this.classType = classType;
    }
}
