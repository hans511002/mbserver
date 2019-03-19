package com.sobey.mbserver.push;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sobey.base.SystemInit;
import com.sobey.base.socket.Order;
import com.sobey.base.socket.OrderHeader;
import com.sobey.base.socket.SCHandler;
import com.sobey.base.socket.order.MessageOrder;
import com.sobey.base.util.HasThread;
import com.sobey.base.util.ToolUtil;
import com.sobey.jcg.support.jdbc.JdbcException;

public class PushListener extends HasThread {
	public static final Log LOG = LogFactory.getLog(PushListener.class.getName());

	PushMsgDAO pushDao = null;
	public HashMap<Long, Long> companySmsStaffRel = null;

	static void pushDelayMsg() {
		if (MessageOrder.Delays.size() > 0) {
			Long[] staffIds = new Long[0];
			synchronized (MessageOrder.Delays) {
				staffIds = (Long[]) MessageOrder.Delays.keySet().toArray(staffIds);
			}
			Long[] arrayOfLong1 = staffIds;
			int j = staffIds.length;
			for (int i = 0; i < j; i++) {
				Long sid = arrayOfLong1[i];
				SCHandler hand = (SCHandler) SystemInit.listener.handles.get(sid);
				if (hand != null) {
					try {
						List<MessageOrder.Msg> list = (List) MessageOrder.Delays.get(sid);
						synchronized (list) {
							for (MessageOrder.Msg msg : list) {
								if ((msg instanceof MessageOrder.SmsMsg)) {
									MessageOrder.SmsMsg sms = (MessageOrder.SmsMsg) msg;
									OrderHeader smsHeader = new OrderHeader();
									smsHeader.isRequest = true;
									smsHeader.order = Order.sms;
									smsHeader.staffId = sid.longValue();
									smsHeader.data.put("smsId", Integer.valueOf(0));
									smsHeader.data.put("dnbr", sms.nbr);
									smsHeader.data.put("cnt", sms.msg);
									smsHeader.data.put("time", Long.valueOf(sms.time));
									synchronized (hand) {
										hand.writeData(smsHeader);
									}
								}
								OrderHeader serverQueryHeader = new OrderHeader();
								serverQueryHeader.isRequest = true;
								serverQueryHeader.order = Order.message;
								serverQueryHeader.staffId = sid.longValue();
								serverQueryHeader.data.put("msg", msg.msg);
								serverQueryHeader.data.put("time", Long.valueOf(msg.time));
								serverQueryHeader.data.put("FROM_USER_ID", Long.valueOf(msg.staffId));
								synchronized (hand) {
									hand.writeData(serverQueryHeader);
								}
							}

							list.clear();
						}
						synchronized (MessageOrder.Delays) {
							synchronized (list) {
								if (list.size() == 0)
									MessageOrder.Delays.remove(sid);
							}
						}
					} catch (IOException ex) {
						LOG.error("发送用户[" + sid + "]消息失败：", ex);
					}
				}
			}
		}
	}

	public void run() {
		while (SystemInit.isRuning)
			try {
				pushDelayMsg();
			} catch (Exception e) {
				if ((((e instanceof SQLException)) || ((e instanceof JdbcException))) && (this.pushDao != null))
					try {
						PushMsgDAO.rollback();
					} catch (Exception localException1) {
					}
				LOG.error("", e);
			} finally {
				try {
					this.pushDao.close();
				} catch (Exception localException3) {
				}
				this.pushDao = null;
				ToolUtil.sleep(10000);
			}
	}

	public static boolean sendMsg(PushMsgPO msg) {
		SCHandler hand = (SCHandler) SystemInit.listener.handles.get(Long.valueOf(msg.getSTAFF_ID()));
		if (hand == null) {
			if (msg.getERROR_MSG() == null) {
				msg.setPUSH_STATE(2);
				msg.setERROR_MSG("用户不在线");
			}
			return false;
		}
		msg.setERROR_MSG("");
		msg.setPUSH_TIME(new Date(System.currentTimeMillis()));
		OrderHeader serverQueryHeader = new OrderHeader();
		serverQueryHeader.isRequest = false;
		serverQueryHeader.order = Order.message;
		serverQueryHeader.staffId = msg.getSTAFF_ID();
		serverQueryHeader.data.put("msg", msg.getMSG());
		serverQueryHeader.data.put("time", Long.valueOf(msg.getSUBMIT_TIME().getTime()));
		serverQueryHeader.data.put("action", Integer.valueOf(msg.getACTION_TYPE()));
		serverQueryHeader.data.put("event", Integer.valueOf(msg.getEVENT_TYPE()));
		serverQueryHeader.data.put("orderId", Long.valueOf(msg.getORDER_ID()));
		try {
			synchronized (hand) {
				hand.writeData(serverQueryHeader);
			}
			msg.setPUSH_STATE(0);
			msg.setRES_PUSH_TYPE(1);
			return true;
		} catch (IOException ex) {
			LOG.error("发送用户[" + msg.getSTAFF_ID() + "]消息失败：", ex);
			msg.setPUSH_STATE(1);
			msg.setERROR_MSG(ex.getMessage());
		}
		return false;
	}

	public static boolean sendSms(PushMsgPO msg) {
		if ((msg.getSTAFF_MOBILE() == null) || (msg.getSTAFF_MOBILE().equals("")) || (msg.getSTAFF_MOBILE().length() != 11)) {
			msg.setPUSH_STATE(4);
			msg.setERROR_MSG("目标手机号非法，不能使用短信发送");
			return false;
		}

		SCHandler hand = (SCHandler) SystemInit.listener.handles.get(Long.valueOf(SystemInit.SmsStaffId));
		if (hand == null) {
			int eventType = msg.getEVENT_TYPE();

			boolean isDaleySend = false;
			if (System.currentTimeMillis() - msg.getSUBMIT_TIME().getTime() < SystemInit.SmsDaleyTimeInterval) {
				isDaleySend = true;
			}
			if (isDaleySend) {
				msg.setPUSH_STATE(-1);
				if (msg.getPUSH_TYPE() == 0)
					msg.setERROR_MSG("用户不在线,短信手机不在线，" + ToolUtil.TimestampToString(SystemInit.SmsDaleyTimeInterval, 4) + "分钟内在线可发");
				else
					msg.setERROR_MSG("短信手机不在线，" + ToolUtil.TimestampToString(SystemInit.SmsDaleyTimeInterval, 4) + "分钟内在线可发");
			} else {
				msg.setPUSH_STATE(9);
				if (msg.getPUSH_TYPE() == 0)
					msg.setERROR_MSG("用户不在线,短信手机不在线");
				else {
					msg.setERROR_MSG("短信手机不在线");
				}
			}
			return false;
		}
		try {
			msg.setERROR_MSG("");
			msg.setPUSH_TIME(new Date(System.currentTimeMillis()));
			OrderHeader smsHeader = new OrderHeader();
			smsHeader.isRequest = true;
			smsHeader.order = Order.sms;
			smsHeader.staffId = msg.getSTAFF_ID();
			smsHeader.data.put("smsId", Long.valueOf(msg.getPUSH_ID()));
			smsHeader.data.put("dnbr", msg.getSTAFF_MOBILE());
			smsHeader.data.put("cnt", msg.getMSG());
			smsHeader.data.put("time", Long.valueOf(msg.getSUBMIT_TIME().getTime()));
			synchronized (hand) {
				hand.writeData(smsHeader);
			}
			msg.setERROR_MSG("等待短信手机返回发送状态");
			msg.setPUSH_STATE(11);
			return true;
		} catch (IOException ex) {
			LOG.error("发送用户[" + msg.getSTAFF_ID() + "]消息失败：", ex);
			msg.setPUSH_STATE(1);
			msg.setERROR_MSG(ex.getMessage());
		}
		return false;
	}

	public static boolean pushMsg(PushMsgPO msg) {
		boolean res = false;
		if ((msg.getPUSH_TYPE() == 0) || (msg.getPUSH_TYPE() == 1)) {
			res = sendMsg(msg);
		}
		boolean useSms = false;
		if (((!res) || (msg.getPUSH_TYPE() == 2)) && (msg.getPUSH_TYPE() != 1)) {
			long delay = System.currentTimeMillis() - msg.getSUBMIT_TIME().getTime();

			if ((msg.getPUSH_TYPE() == 2) || (delay > SystemInit.SmsSendDaleyTime) || (delay > SystemInit.SmsDaleyTimeInterval * 2 / 3)) {
				useSms = true;
			}
		}
		if (useSms) {
			res = sendSms(msg);
			msg.setRES_PUSH_TYPE(2);
		}
		return res;
	}

	public static String broadcast(String msg, long areaID) {
		return msg;
	}
}