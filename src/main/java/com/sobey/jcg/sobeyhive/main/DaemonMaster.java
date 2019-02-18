package com.sobey.jcg.sobeyhive.main;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.I0Itec.zkclient.ZkClient;

import com.sobey.jcg.support.log4j.LogUtils;
import com.sobey.jcg.support.sys.SystemConstant;
import com.sobey.jcg.support.sys.SystemVariable;
import com.sobey.jcg.support.utils.Convert;
import com.sobey.mbserver.util.DateUtils;
import com.sobey.mbserver.util.ToolUtil;
import com.sobey.mbserver.web.ApiServer;
import com.sobey.mbserver.web.ServiceReqHandler;
import com.sobey.mbserver.web.init.Constant;
import com.sobey.mbserver.web.init.SysConfig;
import com.sobey.mbserver.web.init.SysVar;
import com.sobey.mbserver.zk.JsonZkSerializer;

public class DaemonMaster {
	private long loopTimes = -1;
	private boolean isMaster = false;// 是否主节点
	public boolean stopped;// 是否停止
	public final ApiServer uiServer;
	public static DaemonMaster master;
	public boolean isDeployactorService = false;// 是否部署管理服务节点
	public boolean isDeployActor = false;// 是否部署执行节点
	// public DeployAppResourcesZkTracker zkAppStatusTracker;

	public static int zkDataCompressionType = SysVar.getZkDataCompType();
	public static boolean zkDataUrlEncode = SysVar.getZkDataUrlEncode();
	public static boolean updateZkConfig = false;
	public String hostName;
	public String hostIP;
	public String hostNodePath;
	List<Object> masterHostInfos;
	public String configBaseDir = "";
	boolean serviceIsInited = false;
	boolean isDeployAppSrcHost = false;

	public Object zkLock = new Object();
	public final String deployUser;
	DeployZkCheck zkCheckThread = null;
	int serviceFlushInterval = 60000;
	int appStatusFlushInterval = 30000;
	int nodeInfoFlushInterval = 60000;
	int dockerContainerFlushInterval = nodeInfoFlushInterval;
	int installEnvFlushInterval = 0;
	int systemResourcesFlushInterval = 30000;
	long startTime = System.currentTimeMillis();
	static boolean printThreadInfo = false;

	public DaemonMaster(boolean noui) throws IOException {
		deployUser = System.getProperty("user.name");
		System.setProperty("user.dir", SysConfig.getInstallHome());
		stopped = false;
		if (!noui && SysConfig.getUiEnable()) {
			uiServer = new ApiServer(this);
		} else {
			uiServer = null;
		}
		master = this;
		hostName = SysConfig.getHostName();
		hostIP = SysConfig.getHostIP();
		hostNodePath = Constant.ZK_HOST_LIST + "/" + hostName;
		// proxyServer = new ProxyServer(this);
	}

	public boolean enableZkEvent() {
		return isMaster() && serviceIsInited();
	}

	public boolean isMaster() {
		return isMaster && isDeployactorService;
	}

	public boolean serviceIsInited() {
		return this.serviceIsInited;
	}

	public synchronized void setMaster(List<Object> masterHostInfos) {
		if (master.masterHostInfos != null && masterHostInfos != null) {
			if (master.masterHostInfos.get(0).equals(masterHostInfos.get(0)) && master.masterHostInfos.get(1).equals(masterHostInfos.get(1))
			        && master.masterHostInfos.get(2).equals(masterHostInfos.get(2)) && master.masterHostInfos.get(3).equals(masterHostInfos.get(3))) {
				this.masterHostInfos = masterHostInfos;
				return;
			}
		}

		if (masterHostInfos == null) {
			this.masterHostInfos = null;
			this.isMaster = false;

		} else {
			if (isDeployactorService && hostName.equals(masterHostInfos.get(0))) {
				this.isMaster = true;

				if (this.masterHostInfos == null || !this.masterHostInfos.get(0).equals(masterHostInfos.get(0))) {
					this.masterHostInfos = masterHostInfos;
				}
				this.masterHostInfos = masterHostInfos;
			} else {
				this.isMaster = false;
				this.masterHostInfos = masterHostInfos;
			}
		}
		//
		// if (masterHostInfos != null && masterHostInfos.size() >= 4) {
		// String mcode = masterHostInfos.get(3);
		// if (mcode != null && this.masterHostInfos != null && this.masterHostInfos.size() >= 4) {
		// if ((mcode).equals(this.masterHostInfos.get(3))) {
		// startElastic();
		// return;
		// }
		// }
		// }
		// startElastic();

	}

	public boolean haveMaster() {
		return masterHostInfos != null && !masterHostInfos.isEmpty();
	}

	public String getMasterHostName() {
		if (haveMaster()) {
			return this.masterHostInfos.get(0).toString();
		}
		return "";
	}

	public Object getHostTimes() {
		if (haveMaster()) {
			return this.masterHostInfos.get(4);
		}
		return null;
	}

	public String getMasterHostIp() {
		if (haveMaster()) {
			return this.masterHostInfos.get(1).toString();
		}
		return "";
	}

	public void setDeployactorService() {
		isDeployactorService = true;
	}

	/**
	 * 启动
	 * 
	 * @throws IOException
	 */
	public void start() throws IOException {
		LogUtils.info("节点[" + SysConfig.getHostName() + "]开始启动");
		if (uiServer != null) {
			LogUtils.info("启动UI");
			uiServer.start();// ui监控
			long stTime = System.currentTimeMillis();
			while (!uiServer.runned) {
				ToolUtil.sleep(1000);
			}
			while (!uiServer.isStarted()) {
				ToolUtil.sleep(1000);
				if (System.currentTimeMillis() - stTime > 5000)
					throw new IOException("启动UI服务失败");
			}
		}
		// setDeployactorService();
		// System.setProperty("user.dir", SysConfig.getTempDir());
		if (this.isDeployactorService) {
			// proxyServer.start();
		}
		loop();// 循环守护进程
	}

	public boolean stopSuccess = false;

	public void regHostZk() {
		if (!JsonZkSerializer.checkZkNotNull() || !master.isDeployactorService)
			return;
		synchronized (hostNodePath) {
			Map<String, Object> hdata = new HashMap<String, Object>();
			long now = System.currentTimeMillis();
			int thisPid = ToolUtil.getProcessID();
			// 注册节点
			LogUtils.info("beging write host info to zk:" + hostNodePath);
			while (JsonZkSerializer.exists(hostNodePath)) {
				String hostData = JsonZkSerializer.readData(hostNodePath);
				hdata = JsonZkSerializer.deserialize(hostData, Map.class, 2);
				long st = System.currentTimeMillis();
				int proHashCode = Convert.toInt(SysConfig.getMapValue(hdata, "daemonInfo.code"), 0);
				int proPid = Convert.toInt(SysConfig.getMapValue(hdata, "daemonInfo.pid"), 0);
				long proStartTime = Convert.toLong(SysConfig.getMapValue(hdata, "daemonInfo.startTime"), 0);
				if (thisPid != proPid) {
					try {
						if (!ToolUtil.checkProcess(proPid)) {
							break;
						}
					} catch (Exception e) {
						LogUtils.error("checkProcess " + proPid, e);
					}
				}
				if (this.hashCode() == proHashCode && thisPid == proPid && startTime == proStartTime) {
					break;
				}
				if (now - st > 120000) {
					stopby(new Exception(hostName + " exists in zk"));// 自己退出
					return;
				} else {
					LogUtils.warn(hostName + " exists in zk,wait to exit");
					ToolUtil.sleep(2000);
				}
			}
			if (JsonZkSerializer.exists(hostNodePath)) {
				hdata = JsonZkSerializer.deserialize(JsonZkSerializer.readData(hostNodePath), Map.class, 2);
				SysConfig.setMapValue(hdata, "daemonInfo.startTime", startTime);
				SysConfig.setMapValue(hdata, "daemonInfo.startTimeStr", DateUtils.format(new Date(startTime)));
				SysConfig.setMapValue(hdata, "daemonInfo.state", "running");
				SysConfig.setMapValue(hdata, "daemonInfo.code", this.hashCode() + "");
				SysConfig.setMapValue(hdata, "daemonInfo.pid", thisPid + "");
				JsonZkSerializer.updateZkData(hostNodePath, hdata, 2);
			} else {
				hdata.clear();
				SysConfig.setMapValue(hdata, "daemonInfo.startTime", startTime);
				SysConfig.setMapValue(hdata, "daemonInfo.startTimeStr", DateUtils.format(new Date(startTime)));
				SysConfig.setMapValue(hdata, "daemonInfo.state", "running");
				SysConfig.setMapValue(hdata, "daemonInfo.code", this.hashCode() + "");
				SysConfig.setMapValue(hdata, "daemonInfo.pid", thisPid + "");
				JsonZkSerializer.updateZkData(hostNodePath, hdata, 2);
			}
		}
	}

	void checkZkConnect(boolean isRunning) throws IOException {
		if (!isDeployactorService) {
			return;
		}
		initDeployExec();
		if (JsonZkSerializer.checkZkNotNull()) {
			regHostZk();
			initZkTracker();
			regMaster(false);
		} else {
		}
	}

	private void loop() throws IOException {
		boolean zkIsNotNull = JsonZkSerializer.checkZkNotNull();
		LogUtils.info("开始服务守护 ");
		ToolUtil.sleep((long) (Math.round(System.currentTimeMillis()) % 1000 + 100));
		checkZkConnect(false);
		// String logBase = System.getenv("LOGS_BASE");
		// if (logBase != null && !logBase.isEmpty() && new File(logBase + "/installer").exists()) {
		// System.setProperty("user.dir", logBase + "/installer");
		// }
		DeployZkCheck.startCheck();
		while (!stopped) {
			loopTimes++;
			if (loopTimes > 0x7FFFFFFFFFFFFFF0l) {
				loopTimes = 0;
			}
			if (loopTimes % 60 == 0) {
				System.gc();
			}
			ToolUtil.sleep(1000);
			zkIsNotNull = JsonZkSerializer.checkZkNotNull();
			if (!isDeployactorService) {
				continue;
			}
			try {
				if ((loopTimes % 10) == 0) {
					if (!JsonZkSerializer.exists(hostNodePath)) {
						regHostZk();
					}
				}
				if (zkIsNotNull) {
					long snow = System.currentTimeMillis();
					try {
						if (!haveMaster()) {
							regMaster(true);
						} else if (isMaster()) {
						} else {
							if (loopTimes % 60 == 0) {
								LogUtils.info("非mater, 打印守护线程活动日志");
							}
						}
					} finally {
					}
				}
			} catch (Throwable e) {
				if (!this.stopped) {
					LogUtils.warn("守护刷新出现错误：", e);
				}
			}
			if (printThreadInfo) {
				printThreadInfo = false;
				ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
				int noThreads = currentGroup.activeCount();
				Thread[] lstThreads = new Thread[noThreads];
				currentGroup.enumerate(lstThreads);
				for (int i = 0; i < noThreads; i++) {
					LogUtils.info("线程号：" + i + " name: " + lstThreads[i].getName() + " thid:" + lstThreads[i].getId());
				}
				LogUtils.info("==================================================================");
			}
		}
		LogUtils.info("exit daemon server");
		while (!stopSuccess) {
			ToolUtil.sleep(10);
		}
		ToolUtil.sleep(10);
	}

	public synchronized void regMaster(boolean force) throws IOException {
		parseIsDeployHost();
		if (!isDeployAppSrcHost) {// 读取内容并比对
			LogUtils.warn("非安装源主机，不可成为master");
			return;
		}
		if (this.isMaster)
			return;
		if (JsonZkSerializer.exists(Constant.ZK_MANAGER_MASTER)) {
			if (force) {
				JsonZkSerializer.delete(Constant.ZK_MANAGER_MASTER);
			} else {
				List<Object> masterInfo = JsonZkSerializer.deserialize(JsonZkSerializer.readData(Constant.ZK_MANAGER_MASTER), List.class, 1);
				if (masterInfo == null || masterInfo.size() < 3) {
					JsonZkSerializer.delete(Constant.ZK_MANAGER_MASTER);
				} else {
					master.setMaster(masterInfo);
				}
			}
		}
		while (!JsonZkSerializer.exists(Constant.ZK_MANAGER_MASTER)) {
			try {
				List<Object> master = new ArrayList<Object>();
				master.add(hostName);
				master.add(hostIP);
				master.add(DateUtils.format(new Date(System.currentTimeMillis())));
				master.add(this.hashCode() + "");

				final StringBuffer sb = new StringBuffer();

				Map<String, Long> hostTime = new HashMap<String, Long>();
				String[] lines = sb.toString().split("\n");
				String hostName = "";
				for (String line : lines) {
					if (line.indexOf("date") > 0) {
						hostName = line.substring(0, line.indexOf(" "));
					} else if (line.indexOf("time:1") >= 0) {
						long time = Convert.toLong(line.substring(5), 0);
						hostTime.put(hostName, time);
					}
				}
				if (JsonZkSerializer.exists(Constant.ZK_MANAGER_MASTER))
					continue;
				master.add(hostTime);
				String data = JsonZkSerializer.compression(master, 1);
				LogUtils.info("begin reg master with " + master);
				SysVar.getZkClient().createEphemeral(Constant.ZK_MANAGER_MASTER, data);
				// JsonZkSerializer.updateZkData(Constant.ZK_MANAGER_MASTER, master, 1, false);
				LogUtils.info(hostName + " register master success");
				// zkClient.createEphemeral(Constant.ZK_MANAGER_MASTER, JsonZkSerializer.serializes(master));
				this.isMaster = false;
				long nowt = System.currentTimeMillis();
				while (!this.isMaster) {
					ToolUtil.sleep(100);// 等待ZK事件生效
					if (System.currentTimeMillis() - nowt > 3000) {
						this.setMaster(master);
						break;
					}
				}
			} catch (Throwable e) {
				LogUtils.error("reg master " + this.hostName + " error:" + e.getMessage(), e);
				this.setMaster(null);
			}
		}
	}

	void parseIsDeployHost() throws IOException {
		boolean _isDeployHost = isDeployAppSrcHost;
		// SysVar.get
	}

	boolean updateSysConfig() {
		Properties nconf = SysConfig.getConf();
		String masterHosts = nconf.getProperty("masterHosts");
		if (masterHosts == null) {
			masterHosts = "";
		}
		String _masterHosts = masterHosts;
		if (isDeployAppSrcHost) {
			if (masterHosts == null || masterHosts.isEmpty()) {
				masterHosts = master.hostName;
			} else if (("," + masterHosts + ",").indexOf("," + master.hostName + ",") == -1) {
				masterHosts += "," + master.hostName;
			}
		} else if (("," + masterHosts + ",").indexOf("," + master.hostName + ",") >= 0) {
			masterHosts = masterHosts.replace(master.hostName + ",", "").replace("," + master.hostName, "");
		}
		if (!_masterHosts.equals(masterHosts)) {
			nconf.put("masterHosts", masterHosts);
			Object hname = nconf.remove("host.name");
			Object hip = nconf.remove("host.ip");

			JsonZkSerializer.updateZkData(Constant.ZK_BASE_NODE, nconf, 1);
			if (hname != null)
				nconf.put("host.name", hname);
			if (hip != null)
				nconf.put("host.ip", hip);
		}
		return true;
	}

	boolean getSysConfig() {
		if (!JsonZkSerializer.exists(Constant.ZK_BASE_NODE)) {
			return false;
		}
		String data = JsonZkSerializer.readData(Constant.ZK_BASE_NODE);
		Map<String, String> conf = JsonZkSerializer.deserialize(data, Map.class, 1);
		if (data == null || data.isEmpty()) {
			return false;
		}
		if (conf == null) {
			LogUtils.warn("全局配置值为空 zknode:" + Constant.ZK_BASE_NODE);
			return false;
		}
		conf.remove("host.name");
		conf.remove("host.ip");
		SysConfig.getConf().putAll(conf);
		zkDataCompressionType = SysConfig.getZkDataCompressionType();
		zkDataUrlEncode = SysConfig.getZkDataUrlencode();
		DaemonMaster.updateZkConfig = true;
		return true;
	}

	public void stopby(Exception why) {
		LogUtils.error(hostName + " stop by ", why);
		stop();
	}

	public void stopby(String why) {
		LogUtils.info(hostName + " stop by " + why);
		stop();
	}

	/**
	 * 停止一些线程，关闭zk连接
	 */
	public void stop() {
		stopped = true;

		boolean inServ = uiServer != null;
		if (inServ) {
			LogUtils.info("开始停止节点[" + SysConfig.getHostName() + "]！");
		}

		if (uiServer != null) {
			LogUtils.info("beging stop ui server ...");
			uiServer.stop();
			LogUtils.info("UI server stoped");
		}
		if (this.isDeployactorService) {
			LogUtils.info("beging stop proxy server ...");
			// proxyServer.stop();
			LogUtils.info("proxy server stoped");
		}
		if (JsonZkSerializer.checkZkNotNull()) {
			LogUtils.info("beging stop zk client ...");
			this.closeZk();
			LogUtils.info("zk client closed");
		}
		stopSuccess = true;
	}

	public ApiServer getUiServer() {
		return uiServer;
	}

	public void closeZk() {
		synchronized (zkLock) {
			closeZkTracker();
		}
		SysVar.closeZkClient();
	}

	public ZkClient getZkClient() {
		return SysVar.getZkClient();
	}

	public void initZKBaseNode() {
		while (true) {
			try {
				JsonZkSerializer.createNode(Constant.ZK_BASE_NODE);
				JsonZkSerializer.createNode(Constant.ZK_HOST_LIST);

				updateSysConfig(JsonZkSerializer.getZkClient());
				break;
			} catch (Throwable e) {
				LogUtils.warn("创建安装部署服务根节点失败", e);
				ToolUtil.sleep(2000);
			}
		}
	}

	public static void updateSysConfig(ZkClient zkClient) {
		if (!zkClient.exists(Constant.ZK_BASE_NODE)) {
			JsonZkSerializer.createNode(zkClient, Constant.ZK_BASE_NODE, "", true, true);
		}
		String data = zkClient.readData(Constant.ZK_BASE_NODE);
		if (data != null && !data.isEmpty()) {
			Map<String, String> conf = JsonZkSerializer.deserialize(data, Map.class, 1);
			if (conf != null) {
				String zkNodeUrl = SysConfig.getZkUrl();
				conf.remove("host.name");
				conf.remove("host.ip");
				SysConfig.getConf().putAll(conf);
				if (!zkNodeUrl.equals(SysConfig.getZkUrl())) {
					Properties nconf = SysConfig.getConf();
					Object hname = nconf.remove("host.name");
					Object hip = nconf.remove("host.ip");
					JsonZkSerializer.updateZkData(zkClient, Constant.ZK_BASE_NODE, nconf, 1);
					if (hname != null)
						nconf.put("host.name", hname);
					if (hip != null)
						nconf.put("host.ip", hip);
				}
				zkDataCompressionType = SysConfig.getZkDataCompressionType();
				zkDataUrlEncode = SysConfig.getZkDataUrlencode();
				SysVar.setLogLevel();
				DaemonMaster.updateZkConfig = true;
			} else {
				LogUtils.warn("全局配置值为空 zknode: " + Constant.ZK_BASE_NODE);
			}
		} else {
			Properties nconf = SysConfig.getConf();
			Object hname = nconf.remove("host.name");
			Object hip = nconf.remove("host.ip");
			JsonZkSerializer.updateZkData(zkClient, Constant.ZK_BASE_NODE, nconf, 1);
			if (hname != null)
				nconf.put("host.name", hname);
			if (hip != null)
				nconf.put("host.ip", hip);
		}
	}

	public void initZkTracker() {
		synchronized (this) {
			initZKBaseNode();
			LogUtils.info("zk监听初始化完成");
		}
	}

	void closeZkTracker() {

	}

	public void stop(String hostname) throws IOException {
		if (hostname == null || hostname.trim().equals("")) {
			hostname = hostName;
		}
		String hostData = JsonZkSerializer.readData(hostNodePath);
		Map<String, Object> hdata = JsonZkSerializer.deserialize(hostData, Map.class, 2);
		hdata.put("state", "stop");
		JsonZkSerializer.updateZkData(hostNodePath, hdata, 2, false);
		// zkClient.writeData(Constant.MANAGER_HOST + "/" + hostname, JsonZkSerializer.serializes(hdata));
	}

	public void reloadConfig() throws IOException {
		String sysConfFile = SystemConstant.getSYS_CONF_FILE();
		InputStream in = SystemVariable.getResourceAsStream(sysConfFile);
		if (in != null) {
			Properties conf = new Properties();
			conf.load(in);
			in.close();
			System.getProperties().putAll(conf);
			SystemVariable.getConf().putAll(conf);
			// LogUtils.debug("加载[" + sysConfFile + "]文件OK!");
		}
	}

	void connZk() {
		if (!JsonZkSerializer.checkZkNotNull()) {
			String zkUrl = SysConfig.getZkUrl();
			try {
				if (zkUrl != null && !zkUrl.trim().isEmpty()) {
					SysVar.initZkClient();
					initZKBaseNode();
				}
			} catch (Throwable e) {
				if (!ServiceReqHandler.isRunning() || SysConfig.isDebug) {
					LogUtils.warn("connect to zk[" + zkUrl + "] failed: " + e.getMessage());
				}
			}
		}
	}

	public void initDeployExec() {
		isDeployActor = true;
		synchronized (zkLock) {
			connZk();

			if (!isDeployactorService && JsonZkSerializer.checkZkNotNull()) {
				initZkTracker();
			}
		}
	}

	public boolean zkLock() {
		String thisCode = this.hashCode() + "";
		// try {
		// if (!JsonZkSerializer.exists(DeployExec.taskLockZKPath)) {
		// if (JsonZkSerializer.createNode(DeployExec.taskLockZKPath, thisCode, false))
		// return true;
		// } else {
		// String code = JsonZkSerializer.readData(DeployExec.taskLockZKPath);
		// if (thisCode.equals(code)) {
		// return true;
		// } else {
		// return false;
		// }
		// }
		// } catch (Throwable e) {
		// if (JsonZkSerializer.exists(DeployExec.taskLockZKPath)) {
		// String code = JsonZkSerializer.readData(DeployExec.taskLockZKPath);
		// if (thisCode.equals(code)) {
		// return true;
		// } else {
		// return false;
		// }
		// }
		// }
		return false;
	}

	public void zkUnLock() {
		try {
			// if (JsonZkSerializer.exists(DeployExec.taskLockZKPath))
			// JsonZkSerializer.delete(DeployExec.taskLockZKPath);
		} catch (Throwable e) {
		}
	}

}
