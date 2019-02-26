package com.sobey.mbserver.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sobey.jcg.support.log4j.LogUtils;
import com.sobey.mbserver.main.DaemonMaster;
import com.sobey.mbserver.util.JsonFormat;
import com.sobey.mbserver.util.ToolUtil;
import com.sobey.mbserver.web.init.SysConfig;
import com.sobey.mbserver.zk.JsonZkSerializer;

public class ZKAction {
	DaemonMaster master = DaemonMaster.master;
	Object metux = new Object();

	// 客户端有调用
	public boolean checkZk() {
		if (JsonZkSerializer.getZkClient() == null) {
			return false;
		} else {
			return true;
		}
	}

	List<String> getChild(String path) {
		return getChild(path, true);
	}

	public List<String> getChild(String path, boolean all) {
		try {
			if (!JsonZkSerializer.checkZkNotNull()) {
				return null;
			}
			synchronized (metux) {
				if (JsonZkSerializer.exists(path)) {
					if (!all && (path.equals("") || path.equals("/"))) {
						List<String> list = new ArrayList<String>();
						list.add(SysConfig.getZkBaseZone());
						return list;
					} else {
						List<String> list = JsonZkSerializer.getChildren(path);
						return list;
					}
				}
			}
		} catch (Throwable e) {
			LogUtils.error("ZKAction getData(" + path + ")", e);
		}
		return null;
	}

	public Object[] getData(String path, boolean format) {
		try {
			if (!JsonZkSerializer.checkZkNotNull()) {
				return new Object[] { "not connection zk" };
			}
			synchronized (metux) {
				if (JsonZkSerializer.exists(path)) {
					String data = JsonZkSerializer.readData(path);
					if (data == null) {
						return new Object[] { "" };
					} else {
						int com = 0;
						try {
							data = ToolUtil.deserializeString(data, true, DaemonMaster.zkDataUrlEncode);
							com = 3;
						} catch (IOException e) {
						}
						if (format && (data.startsWith("{") || data.startsWith("[")) && data.indexOf("\n") == -1) {
							data = JsonFormat.formatJson(data.trim());
						}
						return new Object[] { data, com };
					}
				}
			}
		} catch (Throwable e) {
			LogUtils.error("ZKAction getData(" + path + ")", e);
		}
		return null;
	}

	public String setData(String path, String obj, int com) {
		try {
			if (!JsonZkSerializer.checkZkNotNull()) {
				return "not connection zk";
			}
			synchronized (metux) {
				JsonZkSerializer.updateZkData(path, obj, com);
			}
			return "ok";
		} catch (Throwable e) {
			LogUtils.error("ZKAction setData(" + path + ")", e);
			return e.getMessage();
		}
	}

	public String rmrNode(String path) {
		try {
			if (!JsonZkSerializer.checkZkNotNull()) {
				return "not connection zk";
			}
			synchronized (metux) {
				JsonZkSerializer.delete(path);
			}
			return "ok";
		} catch (Throwable e) {
			LogUtils.error("ZKAction rmrNode(" + path + ")", e);
			return e.getMessage();
		}
	}

	public String rmrChildNode(String path) {
		try {
			if (!JsonZkSerializer.checkZkNotNull()) {
				return "not connection zk";
			}
			synchronized (metux) {
				List<String> list = getChild(path);
				for (String string : list) {
					JsonZkSerializer.delete(path + "/" + string);
				}
			}
			return "ok";
		} catch (Throwable e) {
			LogUtils.error("ZKAction rmrChildNode(" + path + ")", e);
			return e.getMessage();
		}
	}

	public String formatJson(String json, boolean format) {
		json = json.trim();
		if (format) {
			if ((json.startsWith("{") || json.startsWith("["))) {
				return JsonFormat.formatJson(json);
			} else {
				return json;
			}
		} else {
			if (json.startsWith("{")) {
				Map<String, Object> map = JsonZkSerializer.deserialize(json, Map.class);
				if (map != null) {
					String res = JsonZkSerializer.serializes(map);
					if (res != null) {
						return res;
					}
				}
			} else if (json.startsWith("[")) {
				List<Object> list = JsonZkSerializer.deserialize(json, List.class);
				if (list != null) {
					String res = JsonZkSerializer.serializes(list);
					if (res != null) {
						return res;
					}
				}
			} else {
				return json;
			}
		}
		return json;
	}
}
