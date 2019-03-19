/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sobey.base.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A collection of utility methods for working on GZIPed data.
 */
public class GZIPUtils {
	public static final Log LOG = LogFactory.getLog(GZIPUtils.class);

	private static final int EXPECTED_COMPRESSION_RATIO = 5;
	private static final int BUF_SIZE = 4096;

	/**
	 * Returns an gunzipped copy of the input array. If the gzipped input has been truncated or corrupted, a best-effort
	 * attempt is made to unzip as much as possible. If no data can be extracted <code>null</code> is returned.
	 */
	public static final byte[] unzipBestEffort(byte[] in) {
		return unzipBestEffort(in, Integer.MAX_VALUE);
	}

	/**
	 * Returns an gunzipped copy of the input array, truncated to <code>sizeLimit</code> bytes, if necessary. If the
	 * gzipped input has been truncated or corrupted, a best-effort attempt is made to unzip as much as possible. If no
	 * data can be extracted <code>null</code> is returned.
	 */
	public static final byte[] unzipBestEffort(byte[] in, int sizeLimit) {
		try {
			// decompress using GZIPInputStream
			ByteArrayOutputStream outStream = new ByteArrayOutputStream(EXPECTED_COMPRESSION_RATIO * in.length);

			GZIPInputStream inStream = new GZIPInputStream(new ByteArrayInputStream(in));

			byte[] buf = new byte[BUF_SIZE];
			int written = 0;
			while (true) {
				try {
					int size = inStream.read(buf);
					if (size <= 0)
						break;
					if ((written + size) > sizeLimit) {
						outStream.write(buf, 0, sizeLimit - written);
						break;
					}
					outStream.write(buf, 0, size);
					written += size;
				} catch (Exception e) {
					break;
				}
			}
			try {
				outStream.close();
			} catch (IOException e) {
			}

			return outStream.toByteArray();

		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Returns an gunzipped copy of the input array.
	 * 
	 * @throws IOException
	 *             if the input cannot be properly decompressed
	 */
	public static final byte[] ungzip(byte[] in) throws IOException {
		// decompress using GZIPInputStream
		ByteArrayOutputStream outStream = new ByteArrayOutputStream(EXPECTED_COMPRESSION_RATIO * in.length);

		GZIPInputStream inStream = new GZIPInputStream(new ByteArrayInputStream(in));

		byte[] buf = new byte[BUF_SIZE];
		while (true) {
			int size = inStream.read(buf);
			if (size <= 0)
				break;
			outStream.write(buf, 0, size);
		}
		inStream.close();
		outStream.close();

		return outStream.toByteArray();
	}

	/**
	 * Returns an gzipped copy of the input array.
	 */
	public static final byte[] gzip(byte[] in) {
		try {
			// compress using GZIPOutputStream
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream(in.length / EXPECTED_COMPRESSION_RATIO);

			GZIPOutputStream outStream = new GZIPOutputStream(byteOut);

			try {
				outStream.write(in);
			} catch (Exception e) {
				LOG.error("Failed to get outStream.write input", e);
			}

			try {
				outStream.close();
			} catch (IOException e) {
				LOG.error("Failed to implement outStream.close", e);
			}

			return byteOut.toByteArray();

		} catch (IOException e) {
			LOG.error("Failed with IOException", e);
			return null;
		}
	}

	public static final byte[] gzip(byte[] in, int offset, int length) {
		try {
			// compress using GZIPOutputStream
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream(length / EXPECTED_COMPRESSION_RATIO);

			GZIPOutputStream outStream = new GZIPOutputStream(byteOut);

			try {
				outStream.write(in, offset, length);
			} catch (Exception e) {
				LOG.error("Failed to get outStream.write input", e);
			}

			try {
				outStream.close();
			} catch (IOException e) {
				LOG.error("Failed to implement outStream.close", e);
			}

			return byteOut.toByteArray();

		} catch (IOException e) {
			LOG.error("Failed with IOException", e);
			return null;
		}
	}

	/***
	 * ѹ��Zip
	 * 
	 * @param data
	 * @return
	 */
	public static byte[] zip(byte[] data) {
		byte[] b = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ZipOutputStream zip = new ZipOutputStream(bos);
			ZipEntry entry = new ZipEntry("zip");
			entry.setSize(data.length);
			zip.putNextEntry(entry);
			zip.write(data);
			zip.closeEntry();
			zip.close();
			b = bos.toByteArray();
			bos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return b;
	}

	/***
	 * ��ѹZip
	 * 
	 * @param data
	 * @return
	 */
	public static byte[] unZip(byte[] data) {
		byte[] b = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			ZipInputStream zip = new ZipInputStream(bis);
			while (zip.getNextEntry() != null) {
				byte[] buf = new byte[1024];
				int num = -1;
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				while ((num = zip.read(buf, 0, buf.length)) != -1) {
					baos.write(buf, 0, num);
				}
				b = baos.toByteArray();
				baos.flush();
				baos.close();
			}
			zip.close();
			bis.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return b;
	}

	/**
	 * ���ֽ�����ת����16�����ַ���
	 * 
	 * @param bArray
	 * @return
	 */
	public static String bytesToHexString(byte[] bArray) {
		StringBuffer sb = new StringBuffer(bArray.length);
		String sTemp;
		for (int i = 0; i < bArray.length; i++) {
			sTemp = Integer.toHexString(0xFF & bArray[i]);
			if (sTemp.length() < 2)
				sb.append(0);
			sb.append(sTemp.toUpperCase());
		}
		return sb.toString();
	}

	//
	// /***
	// * ѹ��BZip2
	// *
	// * @param data
	// * @return
	// */
	// public static byte[] bZip2(byte[] data) {
	// byte[] b = null;
	// try {
	// ByteArrayOutputStream bos = new ByteArrayOutputStream();
	// CBZip2OutputStream bzip2 = new CBZip2OutputStream(bos);
	// bzip2.write(data);
	// bzip2.flush();
	// bzip2.close();
	// b = bos.toByteArray();
	// bos.close();
	// } catch (Exception ex) {
	// ex.printStackTrace();
	// }
	// return b;
	// }
	//
	// /***
	// * ��ѹBZip2
	// *
	// * @param data
	// * @return
	// */
	// public static byte[] unBZip2(byte[] data) {
	// byte[] b = null;
	// try {
	// ByteArrayInputStream bis = new ByteArrayInputStream(data);
	// CBZip2InputStream bzip2 = new CBZip2InputStream(bis);
	// byte[] buf = new byte[1024];
	// int num = -1;
	// ByteArrayOutputStream baos = new ByteArrayOutputStream();
	// while ((num = bzip2.read(buf, 0, buf.length)) != -1) {
	// baos.write(buf, 0, num);
	// }
	// b = baos.toByteArray();
	// baos.flush();
	// baos.close();
	// bzip2.close();
	// bis.close();
	// } catch (Exception ex) {
	// ex.printStackTrace();
	// }
	// return b;
	// }
	// /**
	// * jzlib ѹ������
	// *
	// * @param object
	// * @return
	// * @throws IOException
	// */
	// public static byte[] jzlib(byte[] object) {
	//
	// byte[] data = null;
	// try {
	// ByteArrayOutputStream out = new ByteArrayOutputStream();
	// ZOutputStream zOut = new ZOutputStream(out, JZlib.Z_DEFAULT_COMPRESSION);
	// DataOutputStream objOut = new DataOutputStream(zOut);
	// objOut.write(object);
	// objOut.flush();
	// zOut.close();
	// data = out.toByteArray();
	// out.close();
	//
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// return data;
	// }
	//
	// /**
	// * jzLibѹ��������
	// *
	// * @param object
	// * @return
	// * @throws IOException
	// */
	// public static byte[] unjzlib(byte[] object) {
	//
	// byte[] data = null;
	// try {
	// ByteArrayInputStream in = new ByteArrayInputStream(object);
	// ZInputStream zIn = new ZInputStream(in);
	// byte[] buf = new byte[1024];
	// int num = -1;
	// ByteArrayOutputStream baos = new ByteArrayOutputStream();
	// while ((num = zIn.read(buf, 0, buf.length)) != -1) {
	// baos.write(buf, 0, num);
	// }
	// data = baos.toByteArray();
	// baos.flush();
	// baos.close();
	// zIn.close();
	// in.close();
	//
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// return data;
	// }
	public static void main(String[] args) throws IOException {
		String s = "this is a test";

		byte[] b1 = zip(s.getBytes());
		System.out.println("zip:" + bytesToHexString(b1));
		byte[] b2 = unZip(b1);
		System.out.println("unZip:" + new String(b2));
		//
		// byte[] b3 = bZip2(s.getBytes());
		// System.out.println("bZip2:" + bytesToHexString(b3));
		// byte[] b4 = unBZip2(b3);
		// System.out.println("unBZip2:" + new String(b4));

		byte[] b5 = gzip(s.getBytes());
		System.out.println("bZip2:" + bytesToHexString(b5));
		byte[] b6 = ungzip(b5);
		System.out.println("unBZip2:" + new String(b6));
		//
		// byte[] b7 = jzlib(s.getBytes());
		// System.out.println("jzlib:" + bytesToHexString(b7));
		// byte[] b8 = unjzlib(b7);
		// System.out.println("unjzlib:" + new String(b8));
	}
}
