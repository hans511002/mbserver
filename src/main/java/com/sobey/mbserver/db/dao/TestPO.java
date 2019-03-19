package com.sobey.mbserver.db.dao;

import java.sql.Blob;
import java.sql.Clob;

import com.sobey.jcg.support.jdbc.Column;

public class TestPO {
	public TestPO() {

	}

	// test_str4
	@Column("role_id")
	private String testStr1;
	private String testStr2;
	private String testStr3;
	@Column("role_id")
	private int roldID;
	private Clob clob;
	private Blob blob;
	private Clob nclob;

	public Blob getBlob() {
		return blob;
	}

	public void setBlob(Blob blob) {
		this.blob = blob;
	}

	public Clob getNclob() {
		return nclob;
	}

	public void setNclob(Clob nclob) {
		this.nclob = nclob;
	}

	public Clob getClob() {
		return clob;
	}

	public void setClob(Clob clob) {
		this.clob = clob;
	}

	public int getRoldID() {
		return roldID;
	}

	public void setRoldID(int roldID) {
		this.roldID = roldID;
	}

	public String getTestStr1() {
		return testStr1;
	}

	public void setTestStr1(String testStr1) {
		this.testStr1 = testStr1;
	}

	public String getTestStr2() {
		return testStr2;
	}

	public void setTestStr2(String testStr2) {
		this.testStr2 = testStr2;
	}

	public String getTestStr3() {
		return testStr3;
	}

	public void setTestStr3(String testStr3) {
		this.testStr3 = testStr3;
	}
}
