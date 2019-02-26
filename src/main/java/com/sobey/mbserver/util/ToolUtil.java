/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.sobey.mbserver.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sobey.jcg.support.log4j.LogUtils;
import com.sobey.jcg.support.utils.Convert;

import sun.jvmstat.monitor.MonitorException;
import sun.jvmstat.monitor.MonitoredHost;
import sun.jvmstat.monitor.MonitoredVm;
import sun.jvmstat.monitor.MonitoredVmUtil;
import sun.jvmstat.monitor.VmIdentifier;

public class ToolUtil {
	public static final String STAT_JOBS = "jobs";
	public static final String STAT_COUNTERS = "counters";

	public static void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (Throwable e) {
			LogUtils.warn("sleep(" + time + ")" + e.getMessage());
		}
	}

	public static final Map<String, Object> toArgMap(Object... args) {
		if (args == null) {
			return null;
		}
		if (args.length % 2 != 0) {
			throw new RuntimeException("expected pairs of argName argValue");
		}
		HashMap<String, Object> res = new HashMap<String, Object>();
		for (int i = 0; i < args.length; i += 2) {
			if (args[i + 1] != null) {
				res.put(String.valueOf(args[i]), args[i + 1]);
			}
		}
		return res;
	}

	/**
	 * 将对象序列化成字符串
	 * 
	 * @param obj
	 * @return
	 * @throws IOException
	 */
	public static String serialObject(Object obj) throws IOException {
		return serialObject(obj, false, false);
	}

	public static String serialObject(Object obj, boolean isGzip, boolean urlEnCode) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
		objectOutputStream.writeObject(obj);
		String serStr = null;
		byte[] bts = null;
		if (isGzip) {
			bts = GZIPUtils.zip(byteArrayOutputStream.toByteArray());
		} else {
			bts = byteArrayOutputStream.toByteArray();
		}
		if (urlEnCode) {
			serStr = new String(org.apache.commons.codec.binary.Base64.encodeBase64(bts), "ISO-8859-1");
		} else {
			serStr = new String(bts, "ISO-8859-1");
		}
		objectOutputStream.close();
		byteArrayOutputStream.close();
		return serStr;
	}

	public static String serialObject(String str, boolean isGzip, boolean urlEnCode) throws IOException {
		if (!isGzip && !urlEnCode) {
			return str;
		}
		String serStr = null;
		byte[] bts = null;
		if (isGzip) {
			bts = GZIPUtils.zip(str.getBytes());
		} else {
			bts = str.getBytes();
		}
		if (urlEnCode) {
			serStr = new String(org.apache.commons.codec.binary.Base64.encodeBase64(bts), "ISO-8859-1");
		} else {
			serStr = new String(bts, "ISO-8859-1");
		}
		return serStr;
	}

	/**
	 * 反序列化对象
	 * 
	 * @param serStr
	 * @return
	 * @throws IOException
	 */
	public static <T> T deserializeObject(String serStr) throws IOException {
		return deserializeObject(serStr, false, false);
	}

	public static <T> T deserializeObject(String serStr, boolean isGzip, boolean urlEnCode) throws IOException {
		byte[] bts = null;
		if (urlEnCode) {
			bts = org.apache.commons.codec.binary.Base64.decodeBase64(serStr.getBytes("ISO-8859-1"));
		} else {
			bts = serStr.getBytes("ISO-8859-1");
		}
		if (isGzip)
			bts = GZIPUtils.unzip(bts);
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bts);
		ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
		try {
			return (T) objectInputStream.readObject();
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		} finally {
			objectInputStream.close();
			byteArrayInputStream.close();
		}
	}

	public static String deserializeString(String serStr, boolean isGzip, boolean urlEnCode) throws IOException {
		byte[] bts = null;
		if (urlEnCode) {
			bts = org.apache.commons.codec.binary.Base64.decodeBase64(serStr.getBytes("ISO-8859-1"));
		} else {
			bts = serStr.getBytes("ISO-8859-1");
		}
		if (isGzip)
			bts = GZIPUtils.unzip(bts);
		return new String(bts);
	}

	public static Process runProcess(String command, Log log) throws IOException {
		log.info("call system order：" + command);
		// Process proc = Runtime.getRuntime().exec(new String[] { command });
		Process proc = Runtime.getRuntime().exec(command);
		return proc;
	}

	public static Process runProcess(String command) throws IOException {
		Process proc = Runtime.getRuntime().exec(command);
		return proc;
	}

	public static synchronized void killProcess(Process proc) {
		proc.destroy();
	}

	public static final Pattern valPartsRegex = Pattern.compile("\\$(\\d+)");

	// 正则表达式的定向替换
	public static String ReplaceRegex(Matcher m, String substitution) {
		try {
			Matcher vm = valPartsRegex.matcher(substitution);
			String val = substitution;
			String regpar = substitution;
			int gl = m.groupCount();
			while (vm.find()) {
				regpar = regpar.substring(vm.end());
				int g = Integer.parseInt(vm.group(1));
				if (g > gl) {
					val = val.replaceAll("\\$\\d", "");
					break;
				}
				String gv = m.group(Integer.parseInt(vm.group(1)));
				if (gv != null)
					val = val.replaceAll("\\$" + g, gv);
				else
					val = val.replaceAll("\\$" + g, "");
				vm = valPartsRegex.matcher(regpar);
			}
			return val;
		} catch (Exception e) {
			return null;
		}
	}

	public static String getPath(Path path) {
		try {
			URI aUri = new URI(path.toString());
			return aUri.getPath();
		} catch (URISyntaxException e) {
			return path.toString();
		}
	}

	public static void close(Closeable c) {
		if (c != null) {
			try {
				c.close();
			} catch (IOException e) {
				LogUtils.warn("close error:" + e.getMessage());
			}
		}
	}

	public static void close(Socket c) {
		if (c != null) {
			try {
				c.close();
			} catch (IOException e) {
				LogUtils.warn("Socket close error:" + e.getMessage());
			}
		}
	}

	public static long toLong(String val) {
		long l = 0;
		for (char c : val.toCharArray()) {
			if (Character.isDigit(c))
				l = l * 10 + c - 48;
			else
				break;
		}
		return l;
	}

	public static Double toDouble(String val) {
		Double l = 0.0;
		boolean dig = false;
		long len = 10;
		long digs = 0;
		for (char c : val.toCharArray()) {
			if (Character.isDigit(c)) {
				if (dig) {
					digs = digs * 10 + c - 48;
					len *= 10;
				} else {
					l = l * 10 + c - 48;
				}
			} else if (c == '.') {
				dig = true;
			} else {
				break;
			}
		}
		return l + (double) digs * 10 / len;
	}

	public static boolean IsInt(String s) {
		boolean result = false;
		try {
			Integer.parseInt(s);
			result = true;
		} catch (Exception e) {
		}

		return result;
	}

	public static int str2Int(String s) {
		int result = 0;
		try {
			result = Integer.parseInt(s);
		} catch (Exception e) {
			result = -1;
		}
		return result;
	}

	// ��������
	public static String parseChinese(String in) {

		String s = null;
		byte temp[];
		if (in == null) {
			System.out.println("Warn:Chinese null founded!");
			return new String("");
		}
		try {
			temp = in.getBytes("iso-8859-1");
			s = new String(temp, "GBK");
		} catch (UnsupportedEncodingException e) {
			System.out.println("���ֱ���ת�����?" + e.toString());
		}
		return s;
	}

	// ��������
	public static String parseISO(String in) {

		String s = null;
		byte temp[];
		if (in == null) {
			System.out.println("Warn:Chinese null founded!");
			return new String("");
		}
		try {
			temp = in.getBytes("GBK");
			s = new String(temp, "iso-8859-1");

		} catch (UnsupportedEncodingException e) {
			System.out.println("���ֱ���ת�����?" + e.toString());

		}
		return s;

	}

	public static Object convertToExecEnvObj(String value) {
		try {
			if (value.indexOf('.') >= 0) {
				return Double.parseDouble(value);
			} else {
				return Long.parseLong(value);
			}
		} catch (NumberFormatException e) {
			return value;
		}
	}

	public static boolean getEexcBoolean(Object obj) {
		if (obj == null)
			return false;
		boolean res = false;
		if (obj instanceof Boolean) {
			res = (Boolean) obj;
		} else if (obj instanceof Integer) {
			res = ((Integer) obj > 0);
		} else if (obj instanceof Long) {
			res = ((Long) obj > 0);
		} else if (obj instanceof Double) {
			res = ((Double) obj > 0.000001);
		} else if (obj instanceof Float) {
			res = ((Float) obj > 0.000001);
		} else if (obj instanceof String) {
			res = !"".equals(obj) && !"null".equals(obj);
		} else {
			res = !"".equals(obj.toString()) && !"null".equals(obj.toString());
		}
		return res;
	}

	public static final Log LOG = LogFactory.getLog(ToolUtil.class.getName());
	public static java.text.SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	public static java.text.SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static void sleep(int m) {
		try {
			Thread.sleep(m);
		} catch (Throwable e) {
		}
	}

	public static List<String> getAllInet4Address() {
		try {
			List<String> res = new ArrayList<String>();
			Enumeration allNetInterfaces = NetworkInterface.getNetworkInterfaces();
			InetAddress ip = null;
			while (allNetInterfaces.hasMoreElements()) {
				NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
				LOG.info(netInterface.getName());
				Enumeration addresses = netInterface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					ip = (InetAddress) addresses.nextElement();
					if (ip != null && ip instanceof Inet4Address) {
						res.add(ip.getHostAddress());
					}
				}
			}
			return res;
		} catch (SocketException e) {
			return null;
		}
	}

	public static String getPath(String path) {
		try {
			URI aUri = new URI(path);
			return aUri.getPath();
		} catch (URISyntaxException e) {
			return path.toString();
		}
	}

	public static String Join(String[] strings) {
		return Join(",", strings);
	}

	public static String Join(String sp, String... strings) {
		StringBuffer sb = new StringBuffer();
		for (String s : strings) {
			if (sb.length() > 0) {
				sb.append(sp + s);
			} else {
				sb.append(s);
			}
		}
		return sb.toString();
	}

	public static String Join(List<String> list, String sp) {
		StringBuffer sb = new StringBuffer();
		for (String s : list) {
			if (sb.length() > 0) {
				sb.append(sp + s);
			} else {
				sb.append(s);
			}
		}
		return sb.toString();
	}

	public static String Join(List<String> list) {
		return Join(list, ",");
	}

	public static String join(long[] array, String spliter) {
		if (array != null) {
			StringBuffer rs = new StringBuffer();
			for (int i = 0; i < array.length - 1; i++) {
				rs.append(array[i] + (spliter == null ? "" : spliter));
			}
			rs.append(array[array.length - 1]);
			return rs.toString();
		}
		return null;
	}

	public static String join(int[] array, String spliter) {
		if (array != null) {
			StringBuffer rs = new StringBuffer();
			for (int i = 0; i < array.length - 1; i++) {
				rs.append(array[i] + (spliter == null ? "" : spliter));
			}
			rs.append(array[array.length - 1]);
			return rs.toString();
		}
		return null;
	}

	/**
	 * 将一个数组以一个指定的分隔符连接，如果未设置分隔符，直接连接
	 * 
	 * @param array
	 * @return
	 */
	public static String join(Object[] array, String spliter) {
		if (array != null) {
			StringBuffer rs = new StringBuffer();
			for (int i = 0; i < array.length - 1; i++) {
				rs.append(array[i] + (spliter == null ? "" : spliter));
			}
			rs.append(array[array.length - 1]);
			return rs.toString();
		}
		return null;
	}

	public static String join(Object[] array) {
		return join(array, ",");
	}

	public static String TimestampToString(long time, int type) {
		if (type == 2) {
			double d = (double) (time / (double) 86400000);
			d = (double) ((int) ((d + 0.005) * 100)) / 100;
			return d + "";
		}
		if (type == 3) {
			double d = (double) (time / (double) 3600000);
			d = (double) ((int) ((d + 0.005) * 100)) / 100;
			return d + "";
		}
		if (type == 4) {
			double d = (double) (time / (double) 60000);
			d = (double) ((int) ((d + 0.005) * 100)) / 100;
			return d + "";
		}
		if (type > 1 || type < 0)
			type = 0;
		int d, hh, mi, ss, sss;
		String sd = "", shh = "00", smi = "00", stss = "00", stsss = "";
		sss = (int) (time % 1000);
		ss = (int) ((time / 1000) % 60);
		mi = (int) ((time / 60000) % 60);
		hh = (int) ((time / 3600000) % 24);
		d = (int) (time / 86400000);
		if (d != 0) {
			sd = d + ".";
		}
		if (hh != 0) {
			shh = Math.abs(hh) + "";
		}
		sd += shh;
		if (mi != 0) {
			smi = Math.abs(mi) + "";
		}
		sd += ":" + smi;
		if (ss != 0) {
			stss = Math.abs(ss) + "";
		}
		sd += ":" + stss;
		if (type == 1)
			return sd;
		if (sss != 0) {
			stsss = "." + Math.abs(sss) + "";
		}
		return sd + stsss;
	}

	public static Object invokeMethod(Object obj, Method method) throws Exception {
		try {
			int plen = method.getParameterTypes().length;
			if (plen == 0) {
				return method.invoke(obj);
			}
			throw new Exception("反射调用方法与参数不匹配，对象：" + obj + "  方法：" + method);
		} catch (Exception e) {
			throw e;
		}
	}

	public static Object invokeMethod(Object obj, Method method, Object arg) throws Exception {
		try {
			int plen = method.getParameterTypes().length;
			if (plen == 1) {
				Class<?> clazz = method.getParameterTypes()[0];
				if (clazz.equals(int.class)) {
					arg = arg == null ? 0 : Convert.toInt(arg, 0);
				} else if (clazz.equals(long.class)) {
					arg = arg == null ? 0 : Convert.toLong(arg, 0);
				} else if (clazz.equals(double.class)) {
					arg = arg == null ? 0 : Convert.toDouble(arg, 0);
				} else if (clazz.equals(float.class)) {
					arg = arg == null ? 0 : Convert.toFloat(arg, 0);
				} else if (clazz.equals(short.class)) {
					arg = arg == null ? 0 : Convert.toShort(arg);
				} else if (clazz.equals(char.class)) {
					arg = arg == null ? 0 : Convert.toChar(arg);
				} else if (clazz.equals(byte.class)) {
					arg = arg == null ? 0 : Convert.toByte(arg);
				} else if (clazz.equals(boolean.class)) {
					arg = arg == null ? 0 : Convert.toBool(arg);
				} else {
					arg = arg == null ? null : Convert.convert(arg, clazz);
				}
				return method.invoke(obj, arg);
			}
			throw new Exception("反射调用方法与参数不匹配，对象：" + obj + "  方法：" + method);
		} catch (Exception e) {
			throw (e);
		}
	}

	public static String getCallMethodName(int backIndex) {
		return Thread.currentThread().getStackTrace()[backIndex + 2].getMethodName();
	}

	public static String getCallFileName(int backIndex) {
		return Thread.currentThread().getStackTrace()[backIndex + 2].getFileName();
	}

	public static String getCallClassName(int backIndex) {
		return Thread.currentThread().getStackTrace()[backIndex + 2].getClassName();
	}

	public static int getCallClassLine(int backIndex) {
		return Thread.currentThread().getStackTrace()[backIndex + 2].getLineNumber();
	}

	public static Class getCallClass(int backIndex) {
		return Thread.currentThread().getStackTrace()[backIndex + 2].getClass();
	}

	public static Class getThrowClass(Throwable e, int backIndex) {
		return e.getStackTrace()[backIndex].getClass();
	}

	public static String getThrowFile(Throwable e, int backIndex) {
		return e.getStackTrace()[backIndex].getFileName();
	}

	public static String getThrowClassName(Throwable e, int backIndex) {
		return e.getStackTrace()[backIndex].getClassName();
	}

	public static int getThrowClassLine(Throwable e, int backIndex) {
		return e.getStackTrace()[backIndex].getLineNumber();
	}

	public static final int getProcessID() {
		RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
		return Integer.valueOf(runtimeMXBean.getName().split("@")[0]).intValue();
	}

	public static int getProcess(Class<?> cls) throws MonitorException, URISyntaxException {
		if (cls == null) {
			return -1;
		}
		// 获取监控主机
		MonitoredHost local = MonitoredHost.getMonitoredHost("localhost");
		// 取得所有在活动的虚拟机集合
		Set<Integer> vmlist = new HashSet<Integer>(local.activeVms());
		// 遍历集合，输出PID和进程名
		for (Object process : vmlist) {
			MonitoredVm vm = local.getMonitoredVm(new VmIdentifier("//" + process));
			// 获取类名
			String processname = MonitoredVmUtil.mainClass(vm, true);
			if (cls.getName().equals(processname)) {
				return ((Integer) process).intValue();
			}
		}
		return -1;
	}

	public static List<Integer> getJavaProcess() throws MonitorException, URISyntaxException {
		// 获取监控主机
		MonitoredHost local = MonitoredHost.getMonitoredHost("localhost");
		// 取得所有在活动的虚拟机集合
		Set<Integer> vmlist = new HashSet<Integer>(local.activeVms());
		return new ArrayList<Integer>(vmlist);
	}

	/**
	 * 检查进程是否存在
	 */
	public static boolean checkProcess(int pid) throws Exception {
		List<Integer> javaList = getJavaProcess();
		for (Integer pids : javaList) {
			if (pid == Convert.toInt(pids, 0)) {
				return true;
			}
		}
		return false;
	}
}
