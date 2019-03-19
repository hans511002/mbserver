package com.sobey.base.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sobey.jcg.support.log4j.LogUtils;

public class Ping {
	public static boolean ping(String ipAddress) {
		int timeOut = 3000; // 超时应该在3钞以上
		boolean status = false;
		try {
			status = InetAddress.getByName(ipAddress).isReachable(timeOut); // 当返回值是true时，说明host是可用的，false则不可。
		} catch (IOException e) {
			LogUtils.error("ping " + ipAddress + e.getMessage());
		}
		return status;
	}

	public static boolean ping(String ipAddress, int pingTimes) {
		BufferedReader in = null;
		try { // 执行命令并获取输出
			Runtime r = Runtime.getRuntime(); // 将要执行的ping命令,此命令是windows格式的命令
			String pingCommand = null;
			if (File.separator.equals("/")) {
				pingCommand = "ping " + ipAddress + " -c " + pingTimes + " -W " + pingTimes;
			} else {
				pingCommand = "ping " + ipAddress + " -n " + pingTimes + " -w 1000 ";
			}
			LogUtils.info(pingCommand);
			Process p = r.exec(pingCommand);
			if (p == null) {
				return false;
			}
			in = new BufferedReader(new InputStreamReader(p.getInputStream())); // 逐行检查输出,计算类似出现=23ms TTL=62字样的次数
			int connectedCount = 0;
			String line = null;
			while ((line = in.readLine()) != null) {
				connectedCount += getCheckResult(line);
			} // 如果出现类似=23ms TTL=62这样的字样,出现的次数=测试次数则返回真
			return connectedCount >= (pingTimes + 1) / 2;
		} catch (Exception ex) {
			ex.printStackTrace(); // 出现异常则返回假
			return false;
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// 若line含有=18ms TTL=16字样,说明已经ping通,返回1,否則返回0.
	private static int getCheckResult(String line) { // System.out.println("控制台输出的结果为:"+line);
		if (File.separator.equals("/")) {
			// icmp_seq=1 ttl=64 time=0.039 ms
			Pattern pattern = Pattern.compile("(ttl=\\d+)(\\s+)(time=.*ms)", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(line);
			while (matcher.find()) {
				return 1;
			}
		} else {
			Pattern pattern = Pattern.compile("(\\d+ms)(\\s+)(TTL=\\d+)", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(line);
			while (matcher.find()) {
				return 1;
			}
		}
		return 0;
	}

	public static void main(String[] args) throws Exception {
		String ipAddress = "127.0.0.1";
		System.out.println(ping(ipAddress));
		System.out.println(ping(ipAddress, 5));
	}
}
