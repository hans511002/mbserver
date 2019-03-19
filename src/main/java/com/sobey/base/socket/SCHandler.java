package com.sobey.base.socket;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sobey.base.socket.order.StreamDataOrder;
import com.sobey.base.util.Bytes;
import com.sobey.base.util.DataInputBuffer;
import com.sobey.base.util.DataOutputBuffer;
import com.sobey.mbserver.user.UserInfoPO;

public class SCHandler {
	public static final Log LOG = LogFactory.getLog(SCHandler.class.getName());
	public TcpListener.SelectorListen listener = null;
	public SocketChannel channel;
	public static final int readBufferSize = 65536;
	DataInputBuffer inputBuffer = new DataInputBuffer();
	DataOutputBuffer readBuffer = new DataOutputBuffer(65536);
	private DataOutputBuffer writeBuffer = new DataOutputBuffer(32768);
	public long clientId;
	public long lastActiveTime;
	public long clientLoginTime;
	public final long clientConnectTime;
	public UserInfoPO userInfo;
	private boolean isClosed = false;
	public SelectionKey selectKey = null;
	public static Pattern ipParrern = Pattern.compile("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})");
	boolean isMsgLink = true;
	StreamDataOrder recvData = null;
	byte errorDataTimes = 0;
	public final ObjectMapper mapper = new ObjectMapper();
	SocketAddress remoteAddress;
	String remoteIp;

	public SCHandler(long id, SocketChannel channel) throws IOException {
		this.clientId = id;
		this.channel = channel;
		this.lastActiveTime = System.currentTimeMillis();
		this.clientConnectTime = System.currentTimeMillis();
		this.remoteAddress = channel.getRemoteAddress();
		String remoteAddr = channel.getRemoteAddress().toString();
		Matcher m = ipParrern.matcher(remoteAddr);
		if (m.find())
			this.remoteIp = m.group(1);
	}

	public void setDataType(String action) {
	}

	public void processStreamData() {
		if (this.recvData != null)
			this.recvData.processData(this);
	}

	public boolean isLogined() {
		return (this.clientLoginTime > 0L) && (this.userInfo != null) && (this.userInfo.getUSER_ID() > 0L);
	}

	public boolean isMsgLink() {
		return this.isMsgLink;
	}

	public void setIsMsgLink(boolean is) {
		this.isMsgLink = is;
	}

	public TcpListener.ClientInfo getClientInfo() {
		return this.listener.getListen().getClientInfo(getRemoteIP());
	}

	public String getRemoteIP() {
		return this.remoteIp;
	}

	public SocketAddress getRemoteAddress() {
		return this.remoteAddress;
	}

	public void sendClose() throws IOException {
		OrderHeader header = new OrderHeader();
		header.order = Order.close;
		header.staffId = this.clientId;
		writeData(header);
	}

	public void close() {
		close(false);
	}

	public void close(boolean close) {
		try {
			if ((this.listener != null) && (this.listener.getListen() != null))
				synchronized (this.listener.getListen().handles) {
					this.listener.getListen().handles.remove(Long.valueOf(this.clientId));
				}
		} catch (Throwable localThrowable) {
			try {
				if (close)
					sendClose();
			} catch (Throwable localThrowable1) {
			}
			try {
				synchronized (this) {
					this.listener.cancelSelectKey(this.selectKey);
				}
				if (this.channel != null)
					this.channel.close();
			} catch (Throwable localThrowable2) {
			}
			this.isClosed = true;
		}
	}

	public boolean isClose() {
		return this.isClosed;
	}

	public void writeData(OrderHeader header) throws IOException {
		LOG.info("[staffId:" + this.clientId + "]发送消息内容：" + header);
		synchronized (this) {
			writeData(this.channel, header.toBytes(this.writeBuffer));
		}
		this.lastActiveTime = System.currentTimeMillis();
	}

	public void ping() throws IOException {
		writeData(OrderHeader.createPing());
	}

	public void shortPing() throws IOException {
		synchronized (this.channel) {
			ByteBuffer bufs = ByteBuffer.wrap(new byte[1]);
			this.channel.write(bufs);
		}
		this.lastActiveTime = System.currentTimeMillis();
	}

	public synchronized DataInputBuffer readData() throws IOException {
		int pos = this.inputBuffer.getPosition();
		int size = this.readBuffer.getLength();
		if (size > 32768) {
			int len = this.inputBuffer.getLength() - this.inputBuffer.getPosition();
			if (len > 10485760) {
				this.readBuffer.reset();
				pos = 0;
				this.errorDataTimes = ((byte) (this.errorDataTimes + 1));
				TcpListener.ClientInfo info = getClientInfo();
				synchronized (info) {
					info.largeDataTimes += 1;
				}
				if (this.errorDataTimes > 3) {
					throw new SocketException("error data to more times ");
				}
			} else if (len < 8192) {
				byte[] data = new byte[len];
				this.inputBuffer.read(data);
				this.readBuffer.reset();
				this.readBuffer.write(data);
				pos = 0;
			} else {
				DataOutputBuffer _outputBuffer = new DataOutputBuffer(32768);
				DataInputBuffer _inputBuffer = readData(this.channel, _outputBuffer);
				this.readBuffer.write(_inputBuffer.getData());
			}
		}

		this.inputBuffer = readData(this.channel, this.readBuffer);
		if (pos > 0)
			this.inputBuffer.skip(pos);
		this.lastActiveTime = System.currentTimeMillis();
		LOG.info("客户端[" + this.channel.getRemoteAddress() + "]缓冲区数据：" + (this.inputBuffer.getLength() - pos) + "字节");
		return this.inputBuffer;
	}

	public synchronized DataInputBuffer readData(DataOutputBuffer outputBuffer) throws IOException {
		return readData(this.channel, outputBuffer);
	}

	public static DataInputBuffer readData(SocketChannel channel, DataOutputBuffer outputBuffer) throws IOException {
		if (outputBuffer == null) {
			outputBuffer = new DataOutputBuffer(32768);
		}
		DataInputBuffer inputBuffer = new DataInputBuffer();
		ByteBuffer recvBuf = ByteBuffer.allocate(4096);
		int allLen = 0;
		synchronized (channel) {
			while (channel.read(recvBuf) > 0) {
				int len = recvBuf.position() - recvBuf.arrayOffset();
				byte[] data = new byte[len];
				recvBuf.rewind();
				recvBuf.get(data);
				outputBuffer.write(data);
				recvBuf.clear();
				allLen += len;
			}
		}
		outputBuffer.close();
		inputBuffer.reset(outputBuffer.getData(), outputBuffer.getLength());
		LOG.info("本次从客户端[" + channel.getRemoteAddress() + "]读取数据：" + allLen + "字节");
		return inputBuffer;
	}

	public static void writeData(SocketChannel channel, byte[] arrData) throws IOException {
		byte[] vl = Bytes.vintToBytes(arrData.length);
		ByteBuffer[] bufs = { ByteBuffer.wrap(vl), ByteBuffer.wrap(arrData) };
		synchronized (channel) {
			channel.write(bufs);
		}
	}

	public static void writeData(Socket channel, byte[] arrData) throws IOException {
		byte[] vl = Bytes.vintToBytes(arrData.length);
		synchronized (channel) {
			OutputStream out = channel.getOutputStream();
			out.write(vl);
			out.write(arrData);
			out.flush();
		}
	}

	public static void writeData(Socket channel, OrderHeader header) throws IOException {
		writeData(channel, header.toBytes(new DataOutputBuffer(4096)));
	}
}