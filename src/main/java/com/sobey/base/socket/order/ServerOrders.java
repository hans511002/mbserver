package com.sobey.base.socket.order;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sobey.base.exception.POException;
import com.sobey.base.socket.OrderHeader;
import com.sobey.base.socket.SCHandler;

public abstract class ServerOrders {
	private static final Log LOG = LogFactory.getLog(ServerOrders.class.getName());

	// 客户请求对象
	public SCHandler handler;
	public OrderHeader header;

	public ServerOrders(SCHandler handler, OrderHeader header) {
		this.handler = handler;
		this.header = header;
	}

	public abstract int process() throws IOException, POException;

	public boolean isNeedLogin() {
		return true;
	};

	public static ServerOrders createResponseCall(SCHandler handler, OrderHeader header) throws IOException {
		LOG.info("receive " + handler.getRemoteIP() + " msg  userInfo:" + handler.userInfo + " header:" + header);
		if (LOG.isDebugEnabled())
			LOG.debug("收到客户端[" + handler.channel.getRemoteAddress() + "]消息：" + header);
		switch (header.order) {
		case locate:
			return new LocateOrder(handler, header);
		case message:
			return new MessageOrder(handler, header);
		case sms:
			return new SmsOrder(handler, header);
		case login:
			return new LoginOrder(handler, header);
		case remoteAccess:
			return new RemoteOrder(handler, header);
		case connect:
			return new ConnectOrder(handler, header);
		default:
			return null;
		}
	}
}
