package com.sobey.base.socket.test;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sobey.base.socket.Order;
import com.sobey.base.socket.OrderHeader;
import com.sobey.base.socket.SCHandler;
import com.sobey.base.socket.ServerName;
import com.sobey.base.socket.TcpListener;
import com.sobey.base.socket.remote.RemoteClass;
import com.sobey.base.socket.remote.RemoteMethod;
import com.sobey.base.util.DataInputBuffer;
import com.sobey.base.util.DataOutputBuffer;
import com.sobey.base.util.HasThread;
import com.sobey.base.util.ToolUtil;

@RemoteClass
public class MethodTest {
	public static final Log LOG = LogFactory.getLog(MethodTest.class.getName());

	@RemoteMethod
	public String getName(String clientName, List<Long> type) {
		String name = this.getClass().getName();
		LOG.info(clientName + "=" + ToolUtil.join(type.toArray()) + "=>" + name);
		return clientName + "=" + ToolUtil.join(type.toArray()) + "=>" + name;
	}

	static List<SelectionKey> hands = new ArrayList<SelectionKey>();

	public static void main(String args[]) throws IOException {
		// System.err.println(MD5Hash.getMD5AsHex("q".getBytes()));
		// if (1 > 0)
		// return;
		ServerName server = new ServerName("localhost", 1688, 0);
		ListenHandler listen = new ListenHandler();
		listen.start();
		for (int i = 0; i < 100; i++) {
			SocketAddress sa = new InetSocketAddress(server.getHostname(), server.getPort());
			try {
				SocketChannel sc = SocketChannel.open(sa);
				sc.configureBlocking(false);
				SelectionKey key = sc.register(listen.selector, SelectionKey.OP_READ, i);
				synchronized (hands) {
					hands.add(key);
				}
			} catch (IOException e) {
				LOG.error("connect " + i + " " + e.getMessage());
			}
		}
		while (hands.size() > 0) {
			ToolUtil.sleep(1000);
		}
		System.exit(0);
	}

	public static class ListenHandler extends HasThread {
		private ExecutorService clientPool;
		private Selector selector;

		public ListenHandler() throws IOException {
			selector = Selector.open();
		}

		public ListenHandler sendOrder(SocketChannel channel) throws IOException {
			OrderHeader header = new OrderHeader();
			header.isRequest = true;
			header.order = Order.remoteAccess;
			header.staffId = 0;
			String clientName = this.getName();
			List<Long> type = new ArrayList<Long>();
			List<Object> args = new ArrayList<Object>();
			args.add(clientName);
			args.add(type);
			type.add(System.currentTimeMillis());
			header.data.put("args", args);
			header.data.put("action", "MethodTest.getName");
			SCHandler.writeData(channel, header.toBytes(new DataOutputBuffer(1024)));
			return this;
		}

		@Override
		public void run() {
			while (true) {
				listen();
			}
		}

		// 监听端口
		public void listen() {
			try {
				if (hands.size() > 0)
					selector.select(2000);
				Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
				while (iter.hasNext()) {
					SelectionKey key = iter.next();
					iter.remove();
					try {
						process(key);
					} catch (Exception e) {
						key.cancel();
						synchronized (hands) {
							hands.remove(key);
						}
						LOG.error("listen process error " + key, e);
					}
				}
				// ToolUtil.sleep(100);
			} catch (Exception e) {
				LOG.error("listen select error", e);
			}
		}

		public DataInputBuffer readData(SocketChannel channel, Object id) throws IOException {
			DataOutputBuffer outputBuffer = new DataOutputBuffer(32 * 1024);
			DataInputBuffer inputBuffer = null;
			synchronized (channel) {// 读取锁channel 写入锁handler
				inputBuffer = SCHandler.readData(channel, outputBuffer);
			}
			// //多线程读取才需要
			// key.interestOps(key.interestOps() | (SelectionKey.OP_READ));
			if (outputBuffer.getLength() == 0) {
				LOG.error("[" + id + "] 读取数据长度为0，不处理");
				return null;
			}
			return inputBuffer;
		}

		public void process(SelectionKey key) {
			// key.interestOps(key.interestOps() | (SelectionKey.OP_READ));
			SocketChannel sc = (SocketChannel) key.channel();
			Object id = key.attachment();
			DataInputBuffer parseBuffer = new DataInputBuffer();
			try {
				DataInputBuffer inputBuffer = readData(sc, id);
				if (inputBuffer == null) {
					synchronized (hands) {
						hands.remove(key);
						key.cancel();
					}
					return;
				}
				while (inputBuffer.getPosition() < inputBuffer.getLength()) {
					OrderHeader header = OrderHeader.ParseHeader(inputBuffer.readByte(), inputBuffer, parseBuffer);
					LOG.info("[" + id + "] recvie" + header);
				}
			} catch (Exception e) {
				LOG.error("[" + id + "]" + e.getMessage());
				if (TcpListener.isSocketException(e) || e instanceof EOFException) {
					synchronized (hands) {
						hands.remove(key);
						key.cancel();
						return;
					}
				}
			}
		}
	}
}
