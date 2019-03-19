package com.sobey.base.socket;

import java.io.EOFException;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.zip.ZipException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sobey.base.exception.SocketMessageException;
import com.sobey.base.socket.TcpListener.ClientInfo;
import com.sobey.base.socket.order.LoginOrder;
import com.sobey.base.socket.order.ServerOrders;
import com.sobey.base.socket.order.VerifyOrder;
import com.sobey.base.util.DataInputBuffer;
import com.sobey.base.util.ToolUtil;
import com.sobey.jcg.support.sys.DataSourceManager;

public class ProcessRunable implements Runnable {
	public static final Log LOG = LogFactory.getLog(ProcessRunable.class.getName());
	OrderHeader header = null;
	SCHandler handle = null;
	TcpListener listen;
	DataInputBuffer parseBuffer = new DataInputBuffer();
	long oldId;
	public ClientInfo info;
	boolean isFirst = false;

	public ProcessRunable(TcpListener listen, SCHandler handle) {
		this.listen = listen;
		this.handle = handle;
	}

	public OrderHeader getHeader() {
		return this.header;
	}

	public void processStreamData() {
		if (handle != null)
			handle.processStreamData();
	}

	public boolean isMsgLink() {
		if (handle == null)
			return true;
		return handle.isMsgLink();
	}

	public boolean checkChannel() {
		return !(handle == null || handle.channel == null || handle.channel.socket().isClosed() || !handle.channel.isConnected() || !handle.channel.isOpen());
	}

	public boolean readDataHeader() {
		try {
			this.header = null;
			if (handle == null || handle.channel == null)
				return false;
			if (!checkChannel()) {
				LOG.info("socket closed");
				handle.close();
				return false;
			}
			info = listen.getClientInfo(this.handle);

			// //////////////主线程读取数据///////
			handle.readData();
			LOG.info("开始读取：" + this.handle.getRemoteAddress().toString() + " getClientInfo=" + info);
			boolean res = parseNextHeader();
			synchronized (info) {
				info.addRequstTimes();
				info.calc();
			}
			if (res) {
				synchronized (info) {
					info.reSetNullDataCounts();
				}
				if (info.isNeedVerify() && !info.isInVerify()) {// 未验证，抛弃当前请求
					VerifyOrder.sendVerifyCode(handle, info, header);// 发送验证码
					return false;
				}
			} else {

			}
			isFirst = true;
			return res;
		} catch (Exception e) {
			LOG.error("init ProcessRunable", e);
			if (handle != null && TcpListener.isSocketException(e)) {
				handle.close();
			} else {
				synchronized (info) {
					int cs = info.addNullDataCounts();
					if (cs > 126) {
						handle.close();
					}
				}
			}
		}
		return false;
	}

	boolean parseNextHeader() throws IOException {
		while (true) {
			try {
				if (header != null && isFirst) {
					isFirst = false;
					return true;
				}
				header = null;
				int pos = this.handle.inputBuffer.getPosition();
				int dataLen = this.handle.inputBuffer.getLength();
				if (dataLen - pos == 0) {
					String msg = "";
					if (handle != null) {
						if (handle.clientId > 0) {
							msg += "clientId=" + handle.clientId;
						}
						if (handle.userInfo != null && handle.userInfo.getUSER_ID() > 0) {
							msg += "userInfo={" + handle.userInfo.toString() + "}";
						}
					}
					LOG.info("缓存数据已读完 ClientId:" + oldId + " " + msg);
					try {
						long now = System.currentTimeMillis();
						if (now - handle.lastActiveTime > 30000) {
							handle.shortPing();
						}
						synchronized (info) {
							int cs = info.addNullDataCounts();
							if (cs > 126) {
								handle.close();
							}
						}
					} catch (Exception e) {
						if (TcpListener.isSocketException(e)) {
							if (handle != null)
								handle.close();
						}
					}
					return false;
				}
				byte firstByte = 0;
				while ((firstByte = this.handle.inputBuffer.readByte()) == 0) {// 客户端返回的短Ping，不处理
					LOG.info("receive " + handle.channel.getRemoteAddress().toString() + "  shortPing  staffId:" + handle.clientId);
					pos = this.handle.inputBuffer.getPosition();
					continue;
				}
				boolean res = false;
				boolean needAddNullData = false;
				if (dataLen - pos < 3) {// 剩余数据少
					this.handle.inputBuffer.setPosition(pos);
					needAddNullData = true;
				} else {
					int vlen = OrderHeader.parseVlen(firstByte, handle.inputBuffer);
					int _pos = this.handle.inputBuffer.getPosition();
					if (dataLen - _pos < vlen) {
						handle.inputBuffer.setPosition(pos);
						needAddNullData = true;
					} else {
						long now = System.currentTimeMillis();
						header = OrderHeader.ParseHeader(vlen, handle.inputBuffer, parseBuffer);
						LOG.info("[staffId:" + handle.clientId + "]反序列化字节数：" + vlen + " 反序列化用时:" + (System.currentTimeMillis() - now) + "ms  header=" + header);
						if (header == null || header.order == Order.ping) {// ping
							if (LOG.isDebugEnabled())
								LOG.debug("receive header=" + header);
						} else {
							res = true;
						}
					}
				}
				if (needAddNullData) {
					synchronized (info) {
						int cs = info.addNullDataCounts();
						if (cs > 80) {
							handle.close();
						}
					}
				}
				LOG.info("parse header=" + this.header);
				if (res && header.order == Order.ping) {
					LOG.info("receive client " + this.handle.getRemoteAddress().toString() + " ping order ");
				} else if (res && header.order == Order.close) {
					handle.close();
					res = false;
				}
				return res;
			} catch (ZipException e) {
				LOG.error("解析命令失败", e);
			} catch (IOException e) {
				LOG.error("解析命令失败", e);
				if (TcpListener.isSocketException(e)) {
					throw e;
				} else if (e instanceof EOFException) {
					throw e;
				}
			}
		}
	}

	@Override
	public void run() {
		long now = System.currentTimeMillis();
		try {
			TcpListener.LOG.info("异步线程[" + Thread.currentThread().getId() + "]执行");
			if (!checkChannel()) {
				if (handle != null) {
					handle.close();
					return;
				}
			}
			if (!this.isMsgLink()) {
				this.processStreamData();
				handle.close();
				return;
			}
			if (info == null) {
				LOG.info("getCallMethodName(1)=" + ToolUtil.getCallMethodName(1));
			}
			if (info.isNeedVerify() && !isVerifyOrder() && !info.isInVerify()) {// 需要验证,不是与验证码相关的命令，并且未生成验证码
				VerifyOrder.sendVerifyCode(handle, info, header);// 发送验证码
			}
			while (parseNextHeader()) {
				if (header.order == Order.close) {
					handle.close();
					return;
				} else if (header.order == Order.setLinkType) {
					if (header.staffId > 0) {
						SCHandler userHandle = this.listen.handles.get(header.staffId);
						if (userHandle != null) {
							this.handle.userInfo = userHandle.userInfo;
							// this.handle.userId = userHandle.userId;
							this.handle.lastActiveTime = System.currentTimeMillis();
							this.handle.clientLoginTime = userHandle.clientLoginTime;
							handle.setIsMsgLink((Boolean) header.data.get("linkType"));
							String action = header.data.get("action").toString();
							handle.setDataType(action);
						}
					}
					return;
				} else if (header.order == Order.login) {
					synchronized (handle.listener.getListen().handles) {
						if (handle.listener.getListen().handles.containsKey(header.staffId)) {// 重登录
							SCHandler old = handle.listener.getListen().handles.get(header.staffId);
							if (old != null && !old.equals(handle)) {
								old.close();
							}
						}
						if (new LoginOrder(handle, header).process() == 0) {
							handle.listener.getListen().handles.remove(oldId);
							handle.listener.getListen().handles.put(header.staffId, handle);
							oldId = header.staffId;
							handle.selectKey = handle.listener.reRegister(handle.selectKey, handle, SelectionKey.OP_READ, header.staffId);
							// handle.channel.register(handle.listener.selector, SelectionKey.OP_READ, header.staffId);
						}
					}
				} else {

					if (info.isNeedVerify() && !isVerifyOrder()) {// 需要验证,不是与验证码相关的命令
						LOG.info("need verify ,only process Verify Order");
						continue;
					}
					ServerOrders call = ServerOrders.createResponseCall(handle, header);
					if (call != null) {
						if (call.isNeedLogin() && !handle.isLogined()) {// 需要登录后执行，未登录
							throw new SocketMessageException("please login first");
						}
						call.process();
						if (!call.isNeedLogin() && !handle.isLogined()) {// 不需要登录且未登录,关闭连接，当作短连接无状态执行
							if (!handle.listener.getListen().isLongLink)
								handle.close(true);
						}
					}
				}
			}
		} catch (Throwable e) {
			if (e instanceof SocketMessageException) {
				TcpListener.LOG.error("异步线程[" + Thread.currentThread().getId() + "]执行命令异常:" + e.getMessage() + " header=" + header);
			} else {
				TcpListener.LOG.error("异步线程[" + Thread.currentThread().getId() + "]执行命令异常 header=" + header, e);
			}
			boolean needClose = false;
			try {
				if (header != null && handle != null && handle.channel != null && handle.channel.isOpen()) {
					header.data.clear();
					if (e instanceof SocketMessageException) {
						header.data.put("err", e.getMessage());
					} else if (TcpListener.isSocketException(e)) {
						header.data.put("err", "socket error");
					} else {
						header.data.put("err", "服务端异常");
					}
					header.isRequest = false;
					synchronized (handle) {
						handle.writeData(header);
					}
					if (header.order == Order.login) {
						needClose = true;
					}
				}
				if (TcpListener.isSocketException(e)) {
					needClose = true;
				}
			} catch (Throwable e2) {
				TcpListener.LOG.error("异步线程[" + Thread.currentThread().getId() + "]执行命令异常 关闭链接", e2);
			} finally {
				if (needClose && handle != null) {
					handle.close();
				}
			}
		} finally {
			if (info != null) {
				synchronized (info) {
					info.minusUseThreadNum();
				}
			}
			DataSourceManager.destroy();
			// // 多线程读取才需要
			// if (!handle.isClose()) {
			// key.interestOps(key.interestOps() | SelectionKey.OP_READ);
			// }
			TcpListener.LOG.info("执行命令用时：" + (System.currentTimeMillis() - now) + "ms ");
		}
	}

	public boolean isVerifyOrder() {
		if (header.order == Order.remoteAccess) {
			Object action = header.data.get("action");
			if (action == null)
				return false;
			String act = action.toString();
			if (act.endsWith("Verify")) {
				return true;
			} else {
				return false;
			}
		} else {// 未完成验证不处理请求
			return false;
		}
	}
}
