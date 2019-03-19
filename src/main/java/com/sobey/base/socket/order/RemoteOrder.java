package com.sobey.base.socket.order;

import java.io.IOException;

import com.sobey.base.exception.POException;
import com.sobey.base.socket.OrderHeader;
import com.sobey.base.socket.SCHandler;

public class RemoteOrder extends ServerOrders {

	public RemoteOrder(SCHandler handler, OrderHeader header) {
		super(handler, header);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int process() throws IOException, POException {
		// TODO Auto-generated method stub
		return 0;
	}

}
