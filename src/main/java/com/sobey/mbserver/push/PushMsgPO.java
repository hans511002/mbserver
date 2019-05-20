package com.sobey.mbserver.push;

import java.util.Date;

import com.sobey.base.PersistentStatePO;

public class PushMsgPO extends PersistentStatePO {
	long PUSH_ID;
	long STAFF_ID;
	long COMPANY_ID;
	long ORDER_ID;
	Date SUBMIT_TIME;
	Date PUSH_TIME;
	int PUSH_STATE = -1;
	int PUSH_TYPE;
	String MSG;
	String ERROR_MSG;
	int EVENT_TYPE;
	int ACTION_TYPE;
	String STAFF_MOBILE;
	int RES_PUSH_TYPE;

	public int getRES_PUSH_TYPE() {
		return RES_PUSH_TYPE;
	}

	public void setRES_PUSH_TYPE(int rES_PUSH_TYPE) {
		this.set(rES_PUSH_TYPE);// RES_PUSH_TYPE = rES_PUSH_TYPE;
	}

	public long getCOMPANY_ID() {
		return COMPANY_ID;
	}

	public void setCOMPANY_ID(long cOMPANY_ID) {
		this.set(cOMPANY_ID);// COMPANY_ID = cOMPANY_ID;
	}

	public long getORDER_ID() {
		return ORDER_ID;
	}

	public void setORDER_ID(long oRDER_ID) {
		this.set(oRDER_ID);// ORDER_ID = oRDER_ID;
	}

	public int getPUSH_TYPE() {
		return PUSH_TYPE;
	}

	public void setPUSH_TYPE(int pUSH_TYPE) {
		this.set(pUSH_TYPE);// PUSH_TYPE = pUSH_TYPE;
	}

	public int getEVENT_TYPE() {
		return EVENT_TYPE;
	}

	public void setEVENT_TYPE(int eVENT_TYPE) {
		this.set(eVENT_TYPE);// EVENT_TYPE = eVENT_TYPE;
	}

	public int getACTION_TYPE() {
		return ACTION_TYPE;
	}

	public void setACTION_TYPE(int aCTION_TYPE) {
		this.set(aCTION_TYPE);// ACTION_TYPE = aCTION_TYPE;
	}

	public String getSTAFF_MOBILE() {
		return STAFF_MOBILE;
	}

	public void setSTAFF_MOBILE(String sTAFF_MOBILE) {
		this.set(sTAFF_MOBILE);// STAFF_MOBILE = sTAFF_MOBILE;
	}

	public String getERROR_MSG() {
		return ERROR_MSG;
	}

	public void setERROR_MSG(String eRROR_MSG) {
		this.set(eRROR_MSG);// ERROR_MSG = eRROR_MSG;
	}

	public long getPUSH_ID() {
		return PUSH_ID;
	}

	public void setPUSH_ID(long pUSH_ID) {
		this.set(pUSH_ID);// PUSH_ID = pUSH_ID;
	}

	public long getSTAFF_ID() {
		return STAFF_ID;
	}

	public void setSTAFF_ID(long sTAFF_ID) {
		this.set(sTAFF_ID);// STAFF_ID = sTAFF_ID;
	}

	public Date getSUBMIT_TIME() {
		return SUBMIT_TIME;
	}

	public void setSUBMIT_TIME(Date sUBMIT_TIME) {
		this.set(sUBMIT_TIME);// SUBMIT_TIME = sUBMIT_TIME;
	}

	public Date getPUSH_TIME() {
		return PUSH_TIME;
	}

	public void setPUSH_TIME(Date pUSH_TIME) {
		this.set(pUSH_TIME);// PUSH_TIME = pUSH_TIME;
	}

	public String getMSG() {
		return MSG;
	}

	public void setMSG(String mSG) {
		this.set(mSG);// MSG = mSG;
	}

	public int getPUSH_STATE() {
		return PUSH_STATE;
	}

	public void setPUSH_STATE(int pUSH_STATE) {
		this.set(pUSH_STATE);// PUSH_STATE = pUSH_STATE;
	}

}
