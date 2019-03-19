package com.sobey.mbserver.db.po;

import java.io.Serializable;
import java.util.Date;

import com.sobey.jcg.support.jdbc.Column;

public class MBUserSystemPO implements Serializable {
	private static final long serialVersionUID = 7965235926017428458L;

	@Column("rel_id")
	private long relId;

	@Column("user_id")
	private long userId;

	@Column("system_id")
	private long systemId;

	@Column("active_time")
	private Date activeTime;

	@Column("close_time")
	private Date closeTime;

	@Column("cur_state")
	private int curState;

	/**
	 * @return the relId
	 */
	public long getRelId() {
		return relId;
	}

	/**
	 * @param relId
	 *            the relId to set
	 */
	public void setRelId(long relId) {
		this.relId = relId;
	}

	/**
	 * @return the userId
	 */
	public long getUserId() {
		return userId;
	}

	/**
	 * @param userId
	 *            the userId to set
	 */
	public void setUserId(long userId) {
		this.userId = userId;
	}

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
	 * @return the activeTime
	 */
	public Date getActiveTime() {
		return activeTime;
	}

	/**
	 * @param activeTime
	 *            the activeTime to set
	 */
	public void setActiveTime(Date activeTime) {
		this.activeTime = activeTime;
	}

	/**
	 * @return the closeTime
	 */
	public Date getCloseTime() {
		return closeTime;
	}

	/**
	 * @param closeTime
	 *            the closeTime to set
	 */
	public void setCloseTime(Date closeTime) {
		this.closeTime = closeTime;
	}

	/**
	 * @return the curState
	 */
	public int getCurState() {
		return curState;
	}

	/**
	 * @param curState
	 *            the curState to set
	 */
	public void setCurState(int curState) {
		this.curState = curState;
	}

}
