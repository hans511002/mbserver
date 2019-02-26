package com.sobey.mbserver.web;

import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import com.sobey.jcg.support.log4j.LogUtils;
import com.sobey.mbserver.main.DaemonMaster;
import com.sobey.mbserver.util.DateUtils;
import com.sobey.mbserver.web.init.SysConfig;

//@WebSocket
public abstract class DeployWebSocketServer extends WebSocketServlet {
	private static final long serialVersionUID = 176913860911150093L;

	// 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
	static int onlineCount = 0;
	static java.util.concurrent.ConcurrentHashMap<Session, ClientSession> webSocketSession = new ConcurrentHashMap<Session, ClientSession>();
	static Object mutex = new Object();
	// private final CountDownLatch closeLatch;
	// 与某个客户端的连接会话，需要通过它来给客户端发送数据
	Session session;
	final String path;// taskLog
	ClientSession cs = null;

	public DeployWebSocketServer() {
		path = "LogsWS";
	}

	public DeployWebSocketServer(String path) {
		this.path = path;
		// this.closeLatch = new CountDownLatch(1);
	}

	public static class ClientSession {
		Session session;
		String clientIp;
		String clientAddr;
		long activeTime = System.currentTimeMillis();
		DeployWebSocketServer wsServer;
		DeployWebSocketProxy wsProxy = null;
	}

	protected void getProxyWS(ClientSession ss) {
		if (!DaemonMaster.master.isMaster()) {
			synchronized (mutex) {
				if (ss.wsProxy == null) {
					String destUri = "ws://" + DaemonMaster.master.getMasterHostIp() + ":" + SysConfig.getUiPort() + "/deploy/" + path;
					try {
						ss.wsProxy = new DeployWebSocketProxy(ss);
						URI echoUri = new URI(destUri);
						ClientUpgradeRequest request = new ClientUpgradeRequest();
						ss.wsProxy.start();
						ss.wsProxy.connect(ss.wsProxy, echoUri, request);
						LogUtils.info("connected " + echoUri + " for " + ss.session.getRemoteAddress());
					} catch (Throwable t) {
						LogUtils.warn("connecting to:" + destUri, t);
						closeProxy(ss);
					} finally {
					}
				}
			}
		} else if (ss.wsProxy != null) {
			closeProxy(ss);
		}
	}

	protected void closeProxy(ClientSession ss) {
		synchronized (ss) {
			if (ss.wsProxy != null) {
				try {
					if (ss.wsProxy.session != null)
						ss.wsProxy.session.close();
				} catch (Exception e) {
					LogUtils.error("closeProxy " + e.getMessage());
				}
				try {
					ss.wsProxy.stop();
				} catch (Exception e) {
					LogUtils.error("closeProxy " + e.getMessage());
				}
				try {
					ss.wsProxy.destroy();
				} catch (Exception e) {
					LogUtils.error("closeProxy " + e.getMessage());
				}
				ss.wsProxy = null;
			}
		}
	}

	// public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException {
	// return this.closeLatch.await(duration, unit);
	// }

	@Override
	public void configure(WebSocketServletFactory factory) {
		factory.getPolicy().setIdleTimeout(1800000);
		factory.register(this.getClass());
	}

	// @OnWebSocketConnect
	public void onConnect(Session session) throws IOException {
		this.session = session;
		synchronized (mutex) {
			ClientSession ss = webSocketSession.get(session);
			if (ss == null) {
				ss = new ClientSession();
				webSocketSession.put(session, ss);
			}
			ss.wsServer = this;
			ss.clientAddr = session.getRemoteAddress().toString();
			ss.clientIp = ss.clientAddr.substring(0, ss.clientAddr.lastIndexOf(":"));
			ss.session = session;
			LogUtils.info("got websocket connect with: " + session.getRemoteAddress());
			addOnlineCount(); // 在线数加1
			send(ss, "Hello:" + ss.clientAddr);
			this.cs = ss;
			if (!DaemonMaster.master.isMaster()) {
				getProxyWS(ss);
			}
			newConnect(ss);
		}
	}

	public void newConnect(ClientSession ss) {
	}

	public void closeConnect(ClientSession ss) {
	}

	/**
	 * 连接关闭调用的方法
	 */
	// @OnWebSocketClose
	public void onClose(Session session, int statusCode, String reason) {
		closeSession(session, statusCode, reason);
	}

	/**
	 * 收到客户端消息后调用的方法
	 * 
	 * @param message
	 *            客户端发送过来的消息
	 * @param session
	 *            可选的参数
	 */
	// public void onMessage(Session session, byte buf[] ,int offset,int length)
	// @OnWebSocketMessage
	public void onMessage(Session session, String message) {
		ClientSession ss = webSocketSession.get(session);
		ss.activeTime = System.currentTimeMillis();
		if (!DaemonMaster.master.isMaster()) {
			int tryTime = 0;
			while (tryTime++ < 3) {
				getProxyWS(ss);
				try {
					ss.wsProxy.send(message);
					break;
				} catch (IOException e) {
					LogUtils.warn("wsProxy send:" + message);
					closeProxy(ss);
				}
			}
		}
		if (message != null && message.equals("close")) {
			closeSession(session, 0, "close order");
		}
	}

	/**
	 * 发生错误时调用
	 * 
	 * @param session
	 * @param error
	 */
	// @OnWebSocketError
	public void onError(Session session, Throwable error) {
		closeSession(session, -1, error.getMessage());
	}

	void closeSession(Session session) {
		closeSession(session, 0, "close order");
	}

	void closeSession(Session session, int statusCode, String reason) {
		ClientSession ss = webSocketSession.remove(session);
		long now = System.currentTimeMillis();
		LogUtils.info("关闭会话:" + session.getRemoteAddress().toString() + " activeTime=" + DateUtils.format(new Date(ss.activeTime)) + " ttl="
		        + (now - ss.activeTime) + " statusCode=" + statusCode + " reason=" + reason);
		closeConnect(ss);
		closeProxy(ss);
		session.close();
		subOnlineCount();
		// this.destroy();
		System.gc();
	}

	public void send(final String message) throws IOException {
		send(cs, message, null);
	}

	public static void closeSession(final ClientSession session) {
		try {
			send(session, "close");
		} catch (Throwable e) {
		}
		session.wsServer.closeSession(session.session);
	}

	/**
	 * 这个方法与上面几个方法不一样。没有用注解，是根据自己需要添加的方法。
	 * 
	 * @param message
	 * @throws IOException
	 */
	public static void send(final ClientSession session, final String message) throws IOException {
		send(session, message, null);
	}

	public static void send(final ClientSession session, final String message, WriteCallback callback) throws IOException {
		RemoteEndpoint re = session.session.getRemote();
		if (re != null) {
			if (callback != null)
				session.session.getRemote().sendString(message, callback);
			else
				session.session.getRemote().sendString(message);
		}
	}

	static List<String> bufferMsg1 = new ArrayList<String>(1000);

	static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

	public static synchronized int getOnlineCount() {
		return onlineCount;
	}

	public static synchronized void addOnlineCount() {
		DeployWebSocketServer.onlineCount++;
	}

	public static synchronized void subOnlineCount() {
		DeployWebSocketServer.onlineCount--;
	}
}
