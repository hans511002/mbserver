package com.sobey.base.socket.order;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sobey.base.socket.Order;
import com.sobey.base.socket.OrderHeader;
import com.sobey.base.socket.SCHandler;
import com.sobey.jcg.support.utils.Convert;

public class MessageOrder extends ServerOrders {
	public static int maxDelayLength = 100;

	public static class Msg {
		public long staffId;
		public long time;
		public String msg;

		public Msg(long staffId, long time, String msg) {
			this.staffId = staffId;
			this.time = time;
			this.msg = msg;
		}
	}

	public static class SmsMsg extends Msg {
		public SmsMsg(long staffId, long time, String msg, String nbr) {
			super(staffId, time, msg);
			this.nbr = nbr;
		}

		public String nbr = null;
	}

	public static Map<Long, List<Msg>> Delays = new HashMap<Long, List<Msg>>();

	public MessageOrder(SCHandler handler, OrderHeader header) {
		super(handler, header);
	}

	@Override
	public int process() throws IOException {
		if (header.isRequest) {// 本身是服务端，接受来自查询客户端的请求
			if (header.data.get("msg") == null) {
				header.data.clear();
				header.isRequest = false;
				header.data.put("error", "发送消息不能为空");
				synchronized (handler) {
					handler.writeData(header);
				}
				return 0;
			}
			boolean isSingle = false;
			if (header.data.containsKey("toUserId")) {
				isSingle = true;
			}
			String msg = header.data.get("msg").toString();
			long userIds[] = null;
			if (isSingle) {
				userIds = new long[] { (Long) header.data.get("toUserId") };
			} else {
				String locateUsers = header.data.get("userList").toString();
				String users[] = locateUsers.split(",");
				userIds = new long[users.length];
				for (int i = 0; i < userIds.length; i++) {
					userIds[i] = Convert.toLong(users[i], 0);
				}
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
					serverQueryHeader.isRequest = false;
					serverQueryHeader.order = Order.message;
					serverQueryHeader.staffId = userIds[i];
					serverQueryHeader.data.put("msg", msg);// 消息来源用户
					serverQueryHeader.data.put("time", System.currentTimeMillis());
					serverQueryHeader.data.put("FROM_USER_ID", header.staffId);// 消息来源用户
					synchronized (hand) {
						handler.writeData(serverQueryHeader);
					}
					onlineLen++;
					onlineUserList += userIds[i] + ",";
					if (isSingle) {
						header.data.clear();
						header.isRequest = false;
						header.data.put("msg", "发送成功");
						handler.writeData(header);
					}
				} else {
					// 判断是否可以使用短信发送
					if (header.data.containsKey("useSMS")) {

					} else {
						offlineUserList += userIds[i] + ",";
						offlineLen++;
						synchronized (Delays) {
							List<Msg> list = Delays.get(userIds[i]);
							if (list == null) {
								list = new ArrayList<Msg>();
								Delays.put(userIds[i], list);
							}
							synchronized (list) {
								long now = System.currentTimeMillis();
								list.add(new Msg(header.staffId, now, msg));// 缓存延时发送
								if (list.size() > maxDelayLength) {
									// 判断接收用户是否有消息保存入库权限
									list.remove(0);
								}
							}
						}
					}
				}
			}
			synchronized (this.handler) {
				header.data.clear();
				header.isRequest = false;
				if (!isSingle) {
					header.data.put("msg", "有" + onlineLen + "人在线，消息已发送," + offlineLen + "不在线");
					header.data.put("onlineUsers", onlineUserList.substring(0, onlineUserList.length() - 1));
					header.data.put("offlineUsers", offlineUserList.substring(0, offlineUserList.length() - 1));
					handler.writeData(header);
				}
			}
			// header.data.clear();
			// header.isRequest = false;
			// if (onlineLen > 0) {
			// header.data.put("msg", "发送成功" + onlineLen + "人" + (onlineLen < userIds.length ? ",未在线用户将24小时内重发 " : ""));
			// header.data.put("userList", onlineUserList.substring(0, onlineUserList.length() - 1));
			// } else {
			// header.data.put("error", "所请求定位用户均未在线");
			// header.data.put("userList", locateUsers);
			// }
			// header.sendHeader(handler.sc.getOutputStream(), this.handler.outputBuffer); //

		} else {
			// if (header.data.containsKey("status") && Convert.toInt(header.data.get("status"), 1) == 0) {
			// long time = Convert.toLong(header.data.get("time"), 0);
			// synchronized (Delays) {
			// Map<Long, Msg> list = Delays.get(header.staffId);
			// if (list == null)
			// return 0;
			// synchronized (list) {
			// list.remove(time);
			// if (list.size() == 0)
			// Delays.remove(header.staffId);
			// }
			// }
			// }
		}
		return 0;
	}
}