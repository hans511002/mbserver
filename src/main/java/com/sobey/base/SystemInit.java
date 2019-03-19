package com.sobey.base;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.directwebremoting.WebContextFactory;

import com.sobey.base.exception.POException;
import com.sobey.base.socket.SCHandler;
import com.sobey.base.socket.TcpListener;
import com.sobey.base.socket.remote.RemoteClass;
import com.sobey.base.socket.remote.RemoteMethod;
import com.sobey.base.util.ToolUtil;
import com.sobey.jcg.support.jdbc.DataSourceImpl;
import com.sobey.jcg.support.sys.DataSourceManager;
import com.sobey.jcg.support.utils.Convert;
import com.sobey.jcg.support.web.ISystemStart;
import com.sobey.mbserver.push.PushListener;
import com.sobey.mbserver.push.PushMsgDAO;
import com.sobey.mbserver.push.PushMsgPO;
import com.sobey.mbserver.user.UserInfoDAO;
import com.sobey.mbserver.user.UserInfoPO;

@RemoteClass
public class SystemInit implements ISystemStart {
	public static final Log LOG = LogFactory.getLog(SystemInit.class.getName());
	public static TcpListener listener = null;
	public static PushListener pushListener = null;
	public static SystemSeqService seqService = null;
	// AlertListenServer alertServer;
	public static SystemInit system = null;

	public static boolean isRuning = false;
	// 是否启动后台服务
	public static boolean startServer = true;

	public static int serverPort = 1688;
	public static int threadPoolNum = 100;

	// 短信手机用户不在线是否延迟累计发送
	public static boolean SmsStaffLeaveSendFlag = false;
	// 事件类消息可延迟时间,短信手机不在线的有效时间
	public static int SmsDaleyTimeInterval = 600000;
	public static int SmsSendDaleyTime = 60000;
	public static long SmsStaffId = 1;

	public static java.text.SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	static final java.util.regex.Pattern reg = Pattern.compile("mopt_V(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)\\.apk");
	static ServletContext servletContext = null;
	static long MaxVersion = 0;
	static String MaxVersionName = null;

	public List<Object[]> getSystemConfig() {
		List<Object[]> res = new ArrayList<Object[]>();
		res.add(new Object[] { "服务是否启动", isRuning, "", "changeValue(1,SystemInit.changeConfig)" });
		res.add(new Object[] { "是否使用长连接", SystemInit.listener.isLongLink, "socket是否长连接", "changeValue(5,SystemInit.changeConfig)" });
		res.add(new Object[] { "短信是否延迟发送", SmsStaffLeaveSendFlag, "是否所有消息都一直有效", "changeValue(2,SystemInit.changeConfig)" });
		res.add(new Object[] { "短信有效时间", SmsDaleyTimeInterval, "消息重复发送有效时间", "changeValue(3,SystemInit.changeConfig)" });
		res.add(new Object[] { "短信推迟时间", SmsSendDaleyTime, "用户不在线时使用短信发送的推迟时间", "changeValue(4,SystemInit.changeConfig)" });
		// res.add(new Object[] { "预警监控开始时间", AlertListenServer.alertStartHour, "一天预警监控开始时间点",
		// "changeValue(5,SystemInit.changeConfig)" });
		// res.add(new Object[] { "预警监控结束时间", AlertListenServer.alertEndHour, "一天预警监控结束时间点",
		// "changeValue(6,SystemInit.changeConfig)" });
		// res.add(new Object[] { "预警消息间隔", AlertListenServer.alertTimeInterval, "预警消息的生成间隔",
		// "changeValue(7,SystemInit.changeConfig)" });
		// res.add(new Object[] { "任务延后间隔", AlertListenServer.eventDelayInterval, "事件时间延后提醒间隔时间",
		// "changeValue(8,SystemInit.changeConfig)"
		// });
		return res;
	}

	public String changeConfig(int type, String value) {
		switch (type) {
		case 1: {
			boolean val = Convert.toBool(value, startServer);
			try {
				if (val) {
					start();
					return "成功启动服务";
				} else {
					destory();
					return "停止服务完成";
				}
			} catch (Exception e) {
				return e.getMessage();
			}
		}
		case 2:
			SmsStaffLeaveSendFlag = Convert.toBool(value, SmsStaffLeaveSendFlag);
			return "修改成功，服务器重启将会复位。";
		case 3:
			SmsDaleyTimeInterval = Convert.toInt(value, SmsDaleyTimeInterval);
			return "修改成功，服务器重启将会复位。";
		case 4:
			SmsSendDaleyTime = Convert.toInt(value, SmsSendDaleyTime);
			return "修改成功，服务器重启将会复位。";
		case 5:
			SystemInit.listener.isLongLink = Convert.toBool(value, SystemInit.listener.isLongLink);
			return "修改成功，服务器重启将会复位。";
		// case 6:
		// AlertListenServer.alertEndHour = Convert.toInt(value, AlertListenServer.alertEndHour);
		// return "修改成功，服务器重启将复位。";
		// case 7:
		// AlertListenServer.alertTimeInterval = Convert.toInt(value, AlertListenServer.alertTimeInterval);
		// return "修改成功，服务器重启将复位。";
		// case 8:
		// AlertListenServer.eventDelayInterval = Convert.toInt(value, AlertListenServer.eventDelayInterval);
		// return "修改成功，服务器重启将复位。";
		}
		return "不可识别的参数";
	}

	public List<Object[]> getOnlineUsers() {
		List<Object[]> res = new ArrayList<Object[]>();
		List<Object[]> temp = new ArrayList<Object[]>();
		if (SystemInit.listener != null) {
			for (long userId : SystemInit.listener.handles.keySet()) {
				SCHandler hand = SystemInit.listener.handles.get(userId);
				boolean keepAlive = false;
				String inetAddress = "";
				try {
					keepAlive = hand.channel != null ? hand.channel.socket().getKeepAlive() : false;
					inetAddress = hand.channel != null ? hand.getRemoteAddress().toString() : "";
				} catch (IOException e) {
				}
				Object[] cli = new Object[] { hand.clientId, hand.userInfo != null ? hand.userInfo.getMOBILE() : "", sdf.format(new Date(hand.clientLoginTime)),
				        sdf.format(new Date(hand.lastActiveTime)), hand.isLogined(), hand.isMsgLink(),
				        hand.channel != null ? hand.channel.isConnected() : "false", hand.channel != null ? hand.channel.socket().isClosed() : "true",
				        keepAlive, inetAddress };
				// clientConnectTime
				if (hand.clientId > 0) {
					res.add(cli);
				} else {
					temp.add(cli);
				}
			}
		}
		res.addAll(temp);
		return res;
	}

	public long getVersionCode() {
		if (getNewPackagePath() != null) {
			return MaxVersion;
		}
		return 0;
	}

	public String getVersionName() {
		if (getNewPackagePath() != null) {
			return MaxVersionName;
		}
		return null;
	}

	public Map<String, Object> getVersion() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("code", getVersionCode());
		map.put("name", getVersionName());
		return map;
	}

	// WEBq根路径
	public String getNewPackageUrl() {
		try {
			String path = getNewPackagePath();
			String rootDir = new File(SystemInit.class.getClassLoader().getResource("").getFile()).getParentFile().getParentFile().getCanonicalPath()
			        .replaceAll("\\%20", " ");
			return servletContext.getContextPath() + "/" + path.replace(rootDir, "");
		} catch (IOException e) {
		}
		return null;
	}

	// 最新包文件路径
	@RemoteMethod
	public String getNewPackagePath() {
		String rootDir = null;
		try {
			rootDir = new File(SystemInit.class.getClassLoader().getResource("").getFile()).getParentFile().getParentFile().getCanonicalPath()
			        .replaceAll("\\%20", " ");
			String path = rootDir + File.separator + "update";
			System.err.println("path=" + path);
			File file = new File(path);
			File[] list = file.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					if (reg.matcher(name).find()) {
						return true;
					}
					return false;
				}
			});
			if (list == null)
				return null;
			int[] version = new int[4];
			MaxVersion = 0;
			File maxFile = null;
			MaxVersionName = null;
			for (File f : list) {
				System.err.println("f=" + f.getCanonicalPath());
				Matcher m = reg.matcher(f.getName());
				if (m.find()) {
					version[0] = Integer.parseInt(m.group(1));
					version[1] = Integer.parseInt(m.group(2));
					version[2] = Integer.parseInt(m.group(3));
					version[3] = Integer.parseInt(m.group(4));
					long vversion = version[0] * 100000000 + version[1] * 1000000 + version[2] * 1000 + version[3];
					if (maxFile == null) {
						MaxVersion = vversion;
						maxFile = f;
						MaxVersionName = version[0] + "." + version[1] + "." + version[2] + "." + version[3];
					} else if (vversion > MaxVersion) {
						MaxVersion = vversion;
						maxFile = f;
						MaxVersionName = version[0] + "." + version[1] + "." + version[2] + "." + version[3];
					}
				}
			}
			if (maxFile == null)
				return null;
			return maxFile.getCanonicalPath();
		} catch (IOException e) {
			return null;
		}
	}

	// DWR获取下载包WEB全路径
	public String getPkgDowLoadURL() {
		String host = getHostUrl(WebContextFactory.get().getHttpServletRequest());
		String path = getNewPackageUrl();
		if (host != null && path != null) {
			return host + path;
		}
		return null;
	}

	public static String getHostUrl(HttpServletRequest request) {
		String protocol = request.getProtocol();
		if (protocol.indexOf('/') > 0) {
			protocol = protocol.substring(0, protocol.indexOf('/'));
		}
		return protocol + "://" + request.getServerName() + ":" + request.getServerPort();
	}

	public String killUser(long staffId) {
		if (SystemInit.listener != null) {
			if (SystemInit.listener.handles.containsKey(staffId)) {
				SCHandler hand = SystemInit.listener.handles.get(staffId);
				hand.close();
				return "离线用户" + staffId + "成功";
			} else {
				return "用户" + staffId + "在连接表中不存在，请刷新页面重试。";
			}
		}
		return "服务未启动，请刷新页面。";
	}

	// 指定用户或者号码发送消息
	public String sendMsg(PushMsgPO msgpo, boolean writeToBase) throws POException {
		if (writeToBase) {
			PushMsgDAO pdo = new PushMsgDAO();
			try {
				pdo.insertPO(msgpo);
				return "成功添加到数据库";
			} catch (Exception e) {
				return e.getMessage();
			}
		} else {
			if (SystemInit.listener == null)
				return "scocket服务未启动";
			PushListener.pushMsg(msgpo);
			int type = msgpo.getRES_PUSH_TYPE();
			if (type == 1)
				return "链路发送成功";
			if (type == 2)
				return "短信发送成功";
			if (msgpo.getPUSH_STATE() == 11) {
				return "发送到短信手机成功";
			} else if (msgpo.getPUSH_STATE() == -1) {
				return "短信手机不在线";
			}
			if ((msgpo.getERROR_MSG() != null && !msgpo.getERROR_MSG().equals("")) || msgpo.getPUSH_STATE() != 0) {
				return msgpo.getERROR_MSG();
			} else {
				return "发送失败";
			}
		}
	}

	// 广播消息 用户区域
	public String broadcast(String msg, long areaID) {
		return PushListener.broadcast(msg, areaID);
	}

	// 用于延后手动启动
	public static void StartServer() throws IOException {
		if (SystemInit.system == null)
			return;
		SystemInit.system.start();
	}

	void start() throws IOException {
		if (isRuning == false) {
			isRuning = true;
			if (SystemInit.seqService == null) {
				SystemInit.seqService = new SystemSeqService();
				SystemInit.seqService.setName("SeqService").setDaemon(true).setPriority(Thread.MAX_PRIORITY).start();
			}
			if (SystemInit.listener == null) {
				SystemInit.listener = new TcpListener(serverPort, threadPoolNum);
				SystemInit.listener.setName("TCPListener").setDaemon(true).setPriority(Thread.MAX_PRIORITY).start();
			}
			if (SystemInit.pushListener == null) {
				SystemInit.pushListener = new PushListener();
				SystemInit.pushListener.setName("PushListener").setDaemon(true).setPriority(Thread.MAX_PRIORITY).start();
			}
		}
	}

	public void destory() {
		isRuning = false;
		seqService.destory();
		ToolUtil.sleep(100);
		seqService = null;
		try {
			SystemInit.listener.interrupt();
		} catch (Throwable e) {
		} finally {
			SystemInit.listener = null;
		}
		try {
			SystemInit.pushListener.interrupt();
		} catch (Throwable e) {
		} finally {
			SystemInit.pushListener = null;
		}
	}

	public void loadUserInfoToMem() {
		UserInfoDAO udao = null;
		try {
			udao = new UserInfoDAO();
			List<UserInfoPO> us = udao.queryPOList("STATE=1");
			if (us != null && us.size() > 0) {
				synchronized (UserInfoPO.UserInfos) {
					for (UserInfoPO userInfoPO : us) {
						UserInfoPO.UserInfos.put(userInfoPO.getUSER_ID(), userInfoPO);
						UserInfoPO.UserNbrRels.put(userInfoPO.getMOBILE(), userInfoPO);
					}
				}
			}
		} catch (Exception e) {
			LOG.error("loadUserInfoToMem", e);
		} finally {
			if (udao != null) {
				udao.close();
			}
			// DataSourceManager.destroy();
		}

	}

	@Override
	public void init() {
		system = this;
		loadUserInfoToMem();
		if (startServer) {
			try {
				start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			isRuning = false;
		}
		// RemoteJavaApi.reloadJavaApi();
	}

	public void setServletContext(ServletContext servletContext) {
		SystemInit.servletContext = servletContext;
		LOG.info("NewPackagePath=" + getNewPackagePath());
	}

	public static void main(String[] args) throws SQLException {
		SystemInit init = new SystemInit();
		// 需要初始化
		DataSourceImpl ds = new DataSourceImpl("com.mysql.jdbc.Driver", "jdbc:mysql://203.195.200.245:3306/eangel?useUnicode=true&amp;characterEncoding=utf-8",
		        "angel", "eangel");
		ds.setMaxActive(20);
		threadPoolNum = 3;
		DataSourceManager.addDataSource("config1", ds);
		init.init();
		listener.setSelectListens(2);
		while (isRuning) {
			ToolUtil.sleep(1000);
		}
	}
}
