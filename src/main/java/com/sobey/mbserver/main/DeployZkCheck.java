package com.sobey.mbserver.main;

import com.sobey.jcg.support.log4j.LogUtils;
import com.sobey.jcg.support.utils.Utils;
import com.sobey.mbserver.util.HasThread;
import com.sobey.mbserver.web.init.Constant;
import com.sobey.mbserver.web.init.SysVar;
import com.sobey.mbserver.zk.JsonZkSerializer;

public class DeployZkCheck extends HasThread {
	protected final DaemonMaster master;
	static boolean checkRes = false;
	static long checkTime = 0;
	public static DeployZkCheck zkCheckThread = null;
	static long statusTimeout = SysVar.getZkStatusTimeout();
	static Object mutex = new Object();
	static boolean inCheckRunning = false;

	public DeployZkCheck(DaemonMaster master) {
		this.master = master;
	}

	public static void startCheck() {
		synchronized (mutex) {
			if (zkCheckThread == null) {
				zkCheckThread = new DeployZkCheck(DaemonMaster.master);
				zkCheckThread.setDaemon(true).setName("zkCheckThread").start();
			}
		}
	}

	@Override
	public void run() {
		while (!master.stopped) {
			try {
				inCheckRunning = false;
				synchronized (mutex) {
					mutex.wait();
				}
				checkRes = false;
				inCheckRunning = true;
				statusTimeout = SysVar.getZkStatusTimeout();
				checkRes = JsonZkSerializer.exists(Constant.ZK_BASE_NODE);
				checkTime = System.currentTimeMillis();
			} catch (Throwable e) {
				LogUtils.error("ZkCheck error:" + e.getMessage(), e);
				master.closeZk();
			}
		}
	}

	public static boolean checkTimeout(long timeout) {
		if (zkCheckThread == null) {
			startCheck();
			Utils.sleep(10);
		}
		long nowTime = System.currentTimeMillis();
		if (checkRes && nowTime - checkTime < statusTimeout) {
			return checkRes;
		}
		if (!inCheckRunning) {
			synchronized (mutex) {
				mutex.notify();
			}
		}
		if (checkRes && nowTime - checkTime < statusTimeout) {
			return checkRes;
		}
		nowTime = System.currentTimeMillis();
		while ((System.currentTimeMillis() - nowTime) <= timeout) {
			if (checkRes)
				return checkRes;
			Utils.sleep(200);
		}
		return checkRes;
	}
}
