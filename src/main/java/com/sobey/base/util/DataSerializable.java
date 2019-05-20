package com.sobey.base.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sobey.base.PersistentStatePO;
import com.sobey.base.PersistentStatePO.PPOAttr;
import com.sobey.base.socket.OrderHeader;
import com.sobey.base.socket.ServerName;
import com.sobey.base.socket.SocketWriteable;
import com.sobey.jcg.support.log4j.LogUtils;

public class DataSerializable implements Serializable {
	public static final Log LOG = LogFactory.getLog(DataSerializable.class.getName());
	private static final long serialVersionUID = 2748940480212654177L;
	byte[] row = null;

	public static byte[] Serializable(byte[][] row, DataOutputBuffer buffer) throws IOException {
		if (buffer == null)
			buffer = new DataOutputBuffer();
		buffer.reset();
		WritableUtils.writeVInt(buffer, row.length);
		for (byte[] col : row) {
			WritableUtils.write(buffer, col);
		}
		return DataSerializable.getBufferData(buffer);
	}

	public static byte[][] Deserialization(byte[] row, DataInputBuffer buffer) throws IOException {
		if (buffer == null)
			buffer = new DataInputBuffer();
		buffer.reset(row, row.length);
		int colLen = WritableUtils.readVInt(buffer);

		byte[][] cols = new byte[colLen][];
		for (int i = 0; i < cols.length; i++) {
			cols[i] = WritableUtils.read(buffer);
		}
		return cols;
	}

	public static Object readFromBytes(DataInputBuffer buffer) throws IOException {
		byte type = buffer.readByte();
		switch (type) {
		case 1: {
			int len = buffer.readInt();
			if (len > 0) {
				byte[] bt = new byte[len];
				buffer.read(bt);
				return new String(bt);
			} else {
				return "";
			}
		}
		case 2:
			return WritableUtils.readVInt(buffer);
		case 3:
			return buffer.readDouble();
		case 4:
			return WritableUtils.readVLong(buffer);
		case 5:
			return buffer.readByte();
		case 8:
			return new Date(buffer.readLong());
		case 9:
		case 10: {
			List<Object> list = new LinkedList<Object>();
			int len = buffer.readInt();
			for (int i = 0; i < len; i++) {
				list.add(readFromBytes(buffer));
			}
			return list;
		}
		case 11: {
			int len = buffer.readInt();
			Object[] data = new Object[len];
			for (int i = 0; i < len; i++) {
				data[i] = readFromBytes(buffer);
			}
			return data;
		}
		case 12: {
			int len = buffer.readInt();
			int[] data = new int[len];
			for (int i = 0; i < len; i++) {
				data[i] = WritableUtils.readVInt(buffer);
			}
			return data;
		}
		case 13: {
			int len = buffer.readInt();
			long[] data = new long[len];
			for (int i = 0; i < len; i++) {
				data[i] = WritableUtils.readVLong(buffer);
			}
			return data;
		}
		case 14: {
			int len = buffer.readInt();
			char[] data = new char[len];
			for (int i = 0; i < len; i++) {
				data[i] = buffer.readChar();
			}
			return data;
		}
		case 15: {
			int len = buffer.readInt();
			byte[] data = new byte[len];
			buffer.read(data);
			return data;
		}
		case 20: {
			int len = buffer.readInt();
			String[] list = new String[len];
			for (int i = 0; i < len; i++) {
				int sl = buffer.readInt();
				if (sl > 0) {
					byte[] bt = new byte[sl];
					buffer.read(bt);
					list[i] = new String(bt, "utf-8");
				} else {
					list[i] = "";
				}
			}
			return list;
		}
		case 30: {
			Map map = new HashMap();
			int len = buffer.readInt();
			for (int i = 0; i < len; i++) {
				map.put(readFromBytes(buffer), readFromBytes(buffer));
			}
			return map;
		}
		case 71: {
			int len = buffer.readByte();
			byte[] buf = new byte[len];
			buffer.read(buf);
			return new ServerName(new String(buf));
		}
		case 99: {
			OrderHeader head = null;
			int len = buffer.readByte();
			byte[] buf = new byte[len];
			buffer.read(buf);
			head = OrderHeader.deserialize(buf);
			return head;
		}
		case 100: {
			String cn = buffer.readShortString();
			try {
				return Class.forName(cn);
			} catch (ClassNotFoundException e) {
				LOG.error("readFromBytes Calss=" + cn, e);
				return null;
			}
		}
		case PersistentStatePO.SerializableTypeCode: {
			long classType = buffer.readLong();
			PersistentStatePO ppo = null;
			try {
				PPOAttr ppoAttr = PersistentStatePO.getPpoAttr(classType);
				if (ppoAttr == null) {
					Object obj = DataSerializable.readFromBytes(buffer);
					LogUtils.warn("class not in PersistentStatePO, classType=" + classType + " value=" + obj);
					throw new RuntimeException("class not in PersistentStatePO, classType=" + classType + " value=" + obj);
				}
				ppo = (PersistentStatePO) ppoAttr.getClazz().newInstance();
				ppo.readFromBytes(buffer);
			} catch (InstantiationException e) {
				LOG.error("readFromBytes PersistentStatePO", e);
			} catch (IllegalAccessException e) {
				LOG.error("readFromBytes PersistentStatePO", e);
			}
			return ppo;
		}
		case 126: {
			String className = buffer.readShortString();
			SocketWriteable ppo = null;
			try {
				Class<?> c = Class.forName(className);
				if (!c.isAssignableFrom(SocketWriteable.class)) {
					Object obj = DataSerializable.readFromBytes(buffer);
					LogUtils.warn("class not in PersistentStatePO, " + className + ":" + obj);
					throw new RuntimeException("class not in PersistentStatePO, " + className + ":" + obj);
				}
				ppo = (SocketWriteable) c.newInstance();
				ppo.readFromBytes(buffer);
			} catch (InstantiationException e) {
				LOG.error("readFromBytes PersistentStatePO", e);
			} catch (IllegalAccessException e) {
				LOG.error("readFromBytes PersistentStatePO", e);
			} catch (ClassNotFoundException e) {
				LOG.error("readFromBytes PersistentStatePO", e);
			}
			return ppo;
		}
		case 127:
			return true;
		case -1:
			return false;
		case -2: {
			int len = buffer.readInt();
			if (len > 0) {
				byte[] buf = new byte[len];
				buffer.read(buf);
				return deserializeObject(buf);
			}
			return null;
		}
		}
		return null;
	}

	public static void writeToBytes(Object value, DataOutputBuffer destBuffer) throws IOException {
		if (value == null) {
			destBuffer.writeByte(0);
			return;
		}
		if ((value instanceof String)) {
			destBuffer.writeByte(1);
			byte[] bt = ((String) value).getBytes("utf-8");
			destBuffer.writeInt(bt.length);
			if (bt.length > 0)
				destBuffer.write(bt);
		} else if ((value instanceof Integer)) {
			destBuffer.writeByte(2);

			WritableUtils.writeVLong(destBuffer, ((Integer) value).intValue());
		} else if ((value instanceof Boolean)) {
			destBuffer.writeByte(((Boolean) value).booleanValue() ? 127 : -1);
		} else if ((value instanceof Double)) {
			destBuffer.writeByte(3);
			destBuffer.writeDouble(((Double) value).doubleValue());
		} else if ((value instanceof Long)) {
			destBuffer.writeByte(4);
			WritableUtils.writeVLong(destBuffer, ((Long) value).longValue());
		} else if ((value instanceof Byte)) {
			destBuffer.writeByte(5);
			destBuffer.writeByte(((Byte) value).byteValue());
		} else if ((value instanceof Date)) {
			destBuffer.writeByte(8);
			destBuffer.writeLong(((Date) value).getTime());
		} else if ((value instanceof Collection)) {
			destBuffer.writeByte(9);
			Collection list = (Collection) value;
			destBuffer.writeInt(list.size());
			for (Object obj : list) {
				writeToBytes(obj, destBuffer);
			}
		} else if ((value instanceof Object[])) {
			destBuffer.writeByte(11);
			Object[] list = (Object[]) value;
			destBuffer.writeInt(list.length);
			for (Object obj : list)
				writeToBytes(obj, destBuffer);
		} else if ((value instanceof int[])) {
			destBuffer.writeByte(12);
			int[] list = (int[]) value;
			destBuffer.writeInt(list.length);
			for (int obj : list)
				WritableUtils.writeVLong(destBuffer, obj);
		} else if ((value instanceof long[])) {
			destBuffer.writeByte(13);
			long[] list = (long[]) value;
			destBuffer.writeInt(list.length);
			for (long obj : list)
				WritableUtils.writeVLong(destBuffer, obj);
		} else if ((value instanceof char[])) {
			destBuffer.writeByte(14);
			char[] list = (char[]) value;
			destBuffer.writeInt(list.length);
			for (char obj : list)
				destBuffer.writeChar(obj);
		} else if ((value instanceof byte[])) {
			destBuffer.writeByte(15);
			byte[] list = (byte[]) value;
			destBuffer.writeInt(list.length);
			destBuffer.write(list);
		} else if ((value instanceof String[])) {
			destBuffer.writeByte(20);
			String[] list = (String[]) value;
			destBuffer.writeInt(list.length);
			for (String obj : list) {
				byte[] bt = obj.getBytes("utf-8");
				destBuffer.writeInt(bt.length);
				if (bt.length > 0)
					destBuffer.write(bt);
			}
		} else if ((value instanceof Map)) {
			destBuffer.writeByte(30);
			Map map = (Map) value;
			destBuffer.writeInt(map.size());
			for (Iterator localIterator2 = map.keySet().iterator(); localIterator2.hasNext();) {
				Object key = localIterator2.next();
				writeToBytes(key, destBuffer);
				writeToBytes(map.get(key), destBuffer);
			}
		} else if ((value instanceof ServerName)) {
			destBuffer.writeByte(71);
			destBuffer.writeByteString(((ServerName) value).getServerName());
		} else if ((value instanceof OrderHeader)) {
			destBuffer.writeByte(99);
			OrderHeader head = (OrderHeader) value;
			byte[] d = head.serializable();
			destBuffer.writeInt(d.length);
			destBuffer.write(d);
		} else if ((value instanceof Class)) {
			destBuffer.writeByte(100);
			destBuffer.writeShortString(((Class) value).getName());
		} else if ((value instanceof PersistentStatePO)) {
			PersistentStatePO scobj = (PersistentStatePO) value;
			destBuffer.writeByte(PersistentStatePO.SerializableTypeCode);
			destBuffer.writeLong(scobj.getSerUid());
			scobj.writeToBytes(destBuffer);
		} else if ((value instanceof SocketWriteable)) {
			SocketWriteable scobj = (SocketWriteable) value;
			destBuffer.writeByte(126);
			destBuffer.writeShortString(scobj.getClassName());
			scobj.writeToBytes(destBuffer);
		} else {
			destBuffer.writeByte(-2);
			byte[] bt = serialObject(value);
			if (bt != null) {
				destBuffer.writeInt(bt.length);
				destBuffer.write(bt);
			} else {
				destBuffer.writeInt(0);
			}
		}
	}

	public static <T> T deserializeObject(byte[] bts) throws IOException {
		bts = GZIPUtils.ungzip(bts);
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bts);
		ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
		try {
			return (T) objectInputStream.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new IOException(e);
		} finally {
			objectInputStream.close();
			byteArrayInputStream.close();
		}
	}

	public static byte[] serialObject(Object obj) {
		try {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
			objectOutputStream.writeObject(obj);
			byte[] bts = GZIPUtils.gzip(byteArrayOutputStream.toByteArray());
			objectOutputStream.close();
			byteArrayOutputStream.close();
			return bts;
		} catch (Exception e) {
		}
		return null;
	}

	public static byte[] getBufferData(DataOutputBuffer buffer) {
		if (buffer.getLength() == buffer.getData().length) {
			return buffer.getData();
		}
		return Arrays.copyOf(buffer.getData(), buffer.getLength());
	}
}