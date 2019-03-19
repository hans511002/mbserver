package com.sobey.base.socket.remote;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sobey.base.BaseTool;
import com.sobey.base.exception.RemoteAccessException;
import com.sobey.base.socket.order.RemoteOrder;
import com.sobey.jcg.support.sys.DataSourceManager;

public class RemoteExec {
	public static final Log LOG = LogFactory.getLog(RemoteExec.class.getName());

	static Class<?> getClassFormName(String acs[]) {
		if (acs.length < 3)
			return null;
		String claName = acs[1];
		for (int i = 2; i < acs.length - 1; i++) {
			claName += "." + acs[i];
		}
		try {
			LOG.info("获取执行类：" + claName + " 方法：" + acs[acs.length - 1]);
			return Class.forName(claName);
		} catch (ClassNotFoundException e) {
			claName = acs[1];
			for (int i = 2; i < acs.length - 1; i++) {
				if (i == acs.length - 2) {// 内部类
					claName += "$" + acs[i];
				} else {
					claName += "." + acs[i];
				}
			}
			try {
				LOG.info("获取执行类：" + claName + " 方法：" + acs[acs.length - 1]);
				return Class.forName(claName);
			} catch (ClassNotFoundException e2) {
				LOG.error("RemoteExec 执行动态类调用", e);
			}
			return null;
		}
	}

	static Class<?> getClassFormSimpleName(String acs[]) {
		if (acs.length < 2)
			return null;
		String claName = acs[0];
		for (int i = 1; i < acs.length - 1; i++) {
			claName += "." + acs[i];
		}
		LOG.info("获取执行类：" + claName + " 方法：" + acs[acs.length - 1]);
		Class<?> clazz = RemoteJavaApi.getClass(claName);
		if (clazz != null) {
			return clazz;
		} else {
			claName = acs[0];
			for (int i = 1; i < acs.length - 1; i++) {
				if (i == acs.length - 2) {// 内部类
					claName += "$" + acs[i];
				} else {
					claName += "." + acs[i];
				}
			}
			return RemoteJavaApi.getClass(claName);
		}
	}

	public static String toString(Object obj) {
		if (obj == null)
			return "null";
		return obj.toString();
	}

	public static Class<?> getRemoteExecClass(String acs[]) throws RemoteAccessException {
		Class<?> clazz = null;
		if (acs[0].equals("exec")) {// 动态执行
			clazz = getClassFormName(acs);
		} else {
			clazz = getClassFormSimpleName(acs);
		}
		return clazz;
	}

	public static Method getRemoteExecMethod(Class<?> clazz, String[] acs) throws RemoteAccessException {
		HashMap<String, Method> methodMap = RemoteJavaApi.getClassMethods(clazz);
		if (methodMap == null) {
			throw new RemoteAccessException("未找到注册方法，对应的" + clazz.getName() + "类未注册方法");
		}
		String methodName = acs[acs.length - 1];
		Method m = methodMap.get(methodName);
		return m;
	}

	public static Method getRemoteExecMethod(String action) throws RemoteAccessException {
		String acs[] = action.split("[\\.:]");
		if (acs.length == 1) {
			throw new RemoteAccessException("非指定调用方法：action=" + action);
		}
		Class<?> clazz = null;
		if (acs[0].equals("exec")) {// 动态执行
			clazz = getClassFormName(acs);
		} else {
			clazz = getClassFormSimpleName(acs);
		}
		if (clazz == null) {
			throw new RemoteAccessException("未找到对应的" + action + "的类");
		}
		HashMap<String, Method> methodMap = RemoteJavaApi.getClassMethods(clazz);
		if (methodMap == null) {
			throw new RemoteAccessException("未找到对应的" + action + "的类未注册方法");
		}
		String methodName = acs[acs.length - 1];
		Method m = methodMap.get(methodName);
		return m;
	}

	public static Object execute(Class<?> clazz, Method m, Object args, RemoteOrder order) throws RemoteAccessException {
		try {
			Object obj = null;
			if (!Modifier.isStatic(m.getModifiers())) {
				obj = clazz.newInstance();
			}
			Object res = null;
			Class<?>[] params = m.getParameterTypes();
			if (params != null && params.length > 1) {
				if (args == null) {
					throw new RemoteAccessException("参数为空，不足：" + params.length + "个");
				}
				if (!(args instanceof List) && !(args instanceof Object[]) && !(args instanceof int[]) && !(args instanceof long[]) && !(args instanceof char[])
				        && !(args instanceof byte[])) {
					throw new RemoteAccessException("参数类型不正确 " + args.getClass().getName() + " 不是数组或List，需要参数" + params.length + "个");
				}
				List list = null;
				if (args instanceof List) {
					list = (List) args;
				} else {
					list = Arrays.asList(args);
				}
				if (params[params.length - 1].equals(RemoteOrder.class)) {
					list.add(order);
				}
				if (list.size() < params.length) {
					throw new RemoteAccessException(
					        "执行" + clazz.getCanonicalName() + " 方法：" + m.getName() + " 参数个数不足，需要" + params.length + "，传入" + list.size());
				}
				Object[] par = new Object[params.length];
				for (int i = 0; i < params.length; i++) {
					par[i] = BaseTool.castObject(params[i], list.get(i));//
				}
				res = m.invoke(obj, par);
			} else if (params != null && params.length == 1) {
				if (params[0].equals(RemoteOrder.class)) {
					res = m.invoke(obj, order);
				} else {
					res = m.invoke(obj, args);
				}
			} else {
				res = m.invoke(obj);
			}
			if (m.getReturnType() != Void.class)
				LOG.info("执行类：" + clazz.getCanonicalName() + " 方法：" + m.getName() + " 返回值：" + toString(res));
			return res;
		} catch (Exception e) {
			throw new RemoteAccessException(e);
		} finally {
			DataSourceManager.destroy();
		}
	}
}
