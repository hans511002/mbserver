package com.sobey.base.socket.remote;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sobey.base.socket.OrderHeader;

public abstract class ClientCallBack {
	public static final Log LOG = LogFactory.getLog(ClientCallBack.class.getName());
	OrderHeader sendheader = null;
	long time = System.currentTimeMillis();
	long timeOutValue = 0;// 用于轮循判断用

	protected ClientCallBack(OrderHeader sendheader, int timeOutValue) {
		this.sendheader = sendheader;
		this.timeOutValue = timeOutValue;
	}

	protected ClientCallBack() {
	}

	public abstract boolean call(OrderHeader recvHeader)
	// {
	// LOG.info("ClientCallBack ===call===send=" +
	// this.getSendheader() + "========== " + recvHeader);
	// return true;
	// }
	;

	public void timeout() {
		LOG.info("ClientCallBack ===timeout===send=" + this.getSendheader() + "========== ");
	}

	public void setSendheader(OrderHeader sendheader) {
		this.sendheader = sendheader;
	}

	public long getInitTime() {
		return time;
	}

	public void setTimeOut(long timeOutValue) {
		this.timeOutValue = timeOutValue;
	}

	public OrderHeader getSendheader() {
		return sendheader;
	}

	public long getTimeOut() {
		return timeOutValue;
	}
}
