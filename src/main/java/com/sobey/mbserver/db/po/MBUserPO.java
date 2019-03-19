package com.sobey.mbserver.db.po;

import java.util.Date;

import com.sobey.jcg.support.jdbc.Column;

// mb_user
public class MBUserPO extends SqlTool {
	private static final long serialVersionUID = -6425181068617339666L;

	@Column("id")
	private long userId;

	@Column(value = "user_pass")
	private String password;

	@Column(value = "user_name")
	private String userName;

	@Column(value = "user_nick")
	private String userNick;

	@Column(value = "user_mob")
	private String userMobile;

	@Column(value = "user_email")
	private String userEmail;

	@Column(value = "user_province")
	private String userProvince;

	@Column(value = "user_city")
	private String userCity;

	@Column(value = "user_address")
	private String userAddress;

	@Column(value = "reg_time")
	private Date regTime;

	@Column(value = "user_state")
	private int userState;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserNick() {
		return userNick;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getPassword() {
		return password;
	}

	public void setUserNick(String userNick) {
		this.userNick = userNick;
	}

	public String getUserMobile() {
		return userMobile;
	}

	public void setUserMobile(String userMobile) {
		this.userMobile = userMobile;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getUserProvince() {
		return userProvince;
	}

	public void setUserProvince(String userProvince) {
		this.userProvince = userProvince;
	}

	public String getUserCity() {
		return userCity;
	}

	public void setUserCity(String userCity) {
		this.userCity = userCity;
	}

	public String getUserAddress() {
		return userAddress;
	}

	public void setUserAddress(String userAddress) {
		this.userAddress = userAddress;
	}

	public Date getRegTime() {
		return regTime;
	}

	public void setRegTime(Date regTime) {
		this.regTime = regTime;
	}

	public int getUserState() {
		return userState;
	}

	public void setUserState(int userState) {
		this.userState = userState;
	}

	public MBUserPO() {
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public int hashCode() {
		return (int) this.userId;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof MBUserPO)) {
			return false;
		}
		MBUserPO other = (MBUserPO) object;
		return this.userId == other.userId;
	}

	@Override
	public String toString() {
		return "UserPO{" + "userid='" + userId + '\'' + '}';
	}
}
