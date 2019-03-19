package com.sobey.base.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sobey.base.util.DataInputBuffer;
import com.sobey.base.util.DataOutputBuffer;
import com.sobey.base.util.DataSerializable;
import com.sobey.base.util.GZIPUtils;
import com.sobey.base.util.WritableUtils;

public class OrderHeader implements Serializable {
	public static final Log LOG = LogFactory.getLog(OrderHeader.class.getName());
	private static final long serialVersionUID = 2027998401675352881L;
	public Order order = Order.ping;
	public final int serialNumber;
	public boolean isRequest;
	public long staffId;
	public Map<String, Object> data = new HashMap();

	public static Map<OrderSeq, List<OrderHeader>> orderResponse = new HashMap();

	private static int SerialNumber = 0;

	public String toString() {
		return toString(false);
	}

	public String toString(boolean haveData) {
		StringBuffer sb = new StringBuffer();
		sb.append("serialNumber=" + this.serialNumber);
		sb.append(" order=" + this.order);
		sb.append(" isRequest=" + this.isRequest);
		sb.append(" staffId=" + this.staffId);
		if ((!haveData) && (this.order == Order.remoteAccess) && (this.isRequest)) {
			sb.append(" action=" + this.data.get("action"));
			sb.append(" args=" + this.data.get("args"));
		}
		if (haveData)
			sb.append(" data=" + this.data);
		return sb.toString();
	}

	public OrderHeader() {
		this.serialNumber = getOrderSerial();
	}

	private OrderHeader(int s) {
		this.serialNumber = s;
	}

	public static int parseVlen(byte buffer, InputStream input) throws IOException {
		byte firstByte = buffer;
		int len = WritableUtils.decodeVIntSize(firstByte);
		int offset = 0;
		int vlen = 0;
		if (len == 1) {
			vlen = firstByte;
		} else {
			long i = 0L;
			byte[] buf = new byte[len - 1];
			input.read(buf, 0, buf.length);
			for (int idx = 0; idx < len - 1; idx++) {
				byte b = buf[(offset++)];
				i <<= 8;
				i |= b & 0xFF;
			}
			vlen = (int) (WritableUtils.isNegativeVInt(firstByte) ? i ^ 0xFFFFFFFF : i);
		}
		return vlen;
	}

	static byte[] readOrder(int len, InputStream input) throws IOException {
		if (len > 0) {
			byte[] data = new byte[len];
			input.read(data, 0, len);
			return data;
		}
		return null;
	}

	static byte[] readOrder(byte buffer, InputStream input) throws IOException {
		int vlen = parseVlen(buffer, input);
		if (vlen > 0) {
			byte[] data = new byte[vlen];
			input.read(data, 0, vlen);
			return data;
		}
		return null;
	}

	public static OrderHeader ParseHeader(int len, InputStream input, DataInputBuffer inputBuffer) throws IOException {
		OrderHeader header = deserialize(GZIPUtils.ungzip(readOrder(len, input)), inputBuffer);
		return header;
	}

	public static OrderHeader ParseHeader(byte buffer, InputStream input, DataInputBuffer inputBuffer) throws IOException {
		OrderHeader header = deserialize(GZIPUtils.ungzip(readOrder(buffer, input)), inputBuffer);
		return header;
	}

	public OrderHeader clone() {
		try {
			return deserialize(serializable(), null);
		} catch (IOException e) {
		}
		return null;
	}

	public byte[] toBytes(DataOutputBuffer buffer) throws IOException {
		serializable(buffer);

		long now = System.currentTimeMillis();
		int len = buffer.getLength();
		byte[] arrData = GZIPUtils.gzip(buffer.getData(), 0, buffer.getLength());
		LOG.info("[staffId:" + this.staffId + "]序列化命令字节数：" + len + "=>" + arrData.length + " 序列化用时:" + (System.currentTimeMillis() - now) + "ms" + " header="
		        + toString());
		return arrData;
	}

	public static OrderHeader deserialize(byte[] data) throws IOException {
		return deserialize(data, null);
	}

	private static OrderHeader deserialize(byte[] data, DataInputBuffer buffer) throws IOException {
		if (data == null)
			return null;
		if (buffer == null)
			buffer = new DataInputBuffer();
		buffer.reset(data, data.length);
		int serialNumber = buffer.readInt();
		OrderHeader oh = new OrderHeader(serialNumber);
		oh.order = Order.parse(buffer.readByte());
		oh.isRequest = (buffer.readByte() != 0);

		int dSize = buffer.readInt();
		for (int i = 0; i < dSize; i++) {
			byte[] buf = new byte[buffer.readByte()];
			buffer.read(buf);
			String key = new String(buf);
			Object value = DataSerializable.readFromBytes(buffer);
			oh.data.put(key, value);
		}
		return oh;
	}

	private void serializable(DataOutputBuffer buffer) throws IOException {
		if (buffer == null)
			buffer = new DataOutputBuffer();
		buffer.reset();
		buffer.writeInt(this.serialNumber);
		buffer.writeByte(this.order.toByte());
		buffer.writeByte(this.isRequest ? 1 : 0);

		buffer.writeInt(this.data.size());
		for (Map.Entry d : this.data.entrySet()) {
			buffer.writeByteString((String) d.getKey());
			DataSerializable.writeToBytes(d.getValue(), buffer);
		}
	}

	public byte[] serializable() throws IOException {
		DataOutputBuffer buffer = new DataOutputBuffer();
		serializable(buffer);
		return DataSerializable.getBufferData(buffer);
	}

	public static OrderHeader createPing() {
		OrderHeader ping = new OrderHeader(0);
		ping.order = Order.ping;
		return ping;
	}

	public static int getCurrentSerialNumber() {
		return SerialNumber;
	}

	public static synchronized int getOrderSerial() {
		if ((SerialNumber >= 2147483646) || (SerialNumber < 0)) {
			SerialNumber = 0;
		}
		return ++SerialNumber;
	}
}