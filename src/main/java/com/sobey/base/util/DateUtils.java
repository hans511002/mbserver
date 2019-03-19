package com.sobey.base.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DateUtils {
	public static final String sdf = "yyyy-MM-dd HH:mm:ss";
	public static final String yyyy = "yyyy";
	public static final String yyyymm = "yyyyMM";
	public static final String yyyymmdd = "yyyyMMdd";
	public static final long utsTime = new Date(70, 0, 1).getTime();
	private static final Map<String, ThreadLocal<SimpleDateFormat>> sdfMap = new HashMap<String, ThreadLocal<SimpleDateFormat>>();
	public static final String utsTimeString = format(new Date(70, 0, 1));

	public static SimpleDateFormat getSdf(final SimpleDateFormat sdf) {
		ThreadLocal<SimpleDateFormat> tl = sdfMap.get(sdf.toPattern());
		if (tl == null) {
			synchronized (sdfMap) {
				tl = sdfMap.get(sdf.toPattern());
				if (tl == null) {
					tl = new ThreadLocal<SimpleDateFormat>() {
						@Override
						protected SimpleDateFormat initialValue() {
							return new SimpleDateFormat(sdf.toPattern());
						}
					};
					sdfMap.put(sdf.toPattern(), tl);
				}
			}
		}
		SimpleDateFormat s = tl.get();
		return s;
	}

	public static SimpleDateFormat getSdf(final String pattern) {
		ThreadLocal<SimpleDateFormat> tl = sdfMap.get(pattern);
		if (tl == null) {
			synchronized (sdfMap) {
				tl = sdfMap.get(pattern);
				if (tl == null) {
					tl = new ThreadLocal<SimpleDateFormat>() {// initialValue
						@Override
						protected SimpleDateFormat initialValue() {
							return new SimpleDateFormat(pattern);
						}
					};
					tl.set(new SimpleDateFormat(pattern));
					sdfMap.put(pattern, tl);
				}
			}
		}
		SimpleDateFormat s = tl.get();
		return s;
	}

	public static String format(SimpleDateFormat sdf, Date date) {
		return getSdf(sdf).format(date);
	}

	public static String format(String pattern, Date date) {
		return getSdf(pattern).format(date);
	}

	public static String format(SimpleDateFormat sdf, long time) {
		return getSdf(sdf).format(new Date(time));
	}

	public static String format(Date date) {
		return getSdf(sdf).format(date);
	}

	public static String format(long time) {
		return getSdf(sdf).format(new Date(time));
	}

	public static String format(String pattern, long time) {
		return getSdf(pattern).format(new Date(time));
	}

	public static Date parse(SimpleDateFormat sdf, String dateStr) throws ParseException {
		return getSdf(sdf).parse(dateStr);
	}

	public static Date parse(String pattern, String dateStr) throws ParseException {
		return getSdf(pattern).parse(dateStr);
	}

	public static Date parse(String dateStr) throws ParseException {
		return getSdf(sdf).parse(dateStr);
	}
}
