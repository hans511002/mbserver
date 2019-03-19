package com.sobey.mbserver.location;

public class Bounds {
	public double minLng;
	public double minLat;
	public double maxLng;
	public double maxLat;

	public Bounds() {
	}

	public Bounds(double minLng, double minLat, double maxLng, double maxLat) {
		this.minLng = minLng;
		this.minLat = minLat;
		this.maxLng = maxLng;
		this.maxLat = maxLat;
	}

	public double getMinLng() {
		return minLng;
	}

	public void setMinLng(double minLng) {
		this.minLng = minLng;
	}

	public double getMinLat() {
		return minLat;
	}

	public void setMinLat(double minLat) {
		this.minLat = minLat;
	}

	public double getMaxLng() {
		return maxLng;
	}

	public void setMaxLng(double maxLng) {
		this.maxLng = maxLng;
	}

	public double getMaxLat() {
		return maxLat;
	}

	public void setMaxLat(double maxLat) {
		this.maxLat = maxLat;
	}

}
