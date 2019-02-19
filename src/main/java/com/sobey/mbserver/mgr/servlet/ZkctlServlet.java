package com.sobey.mbserver.mgr.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sobey.jcg.sobeyhive.main.DaemonMaster;
import com.sobey.jcg.support.utils.Convert;
import com.sobey.jcg.support.utils.StringUtils;
import com.sobey.mbserver.web.ServiceReqHandler;
import com.sobey.mbserver.web.ZKAction;
import com.sobey.mbserver.web.init.Constant;
import com.sobey.mbserver.web.init.SysConfig;
import com.sobey.mbserver.zk.JsonZkSerializer;

public class ZkctlServlet extends ServiceReqHandler {
	private static final long serialVersionUID = 8899869527456708333L;
	final String parPath;
	static Object mutex = new Object();
	static String cntPath;

	public static Class<? extends ServiceReqHandler> getCLass(String path) {
		cntPath = path;
		return ZkctlServlet.class;
	}

	public ZkctlServlet(String parPath) {
		this.parPath = parPath;
		this.master = DaemonMaster.master;
	}

	public String doHttp(HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (!JsonZkSerializer.checkZkNotNull()) {
			throw new IOException("not connection zk");
		}
		String path = ((HttpServletRequest) request).getRequestURI();
		Map<String, Object> params = getBody(request);
		if (params == null) {
			params = new HashMap<String, Object>();
			String zkpath = request.getParameter("path");
			if (zkpath != null) {
				params.put("path", zkpath);
			}
			String data = request.getParameter("data");
			if (data != null) {
				params.put("data", data);
			}
			String format = request.getParameter("format");
			if (format != null) {
				params.put("format", format);
			}
		}
		String zkpath = SysConfig.getMapValue(params, "path", "");
		if (zkpath == null) {
			throw new IOException("未传入zk操作路径");
		}
		path = path.replaceAll("//", "/");

		ZKAction zk = new ZKAction();
		if (path.equals(parPath + "get")) {//
			boolean format = Convert.toBool(params.get("format"), false);
			Object[] data = zk.getData(zkpath, format);
			if (data != null) {
				return data[0].toString();
			} else {
				throw new IOException("getData error");
			}
		} else if (path.equals(parPath + "set")) {//
			String data = SysConfig.getMapValue(params, "data", "");
			int com = 0;
			if (JsonZkSerializer.exists(zkpath)) {
				Object[] sdata = zk.getData(zkpath, false);
				if (sdata != null) {
					com = Convert.toInt(sdata[1], com);
				}
			} else if (zkpath.startsWith(Constant.ZK_BASE_NODE)) {
				com = DaemonMaster.zkDataCompressionType & 1;
			}
			return zk.setData(zkpath, data, com);
		} else if (path.equals(parPath + "rmr")) {//
			return zk.rmrNode(zkpath);
		} else if (path.equals(parPath + "rmrChild")) {//
			return zk.rmrChildNode(zkpath);
		} else if (path.equals(parPath + "ls")) {//
			return StringUtils.join(JsonZkSerializer.getChildren(zkpath), ",");
		}
		return "not support path";
	}

}
