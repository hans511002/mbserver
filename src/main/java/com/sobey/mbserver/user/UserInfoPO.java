package com.sobey.mbserver.user;

import java.util.HashMap;

import com.sobey.base.PersistentStatePO;

public class UserInfoPO extends PersistentStatePO {

	private static final long c = -6320201036996170285L;
	public static HashMap<Long, UserInfoPO> UserInfos = new HashMap<Long, UserInfoPO>();
	public static HashMap<String, UserInfoPO> UserNbrRels = new HashMap<String, UserInfoPO>();

	long USER_ID;
	String NICK_NAME;
	String USER_AVATAR;
	String USER_NAME;
	String MOBILE;
	String PASSWORD;
	String CONTACT_QQ;
	String CONTACT_EMAIL;
	int USER_SEX;
	int USER_AGE;
	String ID_CARD;
	String ID_CARD_PHOTO;
	String RECENT_PHOTO;
	long AREA_ID;
	String ADDRESS;
	int STATE;
	int PUB_INFO_TYPE;

	String USER_EDUC;// 用户学历
	String USER_INCOME;// 用户年收入范围
	String USERS_CAREER;// 用户职业

	public String getPASSWORD() {
		return PASSWORD;
	}

	public void setPASSWORD(String pASSWORD) {
		PASSWORD = pASSWORD;
	}

	public long getUSER_ID() {
		return USER_ID;
	}

	public void setUSER_ID(long uSER_ID) {
		set(uSER_ID);// USER_ID = uSER_ID;
	}

	public String getUSER_NAME() {
		return USER_NAME;
	}

	public void setUSER_NAME(String uSER_NAME) {
		set(uSER_NAME);// USER_NAME = uSER_NAME;
	}

	public String getMOBILE() {
		return MOBILE;
	}

	public void setMOBILE(String mOBILE) {
		set(mOBILE);// MOBILE = mOBILE;
	}

	public String getCONTACT_QQ() {
		return CONTACT_QQ;
	}

	public void setCONTACT_QQ(String cONTACT_QQ) {
		set(cONTACT_QQ);// CONTACT_QQ = cONTACT_QQ;
	}

	public String getCONTACT_EMAIL() {
		return CONTACT_EMAIL;
	}

	public void setCONTACT_EMAIL(String cONTACT_EMAIL) {
		set(cONTACT_EMAIL);// CONTACT_EMAIL = cONTACT_EMAIL;
	}

	public String getID_CARD() {
		return ID_CARD;
	}

	public void setID_CARD(String iD_CARD) {
		set(iD_CARD);// ID_CARD = iD_CARD;
	}

	public long getAREA_ID() {
		return AREA_ID;
	}

	public void setAREA_ID(long aREA_ID) {
		set(aREA_ID);// AREA_ID = aREA_ID;
	}

	public String getADDRESS() {
		return ADDRESS;
	}

	public void setADDRESS(String aDDRESS) {
		set(aDDRESS);// ADDRESS = aDDRESS;
	}

	public int getSTATE() {
		return STATE;
	}

	public void setSTATE(int sTATE) {
		set(sTATE);// STATE = sTATE;
	}

	public int getPUB_INFO_TYPE() {
		return PUB_INFO_TYPE;
	}

	public void setPUB_INFO_TYPE(int pUB_INFO_TYPE) {
		set(pUB_INFO_TYPE);// PUB_INFO_TYPE = pUB_INFO_TYPE;
	}

	public String getNICK_NAME() {
		return NICK_NAME;
	}

	public void setNICK_NAME(String nICK_NAME) {
		this.set(nICK_NAME);
	}

	public String getUSER_AVATAR() {
		return USER_AVATAR;
	}

	public void setUSER_AVATAR(String uSER_AVATAR) {
		this.set(uSER_AVATAR);// USER_AVATAR = uSER_AVATAR;
	}

	public int getUSER_SEX() {
		return USER_SEX;
	}

	public void setUSER_SEX(int uSER_SEX) {
		this.set(uSER_SEX);// USER_SEX = uSER_SEX;
	}

	public int getUSER_AGE() {
		return USER_AGE;
	}

	public void setUSER_AGE(int uSER_AGE) {
		this.set(uSER_AGE);// USER_AGE = uSER_AGE;
	}

	public String getID_CARD_PHOTO() {
		return ID_CARD_PHOTO;
	}

	public void setID_CARD_PHOTO(String iD_CARD_PHOTO) {
		this.set(iD_CARD_PHOTO);// ID_CARD_PHOTO = iD_CARD_PHOTO;
	}

	public String getRECENT_PHOTO() {
		return RECENT_PHOTO;
	}

	public void setRECENT_PHOTO(String rECENT_PHOTO) {
		this.set(rECENT_PHOTO);// RECENT_PHOTO = rECENT_PHOTO;
	}

	public String getUSER_EDUC() {
		return USER_EDUC;
	}

	public void setUSER_EDUC(String uSER_EDUC) {
		this.set(uSER_EDUC);// USER_EDUC = uSER_EDUC;
	}

	public String getUSER_INCOME() {
		return USER_INCOME;
	}

	public void setUSER_INCOME(String uSER_INCOME) {
		this.set(uSER_INCOME);// USER_INCOME = uSER_INCOME;
	}

	public String getUSERS_CAREER() {
		return USERS_CAREER;
	}

	public void setUSERS_CAREER(String uSERS_CAREER) {
		this.set(uSERS_CAREER);// USERS_CAREER = uSERS_CAREER;
	}

}
