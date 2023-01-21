package com.mola.rpc.core.proxy;


import com.mola.rpc.core.util.BytesUtil;
import com.mola.rpc.core.util.RemotingSerializableUtil;
import com.mola.rpc.core.util.TypeUtil;
import org.springframework.util.Assert;

/**
 * RPC调用方法抽象
 */
public class InvokeMethod {

	/** 调用方法 */
	private String methodName;

	/** 参数类型 */
	private String[] parameterTypes;

	/** 序列化参数 */
	private byte[][] serializedArguments;

	/** 返回类型 */
	private String returnType;

	/**
	 * 调用接口名称
	 */
	private String interfaceClazz;

	public InvokeMethod() {
	}

	public InvokeMethod(String methodName, String[] parameterTypes, Object[] arguments, String returnType, String interfaceClazz) {
		this.methodName = methodName;
		this.parameterTypes = parameterTypes;
		// 进行参数序列化
		this.serializedArguments = new byte[arguments.length][];
		for (int i = 0; i < arguments.length; i++) {
			this.serializedArguments[i] = BytesUtil.objectToBytes(arguments[i]);
		}
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

	public byte[][] getSerializedArguments() {
		return serializedArguments;
	}

	public void setSerializedArguments(byte[][] serializedArguments) {
		this.serializedArguments = serializedArguments;
	}

	public Object invoke(Object providerBean) {
		try {
			Assert.notNull(providerBean, "providerBean not exist, name = " + providerBean);
			// 1、反序列化类型
			Class<?>[] paramTypes = new Class<?>[this.parameterTypes.length];
			for (int i = 0; i < this.parameterTypes.length; i++) {
				// 判断是否是基础类型
				Class baseTypeClazz = TypeUtil.getBaseTypeClazz(this.parameterTypes[i]);
				if (null != baseTypeClazz) {
					paramTypes[i] = baseTypeClazz;
					continue;
				}
				paramTypes[i] = Class.forName(this.parameterTypes[i]);
			}
			// 2、反序列化参数
			Object[] args = new Object[this.serializedArguments.length];
			for (int i = 0; i < this.serializedArguments.length; i++) {
				args[i] = BytesUtil.bytesToObject((this.serializedArguments[i]), paramTypes[i]);
			}
			// 3、反射调用，带方法缓存
			return MethodInvokeHelper.invokeMethod(providerBean, this.methodName, paramTypes, args);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
