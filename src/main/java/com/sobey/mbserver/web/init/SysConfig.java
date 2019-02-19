package com.sobey.mbserver.web.init;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.I0Itec.zkclient.ZkClient;

import com.sobey.jcg.support.log4j.LogUtils;
import com.sobey.jcg.support.sys.SystemVariable;
import com.sobey.jcg.support.utils.Convert;
import com.sobey.jcg.support.utils.RemotingUtils;
import com.sobey.mbserver.util.FileExpand;
import com.sobey.mbserver.util.LineProcess;
import com.sobey.mbserver.util.SystemUtil;
import com.sobey.mbserver.util.ToolUtil;
import com.sobey.mbserver.web.ApiServer;
import com.sobey.mbserver.zk.StringZkSerializer;

public class SysConfig extends SystemVariable {
	static String installHome = null;
	static String initConfigPath = null;
	public static boolean isDebug = false;
	public static boolean lockElastic = false;
	public static boolean iszkUrl = false;
	public final static int appStatusleaveTime = 300000;

	public static String getHostEtcDir() {
		String res = conf.getProperty(Constant.DEPLOY_HOST_ETC_KEY, "/etc/sobey/hive");
		conf.setProperty(Constant.DEPLOY_HOST_ETC_KEY, res + "");
		return res;
	}

	public static int getZkStatusTimeout() {
		int res = Convert.toInt(conf.getProperty(Constant.DEPLOY_ZKCHECK_STATUS_TIMEOUT_KEY), 30000);
		conf.setProperty(Constant.DEPLOY_ZKCHECK_STATUS_TIMEOUT_KEY, res + "");
		return res;
	}

	public static int getPaasLogDBSummaryDays() {
		int res = Convert.toInt(conf.getProperty(Constant.DEPLOY_PAASLOG_DBSUMMARY_DAYS_KEY), 32);
		conf.setProperty(Constant.DEPLOY_PAASLOG_DBSUMMARY_DAYS_KEY, res + "");
		return res;
	}

	public static boolean getLocalAppSrcSyncFlag() {
		boolean res = Convert.toBool(conf.getProperty(Constant.DEPLOY_LOCAL_APPSRC_SYNC_FLAG_KEY), false);
		conf.setProperty(Constant.DEPLOY_LOCAL_APPSRC_SYNC_FLAG_KEY, res + "");
		return res;
	}

	public static boolean getLocalAppSrcSyncScp() {
		boolean res = Convert.toBool(conf.getProperty(Constant.DEPLOY_LOCAL_APPSRC_SYNC_SCP_KEY), true);
		conf.setProperty(Constant.DEPLOY_LOCAL_APPSRC_SYNC_SCP_KEY, res + "");
		return res;
	}

	public static int getResourceZkSize() {
		int res = Convert.toInt(conf.getProperty(Constant.DEPLOY_APPRESOURCES_ZK_SIZE_KEY), 1);
		conf.setProperty(Constant.DEPLOY_APPRESOURCES_ZK_SIZE_KEY, res + "");
		return res;
	}

	public static int getSystemResourceRealtimeInterval() {
		int res = Convert.toInt(conf.getProperty(Constant.DEPLOY_SYSTEM_RESOURCES_REALTIME_INTERVAL_KEY), 2000);
		conf.setProperty(Constant.DEPLOY_SYSTEM_RESOURCES_REALTIME_INTERVAL_KEY, res + "");
		return res;
	}

	public static int getLbsListenInterval() {
		int res = Convert.toInt(conf.getProperty(Constant.DEPLOY_LBS_LISTEN_INTERVAL_KEY), 2000);
		conf.setProperty(Constant.DEPLOY_LBS_LISTEN_INTERVAL_KEY, res + "");
		return res;
	}

	public static int getLBSummaryInterval() {
		int res = Convert.toInt(conf.getProperty(Constant.DEPLOY_LB_SUMMARY_INTERVAL_KEY), 30000);
		conf.setProperty(Constant.DEPLOY_LB_SUMMARY_INTERVAL_KEY, res + "");
		return res;
	}

	public static int getLBDetailInterval() {
		int res = Convert.toInt(conf.getProperty(Constant.DEPLOY_LB_DETAIL_INTERVAL_KEY), 0);
		conf.setProperty(Constant.DEPLOY_LB_DETAIL_INTERVAL_KEY, res + "");
		return res;
	}

	public static boolean getLBAppSummaryLogOut() {
		boolean res = Convert.toBool(conf.getProperty(Constant.DEPLOY_LB_APP_SUMMARY_LOGOUT_KEY), true);
		conf.setProperty(Constant.DEPLOY_LB_APP_SUMMARY_LOGOUT_KEY, res + "");
		return res;
	}

	public static int getResourceApiSize() {
		int res = Convert.toInt(conf.getProperty(Constant.DEPLOY_APPRESOURCES_API_SZIE_KEY), 5);
		conf.setProperty(Constant.DEPLOY_APPRESOURCES_API_SZIE_KEY, res + "");
		return res;
	}

	public static boolean getZkUrlIsAuto() {
		String res = Convert.toString(conf.getProperty(Constant.ZK_URL_TYPE_KEY, "auto"), "auto");
		conf.setProperty(Constant.ZK_URL_TYPE_KEY, res + "");
		return "auto".equals(res);
	}

	public static String getNodeManagerType() {
		String res = conf.getProperty(Constant.DEPLOY_CLUSTER_NODE_MANAGER_TYPE_KEY, "swarm");
		conf.setProperty(Constant.DEPLOY_CLUSTER_NODE_MANAGER_TYPE_KEY, res + "");
		return res;
	}

	public static boolean getImitatSwarm() {
		boolean res = Convert.toBool(conf.getProperty(Constant.DEPLOY_CLUSTER_NODESTATUS_IMITATE_SWARM_KEY, "false"), false);
		conf.setProperty(Constant.DEPLOY_CLUSTER_NODESTATUS_IMITATE_SWARM_KEY, res + "");
		return res;
	}

	public static String getElasticRefreshFlag() {
		String res = conf.getProperty(Constant.DEPLOY_ELASTIC_REFRESH_HAPROXY_FLAG_KEY, "auto");
		conf.setProperty(Constant.DEPLOY_ELASTIC_REFRESH_HAPROXY_FLAG_KEY, res + "");
		return res;
	}

	public static boolean getElasticEnableRefreshFlag() {
		String type = getElasticRefreshFlag();
		return type.equals("true") || type.equals("auto");
	}

	public static String[] getWebAppsDelFiles() {
		String delFiles = conf.getProperty(Constant.DEPLOY_SYSTEM_WEB_DELFILES_KEY);
		String res[] = null;
		if (delFiles == null || delFiles.trim().isEmpty()) {
			delFiles = "";
		}
		res = delFiles.split(",");
		conf.setProperty(Constant.DEPLOY_SYSTEM_WEB_DELFILES_KEY, delFiles + "");
		return res;
	}

	public static String[] getFirstHostInitApps() {
		String res = conf.getProperty(Constant.DEPLOY_CLUSTER_INIT_APP_ILIST_KEY);
		return res.split(",");
	}

	public static boolean getAppCopyFlag() {
		boolean res = Convert.toBool(conf.getProperty(Constant.DEPLOY_COPY_APP_FALG_KEY), false);
		conf.setProperty(Constant.DEPLOY_COPY_APP_FALG_KEY, res + "");
		return res;
	}

	public static boolean getAddNodeFailFlag() {
		boolean res = Convert.toBool(conf.getProperty(Constant.DEPLOY_CLUSTER_ADD_NODE_FAILED_FLAG_KEY), false);
		conf.setProperty(Constant.DEPLOY_CLUSTER_ADD_NODE_FAILED_FLAG_KEY, res + "");
		return res;
	}

	public static boolean getAppManualStartFlag() {
		boolean res = Convert.toBool(conf.getProperty(Constant.DEPLOY_APP_MANUAL_START_KEY), true);
		conf.setProperty(Constant.DEPLOY_APP_MANUAL_START_KEY, res + "");
		return res;
	}

	public static List<String> getDisabledCopyApps() {
		String apps = conf.getProperty(Constant.DEPLOY_COPY_APP_DISABLED_KEY);
		if (apps == null || apps.isEmpty()) {
			apps = "installer,nump,paasman,haproxy,mysql,mongo";
			conf.setProperty(Constant.DEPLOY_COPY_APP_DISABLED_KEY, apps);
		}
		List<String> disapp = new ArrayList<String>(Arrays.asList(apps.split(",")));
		return disapp;
	}

	public static String[] getGobalConfigFiles() {
		String files = conf.getProperty(Constant.DEPLOY_GOBAL_CONFIG_FILELS_KEY, "");
		if (!files.trim().isEmpty()) {
			return files.split(",");
		}
		return new String[0];
	}

	public static String[] getAddHostInitApps() {
		return conf.getProperty(Constant.DEPLOY_CLUSTER_ADDNODE_APP_ILIST_KEY).split(",");
	}

	public static String[] getSystemRootApps() {
		String rooApps = conf.getProperty(Constant.DEPLOY_SYSTEM_ROOT_SUDO_APPS_KEY);
		if (rooApps == null || rooApps.trim().isEmpty()) {
			return new String[0];
		}
		return rooApps.trim().split(",");
	}

	public static void setLang(String lang) {
		if (lang.equals("cn") || lang.equals("en")) {
			conf.setProperty(Constant.INSTALL_LANG, lang);
		}
	}

	public static String getInstallHome() throws IOException {
		if (installHome == null) {
			// installHome = System.getenv("INSTALLER_HOME");
			if (installHome == null || installHome.equals("")) {
				String classHome = ClassLoader.getSystemResource("").getPath();
				// LogUtils.info("classHome=" + classHome);
				String home = new File(classHome).getCanonicalPath() + "/../";
				installHome = new File(home).getCanonicalPath();
			}
		}
		// LogUtils.debug("getInstallHome INSTALLER_HOME=" + installHome);
		return installHome;
	}

	public static String getWebAppDir() {
		return ApiServer.webApp;
	}

	public static String getConfigVal(String key, String defvalue) {
		String val = conf.getProperty(key, defvalue);
		if (val.isEmpty()) {
			val = defvalue;
		}
		return val;
	}

	public static String getShareRootPath() {
		String res = getConfigVal(Constant.DEPLOY_SHARE_ROOTPATH_KEY, "/infinityfs1");
		conf.setProperty(Constant.DEPLOY_SHARE_ROOTPATH_KEY, res + "");
		return res;
	}

	public static String getTempDir() throws IOException {
		String tmp = getConfigVal(Constant.DEPLOY_INSTALL_TMP_DIR_KEY, SysConfig.getInstallHome() + File.separator + "tmp");
		if (File.separator.equals("\\")) {
			if (!(tmp.startsWith("\\") || tmp.indexOf(":") > 0)) {
				tmp = SysConfig.getInstallHome() + File.separator + tmp;
			}
		} else {
			if (!tmp.startsWith("/")) {
				tmp = SysConfig.getInstallHome() + File.separator + tmp;
			}
		}
		if (tmp.endsWith(File.separator)) {
			tmp = tmp.substring(0, tmp.length() - 1);// tmp + File.separator;
		}
		return tmp;
	}

	public static String getLocalHostName() {
		Process proc = null;
		try {
			proc = ToolUtil.runProcess("hostname");
			DataInputStream r = new DataInputStream(proc.getInputStream());
			String hn = r.readLine();
			return hn;
		} catch (IOException e) {
			return null;
		} finally {
			if (proc != null)
				proc.destroy();
		}
	}

	public static String getHostName() {
		String hostName = conf.getProperty(Constant.HOST_NAME_KEY);
		if (hostName == null || "".equals(hostName) || "default".equals(hostName)) {
			try {
				hostName = getLocalHostName();
				if (hostName == null) {
					String hostIP = RemotingUtils.getLocalAddress();
					InetAddress a = java.net.InetAddress.getByName(hostIP);
					hostName = a.getCanonicalHostName();
				}
			} catch (UnknownHostException e) {
				// 执行hostname
				String hon = SystemUtil.execResult(null, "hostname");
				if (hon != null && !hon.equals("") && hon.length() < 128) {
					hon = hon.replaceAll("\n", "");
					hostName = hon;
				} else {
					hostName = "0.0.0.0 ";
				}
			} catch (IOException e) {
				LogUtils.error("getHostName", e);
			}
		}
		return hostName;
	}

	public static String getHostIP() {
		String hostIP = conf.getProperty(Constant.HOST_IP_KEY);
		if (hostIP == null || "".equals(hostIP) || "default".equals(hostIP) || "127.0.0.1".equals(hostIP)) {
			try {
				String fhostIP = RemotingUtils.getLocalAddress();
				InetAddress[] a = java.net.InetAddress.getAllByName(getHostName());
				for (InetAddress inetAddress : a) {
					LogUtils.debug(inetAddress.getHostName() + "   " + inetAddress.getHostAddress());
					String _hostIP = inetAddress.getHostAddress();
					if (_hostIP.equals(fhostIP)) {
						hostIP = _hostIP;
						break;
					} else if (_hostIP.split("\\.").length == 4) {
						hostIP = _hostIP;
					}
				}
			} catch (UnknownHostException e) {
				hostIP = "";
				Process proc = null;
				try {
					proc = ToolUtil.runProcess("hostname -i");
					DataInputStream r = new DataInputStream(proc.getInputStream());
					String hn = r.readLine();
					return hn;
				} catch (IOException ex) {
				} finally {
					if (proc != null)
						proc.destroy();
				}
			}
		}
		return hostIP;
	}

	public static String getZkUrl() {
		String res = conf.getProperty(Constant.ZK_CONNECT_KEY);
		conf.setProperty(Constant.ZK_CONNECT_KEY, res + "");
		return res;
	}

	public static String getZkBaseZone() {
		String res = conf.getProperty(Constant.ZK_BASE_ZONE_KEY, "sobeyPaas");
		conf.setProperty(Constant.ZK_BASE_ZONE_KEY, res + "");
		return res;
	}

	public static String getClusterName() {
		String res = conf.getProperty(Constant.CLUSTER_NAME_KEY, "hive");
		conf.setProperty(Constant.CLUSTER_NAME_KEY, res + "");
		return res;
	}

	public static int getZkConnectTOMS() {
		int res = Convert.toInt(conf.getProperty(Constant.ZK_CONNECT_TIMEOUT_MS_KEY), 10000);
		conf.setProperty(Constant.ZK_CONNECT_TIMEOUT_MS_KEY, res + "");
		return res;
	}

	public static int getZkDataCompressionType() {
		int res = Convert.toInt(conf.getProperty(Constant.ZK_DATA_COMPRESSION_TYPE_KEY), 0);
		conf.setProperty(Constant.ZK_DATA_COMPRESSION_TYPE_KEY, res + "");
		return res;
	}

	public static boolean getZkDataUrlencode() {
		boolean res = Convert.toBool(conf.getProperty(Constant.ZK_DATA_URLENCODE_ENABLED_KEY), true);
		conf.setProperty(Constant.ZK_DATA_URLENCODE_ENABLED_KEY, res + "");
		return res;
	}

	public static int getBackSyncThreadNum() {
		int res = Convert.toInt(conf.getProperty(Constant.DEPLOY_BACK_SYNC_THREAD_NUM_KEY), 10);
		conf.setProperty(Constant.DEPLOY_BACK_SYNC_THREAD_NUM_KEY, res + "");
		return res;
	}

	public static boolean getAppAlertDisperseFlag() {
		boolean res = Convert.toBool(conf.getProperty(Constant.DEPLOY_ELASTIC_APP_ALERT_DISPERSE_KEY), false);
		conf.setProperty(Constant.DEPLOY_ELASTIC_APP_ALERT_DISPERSE_KEY, res + "");
		return res;
	}

	public static int getElasticReduceBaseSize() {
		int res = Convert.toInt(conf.getProperty(Constant.DEPLOY_ELASTIC_REDUCE_BASE_SIZE_KEY), 2);
		conf.setProperty(Constant.DEPLOY_ELASTIC_REDUCE_BASE_SIZE_KEY, res + "");
		return res;
	}

	public static int[] getElasticHostAlertScores() {
		String alertScore = conf.getProperty(Constant.DEPLOY_ELASTIC_HOST_ALERT_SCORES_KEY);
		if (alertScore == null || alertScore.isEmpty()) {
			alertScore = "90,120,150,200,300";
			conf.setProperty(Constant.DEPLOY_ELASTIC_HOST_ALERT_SCORES_KEY, alertScore);
		}
		String als[] = alertScore.split(",");
		if (als.length != 5) {
			als = "90,120,150,200,300".split(",");
		}
		int dalts[] = new int[] { 90, 120, 150, 200, 300 };
		int alts[] = new int[5];
		for (int i = 0; i < alts.length; i++) {
			alts[i] = Convert.toInt(als[i], dalts[i]);
		}
		return alts;
	}

	public static int[] getElasticCpuHightLowerScores() {
		String alertScore = conf.getProperty(Constant.DEPLOY_ELASTIC_APP_ALERT_SCORES_KEY);
		if (alertScore == null || alertScore.isEmpty()) {
			alertScore = "70,10,80,10,2";
			conf.setProperty(Constant.DEPLOY_ELASTIC_APP_ALERT_SCORES_KEY, alertScore);
		}
		String als[] = alertScore.split(",");
		if (als.length != 4) {
			als = "70,10,80,10,2".split(",");
		}
		int dalts[] = new int[] { 70, 10, 90, 10, 2 };
		int alts[] = new int[5];
		for (int i = 0; i < alts.length; i++) {
			alts[i] = Convert.toInt(als[i], dalts[i]);
		}
		return alts;
	}

	public static boolean getElasticFlag() {
		boolean res = Convert.toBool(conf.getProperty(Constant.DEPLOY_ELASTIC_FLAG_KEY), true);
		conf.setProperty(Constant.DEPLOY_ELASTIC_FLAG_KEY, res + "");
		return res;
	}

	public static void setElasticEnable() {
		conf.setProperty(Constant.DEPLOY_ELASTIC_FLAG_KEY, "true");
	}

	public static boolean getElasticReduceOptimized() {
		String ecRedOpt = conf.getProperty(Constant.DEPLOY_ELASTIC_REDUCE_TYPE_KEY);
		boolean res = "optimized".equals(ecRedOpt);
		conf.setProperty(Constant.DEPLOY_ELASTIC_REDUCE_TYPE_KEY, ecRedOpt + "");
		return res;
	}

	public static boolean getDockerPSFlag() {
		boolean res = Convert.toBool(conf.getProperty(Constant.DEPLOY_DOCKER_PS_ENABLE_KEY), true);
		conf.setProperty(Constant.DEPLOY_DOCKER_PS_ENABLE_KEY, res + "");
		return res;
	}

	public static boolean getAddHostRerunDockerFlag() {
		boolean res = Convert.toBool(conf.getProperty(Constant.DEPLOY_ADD_HOST_RERUN_DOCKER_FALG_KEY), false);
		conf.setProperty(Constant.DEPLOY_ADD_HOST_RERUN_DOCKER_FALG_KEY, res + "");
		return res;
	}

	public static boolean getDockerStatsFlag() {
		boolean res = Convert.toBool(conf.getProperty(Constant.DEPLOY_RESOURCE_DOCKER_STATS_KEY), true);
		conf.setProperty(Constant.DEPLOY_RESOURCE_DOCKER_STATS_KEY, res + "");
		return res;
	}

	// 0:全部应用 1:分离应用 2:有扩容缩容规则的应用
	public static int getElasticAppAlertType() {
		int res = Convert.toInt(conf.getProperty(Constant.DEPLOY_ELASTIC_APP_ALERTTYPE_KEY), 2);
		conf.setProperty(Constant.DEPLOY_ELASTIC_APP_ALERTTYPE_KEY, res + "");
		return res;
	}

	public static void setElasticSupportParams() {
		if (getElasticInterval() == 0) {
			conf.setProperty(Constant.DEPLOY_ELASTIC_INTERVAL_KEY, "30000");
		}
		if (getNodeInfoFlushInterval() == 0) {
			conf.setProperty(Constant.DEPLOY_NODEINFO_FLUSH_INTERVAL_KEY, "60000");
		}
		if (getServiceFlushInterval() == 0) {
			conf.setProperty(Constant.DEPLOY_SERVICE_FLUSH_INTERVAL_KEY, "60000");
		}
		if (getAppStatusFlushInterval() == 0) {
			conf.setProperty(Constant.DEPLOY_APPSTATUSE_FLUSH_INTERVAL_KEY, "10000");
		}
		if (getSystemResourcesFlushInterval() == 0) {
			conf.setProperty(Constant.DEPLOY_SYSTEM_RESOURCES_FLUSH_INTERVAL_KEY, "5000");
		}
		if (getAppResourcesLevel() == 0) {
			conf.setProperty(Constant.DEPLOY_APPRESOURCES_LEVEL_KEY, "1");
		}
	}

	public static int getElasticInterval() {
		int res = Convert.toInt(conf.getProperty(Constant.DEPLOY_ELASTIC_INTERVAL_KEY), 30000);
		conf.setProperty(Constant.DEPLOY_ELASTIC_INTERVAL_KEY, res + "");
		return res;
	}

	public static int getNodeInfoFlushInterval() {
		int res = Convert.toInt(conf.getProperty(Constant.DEPLOY_NODEINFO_FLUSH_INTERVAL_KEY), 60000);
		conf.setProperty(Constant.DEPLOY_NODEINFO_FLUSH_INTERVAL_KEY, res + "");
		return res;
	}

	public static int getServiceFlushInterval() {
		int res = Convert.toInt(conf.getProperty(Constant.DEPLOY_SERVICE_FLUSH_INTERVAL_KEY), 60000);
		conf.setProperty(Constant.DEPLOY_SERVICE_FLUSH_INTERVAL_KEY, res + "");
		return res;
	}

	public static int getAppStatusFlushInterval() {
		int res = Convert.toInt(conf.getProperty(Constant.DEPLOY_APPSTATUSE_FLUSH_INTERVAL_KEY), 10000);
		conf.setProperty(Constant.DEPLOY_APPSTATUSE_FLUSH_INTERVAL_KEY, res + "");
		return res;
	}

	public static int getAppResourcesLevel() {
		int res = Convert.toInt(conf.getProperty(Constant.DEPLOY_APPRESOURCES_LEVEL_KEY), 1);
		conf.setProperty(Constant.DEPLOY_APPRESOURCES_LEVEL_KEY, res + "");
		return res;
	}

	public static int getSystemResourcesFlushInterval() {
		int res = Convert.toInt(conf.getProperty(Constant.DEPLOY_SYSTEM_RESOURCES_FLUSH_INTERVAL_KEY), 30000);
		conf.setProperty(Constant.DEPLOY_SYSTEM_RESOURCES_FLUSH_INTERVAL_KEY, res + "");
		return res;
	}

	public static int getAppStatusFlushTimeout() {
		int res = Convert.toInt(conf.getProperty(Constant.DEPLOY_APPSTATUSE_FLUSH_TIMEOUT_KEY), 30000);
		conf.setProperty(Constant.DEPLOY_APPSTATUSE_FLUSH_TIMEOUT_KEY, res + "");
		return res;
	}

	public static int getInstallEnvFlushInterval() {
		int res = Convert.toInt(conf.getProperty(Constant.DEPLOY_INSTALL_ENV_FLUSH_INTERVAL_KEY), 0);
		conf.setProperty(Constant.DEPLOY_INSTALL_ENV_FLUSH_INTERVAL_KEY, res + "");
		return res;
	}

	public static int getZkSessionTOMS() {
		int res = Convert.toInt(conf.getProperty(Constant.ZK_SESSION_TIMEOUT_MS_KEY), 7000);
		conf.setProperty(Constant.ZK_SESSION_TIMEOUT_MS_KEY, res + "");
		return res;
	}

	public static boolean getUiEnable() {
		boolean res = Convert.toBool(conf.getProperty(Constant.UI_ENABLE_KEY), true);
		conf.setProperty(Constant.UI_ENABLE_KEY, res + "");
		return res;
	}

	public static int getUiPort() {
		int port = Convert.toInt(conf.getProperty(Constant.UI_PORT_KEY), 8081);
		port = port > 0 ? port : 8081;
		conf.setProperty(Constant.UI_PORT_KEY, port + "");
		return port;
	}

	public static int getHttpsPort() {
		int res = Convert.toInt(conf.getProperty(Constant.UI_HTTPS_PORT_KEY), 0);
		conf.setProperty(Constant.UI_HTTPS_PORT_KEY, res + "");
		return res;
	}

	public static int getProxySocketPort() {
		int res = Convert.toInt(conf.getProperty(Constant.PROXY_PORT_KEY), 2222);
		conf.setProperty(Constant.PROXY_PORT_KEY, res + "");
		return res;
	}

	static int tmpFileIdx = 0;

	public static String replaceFileMac(String infile, Map<String, String> macrs) throws IOException {
		String tempFile = getTempDir() + "/tmpFile_";
		synchronized (installHome) {
			tempFile += tmpFileIdx++ + ".tmp";
		}
		DataInputStream in = null;
		DataOutputStream out = null;
		try {
			if (!new File(tempFile).getParentFile().exists()) {
				new File(tempFile).getParentFile().mkdir();
			}
			in = new DataInputStream(new FileInputStream(infile));
			out = new DataOutputStream(new FileOutputStream(tempFile));
			String line = null;
			while ((line = in.readLine()) != null) {// CLUSTER_HOST_LIST
				                                    // CLUSTER_HOST_LIST
				for (String mac : macrs.keySet()) {
					line = line.replaceAll("\\$\\{" + mac + "\\}", macrs.get(mac));
				}
				out.write(line.getBytes());
				out.write("\n".getBytes());
			}
			in.close();
			out.close();
			out = null;
			in = null;
			return tempFile;
		} finally {
			if (in != null)
				in.close();
			if (out != null)
				out.close();
		}

	}

	public static void writeFile(String tempFile, String content) throws IOException {
		writeFile(tempFile, content, "utf8");
	}

	public static void writeFile(String tempFile, String content, String code) throws IOException {
		BufferedWriter w = null;
		try {
			if (!new File(tempFile).getParentFile().exists()) {
				new File(tempFile).getParentFile().mkdirs();
			}
			w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), code));
			w.write(content);
			if (!content.endsWith("\n"))
				w.write("\n");
			w.flush();
			w.close();
			w = null;
		} finally {
			if (w != null)
				w.close();
		}
	}

	public static void appendFile(String file, String content) throws IOException {
		appendFile(file, content, "utf8");
	}

	public static void appendFile(String file, String content, String code) throws IOException {
		BufferedWriter w = null;
		try {
			if (!new File(file).getParentFile().exists()) {
				new File(file).getParentFile().mkdirs();
			}
			w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), code));
			w.write(content);
			if (!content.endsWith("\n"))
				w.write("\n");
			w.flush();
			w.close();
			w = null;
		} finally {
			if (w != null)
				w.close();
		}
	}

	public static BufferedWriter openFile(String file, String code, boolean append) throws IOException {
		BufferedWriter w = null;
		try {
			if (!new File(file).getParentFile().exists()) {
				new File(file).getParentFile().mkdirs();
			}
			w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append), code));
			return w;
		} catch (Throwable e) {
			if (w != null) {
				ToolUtil.close(w);
			}
			return null;
		}
	}

	public static String readFile(String tempFile) throws IOException {
		return readFile(tempFile, null);
	}

	public static String readFile(String tempFile, LineProcess call) throws IOException {
		BufferedReader r = null;
		try {
			if (!new File(tempFile).getParentFile().exists()) {
				new File(tempFile).getParentFile().mkdirs();
			}
			String code = FileExpand.parseFileCode(tempFile, "utf8");
			r = FileExpand.getFileBufferedReader(tempFile, code);
			String line = r.readLine();
			StringBuffer sb = new StringBuffer();
			while (line != null) {
				if (call != null) {
					line = call.processLine(line);
					if (line != null) {
						sb.append(line);
						sb.append("\n");
					}
				} else {
					sb.append(line);
					sb.append("\n");
				}
				line = r.readLine();
			}
			return sb.toString();
		} finally {
			if (r != null)
				r.close();
		}
	}

	public static String getMapValue(Map<String, Object> m, String keys, String defaultS) {
		Object v = getMapValue(m, keys);
		if (v == null) {
			return defaultS;
		}
		return v.toString();
	}

	public static Object getMapValue(Map<String, Object> m, String keys) {
		if (m == null)
			return null;
		String key[] = keys.split("\\.");
		Map<String, Object> sm = m;
		for (int i = 0; i < key.length - 1; i++) {
			sm = (Map<String, Object>) sm.get(key[i]);
			if (sm == null)
				return null;
		}
		Object v = sm.get(key[key.length - 1]);
		return v;
	}

	public static Object setMapValue(Map<String, Object> m, String keys, Object val) {
		String key[] = keys.split("\\.");
		Map<String, Object> sm = m;
		for (int i = 0; i < key.length - 1; i++) {
			Map<String, Object> _sm = (Map<String, Object>) sm.get(key[i]);
			if (_sm == null) {
				_sm = new HashMap<String, Object>();
				sm.put(key[i], _sm);
			}
			sm = _sm;
		}
		return sm.put(key[key.length - 1], val);
	}

	public static Object removeMapValue(Map<String, Object> m, String keys) {
		if (m == null)
			return null;
		String key[] = keys.split("\\.");
		Map<String, Object> sm = m;
		for (int i = 0; i < key.length - 1; i++) {
			sm = (Map<String, Object>) sm.get(key[i]);
			if (sm == null)
				return null;
		}
		Object v = sm.remove(key[key.length - 1]);
		return v;
	}

	public static boolean testZk() {
		try {
			ZkClient zkClient = new ZkClient(SysConfig.getZkUrl(), SysConfig.getZkSessionTOMS(), SysConfig.getZkConnectTOMS(), new StringZkSerializer());
			zkClient.close();
			return true;
		} catch (Throwable e) {
			LogUtils.error("testZk", e);
			return false;
		}

	}

	public static boolean compMap(Map<String, Object> gobalConfig, Map<String, Object> ngc) {
		return compMap(gobalConfig, ngc, true);
	}

	public static boolean compMap(Map<String, Object> gobalConfig, Map<String, Object> ngc, boolean type) {
		for (String key : gobalConfig.keySet()) {
			if (!ngc.containsKey(key)) {
				return false;
			}
		}
		for (String key : ngc.keySet()) {
			if (!gobalConfig.containsKey(key)) {
				return false;
			}
		}
		for (String key : gobalConfig.keySet()) {
			Object obj1 = gobalConfig.get(key);
			Object obj2 = ngc.get(key);
			if (obj1 == null && obj2 == null) {
				continue;
			} else if ((obj1 == null) != (obj2 == null)) {
				continue;
			} else if (obj1 instanceof Map && obj2 instanceof Map) {
				return compMap((Map<String, Object>) obj1, (Map<String, Object>) obj2);
			} else if (type && !obj1.getClass().equals(obj2.getClass())) {
				return false;
			} else if (!obj1.toString().equals(obj2.toString())) {
				return false;
			}
		}
		return true;
	}

	public static void main(String args[]) throws IOException {

		// double a[] = new double[3];
		// a[0] = 1;
		// a[1] = 1;
		// a[2] = 1;
		// System.out.println(a);
		// System.out.println(JsonZkSerializer.serializes(a));
		//
		// ClusterConfigPo clusterConfig = new ClusterConfigPo();
		// clusterConfig.env.put("aa.aa", "$NEBULA");
		// clusterConfig.conf
		// .put("app.install.env",
		// "{\"NEBULA_VIP\":\"172.16.131.40\",\"DOCKER_NETWORK_NAME\":\"hivenet\",\"DOCKER_NETWORK_HOSTS\":\"--add-host=${HOST_NAME_0}:${HOST_IP_0} --add-host=$HOST_NAME_1}:${HOST_IP_1} --add-host=${HOST_NAME_2}:${HOST_IP_2} \"}");
		// String content = "aa=${aa.aa}";
		// content += "\napp.install.env=${app.install.env}";
		// System.err.println(replaceGlobalEnv(clusterConfig, content));
		// List<double[]> level1_path1=new ArrayList<double[]>();
		// List<double[]> level1_path2=new ArrayList<double[]>();
		// List<double[]> level1_path3=new ArrayList<double[]>();
		// level1_path1.add(new double[]{-30, 160});
		//

	}

}
