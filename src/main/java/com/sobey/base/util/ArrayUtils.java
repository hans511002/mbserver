/*
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sobey.jcg.support.utils.Convert;

/**
 * A set of array utility functions that return reasonable values in cases where an array is allocated or if it is null
 */
public class ArrayUtils extends com.sobey.jcg.support.utils.ArrayUtils{

	public static int length(byte[] a) {
		if (a == null) {
			return 0;
		}
		return a.length;
	}

	public static int length(long[] a) {
		if (a == null) {
			return 0;
		}
		return a.length;
	}

	public static int length(Object[] a) {
		if (a == null) {
			return 0;
		}
		return a.length;
	}

	public static boolean isEmpty(byte[] a) {
		if (a == null) {
			return true;
		}
		if (a.length == 0) {
			return true;
		}
		return false;
	}

	public static boolean isEmpty(long[] a) {
		if (a == null) {
			return true;
		}
		if (a.length == 0) {
			return true;
		}
		return false;
	}

	public static boolean isEmpty(Object[] a) {
		if (a == null) {
			return true;
		}
		if (a.length == 0) {
			return true;
		}
		return false;
	}

	public static long getFirst(long[] a) {
		return a[0];
	}

	public static long getLast(long[] a) {
		return a[a.length - 1];
	}

	public static int getTotalLengthOfArrays(Iterable<byte[]> arrays) {
		if (arrays == null) {
			return 0;
		}
		int length = 0;
		for (byte[] bytes : arrays) {
			length += length(bytes);
		}
		return length;
	}

	public static ArrayList<Long> toList(long[] array) {
		int length = length(array);
		ArrayList<Long> list = new ArrayList<Long>(length);
		for (int i = 0; i < length; ++i) {
			list.add(array[i]);
		}
		return list;
	}

	public static byte[] growIfNecessary(byte[] array, int minLength, int numAdditionalBytes) {
		if (array.length >= minLength) {
			return array;
		}
		return Arrays.copyOf(array, minLength + numAdditionalBytes);
	}

	public static int[] growIfNecessary(int[] array, int minLength, int numAdditionalInts) {
		if (array.length >= minLength) {
			return array;
		}
		return Arrays.copyOf(array, minLength + numAdditionalInts);
	}

	public static long[] growIfNecessary(long[] array, int minLength, int numAdditionalLongs) {
		if (array.length >= minLength) {
			return array;
		}
		return Arrays.copyOf(array, minLength + numAdditionalLongs);
	}

	// 版本排序
	public static void sortAppVersion(List<String> vers) {
		String[] ver = vers.toArray(new String[0]);
		Arrays.sort(ver, new java.util.Comparator<String>() {

			public int compare(String o1, String o2) {
				if (o1 == null) {
					return -1;
				}
				if (o2 == null) {
					return 1;
				}
				String s1[] = o1.split("\\.");
				String s2[] = o2.split("\\.");
				int index = 0;
				while (true) {
					if (s1.length < index) {
						return -1;
					}
					if (s2.length < index) {
						return 1;
					}
					int sv1 = Convert.toInt(s1[index], 0);
					int sv2 = Convert.toInt(s2[index], 0);
					if (sv1 == sv2) {
						index++;
						continue;
					}
					if (sv1 < sv2) {
						return -1;
					} else {
						return 1;
					}
				}
			}

		});
		vers.clear();
		vers.addAll(Arrays.asList(ver));
	}

}
