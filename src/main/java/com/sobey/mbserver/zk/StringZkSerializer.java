package com.sobey.mbserver.zk;

import java.io.UnsupportedEncodingException;

import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;

import com.sobey.jcg.support.log4j.LogUtils;

public class StringZkSerializer implements ZkSerializer {

	private String charset = "utf8";

	public StringZkSerializer() {
	}

	public StringZkSerializer(String charset) {
		this.charset = charset;
	}

	@Override
	public byte[] serialize(Object o) throws ZkMarshallingError {
		if (charset != null && !"".equals(charset)) {
			try {
				o.toString().getBytes(charset);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return o.toString().getBytes();
	}

	@Override
	public Object deserialize(byte[] bytes) throws ZkMarshallingError {
		if (charset != null && !"".equals(charset)) {
			try {
				return new String(bytes, charset);
			} catch (UnsupportedEncodingException e) {
				LogUtils.warn("zk数据编码错误:" + charset);
			}
		}
		return new String(bytes);
	}
}
