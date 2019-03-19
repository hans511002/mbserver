package com.sobey.base.socket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.sobey.base.exception.DeserializationException;
import com.sobey.base.util.Addressing;
import com.sobey.base.util.GZIPUtils;
import com.sobey.jcg.support.log4j.LogUtils;

public class ServerName implements Comparable<ServerName>, Serializable {
	private static final long serialVersionUID = 948662243093059938L;
	public static final String SERVERNAME_SEPARATOR = ",";
	public static final String UNKNOWN_SERVERNAME = "#unknown#";
	private final String servername;
	public String hostnameOnly;
	public final int port;
	public final long startTime;
	private byte[] bytes;
	public static final List<ServerName> EMPTY_SERVER_LIST = new ArrayList(0);

	public ServerName(String hostname, int port, long startTime) {
		this.hostnameOnly = hostname;
		this.port = port;
		this.startTime = startTime;
		this.servername = getServerName(this.hostnameOnly, port, startTime);
	}

	public ServerName(String serverName) {
		this(parseHostname(serverName), parsePort(serverName), parseServerStime(serverName));
	}

	public ServerName(String hostAndPort, long startTime) {
		this(Addressing.parseHostname(hostAndPort), Addressing.parsePort(hostAndPort), startTime);
	}

	public static String parseHostname(String serverName) {
		if ((serverName == null) || (serverName.length() <= 0)) {
			throw new IllegalArgumentException("Passed hostname is null or empty");
		}
		if (!Character.isLetterOrDigit(serverName.charAt(0))) {
			throw new IllegalArgumentException("Bad passed hostname, serverName=" + serverName);
		}
		int index = serverName.indexOf(",");
		return serverName.substring(0, index);
	}

	public static int parsePort(String serverName) {
		String[] split = serverName.split(",");
		return Integer.parseInt(split[1]);
	}

	public static long parseServerStime(String serverName) {
		int index = serverName.lastIndexOf(",");
		return Long.parseLong(serverName.substring(index + 1));
	}

	public String toString() {
		return getServerName();
	}

	public String toShortString() {
		return this.hostnameOnly + ":" + this.port;
	}

	public String getServerName() {
		return this.servername;
	}

	public String getHostname() {
		return this.hostnameOnly;
	}

	public int getPort() {
		return this.port;
	}

	public long getStartTime() {
		return this.startTime;
	}

	static String getServerName(String hostName, int port, long startTime) {
		StringBuilder name = new StringBuilder(hostName.length() + 1 + 5 + 1 + 9);
		name.append(hostName);
		name.append(",");
		name.append(port);
		name.append(",");
		name.append(startTime);
		return name.toString();
	}

	public static String getServerName(String hostAndPort, long startTime) {
		int index = hostAndPort.indexOf(":");
		if (index <= 0)
			throw new IllegalArgumentException("Expected <hostname> ':' <port>");
		return getServerName(hostAndPort.substring(0, index), Integer.parseInt(hostAndPort.substring(index + 1)), startTime);
	}

	public String getHostAndPort() {
		return Addressing.createHostAndPortStr(this.hostnameOnly, this.port);
	}

	public static long getServerStartcodeFromServerName(String serverName) {
		int index = serverName.lastIndexOf(",");
		return Long.parseLong(serverName.substring(index + 1));
	}

	public static String getServerNameLessStartCode(String inServerName) {
		if ((inServerName != null) && (inServerName.length() > 0)) {
			int index = inServerName.lastIndexOf(",");
			if (index > 0) {
				return inServerName.substring(0, index);
			}
		}
		return inServerName;
	}

	public int compareTo(ServerName other) {
		int compare = getHostname().toLowerCase().compareTo(other.getHostname().toLowerCase());
		if (compare != 0)
			return compare;
		compare = getPort() - other.getPort();
		if (compare != 0)
			return compare;
		return (int) (getStartTime() - other.getStartTime());
	}

	public int hashCode() {
		return getServerName().hashCode();
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null)
			return false;
		if (!(o instanceof ServerName))
			return false;
		return compareTo((ServerName) o) == 0;
	}

	public static boolean isSameHostnameAndPort(ServerName left, ServerName right) {
		if (left == null)
			return false;
		if (right == null)
			return false;
		return (left.getHostname().equals(right.getHostname())) && (left.getPort() == right.getPort());
	}

	public byte[] toByteArray() {
		try {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
			try {
				objectOutputStream.writeObject(this);
				byte[] bts = GZIPUtils.gzip(byteArrayOutputStream.toByteArray());
				return bts;
			} finally {
				objectOutputStream.close();
				byteArrayOutputStream.close();
			}
		} catch (IOException e) {
		}

		return new byte[0];
	}

	public static ServerName parseFrom(byte[] data) throws DeserializationException {
		if ((data == null) || (data.length <= 0))
			return null;
		try {
			byte[] bts = GZIPUtils.ungzip(data);
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bts);
			ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
			try {
				return (ServerName) objectInputStream.readObject();
			} finally {
				objectInputStream.close();
				byteArrayInputStream.close();
			}
		} catch (Exception e) {
			LogUtils.error("反序列化错误", e);
			throw new DeserializationException(e);
		}
	}
}