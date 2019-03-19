package com.sobey.mbserver.db.po;

import java.io.Serializable;

import com.sobey.jcg.support.jdbc.Column;

// mb_user
public class MBUserDataPO implements Serializable {
	private static final long serialVersionUID = -3263479860938450658L;

	@Column("data_id")
	private long dataId;

	@Column("user_id")
	private long userId;

	@Column("system_id")
	private long systemId;

	@Column("data_type")
	private String dataType;

	@Column("data_value")
	private String dataValue;

	public long getDataId() {
		return dataId;
	}

	public void setDataId(long dataId) {
		this.dataId = dataId;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public long getSystemId() {
		return systemId;
	}

	public void setSystemId(long systemId) {
		this.systemId = systemId;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getDataValue() {
		return dataValue;
	}

	public void setDataValue(String dataValue) {
		this.dataValue = dataValue;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
