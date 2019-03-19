package com.sobey.mbserver.db.po;

import java.io.Serializable;

import com.sobey.jcg.support.jdbc.Column;

public class MBSystemPO implements Serializable {
	private static final long serialVersionUID = -3867849541616076585L;

	@Column("id")
	private long systemId;

	@Column("system_name")
	private String systemName;

	@Column("system_state")
	private int systemState;

	@Column("system_desc")
	private String systemDesc;

	/**
	 * @return the systemId
	 */
	public long getSystemId() {
		return systemId;
	}

	/**
	 * @param systemId
	 *            the systemId to set
	 */
	public void setSystemId(long systemId) {
		this.systemId = systemId;
	}

	/**
	 * @return the systemName
	 */
	public String getSystemName() {
		return systemName;
	}

	/**
	 * @param systemName
	 *            the systemName to set
	 */
	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}

	/**
	 * @return the systemState
	 */
	public int getSystemState() {
		return systemState;
	}

	/**
	 * @param systemState
	 *            the systemState to set
	 */
	public void setSystemState(int systemState) {
		this.systemState = systemState;
	}

	/**
	 * @return the systemDesc
	 */
	public String getSystemDesc() {
		return systemDesc;
	}

	/**
	 * @param systemDesc
	 *            the systemDesc to set
	 */
	public void setSystemDesc(String systemDesc) {
		this.systemDesc = systemDesc;
	}

}
