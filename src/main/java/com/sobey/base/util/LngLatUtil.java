package com.sobey.base.util;

import com.sobey.mbserver.location.Bounds;

public class LngLatUtil {
	private static final double EARTH_RADIUS = 6378137;
	private static final double RAD = Math.PI / 180.0;

	/**
	 * 根据经纬度和半径获得最小矩形区域
	 * 
	 * @param lng
	 * @param lat
	 * @param d
	 *            单位米
	 */
	public static Bounds getBounds(double lng, double lat, double d) {
		Double longitude = lng;
		Double latitude = lat;

		Double degree = (24901 * 1609) / 360.0;
		double raidusMile = d;

		Double dpmLat = 1 / degree;
		Double radiusLat = dpmLat * raidusMile;
		Double minLat = latitude - radiusLat;
		Double maxLat = latitude + radiusLat;

		Double mpdLng = degree * Math.cos(latitude * (Math.PI / 180));
		Double dpmLng = 1 / mpdLng;
		Double radiusLng = dpmLng * raidusMile;
		Double minLng = longitude - radiusLng;
		Double maxLng = longitude + radiusLng;
		return new Bounds(minLng, minLat, maxLng, maxLat);
	}

	/**
	 * 根据两点间经纬度坐标（double值），计算两点间距离，单位为米
	 * 
	 * @param lng1
	 * @param lat1
	 * @param lng2
	 * @param lat2
	 * @return
	 */
	public static double getDistance(double lng1, double lat1, double lng2, double lat2) {
		double radLat1 = lat1 * RAD;
		double radLat2 = lat2 * RAD;
		double a = radLat1 - radLat2;
		double b = (lng1 - lng2) * RAD;
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2)
				* Math.pow(Math.sin(b / 2), 2)));
		s = s * EARTH_RADIUS;
		return Math.round(s);
	}

}
