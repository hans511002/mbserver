package com.sobey.mbserver.db.dao;

import java.util.Date;

import com.sobey.jcg.support.jdbc.Column;

public class MeteTestPO {
	@Column("USER_ID")
	private int userId;

	@Column("LOG_ID")
	private int loginId;

	@Column("CHANGE_TIME")
	private Date changDate;

	@Column("CHANGE_TYPE")
	private int changType;

	@Column("EDITOR_TYPE")
	private int editorType;

	@Column("EDITOR_ID")
	private int editorId;

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getLoginId() {
		return loginId;
	}

	public void setLoginId(int loginId) {
		this.loginId = loginId;
	}

	public Date getChangDate() {
		return changDate;
	}

	public void setChangDate(Date changDate) {
		this.changDate = changDate;
	}

	public int getChangType() {
		return changType;
	}

	public void setChangType(int changType) {
		this.changType = changType;
	}

	public int getEditorType() {
		return editorType;
	}

	public void setEditorType(int editorType) {
		this.editorType = editorType;
	}

	public int getEditorId() {
		return editorId;
	}

	public void setEditorId(int editorId) {
		this.editorId = editorId;
	}
}
