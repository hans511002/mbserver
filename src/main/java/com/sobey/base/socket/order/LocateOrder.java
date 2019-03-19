package com.sobey.base.socket.order;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sobey.base.SystemInit;
import com.sobey.base.exception.POException;
import com.sobey.base.socket.Order;
import com.sobey.base.socket.OrderHeader;
import com.sobey.base.socket.SCHandler;
import com.sobey.jcg.support.utils.Convert;
import com.sobey.mbserver.location.StaffLocInfoAction;
import com.sobey.mbserver.location.StaffLocInfoDAO;

public class LocateOrder extends ServerOrders {
	private static final Log LOG = LogFactory.getLog(LocateOrder.class.getName());

	public LocateOrder(SCHandler handler, OrderHeader header) {
		super(handler, header);
	}

	// 处理请求数据，并返回结果
	public int process() throws IOException {
		if (header.isRequest) {// 本身是服务端，接受来自查询客户端的请求
			String locateUsers = header.data.get("userList").toString();
			String users[] = locateUsers.split(",");
			long userIds[] = new long[users.length];
			for (int i = 0; i < userIds.length; i++) {
				userIds[i] = Convert.toLong(users[i], 0);
			}
			// 发命令到对应用户手机客户端上
			int onlineLen = 0;
			int offlineLen = 0;
			String onlineUserList = "";
			String offlineUserList = "";
			for (int i = 0; i < userIds.length; i++) {
				if (userIds[i] == 0)
					continue;
				SCHandler hand = this.handler.listener.getListen().handles.get(userIds[i]);
				if (hand != null) {
					OrderHeader serverQueryHeader = new OrderHeader();// 生成新的header请求
					serverQueryHeader.isRequest = true;
					serverQueryHeader.order = Order.locate;
					serverQueryHeader.staffId = userIds[i];
					serverQueryHeader.data.put("LOCATE_USER_ID", header.staffId);// 保留请求的用户，用于返回信息到客户端
					synchronized (hand) {
						hand.writeData(serverQueryHeader);
					}
					onlineLen++;
					onlineUserList += userIds[i] + ",";
				} else {
					offlineUserList += userIds[i] + ",";
					offlineLen++;
				}
			}
			synchronized (this.handler) {
				header.data.clear();
				header.isRequest = false;
				header.data.put("msg", "有" + onlineLen + "人在线，已发送定位请求," + offlineLen + "不在线");
				header.data.put("onlineUsers", onlineUserList.length() > 0 ? onlineUserList.substring(0, onlineUserList.length() - 1) : "");
				header.data.put("offlineUsers", offlineUserList.substring(0, offlineUserList.length() - 1));
				handler.writeData(header);
			}
		} else {// 客户端的回复
			Long LUSID = (Long) header.data.get("LOCATE_USER_ID");
			if (LUSID != null) {// 是回复定位请求
				long locateUserId = LUSID;
				SCHandler hand = this.handler.listener.getListen().handles.get(locateUserId);
				if (hand != null) {
					header.data.remove("LOCATE_USER_ID");
					header.isRequest = false;
					header.order = Order.locate;
					header.data.put("ISLOCATE_USER_ID", header.staffId);
					header.staffId = locateUserId;
					synchronized (hand) {
						handler.writeData(header);
					}
				}
			}
		}
		return 0;
	}

	public void insertToDb(long staffId, String mobileNum, double lng, double lat, int radius, String errMsg) {
		StaffLocInfoAction slaction = new StaffLocInfoAction();
		StaffLocInfoDAO slDao = null;
		try {
			slDao = new StaffLocInfoDAO();
			slaction.setComDao(slDao);
			slaction.receive(staffId, mobileNum, 5, System.currentTimeMillis(), lng, lat, radius, errMsg);
		} catch (POException e) {
			LOG.error("", e);
		} catch (SQLException e) {
			LOG.error("", e);
		} finally {
			try {
				if (slDao != null)
					slDao.close();
			} catch (Exception e) {
			}
		}
	}

	// 返回错误信息，正确返回NULL
	public static String sendLocateOrder(long staffId) {
		SCHandler hand = SystemInit.listener.handles.get(staffId);
		if (hand != null) {
			OrderHeader serverQueryHeader = new OrderHeader();
			serverQueryHeader.isRequest = true;
			serverQueryHeader.order = Order.locate;
			serverQueryHeader.staffId = staffId;
			serverQueryHeader.data.put("LOCATE_USER_ID", 0);// 系统请求置0
			try {
				synchronized (hand) {
					hand.writeData(serverQueryHeader);
				}
				return null;
			} catch (IOException e) {
				LOG.error("发送定位用户[" + staffId + "]的命令失败：", e);
				return e.getMessage();
			} // 发送查询命令到 spoutQuery
		} else {
			return "用户未在线";
		}
	}
}
