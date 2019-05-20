package com.sobey.mbserver.location;

import com.sobey.base.PersistentStatePO;

public class MarkerLocInfoPO extends PersistentStatePO {
	long MARKER_ID;
	int MARKER_TYPE;
	long MARKER_SRC_ID;
	String MARKER_NAME;
	double LONGITUDE;
	double LATITUDE;
	double ELEVATION;
	double DISTANCE_RANGE;
	String MARKER_DESC;

	public long getMARKER_ID() {
		return MARKER_ID;
	}

	public void setMARKER_ID(long mARKER_ID) {
		this.set(mARKER_ID);// MARKER_ID = mARKER_ID;
	}

	public int getMARKER_TYPE() {
		return MARKER_TYPE;
	}

	public void setMARKER_TYPE(int mARKER_TYPE) {
		this.set(mARKER_TYPE);// MARKER_TYPE = mARKER_TYPE;
	}

	public long getMARKER_SRC_ID() {
		return MARKER_SRC_ID;
	}

	public void setMARKER_SRC_ID(long mARKER_SRC_ID) {
		this.set(mARKER_SRC_ID);// MARKER_SRC_ID = mARKER_SRC_ID;
	}

	public String getMARKER_NAME() {
		return MARKER_NAME;
	}

	public void setMARKER_NAME(String mARKER_NAME) {
		this.set(mARKER_NAME);// MARKER_NAME = mARKER_NAME;
	}

	public double getLONGITUDE() {
		return LONGITUDE;
	}

	public void setLONGITUDE(double lONGITUDE) {
		this.set(lONGITUDE);// LONGITUDE = lONGITUDE;
	}

	public double getLATITUDE() {
		return LATITUDE;
	}

	public void setLATITUDE(double lATITUDE) {
		this.set(lATITUDE);// LATITUDE = lATITUDE;
	}

	public double getELEVATION() {
		return ELEVATION;
	}

	public void setELEVATION(double eLEVATION) {
		this.set(eLEVATION);// ELEVATION = eLEVATION;
	}

	public double getDISTANCE_RANGE() {
		return DISTANCE_RANGE;
	}

	public void setDISTANCE_RANGE(double dISTANCE_RANGE) {
		this.set(dISTANCE_RANGE);// DISTANCE_RANGE = dISTANCE_RANGE;
	}

	public String getMARKER_DESC() {
		return MARKER_DESC;
	}

	public void setMARKER_DESC(String mARKER_DESC) {
		this.set(mARKER_DESC);// MARKER_DESC = mARKER_DESC;
	}

}
