package com.mola.rpc.core.util;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;

/**
 * 字节数组工具
 * @author zhuyueqian
 *
 */
public class BytesUtil {

	/**
	 * 判断字节数组是否为空
	 * @param bytes
	 * @return
	 */
	public static boolean isEmpty(byte[] bytes) {
		if(bytes == null) {
			return true;
		}
		if(bytes.length <= 0) {
			return true;
		}
		return false;
	}

	/**
	 * 将对象转换为byte数组
	 * @param object
	 * @return
	 */
	public static byte[] objectToBytes(Object object) {
		ByteArrayOutputStream byteArrayOutputStream =
				new ByteArrayOutputStream();
		HessianOutput hessianOutput = new HessianOutput(byteArrayOutputStream);
		hessianOutput.getSerializerFactory().setAllowNonSerializable(true);
		try {
			hessianOutput.writeObject(object);
		} catch (Exception e) {
			throw new RuntimeException("write object error", e);
		}
		return byteArrayOutputStream.toByteArray();
	}

	/**
	 * 将byte数组转换成对象
	 * @param bytes
	 * @return
	 */
	public static Object bytesToObject(byte[] bytes) {
		if(bytes == null) {
			throw new RuntimeException("bytes is null");
		}
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
		HessianInput hessianInput = new HessianInput(byteArrayInputStream);
		hessianInput.getSerializerFactory().setAllowNonSerializable(true);
		Object object = null;
		try {
			object = hessianInput.readObject();
		} catch (Exception e) {
			throw new RuntimeException("read object error", e);
		}
		return object;
	}

	public static <T> T bytesToObject(byte[] bytes, Class<T> clazz) {
		if(bytes == null) {
			throw new RuntimeException("bytes is null");
		}
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
		HessianInput hessianInput = new HessianInput(byteArrayInputStream);
		hessianInput.getSerializerFactory().setAllowNonSerializable(true);
		Object object = null;
		try {
			object = hessianInput.readObject(clazz);
		} catch (Exception e) {
			throw new RuntimeException("read object error", e);
		}
		return (T) object;
	}

	/**
	 * 将byte数组计算MD5
	 * @param bytes
	 * @return
	 */
	public static String md5(byte[] bytes){
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			throw new RuntimeException("message digest error", e);
		}
		messageDigest.update(bytes);
		byte[] resultBytes = messageDigest.digest();
		return bytesToHex(resultBytes);
	}

	/**
	 * 把byte数组转换成字符串
	 * @param bytes
	 * @return
	 */
	public static String bytesToHex(byte[] bytes) {
		char[] hexDigits = {
				'0', '1', '2', '3', '4',
				'5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f'};
		char[] resultCharArray = new char[16 * 2];
		int index = 0;
		for(int i = 0 ; i < 16 ; i ++) {
			resultCharArray[index ++] = hexDigits[bytes[i] >>> 4 & 0xf];
			resultCharArray[index ++] = hexDigits[bytes[i] & 0xf];
		}
		return new String(resultCharArray);
	}
}
