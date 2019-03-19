package com.sobey.base.socket.order;

import java.io.IOException;

import com.sobey.base.exception.POException;
import com.sobey.base.socket.OrderHeader;
import com.sobey.base.socket.SCHandler;
import com.sobey.jcg.support.utils.Convert;
import com.sobey.mbserver.push.PushMsgDAO;
import com.sobey.mbserver.push.PushMsgPO;

public class SmsOrder extends ServerOrders {

	public SmsOrder(SCHandler handler, OrderHeader header) {
		super(handler, header);
	}

	@Override
	public int process() throws IOException {
		PushMsgDAO pushDao = null;
		PushMsgPO msg = new PushMsgPO();
		try {
			if (!header.isRequest) {
				pushDao = new PushMsgDAO();
				long smsId = Convert.toLong(header.data.get("smsId"), 0);
				int status = Convert.toInt(header.data.get("status"), 1);
				if (smsId > 0 && status == 0) {
					// update table to send Success
					msg.setPUSH_ID(smsId);
					msg.setPUSH_STATE(0);
					msg.setRES_PUSH_TYPE(2);
					msg.setERROR_MSG("");
				} else {
					// update table to send fail
					msg.setPUSH_ID(smsId);
					msg.setPUSH_STATE(5);// 客户端发送错误
					msg.setERROR_MSG(Convert.toString(header.data.get("err"), "未知错误"));
				}
				pushDao.updatePO(msg);
			}
		} catch (POException e) {
		} finally {
			if (pushDao != null) {
				pushDao.close();
			}
		}
		return 0;
	}
}
