package com.sobey.mbserver.web;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import com.sobey.jcg.support.log4j.LogUtils;

/**
 * Example of a simple Echo Client.
 */
@WebSocket(maxTextMessageSize = 64 * 1024)
public class SimpleEchoClient {
	private final CountDownLatch closeLatch;

	@SuppressWarnings("unused")
	private Session session;

	public SimpleEchoClient() {
		this.closeLatch = new CountDownLatch(1);
	}

	public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException {
		return this.closeLatch.await(duration, unit);
	}

	@OnWebSocketClose
	public void onClose(int statusCode, String reason) {
		System.out.printf("Connection closed: %d - %s%n", statusCode, reason);
		this.session = null;
		this.closeLatch.countDown(); // trigger latch
	}

	@OnWebSocketConnect
	public void onConnect(Session session) {
		System.out.printf("Got connect: %s%n", session);
		this.session = session;
		try {
			Future<Void> fut;
			fut = session.getRemote().sendStringByFuture("Hello");
			fut.get(2, TimeUnit.SECONDS); // wait for send to complete.

			fut = session.getRemote().sendStringByFuture("Thanks for the conversation.");
			fut.get(2, TimeUnit.SECONDS); // wait for send to complete.

			// session.close(StatusCode.NORMAL, "I'm done");
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@OnWebSocketMessage
	public void onMessage(String msg) {
		System.out.printf("Got msg: %s%n", msg);
	}

	public void send(final String message) throws IOException {
		this.session.getRemote().sendString(message, new WriteCallback() {
			@Override
			public void writeSuccess() {
			}

			@Override
			public void writeFailed(Throwable arg0) {
				LogUtils.error("推送消息[" + message + "]到客户端[" + session.getRemoteAddress().toString() + "]异常: " + arg0.getMessage());
			}
		});
	}

	public static void main(String[] args) {
		String destUri = "ws://172.16.128.223:64001/deploy/LogsWS";
		if (args.length > 0) {
			destUri = args[0];
		}
		WebSocketClient client = new WebSocketClient();
		SimpleEchoClient socket = new SimpleEchoClient();
		try {
			client.start();
			URI echoUri = new URI(destUri);
			ClientUpgradeRequest request = new ClientUpgradeRequest();
			client.connect(socket, echoUri, request);
			System.out.printf("Connecting to : %s%n", echoUri);
			Thread.sleep(1000);
			socket.awaitClose(50, TimeUnit.SECONDS);
			// while (client.isRunning()) {
			// socket.send("time: " + (new Date().toLocaleString()));
			// Thread.sleep(1000);
			// }
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			try {
				client.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
