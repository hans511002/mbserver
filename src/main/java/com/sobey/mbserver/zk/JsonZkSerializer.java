package com.sobey.mbserver.zk;

import java.io.IOException;
import java.util.List;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sobey.base.exception.ZKDataException;
import com.sobey.base.util.ToolUtil;
import com.sobey.jcg.support.log4j.LogUtils;
import com.sobey.mbserver.web.init.SysVar;

public class JsonZkSerializer implements ZkSerializer {
	public static ObjectMapper objectMapper = new ObjectMapper();

	public JsonZkSerializer() {
	}

	@Override
	public byte[] serialize(Object o) throws ZkMarshallingError {
		try {
			String json = objectMapper.writeValueAsString(o);
			return json.getBytes();
		} catch (IOException e) {
			LogUtils.error("serialize " + o, e);
			return null;
		}
	}

	@Override
	public Object deserialize(byte[] bytes) throws ZkMarshallingError {
		try {
			String json = new String(bytes);
			return objectMapper.readValue(json, Object.class);
		} catch (IOException e) {
			LogUtils.error("deserialize ", e);
		}
		return null;
	}

	public static <T> T deserialize(byte[] bytes, Class<T> claz) throws ZkMarshallingError {
		if (bytes == null)
			return null;
		return deserialize(new String(bytes), claz);
	}

	public static <T> T deserialize(String json, Class<T> claz) throws ZkMarshallingError {
		return deserialize(json, claz, 0);
	}

	public static String serializes(Object o) throws ZkMarshallingError {
		try {
			if (o instanceof String) {
				return (String) o;
			}
			String json = objectMapper.writeValueAsString(o);
			return json;
		} catch (IOException e) {
			LogUtils.error("serializes data error:" + o, e);
			return null;
		}
	}

	public static <T> T getZkData(String path, Class<T> claz) throws IOException {
		return getZkData(path, claz, 0);
	}

	public static <T> T getZkData(String path, Class<T> claz, int dataType) {
		if (!exists(path)) {
			return null;
		}
		Object data = SysVar.getZkClient().readData(path);
		if (data == null) {
			return null;
		}
		return deserialize(data, claz, dataType);
	}

	public static boolean checkZkNotNull() {
		// if (JsonZkSerializer.getZkClient() == null) {
		// return false;
		// } else {
		// return true;
		// }
		return JsonZkSerializer.getZkClient() != null;
	}

	public static ZkClient getZkClient() {
		return SysVar.getZkClient();
	}

	public static boolean delete(String path) {
		return SysVar.getZkClient().deleteRecursive(path);
	}

	public static boolean exists(String path) {
		return SysVar.getZkClient().exists(path);
	}

	public static String readData(String path) {
		return SysVar.getZkClient().readData(path);
	}

	public static List<String> getChildren(String path) {
		ZkClient zk = SysVar.getZkClient();
		if (zk == null) {
			throw new ZKDataException("ZK connection disconnected");
		}
		return zk.getChildren(path);
	}

	public static boolean createNode(String path) {
		return createNode(path, "", true, true);
	}

	public static boolean createNode(String path, String data) {
		return createNode(path, data, true, false);
	}

	public static boolean createNode(String path, boolean pars) {
		return createNode(path, "", true, pars);
	}

	public static boolean createNode(String path, boolean pars, boolean isPersistent) {
		return createNode(path, "", isPersistent, pars);
	}

	public static boolean createNodes(String path) {
		return createNode(path, "", true, true);
	}

	public static boolean createNodes(String path, String data) {
		return createNode(path, data, true, true);
	}

	public static boolean createNodes(String path, boolean isPersistent) {
		return createNode(path, "", true, true);
	}

	public static boolean createNode(String path, String data, boolean isPersistent) {
		return createNode(SysVar.getZkClient(), path, data, isPersistent, false);
	}

	public static boolean createNode(String path, String data, boolean isPersistent, boolean pars) {
		return createNode(SysVar.getZkClient(), path, data, isPersistent, pars);
	}

	public static boolean createNode(ZkClient zkClient, String path, String data, boolean isPersistent, boolean pars) {
		synchronized (zkClient) {
			if (!zkClient.exists(path)) {
				if (pars) {
					String parPath = path.substring(0, path.lastIndexOf("/"));
					if (!parPath.isEmpty()) {
						String parPaths[] = parPath.substring(1).split("/");
						parPath = "";
						for (String nde : parPaths) {
							parPath += "/" + nde;
							if (!zkClient.exists(parPath)) {
								zkClient.createPersistent(parPath);
							}
						}
					}
				}
				if (isPersistent) {
					zkClient.createPersistent(path, data);
					return true;
				} else {
					zkClient.createEphemeral(path, data);
					return true;
				}
			} else {
				return false;
			}
		}
	}

	public static void updateZkData(String path, String data) {
		updateZkData(path, data, 0, true);
	}

	public static void updateZkData(String path, Object data) {
		updateZkData(path, data, 0, true);
	}

	public static void updateZkData(String path, Object data, int dataType) {
		updateZkData(path, data, dataType, true);
	}

	public static void updateZkData(String path, Object data, int dataType, boolean isPersistent) {
		String str = "";
		if (data != null) {
			if (data instanceof String) {
				str = (String) data;
			} else {
				str = serializes(data);
			}
		}
		updateZkData(path, str, dataType, isPersistent);
	}

	public static void updateZkData(String path, String cnt, int dataType, boolean isPersistent) {
		updateZkData(getZkClient(), path, cnt, dataType, isPersistent);
	}

	public static void updateZkData(ZkClient zkClient, String path, Object obj, int dataType) {
		String cnt = "";
		if (obj != null) {
			cnt = serializes(obj);
		}
		updateZkData(zkClient, path, cnt, dataType, true);
	}

	public static void updateZkData(ZkClient zkClient, String path, Object obj, int dataType, boolean isPersistent) {
		String cnt = "";
		if (obj != null) {
			cnt = serializes(obj);
		}
		updateZkData(zkClient, path, cnt, dataType, isPersistent);
	}

	public static void updateZkData(ZkClient zkClient, String path, String cnt, int dataType) {
		updateZkData(getZkClient(), path, cnt, dataType, true);
	}

	public static void updateZkData(ZkClient zkClient, String path, String cnt, int dataType, boolean isPersistent) {
		if ((SysVar.getZkDataCompType() & dataType) > 0 && !cnt.isEmpty()) {
			try {
				cnt = ToolUtil.serialObject(cnt, true, SysVar.getZkDataUrlEncode());
			} catch (IOException e) {
				LogUtils.error("压缩：" + cnt, e);
				return;
			}
		}
		if (cnt.length() > 1024000) {
			LogUtils.error("Zk节点" + path + "数据大于1M:" + cnt.length());
			throw new ZKDataException("Zk数据大于1M");
		}
		synchronized (zkClient) {
			if (zkClient.exists(path)) {
				zkClient.writeData(path, cnt);
			} else {
				createNode(zkClient, path, cnt, isPersistent, true);
				// if (isPersistent) {
				// zkClient.createPersistent(path, cnt);
				// } else {
				// zkClient.createEphemeral(path, cnt);
				// }
			}
		}
	}

	public static String compression(Object obj, int dataType) {
		return compression(serializes(obj), dataType);
	}

	public static String compression(String cnt, int dataType) {
		if ((SysVar.getZkDataCompType() & dataType) > 0 && !cnt.isEmpty()) {
			try {
				return ToolUtil.serialObject(cnt, true, SysVar.getZkDataUrlEncode());
			} catch (IOException e) {
				LogUtils.error("压缩：" + cnt, e);
			}
		}
		return cnt;
	}

	/**
	 * @param dataType
	 *            1:状态数据 2:配置数据
	 */
	public static <T> T deserialize(Object json, Class<T> claz, int dataType) throws ZkMarshallingError {
		if (json == null)
			return null;
		return deserialize(json.toString(), claz, dataType);
	}

	public static String decompression(String cnt, int dataType) {
		return decompression(cnt, dataType, true);
	}

	public static String decompression(String cnt, int dataType, boolean errLog) {
		return decompression(cnt, dataType, false, errLog);
	}

	public static String decompression(String cnt, int dataType, boolean forceCom, boolean errLog) {
		if (cnt == null)
			return null;
		if ((forceCom || (SysVar.getZkDataCompType() & dataType) > 0) && !cnt.isEmpty()) {
			try {
				cnt = ToolUtil.deserializeString(cnt, true, SysVar.getZkDataUrlEncode());
			} catch (IOException e) {
				if (errLog) {
					LogUtils.error("decompression " + cnt, e);
				}
			}
		}
		return cnt;
	}

	public static <T> T deserialize(String json, Class<T> claz, int dataType) throws ZkMarshallingError {
		return deserialize(json, claz, dataType, true);
	}

	public static <T> T deserialize(String json, Class<T> claz, int dataType, boolean errLog) throws ZkMarshallingError {
		return deserialize(json, claz, dataType, false, errLog);
	}

	public static <T> T deserialize(String cnt, Class<T> claz, int dataType, boolean forceCom, boolean errLog) throws ZkMarshallingError {
		try {
			if (cnt == null)
				return null;
			if (cnt.isEmpty()) {
				if (claz.equals(String.class)) {
					return (T) "";
				} else {
					return null;
				}
			}
			cnt = decompression(cnt, dataType, forceCom, errLog);
			// json = json.replaceAll("\\$", "\\$");
			if (claz.equals(String.class)) {
				return (T) cnt;
			}
			return objectMapper.readValue(cnt, claz);
		} catch (IOException e) {
			LogUtils.error("deserialize error:", e);
		}
		return null;
	}
}
