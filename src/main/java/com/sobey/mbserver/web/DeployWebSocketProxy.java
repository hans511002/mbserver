package com.sobey.mbserver.web;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import com.sobey.jcg.support.log4j.LogUtils;
import com.sobey.mbserver.main.DaemonMaster;
import com.sobey.mbserver.web.DeployWebSocketServer.ClientSession;
import com.sobey.mbserver.web.init.SysConfig;

@WebSocket
public class DeployWebSocketProxy extends WebSocketClient {
	// private final CountDownLatch closeLatch;
	Session session;
	final ClientSession sws;

	public DeployWebSocketProxy(ClientSession sws) {
		// this.closeLatch = new CountDownLatch(1);
		this.sws = sws;
	}

	@OnWebSocketConnect
	public void onConnect(Session session) throws IOException {
		this.session = session;
		LogUtils.info("wsproxy Got websocket connect: " + session.getRemoteAddress());
	}

	@OnWebSocketClose
	public void onClose(int statusCode, String reason) {
		session.close();
		session = null;
	}

	@OnWebSocketMessage
	public void onMessage(Session session, String message) {
		try {
			DeployWebSocketServer.send(sws, message);
		} catch (Throwable e) {
			LogUtils.error("转发" + sws.session.getRemoteAddress() + "消息失败:" + message);
		}
	}

	public void send(final String message) throws IOException {
		if (this.session != null && this.session.isOpen() && this.session.getRemote() != null) {
			this.session.getRemote().sendString(message, new WriteCallback() {
				@Override
				public void writeSuccess() {
				}

				@Override
				public void writeFailed(Throwable arg0) {
					LogUtils.warn("wsproxy send[" + sws.wsServer.path + "] failed:" + message);
				}
			});
		} else {
			String destUri = "ws://" + DaemonMaster.master.getMasterHostIp() + ":" + SysConfig.getUiPort() + "/deploy/" + sws.wsServer.path;
			throw new IOException("proxy session is null:" + destUri);
		}
	}

	// public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException {
	// return this.closeLatch.await(duration, unit);
	// }

}
