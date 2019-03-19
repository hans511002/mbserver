package com.sobey.base.socket.order;

import java.io.IOException;

import com.sobey.base.socket.Order;
import com.sobey.base.socket.OrderHeader;
import com.sobey.base.socket.SCHandler;
import com.sobey.base.socket.TcpListener.ClientInfo;
import com.sobey.base.socket.remote.NotNeedLoginMethod;
import com.sobey.base.socket.remote.RemoteClass;
import com.sobey.base.socket.remote.RemoteMethod;

@RemoteClass
public class VerifyOrder {
	public static int maxDelayLength = 100;

	@RemoteMethod
	@NotNeedLoginMethod
	public boolean Verify(int code, ServerOrders order) {
		SCHandler hand = order.handler;
		ClientInfo info = hand.getClientInfo();
		synchronized (info) {
			boolean res = (code == info.getVerifyCode());
			if (res) {
				info.setInVerify(0);
				info.resetVerify();
			}
			return res;
		}
	}

	/**
	 * 刷新验证码 应该返回图片的二进制字节
	 * 
	 * @param order
	 * @return
	 */
	@RemoteMethod
	@NotNeedLoginMethod
	public int getVerify(ServerOrders order) {
		SCHandler hand = order.handler;
		ClientInfo info = hand.getClientInfo();
		synchronized (info) {
			return info.getVerifyCode(true);
		}
	}

	/**
	 * 发送验证要求及验证码
	 * 
	 * @throws IOException
	 */
	public static void sendVerifyCode(SCHandler hand, ClientInfo info, OrderHeader reqHeader) throws IOException {
		OrderHeader header = new OrderHeader();
		header.order = Order.verify;
		synchronized (info) {
			int code = info.getVerifyCode(true);
			header.data.put("code", code);
		}
		header.data.put("req", reqHeader);// 标识由什么命令引起的验证
		header.data.put("reqOrder", Integer.valueOf(reqHeader.order.toByte()));
		header.data.put("reqIs", reqHeader.isRequest);
		header.data.put("reqsnum", reqHeader.serialNumber);
		// //////////////////////
		hand.writeData(header);
	}
}
