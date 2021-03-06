/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

import java.io.UnsupportedEncodingException;

/**
 * Utility for Strings.
 */
public class Strings {
	public final static String DEFAULT_SEPARATOR = "=";
	public final static String DEFAULT_KEYVALUE_SEPARATOR = ", ";

	/**
	 * Append to a StringBuilder a key/value. Uses default separators.
	 * 
	 * @param sb
	 *            StringBuilder to use
	 * @param key
	 *            Key to append.
	 * @param value
	 *            Value to append.
	 * @return Passed <code>sb</code> populated with key/value.
	 */
	public static StringBuilder appendKeyValue(final StringBuilder sb, final String key, final Object value) {
		return appendKeyValue(sb, key, value, DEFAULT_SEPARATOR, DEFAULT_KEYVALUE_SEPARATOR);
	}

	/**
	 * Append to a StringBuilder a key/value. Uses default separators.
	 * 
	 * @param sb
	 *            StringBuilder to use
	 * @param key
	 *            Key to append.
	 * @param value
	 *            Value to append.
	 * @param separator
	 *            Value to use between key and value.
	 * @param keyValueSeparator
	 *            Value to use between key/value sets.
	 * @return Passed <code>sb</code> populated with key/value.
	 */
	public static StringBuilder appendKeyValue(final StringBuilder sb, final String key, final Object value, final String separator,
			final String keyValueSeparator) {
		if (sb.length() > 0) {
			sb.append(keyValueSeparator);
		}
		return sb.append(key).append(separator).append(value);
	}

	/**
	 * Given a PTR string generated via reverse DNS lookup, return everything except the trailing period. Example for host.example.com.,
	 * return host.example.com
	 * 
	 * @param dnPtr
	 *            a domain name pointer (PTR) string.
	 * @return Sanitized hostname with last period stripped off.
	 * 
	 */
	public static String domainNamePointerToHostName(String dnPtr) {
		if (dnPtr == null)
			return null;
		return dnPtr.endsWith(".") ? dnPtr.substring(0, dnPtr.length() - 1) : dnPtr;
	}

	/**
	 * Null-safe length check.
	 * 
	 * @param input
	 * @return true if null or length==0
	 */
	public static boolean isEmpty(String input) {
		return input == null || input.length() == 0;
	}

	/**
	 * Push the input string to the right by appending a character before it, usually a space.
	 * 
	 * @param input
	 *            the string to pad
	 * @param padding
	 *            the character to repeat to the left of the input string
	 * @param length
	 *            the desired total length including the padding
	 * @return padding characters + input
	 */
	public static String padFront(String input, char padding, int length) {
		if (input.length() > length) {
			throw new IllegalArgumentException("input \"" + input + "\" longer than maxLength=" + length);
		}
		int numPaddingCharacters = length - input.length();
		return repeat(padding, numPaddingCharacters) + input;
	}

	/**
	 * @param c
	 *            repeat this character
	 * @param reapeatFor
	 *            the length of the output String
	 * @return c, repeated repeatFor times
	 */
	public static String repeat(char c, int reapeatFor) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < reapeatFor; ++i) {
			sb.append(c);
		}
		return sb.toString();
	}

	public static String parseISO2GBK(String in) {

		String s = null;
		byte temp[];
		if (in == null) {
			System.out.println("Warn:Chinese null founded!");
			return new String("");
		}
		try {
			temp = in.getBytes("iso-8859-1");
			s = new String(temp, "GBK");
		} catch (UnsupportedEncodingException e) {
			System.out.println("���ֱ���ת�����?" + e.toString());
		}
		return s;
	}

	public static String parseGBK2ISO(String in) {

		String s = null;
		byte temp[];
		if (in == null) {
			System.out.println("Warn:Chinese null founded!");
			return new String("");
		}
		try {
			temp = in.getBytes("GBK");
			s = new String(temp, "iso-8859-1");

		} catch (UnsupportedEncodingException e) {
			System.out.println("���ֱ���ת�����?" + e.toString());

		}
		return s;

	}

}
