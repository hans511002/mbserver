package com.sobey.base.socket;

public class OrderSeq {
	public final int serialNumber;
	public final Order order;// 命令编码
	public boolean isRequest;// 1请求 0回复

	protected OrderSeq(OrderHeader oh) {
		this.order = oh.order;
		this.isRequest = oh.isRequest;
		this.serialNumber = oh.serialNumber;
	}

	public boolean equals(Object oth) {
		if (oth instanceof OrderSeq) {
			OrderSeq os = (OrderSeq) oth;
			return this.serialNumber == os.serialNumber && this.order == os.order && this.isRequest == os.isRequest;
		} else {
			return false;
		}
	}

	public int hashCode() {
		return (this.serialNumber + "_" + this.order.ordinal() + "_" + this.isRequest).hashCode();
	}

	public static OrderSeq createOrderSeq(OrderHeader oh) {
		return new OrderSeq(oh);
	}

	// 创建请求需要回复SEQ
	public static OrderSeq createRequestOrderSeq(OrderHeader oh) {
		if (!oh.isRequest)// 本身是请求命令
			return null;
		OrderSeq os = new OrderSeq(oh);
		os.isRequest = false;
		return os;
	}

	// 创建回复对应的请求SEQ
	public static OrderSeq createResponseOrderSeq(OrderHeader oh) {
		if (oh.isRequest == true)// 本身是回复命令
			return null;
		OrderSeq os = new OrderSeq(oh);
		os.isRequest = true;
		return os;
	}
}
