package com.sobey.base.socket.order;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sobey.base.exception.POException;
import com.sobey.base.socket.OrderHeader;
import com.sobey.base.socket.SCHandler;
import com.sobey.base.util.MD5Hash;
import com.sobey.base.util.ToolUtil;
import com.sobey.mbserver.location.IndexAction;
import com.sobey.mbserver.user.UserInfoDAO;
import com.sobey.mbserver.user.UserInfoPO;

public class LoginOrder extends ServerOrders {
	private static final Log LOG = LogFactory.getLog(LoginOrder.class.getName());

	public LoginOrder(SCHandler handler, OrderHeader header) {
		super(handler, header);
	}

	@Override
	public int process() throws IOException, POException {
		Map<String, Object> data = header.data;
		// 判断登录
		LOG.info("[login]" + header);
		header.isRequest = false;
		String clientType = data.get("clientType").toString();
		if (clientType == null) {// 未知客户端
			header.data.clear();
			header.data.put("error", "未知客户类型不允许登录");
			synchronized (handler) {
				handler.writeData(header);
			}
			this.handler.close();
			return -1;
		} else if (clientType.equals("AppClient")) {
			long initTime = Long.parseLong(header.data.get("initTime").toString());// 当前时间
			String clientNbr = header.data.get("userNbr").toString();
			String passWord = header.data.get("pass").toString();
			UserInfoPO loginUser = UserInfoPO.UserNbrRels.get(clientNbr);
			if (loginUser == null) {
				UserInfoDAO udao = new UserInfoDAO();
				loginUser = udao.queryUser(clientNbr);
				if (loginUser != null) {
					synchronized (UserInfoPO.UserInfos) {
						UserInfoPO.UserNbrRels.put(clientNbr, loginUser);
						UserInfoPO.UserInfos.put(loginUser.getUSER_ID(), loginUser);
					}
				}
			}
			// 判断 用户名密码，读取权限，返回权限数据
			passWord = MD5Hash.getMD5AsHex(passWord.getBytes());
			if (loginUser == null || !loginUser.getPASSWORD().equals(passWord)) {// 用户不存在，未注册
				header.data.clear();
				header.data.put("res", false);
				if (loginUser == null) {
					header.data.put("error", "用户不存在，请先注册。");
				} else {
					header.data.put("error", "密码错误。");
				}
				synchronized (handler) {
					handler.writeData(header);
				}
				ToolUtil.sleep(100);
				this.handler.close();
				return -1;
			}
			this.handler.userInfo = loginUser;
			if (header.data.containsKey("update") && (Boolean) header.data.get("update")) {// upTime
				ConnectOrder.pushSysInfo(handler);
			}
			header.staffId = this.handler.userInfo.getUSER_ID();
			Map<String, Object> params = new HashMap<String, Object>(header.data);
			header.data.clear();
			header.data.put("res", true);
			header.data.put("userInfo", this.handler.userInfo);// 用户信息
			header.data.put("right", "");// 权限
			LOG.info("[login]" + header.staffId + " login Successful " + (new Date(initTime).toLocaleString()));
			synchronized (handler) {
				handler.writeData(header);
			}
			this.handler.clientLoginTime = initTime;
			this.handler.clientId = handler.userInfo.getUSER_ID();
			// 写登录日志
			IndexAction.writeLog(header.staffId, params);
			return 0;
		}
		return -1;
	}
}
