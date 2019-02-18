package com.sobey.mbserver.web.init;

import com.sobey.mbserver.zk.ZKeeperNodeTracker;

public class SysConfigZkTracker extends ZKeeperNodeTracker {

	public SysConfigZkTracker(String node) {
		super(node);
	}

	@Override
	public void start() {
		enableListener(false, true);
		startListener();
		isFirstInit = false;
	}

	@Override
	public void nodeDataChanged(Object data) {
		// DaemonMaster.updateSysConfig(JsonZkSerializer.getZkClient());
		SysVar.setLogLevel();
	}

}
