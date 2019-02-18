package com.sobey.mbserver.web.init;

public class Constant extends InstallConstant {

	//
	public static final String KEY_ZK_CONNECT = "zk.connect";// zk连接
	public static final String KEY_ZK_BASE_NODE = "zk.base.node";
	public static final String KEY_APP_NAME = "app.name";
	public static final String KEY_HOST_NAME = "host.name";// 集群当前机器IP
	public static final String KEY_HOST_IP = "host.ip";// 集群当前机器IP

	public static final String KEY_ZK_CONNECT_TIMEOUT_MS = "zk.connect.timeout.ms";
	public static final String KEY_ZK_SESSION_TIMEOUT_MS = "zk.session.timeout.ms";
	public static final String KEY_ZK_DATA_COMPRESSION_TYPE = "zk.data.compression.type";
	public static final String KEY_ZK_DATA_URLENCODE_ENABLED = "zk.data.urlencode.enabled";

	// ZK节点
	public static final String ZK_BASE_NODE = "/" + SysVar.getZkBaseZone();// 跟节点
	public static final String ZK_MANAGER_MASTER = ZK_BASE_NODE + "/master";// 临时节点，master节点信息
	public static final String ZK_HOST_LIST = ZK_BASE_NODE + "/hostList";

}
