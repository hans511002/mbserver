package com.sobey.base.socket;

import java.io.IOException;

import com.sobey.base.util.DataInputBuffer;
import com.sobey.base.util.DataOutputBuffer;

public interface SocketWriteable {
	public final static byte SerializableTypeCode = 126;

	public String getClassName();

	public void writeToBytes(DataOutputBuffer destBuffer) throws IOException;

	public void readFromBytes(DataInputBuffer srcBuffer) throws IOException;

}
