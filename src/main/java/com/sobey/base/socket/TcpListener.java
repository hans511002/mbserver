package com.sobey.base.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sobey.base.SystemInit;
import com.sobey.base.socket.order.ConnectOrder;
import com.sobey.base.util.HasThread;
import com.sobey.base.util.ToolUtil;

//消息模式 
public class TcpListener extends HasThread {
	public static final Log LOG = LogFactory.getLog(TcpListener.class.getName());
	private ExecutorService clientPool;

	public ServerSocketChannel server;
	public TreeSet<SelectorListen> selectors = new TreeSet<SelectorListen>();
	DelayExecProcess delayExec;

	public HashMap<Long, SCHandler> handles = new HashMap<Long, SCHandler>();
	public long tempId = -1;
	final int port;
	public int threadNum = 10;
	public int clientLimit = 1000;
	public int selectorThreads = 1;// 监听读取数据线程数
	public boolean isLongLink = true;

	HashMap<String, ClientInfo> clientInfos = new HashMap<String, ClientInfo>();// 客户信息,包括在线和我拒绝的

	public static byte MaxLinkTimes = 5;// 客户端最大5秒连接数
	public static byte MaxRequestTimes = 50;// 客户端最大请求数，5秒平均值，接收到数据算请求一次（包括ping）,超过则需要客户端输入验证码
	public static byte MaxParallelThread = 5;// 客户端最大并发请求数，使用线程数
	public static int calcDelayTime = 1000;

	public static class ClientInfo {
		byte verifyCode;// 验证码值
		byte useThreadNum = 0;// 并发使用线程数 过多输入验证码
		byte avgRequestTimes = 0;// 5秒请求数 过多输入验证码
		byte avgLinkTimes = 0;// 5秒连接数 过多拒绝连接一段时间
		byte verifyIng;// 验证中
		byte nullDataCounts;// 空数据次数，用于判断是否断开或者异常造成的循环读取，浪费资源

		int linkTimes = 0;// 总连接次数
		int lastLinkTimes = 0;// 上次计算的总连接次数
		int requestTimes = 0;// 总请求次数
		int lastRequestTimes = 0;// 上次计算的总请求次数
		int rejectTime = 0;// 拒绝连接时间
		int largeDataTimes = 0;// 错误大数据发送次数
		// int verifyTimes;// 验证发送次数
		// int sendV;
		// int resv;

		long rejectBeginTime = 0;// 拒绝请求时间
		long calcTime = System.currentTimeMillis();// 上一次5秒的计算时间

		public String toString() {
			return "useThreadNum=" + useThreadNum + " avgRequestTimes=" + avgRequestTimes + " linkTimes=" + linkTimes + " verifyIng=" + verifyIng
			        + " nullDataCounts=" + nullDataCounts + " avgLinkTimes=" + avgLinkTimes + " requestTimes=" + requestTimes + " rejectTime=" + rejectTime
			        + " largeDataTimes=" + largeDataTimes;
		}

		public void addLinkTimes() {
			linkTimes++;
			if (linkTimes >= Integer.MAX_VALUE) {
				linkTimes = Integer.MAX_VALUE - lastLinkTimes + 1;
				lastLinkTimes = 0;
			}
		}

		public void addRequstTimes() {
			requestTimes++;
			if (requestTimes >= Integer.MAX_VALUE) {
				requestTimes = Integer.MAX_VALUE - lastRequestTimes + 1;
				lastRequestTimes = 0;
			}
		}

		public void addUseThreadNum() {
			if (useThreadNum < Byte.MAX_VALUE)
				useThreadNum++;
		}

		public void minusUseThreadNum() {
			if (useThreadNum > 0)
				useThreadNum--;
		}

		public int getVerifyCode() {
			return this.getVerifyCode(false);
		}

		public int getVerifyCode(boolean buildNew) {
			if (buildNew) {
				buildVerifyCode();
			}
			return verifyCode & 0xFF;
		}

		void buildVerifyCode() {
			this.verifyIng = 1;
			this.verifyCode = (byte) ((int) (Integer.MAX_VALUE * Math.random() + 1) & 0xFF);
		}

		public int getUseThreadNum() {
			return useThreadNum & 0xFF;
		}

		public int getAvgLinkTimes() {
			return avgLinkTimes & 0xFF;
		}

		public int getAvgRequestTimes() {
			return avgRequestTimes & 0xFF;
		}

		public int getLinkTimes() {
			return linkTimes;
		}

		public int getRequestTimes() {
			return requestTimes;
		}

		public int getRejectTime() {
			return rejectTime;
		}

		public void calc() {
			long now = System.currentTimeMillis();
			long lt = now - this.calcTime;
			if (lt >= 10) {
				double ps = ((double) lt / 1000 + 1);
				double ls = ((this.linkTimes - lastLinkTimes) / ps);
				avgLinkTimes = (byte) (ls > 255 ? 255 : ls);
				if (avgLinkTimes > MaxLinkTimes) {
					int oldEnd = (int) (now - (rejectBeginTime > 0 ? rejectBeginTime : now));
					rejectBeginTime = now;
					double n = Math.sqrt(ls / MaxLinkTimes);
					if (n > 18) {
						rejectTime = Integer.MAX_VALUE;
					} else {
						int st = 5000 << (int) Math.floor(n);
						rejectTime = st - oldEnd;
					}
				}
				ls = (int) ((this.requestTimes - lastRequestTimes) / ps);
				avgRequestTimes = (byte) (ls > 255 ? 255 : ls);
			}
			if (lt >= calcDelayTime) {
				lastLinkTimes = linkTimes;
				lastRequestTimes = requestTimes;
				calcTime = now;
			}
		}

		// 是否需要验证码，并生成随机验证码
		public boolean isNeedVerify() {
			if (avgRequestTimes >= MaxRequestTimes) {
				return true;
			}
			return false;
		}

		public boolean isBusy() {
			return useThreadNum > MaxParallelThread;
		}

		public boolean isInVerify() {
			return verifyIng > 0;
		}

		public void setInVerify(int v) {
			verifyIng = (byte) v;
		}

		public void resetVerify() {
			calcTime = System.currentTimeMillis() - calcDelayTime;
			this.lastRequestTimes = this.requestTimes;
			this.avgRequestTimes = 0;
		}

		// 是否拒绝连接中
		public boolean isInRejectTime() {
			long now = System.currentTimeMillis();
			if (rejectTime > 0 && now - rejectBeginTime < this.rejectTime) {
				return true;
			}
			return false;
		}

		public int addNullDataCounts() {
			if (nullDataCounts < 127) {
				nullDataCounts++;
			}
			return nullDataCounts;
		}

		public void reSetNullDataCounts() {
			nullDataCounts = 0;
		}
	}

	public void setSelectListens(int selectorThreads) {
		if (selectorThreads > 0)
			this.selectorThreads = selectorThreads;
	}

	public ClientInfo getClientInfo(SCHandler handler) {
		return getClientInfo(handler.getRemoteIP());
	}

	public ClientInfo getClientInfo(String ip) {
		if (ip == null)
			return null;
		synchronized (clientInfos) {
			ClientInfo info = clientInfos.get(ip);
			if (info == null) {
				info = new ClientInfo();
				clientInfos.put(ip, info);
			}
			return info;
		}
	}

	public TcpListener(int port) throws IOException {
		this(port, 100);
	}

	public TcpListener(int port, int threadNum) {
		this.port = port;
		this.threadNum = threadNum;
		clientPool = Executors.newFixedThreadPool(threadNum);
		while (true) {
			try {
				rebind();
				break;
			} catch (IOException e) {
				LOG.error("bind listen socket on port " + port + " error", e);
			}
		}
		// selector = Selector.open();
		// ServerSocketChannel server = ServerSocketChannel.open();
		// server.socket().bind(new InetSocketAddress(port));
		// server.configureBlocking(false);
		// server.register(selector, SelectionKey.OP_ACCEPT);
	}

	public void setThreadNum(int threadNum) {
		if (this.threadNum != threadNum) {
			this.threadNum = threadNum;
			clientPool.shutdown();
			ToolUtil.sleep(2000);
			clientPool = Executors.newFixedThreadPool(threadNum);
		}
	}

	@Override
	public void run() {
		if (delayExec == null) {
			delayExec = new DelayExecProcess(this);
			delayExec.setDaemon(true).setName("delayExec").start();
		}
	}

	void rebind() throws IOException {
		if (server != null) {
			try {
				server.close();
			} catch (IOException e) {
			}
		}
		List<Long> ids = new ArrayList<>(handles.keySet());
		for (Long id : ids) {
			SCHandler handler = handles.get(id);
			try {
				handler.close();
			} catch (Exception e) {
			}
		}
		for (SelectorListen sel : selectors) {
			sel.rebind();
		}

		server = ServerSocketChannel.open();
		server.socket().bind(new InetSocketAddress(port));
		server.configureBlocking(false);
		register(server, SelectionKey.OP_ACCEPT, 0);
	}

	public SelectionKey register(SCHandler handler, int ops, Object id) throws IOException {
		synchronized (selectors) {
			if (selectors.size() < selectorThreads) {
				selectors.add(new SelectorListen(this, selectors.size()));
			}
			SelectorListen listen = selectors.pollFirst();
			SelectionKey key = listen.register(handler, ops, id);
			selectors.add(listen);
			return key;
		}
	}

	public SelectionKey register(ServerSocketChannel sockch, int ops, Object id) throws IOException {
		synchronized (selectors) {
			if (selectors.size() < selectorThreads) {
				selectors.add(new SelectorListen(this, selectors.size()));
			}
			SelectorListen listen = selectors.pollFirst();
			synchronized (listen.selector) {
				SelectionKey key = sockch.register(listen.selector, ops, id);
				listen.clientNum++;
				listen.setName("listenId=" + listen.listenId + " clientNum=" + listen.clientNum);
				if (!listen.isStarted()) {
					listen.start();
				}
				synchronized (listen.listenKeys) {
					listen.listenKeys.put(key, null);
				}
				selectors.add(listen);
				return key;
			}
		}
	}

	public boolean cancelSelectKey(SelectionKey key) {
		for (SelectorListen listen : selectors) {
			if (listen.listenKeys.containsKey(key)) {
				if (listen.cancelSelectKey(key)) {
					return true;
				}
			}
		}
		key.cancel();
		return false;
	}

	public static class SelectorListen extends HasThread implements Comparable<SelectorListen> {
		private Selector selector;
		private TcpListener listen;
		private boolean isStarted = false;
		private int clientNum = 0;
		private int listenId = 0;
		private Map<SelectionKey, SCHandler> listenKeys = new HashMap<SelectionKey, SCHandler>();

		public boolean isStarted() {
			return isStarted;
		}

		public Selector getSelector() {
			return selector;
		}

		public TcpListener getListen() {
			return listen;
		}

		public int getClientNum() {
			return clientNum;
		}

		@Override
		public int compareTo(SelectorListen o) {
			if (o == null)
				return -1;
			if (selector.equals(o.selector)) {
				return 0;
			}
			int m = this.clientNum - o.clientNum;
			if (m != 0)
				return m;
			else
				return -1;
		}

		@Override
		public boolean equals(Object o) {
			if (o == null)
				return false;
			if (o instanceof SelectorListen) {
				SelectorListen oth = (SelectorListen) o;
				this.selector.equals(oth.selector);
			}
			return false;

		}

		public SelectionKey register(SCHandler handler, int ops, Object id) throws ClosedChannelException {
			synchronized (selector) {
				SelectionKey key = handler.channel.register(selector, ops, id);
				clientNum++;
				this.setName("listenId=" + listenId + " clientNum=" + clientNum);
				if (!this.isStarted()) {
					this.start();
				}
				synchronized (listenKeys) {
					listenKeys.put(key, handler);
				}
				synchronized (handler) {
					handler.listener = this;
				}
				return key;
			}
		}

		public SelectionKey reRegister(SelectionKey oldKey, SCHandler handler, int ops, Object id) throws ClosedChannelException {
			synchronized (listenKeys) {
				if (!listenKeys.containsKey(oldKey)) {
					clientNum++;
					this.setName("listenId=" + listenId + " clientNum=" + clientNum);
				}
				listenKeys.remove(oldKey);
				SelectionKey key = handler.channel.register(selector, ops, id);
				listenKeys.put(key, handler);
				return key;
			}
		}

		public SelectorListen(TcpListener listen, int listenId) throws IOException {
			LOG.info("add SelectorListen " + listenId);
			this.listenId = listenId;
			this.listen = listen;
			selector = Selector.open();
		}

		void rebind() throws IOException {
			if (selector != null) {
				try {
					selector.close();
				} catch (IOException e) {
				}
			}
			selector = Selector.open();
		}

		@Override
		public void run() {
			isStarted = true;
			while (SystemInit.isRuning) {
				listen();
			}
		}

		public boolean cancelSelectKey(SelectionKey key) {
			synchronized (this.listen.selectors) {
				synchronized (listenKeys) {
					if (key != null && listenKeys.containsKey(key)) {
						listenKeys.remove(key);
						clientNum--;
						this.setName("listenId=" + listenId + " clientNum=" + clientNum);
						this.listen.selectors.remove(this);
						this.listen.selectors.add(this);
						key.cancel();
						return true;
					} else {
						return false;
					}
				}
			}
		}

		// 监听端口
		public void listen() {
			try {
				if (listenKeys.size() > 0) {
					selector.select(2000);
					Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
					while (iter.hasNext()) {
						SelectionKey key = iter.next();
						iter.remove();
						try {
							process(key);
						} catch (Exception e) {
							cancelSelectKey(key);
							LOG.error("listen process error " + key, e);
						}
					}
				} else {
					ToolUtil.sleep(100);
				}
			} catch (Exception e) {
				LOG.error("listen select error", e);
			}
		}

		// 处理事件
		protected void process(SelectionKey key) throws IOException {
			if (key.isAcceptable()) { // 接收请求
				ConnectOrder.process(this.listen, key);
			} else if (key.isReadable() && key.isValid()) { // 读信息
				try {
					long oldId = (Long) key.attachment();
					SocketChannel channel = (SocketChannel) key.channel();
					SCHandler handle = listen.handles.get(oldId);
					if (handle != null && handle.selectKey != null && handle.selectKey.equals(key) && handle.channel.isOpen()) {
						if (!channel.equals(handle.channel)) {
							try {
								handle.channel.close();
							} catch (IOException e) {
							}
							handle.channel = channel;
						}
						if (oldId != handle.clientId && handle.isMsgLink()) {// 不允许非消息链接不是主链接
							handle.close();
						}
					} else {
						if (handle != null) {
							handle.close();
						} else {
							listen.cancelSelectKey(key);
						}
						return;
					}
					ProcessRunable proc = new ProcessRunable(this.listen, handle);
					if (handle.isMsgLink()) {
						// 读取数据，解析出一个header
						if (!proc.readDataHeader()) {
							return;
						}
						// 需要判断是否在验证中及时间
						boolean isBusy = true;
						synchronized (proc.info) {
							isBusy = proc.info.isBusy();
							if (!isBusy) {
								proc.info.addUseThreadNum();
								proc.info.calc();
							}
						}
						if (isBusy) {
							LOG.info("超过并行数，延迟执行" + proc.handle.getRemoteIP());
							listen.delayExec.putDelayProc(proc);
						} else {
							LOG.info("提交新线程执行" + proc.handle.getRemoteIP());
							listen.clientPool.execute(proc);
						}
					} else {// 流式数据处理
						proc.handle.listener.cancelSelectKey(key);// 一次性处理数据
						listen.clientPool.execute(proc);
					}
				} catch (Exception e) {
					String cip = "";
					SocketChannel ch = (SocketChannel) key.channel();
					if (ch != null) {
						try {
							cip = ch.getRemoteAddress().toString();
						} catch (Exception ex) {
							cip = ex.getMessage();
						}
					}
					LOG.error("process " + key + "[" + cip + "] ", e);
				}
			}
		}
	}

	public static class DelayExecProcess extends HasThread {

		HashMap<Long, ProcessRunable> delayProcs = new HashMap<Long, ProcessRunable>();
		TcpListener listen;
		public int maxDelayProcessSize = 1000;

		DelayExecProcess(TcpListener listen) {
			this.listen = listen;
		}

		public void putDelayProc(ProcessRunable proc) {
			synchronized (delayProcs) {
				if (delayProcs.size() < maxDelayProcessSize)
					delayProcs.put(System.nanoTime(), proc);
			}
		}

		@Override
		public void run() {
			while (true) {
				try {
					TreeSet<Long> procList = new TreeSet<Long>(delayProcs.keySet());
					for (Long time : procList) {
						ProcessRunable proc = delayProcs.get(time);
						boolean isBusy = true;
						synchronized (proc.info) {
							isBusy = proc.info.isBusy();
							if (!isBusy) {
								proc.info.addUseThreadNum();
								proc.info.calc();
							}
						}
						if (!isBusy) {
							synchronized (delayProcs) {
								delayProcs.remove(time);
							}
							LOG.info("提交新线程执行" + proc.handle.getRemoteIP());
							listen.clientPool.execute(proc);
						} else {
							if (System.nanoTime() - time > 30000000000l) {// 30秒过期
								synchronized (delayProcs) {
									delayProcs.remove(time);
								}
							}
						}
					}
					// 断开大于30秒不登录的
					List<Long> ids = new ArrayList<>(listen.handles.keySet());
					long now = System.currentTimeMillis();
					for (Long id : ids) {
						SCHandler handler = listen.handles.get(id);
						boolean needClose = false;
						try {
							long acl = now - handler.lastActiveTime;
							long ccl = now - handler.clientConnectTime;

							if (!this.listen.isLongLink && ccl > 30000 && handler.clientLoginTime == 0 && handler.isMsgLink()) {
								needClose = true;
								OrderHeader header = new OrderHeader();
								header.isRequest = false;
								header.order = Order.close;
								header.staffId = handler.clientId;
								header.data.put("err", "Over time without logging in");
								LOG.warn(header.staffId + " Over time without logging in clientLoginTime=" + handler.clientLoginTime + " now=" + now);
								handler.writeData(header);
							} else if (acl > 300000) {// 5分钟不活动，关闭连接
								OrderHeader header = new OrderHeader();
								header.isRequest = false;
								header.staffId = handler.clientId;
								header.order = Order.ping;
								if (!this.listen.isLongLink || !handler.isLogined()) {
									needClose = true;
									header.order = Order.close;
									LOG.warn(header.staffId + " Inactivity Timeout lastActiveTime=" + handler.lastActiveTime + " now=" + now);
									header.data.put("err", "Inactivity Timeout");
								}
								handler.writeData(header);
							}
						} catch (Exception e) {
							if (isSocketException(e)) {
								needClose = true;
							}
						} finally {
							if (needClose) {
								handler.close();
								synchronized (this.listen.handles) {
									this.listen.handles.remove(id);
								}
							}
						}
					}
				} catch (Exception e) {
					LOG.error("", e);
				} finally {
					ToolUtil.sleep(1000);
				}
			}
		}
	}

	public static boolean isSocketException(Throwable e) {
		if (e instanceof SocketException || e instanceof IOException)
			return true;
		if (e instanceof IOException) {
			String eClassName = e.getClass().getName();
			if (eClassName.startsWith("java.nio.channels."))
				return true;
			else if (e instanceof java.nio.channels.AcceptPendingException)
				return true;
			else if (e instanceof java.nio.channels.AlreadyBoundException)
				return true;
			else if (e instanceof java.nio.channels.AlreadyConnectedException)
				return true;
			else if (e instanceof java.nio.channels.AsynchronousCloseException)
				return true;
			else if (e instanceof java.nio.channels.CancelledKeyException)
				return true;
			else if (e instanceof java.nio.channels.ClosedByInterruptException)
				return true;
			else if (e instanceof java.nio.channels.ClosedChannelException)
				return true;
			else if (e instanceof java.nio.channels.ClosedSelectorException)
				return true;
			else if (e instanceof java.nio.channels.ConnectionPendingException)
				return true;
			else if (e instanceof java.nio.channels.FileLockInterruptionException)
				return true;
			else if (e instanceof java.nio.channels.IllegalBlockingModeException)
				return true;
			else if (e instanceof java.nio.channels.IllegalChannelGroupException)
				return true;
			else if (e instanceof java.nio.channels.NoConnectionPendingException)
				return true;
			else if (e instanceof java.nio.channels.NonReadableChannelException)
				return true;
			else if (e instanceof java.nio.channels.NonWritableChannelException)
				return true;
			else if (e instanceof java.nio.channels.IllegalSelectorException)
				return true;
			else if (e instanceof java.nio.channels.InterruptedByTimeoutException)
				return true;
			else if (e instanceof java.nio.channels.NotYetBoundException)
				return true;
			else if (e instanceof java.nio.channels.NotYetConnectedException)
				return true;
			else if (e instanceof java.nio.channels.OverlappingFileLockException)
				return true;
			else if (e instanceof java.nio.channels.ShutdownChannelGroupException)
				return true;
			else if (e instanceof java.nio.channels.UnresolvedAddressException)
				return true;
			else if (e instanceof java.nio.channels.UnsupportedAddressTypeException)
				return true;
			else if (e instanceof java.nio.channels.ReadPendingException)
				return true;
			else if (e instanceof java.nio.channels.WritePendingException)
				return true;
		}
		return false;
	}
}