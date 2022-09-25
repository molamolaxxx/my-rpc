package com.mola.rpc.core.proxy;


import com.mola.rpc.core.util.RemotingSerializableUtil;
import org.springframework.util.Assert;

import java.lang.reflect.Method;

/**
 * RPC调用方法抽象
 */
public class InvokeMethod {

	/** 调用方法 */
	private String methodName;

	/** 参数类型 */
	private String[] parameterTypes;

	/** 参数 */
	private String[] arguments;

	/** 返回类型 */
	private String returnType;

	/**
	 * 调用接口名称
	 */
	private String interfaceClazz;

	public InvokeMethod() {
	}

	public InvokeMethod(String methodName, String[] parameterTypes, String[] arguments, String returnType, String interfaceClazz) {
		this.methodName = methodName;
		this.parameterTypes = parameterTypes;
		this.arguments = arguments;
		this.returnType = returnType;
		this.interfaceClazz = interfaceClazz;
	}


	/**
	 * json转换成对象
	 * @param json
	 * @return
	 */
	public static InvokeMethod newInstance(String json) {
		InvokeMethod invokeMethod = RemotingSerializableUtil.fromJson(json, InvokeMethod.class);
		return invokeMethod;
	}

	/**
	 * 对象转换成json
	 */
	@Override
	public String toString() {
		return RemotingSerializableUtil.toJson(this, false);
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String[] getParameterTypes() {
		return parameterTypes;
	}

	public void setParameterTypes(String[] parameterTypes) {
		this.parameterTypes = parameterTypes;
	}

	public String[] getArguments() {
		return arguments;
	}

	public void setArguments(String[] arguments) {
		this.arguments = arguments;
	}

	public String getReturnType() {
		return returnType;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	public String getInterfaceClazz() {
		return interfaceClazz;
	}

	public void setInterfaceClazz(String interfaceClazz) {
		this.interfaceClazz = interfaceClazz;
	}

	public Object invoke(Object providerBean) {
		try {
			Assert.notNull(providerBean, "providerBean is null, name = " + providerBean);
			// 1、反序列化类型
			Class<?>[] paramTypes = new Class<?>[this.parameterTypes.length];
			for (int i = 0; i < this.parameterTypes.length; i++) {
				paramTypes[i] = Class.forName(this.parameterTypes[i]);
			}
			// 2、反序列化参数
			Object[] args = new Object[this.arguments.length];
			for (int i = 0; i < this.arguments.length; i++) {
				args[i] = RemotingSerializableUtil.fromJson(this.arguments[i], paramTypes[i]);
			}
			Method method = providerBean.getClass().getMethod(this.methodName, paramTypes);
			// 3、反射调用provider
			return method.invoke(providerBean, args);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
