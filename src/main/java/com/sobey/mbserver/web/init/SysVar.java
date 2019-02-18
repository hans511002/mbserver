package com.sobey.mbserver.web.init;

import org.I0Itec.zkclient.ZkClient;

import com.sobey.jcg.support.log4j.LogUtils;
import com.sobey.jcg.support.sys.SystemVariable;
import com.sobey.jcg.support.utils.Convert;
import com.sobey.mbserver.zk.StringZkSerializer;

public class SysVar extends SystemVariable {
	public static boolean isDebug = false;
	public static ZkClient zkClient = null;
	public static Object mutex = new Object();

	public static String getValue(String key, Object defval) {
		String res = conf.getProperty(key);
		if (res == null) {
			conf.setProperty(key, Convert.toString(defval, ""));
		}
		return res;
	}

	public static ZkClient getZkClient() {
		return zkClient;

	}

	public static String getZkBaseZone() {
		return getValue(Constant.KEY_ZK_BASE_NODE, "mbserver");
	}

	public static String getAppName() {
		return getValue(Constant.KEY_APP_NAME, "mbweb");
	}

	public static int getZkDataCompType() {
		return Convert.toInt(getValue(Constant.KEY_ZK_DATA_COMPRESSION_TYPE, 0));
	}

	public static boolean getZkDataUrlEncode() {
		return Convert.toBool(getValue(Constant.KEY_ZK_DATA_URLENCODE_ENABLED, true));
	}

	public static long getZkStatusTimeout() {
		return Convert.toLong(getValue(Constant.KEY_ZK_SESSION_TIMEOUT_MS, 0));
	}

	public static void initZkClient() {
		String ZOOKEEPER_URL = System.getenv("ZOOKEEPER_URL");

		SysVar.zkClient = new ZkClient(SysConfig.getZkUrl(), SysConfig.getZkSessionTOMS(), SysConfig.getZkConnectTOMS(), new StringZkSerializer());
	}

	public static void closeZkClient() {
		synchronized (mutex) {
			if (zkClient != null) {
				try {
					zkClient.close();
				} catch (Throwable e) {
				}
				zkClient = null;
			}
		}
	}

	public static void setLogLevel() {
		String level = SystemVariable.getConf().getProperty("log.level");
		// if (!"DEBUG".equals(level) && !"INFO".equals(level))
		// SystemVariable.isDebug = false;
		if ("DEBUG".equals(level)) {
			// SystemVariable.isDebug = true;
			LogUtils.setLevel(LogUtils.DEBUG);
		} else if ("INFO".equals(level)) {
			LogUtils.setLevel(LogUtils.INFO);
		} else if ("WARN".equals(level)) {
			LogUtils.setLevel(LogUtils.WARN);
		} else if ("ERROR".equals(level)) {
			LogUtils.setLevel(LogUtils.ERROR);
		} else if ("TRACE".equals(level)) {
			LogUtils.setLevel(LogUtils.TRACE);
		} else if ("FATAL".equals(level)) {
			LogUtils.setLevel(LogUtils.FATAL);
		}

	}
}
