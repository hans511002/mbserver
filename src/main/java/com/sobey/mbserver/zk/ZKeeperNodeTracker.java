package com.sobey.mbserver.zk;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.apache.zookeeper.Watcher.Event.KeeperState;

import com.sobey.jcg.support.log4j.LogUtils;
import com.sobey.mbserver.web.init.Constant;

public abstract class ZKeeperNodeTracker {

	/** Path of node being tracked */
	protected String path;
	protected boolean listenerChild = true;// 是否监听数据
	private IZkChildListener zkChildListener;
	protected boolean listenerData = true;// 是否监听数据
	private IZkDataListener zkDataListener;
	private static IZkStateListener stateListener;
	protected boolean started = false;// 是否已经启动监听
	protected boolean isFirstInit = true;
	public static Object mutex = new Object();

	protected Map<String, ZKeeperNodeTracker> subPaths = new HashMap<String, ZKeeperNodeTracker>();

	public String getPath() {
		return path;
	}

	/**
	 * Constructs a new ZK node tracker.
	 * 
	 * @param node
	 * @param zkClient
	 * @param daemonMaster
	 */
	public ZKeeperNodeTracker(String node) {
		if (JsonZkSerializer.getZkClient() == null) {
			throw new RuntimeException("ZkClient must not null");
		}
		if (node.startsWith("/")) {
			this.path = node;
		} else {
			this.path = Constant.ZK_BASE_NODE + "/" + node;
		}
	}

	public boolean isFirstInited() {
		return isFirstInit;
	}

	/**
	 * 确定哪些需要监听
	 * 
	 * @param child
	 *            是否监听子节点变更
	 * @param data
	 *            是否监听数据
	 */
	public void enableListener(boolean child, boolean data) {
		listenerChild = child;
		listenerData = data;
	}

	/**
	 * Starts the tracking of the node in ZooKeeper.
	 */
	public synchronized void startListener() {
		if (!started) {// && master.isDeployactorService
			if (listenerChild) {
				initZkChildListener();
				LogUtils.debug("init ZkTracker Child " + path);
				JsonZkSerializer.getZkClient().subscribeChildChanges(path, zkChildListener);
			}
			if (listenerData) {
				initZkDataListener();
				LogUtils.debug("init ZkTracker data " + path);
				JsonZkSerializer.getZkClient().subscribeDataChanges(path, zkDataListener);
			}
			started = true;
			synchronized (mutex) {
				if (stateListener == null) {
					stateListener = new IZkStateListener() {
						@Override
						public void handleStateChanged(KeeperState arg0) throws Exception {
							// if (master.isDeployactorService) {
							LogUtils.warn("zk stateListener handleStateChanged:" + arg0.toString());
							// }

							// if (KeeperState.Disconnected.equals(arg0) || KeeperState.Expired.equals(arg0)) {
							// new HasThread() {
							// @Override
							// public void run() {
							// LogUtils.warn("zk Disconnected: wait reConnection");
							// synchronized (master.zkLock) {
							// master.closeZk();
							// if (!master.isDeployactorService) {
							// master.initDeployExec();
							// }
							// }
							// }
							// }.setDaemon(true).setName("zk_reConnection").start();
							// }
						}

						@Override
						public void handleSessionEstablishmentError(Throwable arg0) throws Exception {
							// master.closeZk();
							// if (master.isDeployactorService) {
							LogUtils.warn("zk stateListener handleSessionEstablishmentError:", arg0);
							// }
						}

						@Override
						public void handleNewSession() throws Exception {
							// master.closeZk();
							LogUtils.info("zk stateListener handleNewSession");
						}
					};
					JsonZkSerializer.getZkClient().subscribeStateChanges(stateListener);
				}
			}
		}
	}

	public synchronized void stopListener() {
		if (listenerChild) {
			JsonZkSerializer.getZkClient().unsubscribeChildChanges(path, zkChildListener);
		}
		if (listenerData) {
			JsonZkSerializer.getZkClient().unsubscribeDataChanges(path, zkDataListener);
		}
		if (stateListener != null) {
			JsonZkSerializer.getZkClient().unsubscribeStateChanges(stateListener);
		}
		started = false;
	}

	public static class ZkChildListener implements IZkChildListener {
		ZKeeperNodeTracker tracker;
		List<String> strings = null;

		public ZkChildListener(ZKeeperNodeTracker zKeeperNodeTracker) {
			this.tracker = zKeeperNodeTracker;
		}

		@Override
		public void handleChildChange(String s, List<String> strings) throws Exception {
			if (!tracker.path.equals(s)) {
				return;
			}
			try {
				// if (tracker.master.isDeployactorService) {
				// if (SysConfig.isDebug) {
				// if (this.strings == null) {
				// this.strings = new ArrayList<String>();
				// }
				// LogUtils.info(tracker.getClass().getCanonicalName() + " 子节点变更:" + s + "-->\n" +
				// StringUtils.join(this.strings, ",") + " ==> "
				// + StringUtils.join(strings, ","));
				// this.strings = strings;
				// } else {
				// LogUtils.info(tracker.getClass().getCanonicalName() + " 子节点变更:" + s + "-->" +
				// StringUtils.join(strings, ","));
				// }
				// }
				tracker.nodeChildrenChanged(strings);
			} catch (Throwable e) {
				LogUtils.error("handleChildChange path=" + s + "  subs=" + strings, e);
			}
		}
	}

	public static class ZkDataListener implements IZkDataListener {
		ZKeeperNodeTracker tracker;

		public ZkDataListener(ZKeeperNodeTracker zKeeperNodeTracker) {
			this.tracker = zKeeperNodeTracker;
		}

		@Override
		public void handleDataChange(String s, Object o) throws Exception {
			if (tracker.path.equals(s)) {
				// if (tracker.master.isDeployactorService) {
				// if (!tracker.path.startsWith(ZkConstant.APP_STATUS)) {
				// LogUtils.info(tracker.getClass().getCanonicalName() + " " + tracker.path + " dataChanged");
				// } else {
				// LogUtils.debug(tracker.getClass().getCanonicalName() + " " + tracker.path + " dataChanged");
				// }
				// }
				// if (master.enableZkEvent() && LogUtils.debugEnabled())
				// LogUtils.debug(path + "====>" + o);
				try {
					tracker.nodeDataChanged(o);
				} catch (Throwable e) {
					LogUtils.error(tracker.getClass().getCanonicalName() + " nodeDataChanged path=" + s, e);
				}
			} else if (s.startsWith(tracker.path + "/")) {// 子节点数据变更
				String node = s.substring(tracker.path.length() + 1);
				// if (node.indexOf("/") == -1) {
				// if (tracker.master.isDeployactorService) {
				// if (!tracker.path.startsWith("")) {
				// LogUtils.info(tracker.getClass().getCanonicalName() + " " + s + " dataChanged");
				// } else {
				// LogUtils.debug(tracker.getClass().getCanonicalName() + " " + s + " dataChanged");
				// }
				// }
				// if (master.enableZkEvent() && LogUtils.debugEnabled())
				// LogUtils.debug(path + "/" + node + "====>" + o);
				try {
					tracker.subNodeDataChanged(node, o);
				} catch (Throwable e) {
					LogUtils.error(tracker.getClass().getCanonicalName() + " subNodeDataChanged path=" + s, e);
				}// }
			}
		}

		@Override
		public void handleDataDeleted(String s) throws Exception {
			// if (tracker.master.isDeployactorService) {
			// LogUtils.info(tracker.getClass().getCanonicalName() + " [" + s + "]节点删除");
			// }
			if (tracker.path.equals(s)) {
				tracker.nodeDeleted();
			} else if (s.startsWith(tracker.path + "/")) {// 子节点数据变更
				String node = s.substring(tracker.path.length() + 1);
				if (node.indexOf("/") == -1) {
					tracker.subNodeDeleted(node);
				}
			}
		}
	}

	void initZkChildListener() {
		if (zkChildListener != null) {
			return;
		}
		zkChildListener = new ZkChildListener(this);

	}

	void initZkDataListener() {
		if (zkDataListener != null) {
			return;
		}
		zkDataListener = new ZkDataListener(this);
	}

	public synchronized void startSubListener(String subNode) {
		if (!subPaths.containsKey(subNode)) {
			subPaths.put(subNode, this);
			if (zkDataListener == null) {
				initZkDataListener();
			}
			JsonZkSerializer.getZkClient().subscribeDataChanges(path + "/" + subNode, zkDataListener);
		}
	}

	public synchronized void stopSubListener(String subNode) {
		if (subPaths.containsKey(subNode) && zkDataListener != null) {
			subPaths.remove(subNode);
			JsonZkSerializer.getZkClient().unsubscribeDataChanges(path + "/" + subNode, zkDataListener);
		}
	}

	public abstract void start();

	public void stop() {
		try {
			Map<String, ZKeeperNodeTracker> subPaths = new HashMap<String, ZKeeperNodeTracker>(this.subPaths);
			for (String pathNode : subPaths.keySet()) {
				try {
					stopSubListener(pathNode);
				} catch (Throwable e) {
				}
			}
		} catch (Throwable e) {
		}
		try {
			stopListener();
		} catch (Throwable e) {
		}
		subPaths.clear();
		started = false;
		LogUtils.debug(this.getClass().getSimpleName() + " stoped node:" + path);
	}

	public boolean isStarted() {
		return started;
	}

	// 子节点变更
	public void nodeChildrenChanged(List<String> childs) {
	}

	// 节点数据变更
	public void subNodeDataChanged(String node, Object data) {
	}

	public void subNodeDeleted(String node) {

	}

	// 节点数据变更
	public void nodeDataChanged(Object data) {
	}

	// 节点数据删除
	public void nodeDeleted() {
	}

}
