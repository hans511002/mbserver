package com.sobey.base.socket.remote;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sobey.base.socket.Order;
import com.sobey.base.socket.OrderHeader;
import com.sobey.base.socket.ServerName;
import com.sobey.base.util.Bytes;
import com.sobey.base.util.DataInputBuffer;
import com.sobey.base.util.DataOutputBuffer;
import com.sobey.base.util.HasThread;
import com.sobey.base.util.ToolUtil;
import com.sobey.mbserver.user.UserInfoPO;

public class ClientConnection implements Closeable {
	public static final Log LOG = LogFactory.getLog(ClientConnection.class.getName());
	public Socket sc;
	public ServerName server;
	public long sTime;// 连接时间
	public long lastActive;
	// public OutputStream output;
	// public InputStream input;
	public DataOutputBuffer outputBuffer = new DataOutputBuffer(64 * 1024);
	public DataInputBuffer inputBuffer = new DataInputBuffer();
	public boolean logined = false;
	final long initTime = System.currentTimeMillis();
	public UserInfoPO userInfo;

	public ClientConnection(UserInfoPO upo, ServerName server) {
		userInfo = upo;
		this.server = server;
	}

	public void ReConnect() {
		ReConnect(Integer.MAX_VALUE);
	}

	public void ReConnect(long loginTimeout) {
		ReConnect(Integer.MAX_VALUE, loginTimeout);
	}

	boolean connecting = true;

	public void ReConnect(int times, final long loginTimeout) {
		int trys = 0;
		connecting = true;
		// 等待登录完成
		final long stTime = System.currentTimeMillis();
		if (loginTimeout > 0) {
			final ClientConnection con = this;
			new HasThread() {
				@Override
				public void run() {
					try {
						while (connecting && !con.logined) {
							if (System.currentTimeMillis() - stTime > loginTimeout) {
								connecting = false;
								break;
							}
							ToolUtil.sleep(100);
						}
					} catch (Exception e) {
					} finally {
					}
				}
			}.setDaemon(true).setName("login wait").start();
		}
		while (trys++ <= times && connecting) {
			close();
			sc = new Socket();
			SocketAddress sa = new InetSocketAddress(server.getHostname(), server.getPort());
			try {
				LOG.info(" ReConnect .............. ");
				if (userInfo != null && userInfo.getMOBILE() != null && userInfo.getPASSWORD() != null) {
					sc.connect(sa);
					sTime = System.currentTimeMillis();
					break;
				} else {
					ToolUtil.sleep(1000);
				}
				break;
			} catch (IOException e) {
				logined = false;
				if (e instanceof java.net.ConnectException) {
					if (trys < 60 || trys % 60 < 10) {
						ToolUtil.sleep(2000);
					} else {
						ToolUtil.sleep(2000 * 10);
					}
				}
			}
			ToolUtil.sleep(100);
		}
		connecting = false;
	}

	OrderHeader sendLogin() throws IOException {
		LOG.info("send login data .............. ");
		// output = this.sc.getOutputStream();
		// input = this.sc.getInputStream();
		// 发送登录信息
		OrderHeader loginHeader = new OrderHeader();
		loginHeader.staffId = userInfo.getUSER_ID();
		loginHeader.data.put("userNbr", userInfo.getMOBILE());// 用户号码
		loginHeader.data.put("pass", userInfo.getPASSWORD());// 用户号码
		loginHeader.data.put("initTime", initTime);// 当前时间
		loginHeader.data.put("update", true);// 更新编码
		synchronized (this) {
			send(loginHeader);
		}
		// sendLogin();
		// listen.readData(false);
		return loginHeader;
	}

	public synchronized void send(OrderHeader header) throws IOException {
		byte[] arrData = header.toBytes(outputBuffer);
		byte[] vl = Bytes.vintToBytes(arrData.length);
		OutputStream output = this.sc.getOutputStream();
		output.write(vl);
		output.write(arrData);
		output.flush();
		LOG.info("客户端发送消息： " + header);
	}

	public void close() {
		OrderHeader header = new OrderHeader();
		header.order = Order.close;
		header.isRequest = true;
		try {
			if (logined && this.sc != null && this.sc.isConnected())
				send(header);
		} catch (IOException e) {
			LOG.error("send close order error: ", e);
		}
		logined = false;
		try {
			if (sc != null) {
				ToolUtil.close(sc.getOutputStream());
				ToolUtil.close(sc.getInputStream());
			}
		} catch (Exception e) {
		}
		ToolUtil.close(sc);
		sc = null;
	}

}