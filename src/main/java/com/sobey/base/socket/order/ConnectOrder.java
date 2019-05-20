package com.sobey.base.socket.order;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sobey.base.PersistentStatePO;
import com.sobey.base.exception.POException;
import com.sobey.base.socket.Order;
import com.sobey.base.socket.OrderHeader;
import com.sobey.base.socket.SCHandler;
import com.sobey.base.socket.TcpListener;
import com.sobey.base.socket.TcpListener.ClientInfo;
import com.sobey.base.socket.remote.RemoteJavaApi;

public class ConnectOrder extends ServerOrders {

	public ConnectOrder(SCHandler handler, OrderHeader header) {
		super(handler, header);
	}

	private static final Log LOG = LogFactory.getLog(ConnectOrder.class.getName());

	// 需要添加统计连接次数和时间等攻击控制
	public synchronized static void process(TcpListener listener, SelectionKey key) throws IOException {
		ServerSocketChannel server = (ServerSocketChannel) key.channel();
		SocketChannel channel = server.accept();
		channel.configureBlocking(false); // 设置非阻塞模式
		channel.socket().setKeepAlive(true);
		// channel.socket().setTcpNoDelay(true);
		SCHandler newHandle = new SCHandler(listener.tempId, channel);
		ClientInfo info = listener.getClientInfo(newHandle);
		synchronized (info) {
			info.addLinkTimes();// 连接次数
			info.calc();
		}
		synchronized (listener.handles) {
			while (true) {
				if (!listener.handles.containsKey(listener.tempId)) {
					break;
				}
				listener.tempId--;
				if (listener.tempId < -1 * 2 << 60)
					listener.tempId = -1;
			}
			listener.handles.put(listener.tempId, newHandle);
		}
		boolean inReject = info.isInRejectTime();
		LOG.info(info);
		if (inReject || listener.clientLimit < listener.handles.size()) {
			OrderHeader header = new OrderHeader();
			header.order = Order.login;
			header.isRequest = false;
			header.data.put("res", false);
			if (inReject) {
				header.data.put("error", "连接太频繁，拒绝连接。等待：" + info.getRejectTime() + "ms");
			} else {
				header.data.put("error", "链接超过服务器承载数，请稍后重试。");
			}
			newHandle.writeData(header);
			newHandle.close();
		} else {
			try {
				LOG.info(newHandle.getRemoteAddress().toString() + " connected");
				newHandle.selectKey = listener.register(newHandle, SelectionKey.OP_READ, listener.tempId);
				listener.tempId--;
				if (listener.tempId < -1 * 2 << 60)
					listener.tempId = -1;
			} catch (Exception e) {
				// java.nio.channels.*Exception
				if (TcpListener.isSocketException(e)) {
					newHandle.close();
				}
			}
		}
	}

	public static void pushSysInfo(SCHandler newHandle) throws IOException {
		// 写入客户端通信需要的初始化数据
		OrderHeader header = new OrderHeader();
		header.order = Order.connect;
		header.isRequest = false;
		header.data.put("res", true);
		header.data.put("upTime", PersistentStatePO.updateTime);
		header.data.put("pos", PersistentStatePO.getPPoTypes());// 发送服务器端PO编码信息
		header.data.put("api", RemoteJavaApi.getAllClassSimpleNameMethodNames());// 发送服务器端API信息

		LOG.info("PO ClassRel:" + PersistentStatePO.getPPoTypes().toString());
		LOG.info("Remote API :" + RemoteJavaApi.getAllClassSimpleNameMethodNames().toString());
		synchronized (newHandle) {
			newHandle.writeData(header);
		}
	}

	@Override
	public int process() throws IOException, POException {
		long updateTime = (Long) header.data.get("uptime");
		if (updateTime >= PersistentStatePO.updateTime) {
			pushSysInfo(handler);
		}
		return 0;
	}
}
