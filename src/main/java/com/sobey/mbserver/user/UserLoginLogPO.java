package com.sobey.mbserver.user;

import java.util.Date;

import com.sobey.base.PersistentStatePO;

public class UserLoginLogPO extends PersistentStatePO {
	long LOGIN_LOG_ID;
	long USER_ID;
	Date LOGIN_TIME;
	Date LOGOUT_TIME;
	double LOGIN_LNG;
	double LOGIN_LAT;
	String LOGIN_ADDRESS;

	public long getLOGIN_LOG_ID() {
		return this.LOGIN_LOG_ID;
	}

	public void setLOGIN_LOG_ID(long lOGIN_LOG_ID) {
		set(Long.valueOf(lOGIN_LOG_ID));
	}

	public long getUSER_ID() {
		return this.USER_ID;
	}

	public void setUSER_ID(long uSER_ID) {
		set(Long.valueOf(uSER_ID));
	}

	public Date getLOGIN_TIME() {
		return this.LOGIN_TIME;
	}

	public void setLOGIN_TIME(Date lOGIN_TIME) {
		set(lOGIN_TIME);
	}

	public Date getLOGOUT_TIME() {
		return this.LOGOUT_TIME;
	}

	public void setLOGOUT_TIME(Date lOGOUT_TIME) {
		set(lOGOUT_TIME);
	}

	public double getLOGIN_LNG() {
		return this.LOGIN_LNG;
	}

	public void setLOGIN_LNG(double lOGIN_LNG) {
		set(Double.valueOf(lOGIN_LNG));
	}

	public double getLOGIN_LAT() {
		return this.LOGIN_LAT;
	}

	public void setLOGIN_LAT(double lOGIN_LAT) {
		this.LOGIN_LAT = lOGIN_LAT;
	}

	public String getLOGIN_ADDRESS() {
		return this.LOGIN_ADDRESS;
	}

	public void setLOGIN_ADDRESS(String lOGIN_ADDRESS) {
		set(lOGIN_ADDRESS);
	}
}