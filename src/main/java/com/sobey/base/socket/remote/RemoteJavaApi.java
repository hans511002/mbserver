package com.sobey.base.socket.remote;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sobey.jcg.support.utils.ClassUtils;

public abstract class RemoteJavaApi {
	public static final Log LOG = LogFactory.getLog(RemoteJavaApi.class.getName());
	static HashMap<Class<?>, HashMap<String, Method>> classApis = new HashMap<Class<?>, HashMap<String, Method>>();

	static HashMap<String, Class<?>> classRel = new HashMap<String, Class<?>>();
	public static HashMap<String, Boolean> loadedPaths = new HashMap<String, Boolean>();
	static {
		reloadJavaApi();
	}

	public static void reloadJavaApi() {
		List<Class<?>> clazzs = null;
		clazzs = ClassUtils.getAllClassByInterface(RemoteClass.class, "com.sobey.angl.base");
		RemoteJavaApi.putJavaApiMethods(clazzs);
		clazzs = ClassUtils.getAllClassByInterface(RemoteJavaApi.class, "com.sobey.angl.base");
		RemoteJavaApi.putJavaApiMethods(clazzs);
	}

	public static Class<?> getClass(String claName) {
		return RemoteJavaApi.classRel.get(claName);
	}

	public static HashMap<String, Method> getClassMethods(Class<?> clazz) {
		return RemoteJavaApi.classApis.get(clazz);
	}

	public static void putJavaApiMethods(List<Class<?>> clazzs) {
		for (Class<?> clazz : clazzs) {
			putJavaApiMethods(clazz);
		}
	}

	// 反射扫描所有支持调用的类和方法
	public static void putJavaApiMethods(Class<?> clazz) {
		if (!RemoteJavaApi.classApis.containsKey(clazz)) {
			HashMap<String, Method> methodMap = new HashMap<String, Method>();
			Method[] methods = clazz.getDeclaredMethods();
			for (Method m : methods) {
				if (m.isAnnotationPresent(RemoteMethod.class)) {
					methodMap.put(m.getName(), m);
				}
			}
			RemoteJavaApi.classApis.put(clazz, methodMap);
			String simpleName = clazz.getSimpleName();
			putClassName(clazz, simpleName, clazz.getPackage().getName());
		}
	}

	public static HashMap<String, Method> getMethods(String clazzSimpleName) {
		return RemoteJavaApi.classApis.get(RemoteJavaApi.classRel.get(clazzSimpleName));
	}

	public static HashMap<Class<?>, HashMap<String, Method>> getAllClassMethods() {
		return RemoteJavaApi.classApis;
	}

	public static HashMap<Class<?>, Set<String>> getAllClassMethodNames() {
		HashMap<Class<?>, Set<String>> clssMethod = new HashMap<Class<?>, Set<String>>();
		for (Class<?> cl : RemoteJavaApi.classApis.keySet()) {
			clssMethod.put(cl, RemoteJavaApi.classApis.get(cl).keySet());
		}
		return clssMethod;
	}

	public static HashMap<String, Set<String>> getAllClassSimpleNameMethodNames() {
		HashMap<String, Set<String>> clssMethod = new HashMap<String, Set<String>>();
		for (String classSimpleName : classRel.keySet()) {
			clssMethod.put(classSimpleName, RemoteJavaApi.classApis.get(classRel.get(classSimpleName)).keySet());
		}

		return clssMethod;
	}

	static void putClassName(Class clazz, String simpleName, String parPackageName) {
		if (RemoteJavaApi.classRel.containsKey(simpleName)) {
			simpleName = parPackageName.substring(parPackageName.lastIndexOf(".") + 1) + "." + simpleName;
			parPackageName = parPackageName.substring(0, parPackageName.lastIndexOf("."));
			putClassName(clazz, simpleName, parPackageName);
		} else {
			RemoteJavaApi.classRel.put(simpleName, clazz);
		}
	}
}
