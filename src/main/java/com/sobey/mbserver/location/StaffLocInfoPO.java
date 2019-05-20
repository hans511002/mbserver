package com.sobey.mbserver.location;

import java.util.Date;

import com.sobey.base.PersistentStatePO;

public class StaffLocInfoPO extends PersistentStatePO {
	long LOCINFO_ID;
	long STAFF_ID;
	long MIN_MARKER_ID1;
	long MIN_MARKER_ID2;
	long MIN_MARKER_ID3;
	String STAFF_MOBILE;
	double LONGITUDE;
	double LATITUDE;
	double ELEVATION;
	Date FEEDBACK_TIME;
	int STAFF_ACTION;
	double MARKER_ID1_DISTANCE;
	double MARKER_ID2_DISTANCE;
	double MARKER_ID3_DISTANCE;
	String ADDRESS_DESC;
	String ERROR_MSG;
	int LOCATE_TYPE;
	int LOCATE_RADIUS;

	public int getLOCATE_TYPE() {
		return LOCATE_TYPE;
	}

	public void setLOCATE_TYPE(int lOCATE_TYPE) {
		set(lOCATE_TYPE);// LOCATE_TYPE = lOCATE_TYPE;
	}

	public int getLOCATE_RADIUS() {
		return LOCATE_RADIUS;
	}

	public void setLOCATE_RADIUS(int lOCATE_RADIUS) {
		set(lOCATE_RADIUS);// LOCATE_RADIUS = lOCATE_RADIUS;
	}

	public String getERROR_MSG() {
		return ERROR_MSG;
	}

	public void setERROR_MSG(String eRROR_MSG) {
		set(eRROR_MSG);// this.setValue(eRROR_MSG);// ERROR_MSG = eRROR_MSG;
	}

	public StaffLocInfoPO() {
		LOCINFO_ID = 0;
		FEEDBACK_TIME = new Date();
	}

	public long getLOCINFO_ID() {
		return LOCINFO_ID;
	}

	public void setLOCINFO_ID(long lOCINFO_ID) {
		set(lOCINFO_ID);// this.setValue(lOCINFO_ID);// LOCINFO_ID = lOCINFO_ID;
	}

	public long getSTAFF_ID() {
		return STAFF_ID;
	}

	public void setSTAFF_ID(long sTAFF_ID) {
		this.set(sTAFF_ID);// STAFF_ID = sTAFF_ID;
	}

	public long getMIN_MARKER_ID1() {
		return MIN_MARKER_ID1;
	}

	public void setMIN_MARKER_ID1(long mIN_MARKER_ID1) {
		this.set(mIN_MARKER_ID1);// MIN_MARKER_ID1 = mIN_MARKER_ID1;
	}

	public long getMIN_MARKER_ID2() {
		return MIN_MARKER_ID2;
	}

	public void setMIN_MARKER_ID2(long mIN_MARKER_ID2) {
		this.set(mIN_MARKER_ID2);// MIN_MARKER_ID2 = mIN_MARKER_ID2;
	}

	public long getMIN_MARKER_ID3() {
		return MIN_MARKER_ID3;
	}

	public void setMIN_MARKER_ID3(long mIN_MARKER_ID3) {
		this.set(mIN_MARKER_ID3);// MIN_MARKER_ID3 = mIN_MARKER_ID3;
	}

	public String getSTAFF_MOBILE() {
		return STAFF_MOBILE;
	}

	public void setSTAFF_MOBILE(String sTAFF_MOBILE) {
		this.set(sTAFF_MOBILE);// STAFF_MOBILE = sTAFF_MOBILE;
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

	public Date getFEEDBACK_TIME() {
		return FEEDBACK_TIME;
	}

	public void setFEEDBACK_TIME(Date fEEDBACK_TIME) {
		this.set(fEEDBACK_TIME);// FEEDBACK_TIME = fEEDBACK_TIME;
	}

	public int getSTAFF_ACTION() {
		return STAFF_ACTION;
	}

	public void setSTAFF_ACTION(int sTAFF_ACTION) {
		this.set(sTAFF_ACTION);// STAFF_ACTION = sTAFF_ACTION;
	}

	public double getMARKER_ID1_DISTANCE() {
		return MARKER_ID1_DISTANCE;
	}

	public void setMARKER_ID1_DISTANCE(double mARKER_ID1_DISTANCE) {
		this.set(mARKER_ID1_DISTANCE);// MARKER_ID1_DISTANCE = mARKER_ID1_DISTANCE;
	}

	public double getMARKER_ID2_DISTANCE() {
		return MARKER_ID2_DISTANCE;
	}

	public void setMARKER_ID2_DISTANCE(double mARKER_ID2_DISTANCE) {
		this.set(mARKER_ID2_DISTANCE);// MARKER_ID2_DISTANCE = mARKER_ID2_DISTANCE;
	}

	public double getMARKER_ID3_DISTANCE() {
		return MARKER_ID3_DISTANCE;
	}

	public void setMARKER_ID3_DISTANCE(double mARKER_ID3_DISTANCE) {
		this.set(mARKER_ID3_DISTANCE);// MARKER_ID3_DISTANCE = mARKER_ID3_DISTANCE;
	}

	public String getADDRESS_DESC() {
		return ADDRESS_DESC;
	}

	public void setADDRESS_DESC(String aDDRESS_DESC) {
		this.set(aDDRESS_DESC);// ADDRESS_DESC = aDDRESS_DESC;
	}

}
