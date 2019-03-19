package com.sobey.mbserver.location;

import java.util.Date;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sobey.base.BaseAction;
import com.sobey.base.socket.remote.NotNeedLoginMethod;
import com.sobey.base.socket.remote.RemoteClass;
import com.sobey.base.socket.remote.RemoteMethod;
import com.sobey.base.util.MD5Hash;
import com.sobey.jcg.support.utils.Convert;
import com.sobey.mbserver.user.UserInfoDAO;
import com.sobey.mbserver.user.UserInfoPO;
import com.sobey.mbserver.user.UserLoginLogDAO;
import com.sobey.mbserver.user.UserLoginLogPO;

@RemoteClass
public class IndexAction extends BaseAction {
	public static final Log LOG = LogFactory.getLog(IndexAction.class.getName());

	/**
	 * 后期使用数据库配置及模板实现动态变更 为支持二种调用 方式：http及java remoteApi,否则可以使用协议中的用户信息
	 * 
	 * @return
	 */
	@RemoteMethod
	@NotNeedLoginMethod
	public String getIndexHtml(long userId) {
		return "<div>首页推广内容</div>";
	}

	@RemoteMethod
	@NotNeedLoginMethod
	public Map<String, Object>[] queryAreas() {
		try {
			UserInfoDAO dao = new UserInfoDAO();
			Map<String, Object>[] res = dao.getDataAccess()
			        .queryForArrayMap("SELECT AREA_ID, PARENT_AREA_ID, AREA_NAME, ORDER_ID, AREA_CODE,ZIP_CODE"
			                + ",(SELECT COUNT(0) FROM C_SYSTEM_AREA a WHERE a.PARENT_AREA_ID=t.AREA_ID) SUBS"
			                + " FROM  C_SYSTEM_AREA  t ORDER BY PARENT_AREA_ID,ORDER_ID,AREA_CODE,AREA_ID");
			return res;
		} catch (Exception e) {
		}
		return null;

	}

	public static void writeLog(long uid, Map<String, Object> map) {
		try {
			UserLoginLogPO log = new UserLoginLogPO();
			log.setUSER_ID(uid);
			log.setLOGIN_TIME(new Date(System.currentTimeMillis()));
			if (map.containsKey("LOGIN_LNG")) {
				log.setLOGIN_LNG(Convert.toDouble("LOGIN_LNG", 0));
			}
			if (map.containsKey("LOGIN_LAT")) {
				log.setLOGIN_LAT(Convert.toDouble("LOGIN_LAT", 0));
			}
			if (map.containsKey("LOGIN_LNG")) {
				log.setLOGIN_LAT(Convert.toDouble("LOGIN_LNG", 0));
			}
			if (map.containsKey("LOGIN_ADDRESS")) {
				log.setLOGIN_ADDRESS(Convert.toString("LOGIN_ADDRESS", ""));
			}
			new UserLoginLogDAO().insertPO(log);
		} catch (Exception e) {
		}
	}

	// 浏览测试页面使用
	// 返回用户登录成功后的用户信息、权限数据
	public Object[] userLogin(String nbr, String pass, Map<String, Object> map) {
		try {
			Object[] res = new Object[2];
			UserInfoPO loginUser = UserInfoPO.UserNbrRels.get(nbr);
			if (loginUser == null) {
				UserInfoDAO udao = new UserInfoDAO();
				loginUser = udao.queryUser(nbr);
				if (loginUser != null) {
					synchronized (UserInfoPO.UserInfos) {
						UserInfoPO.UserNbrRels.put(nbr, loginUser);
						UserInfoPO.UserInfos.put(loginUser.getUSER_ID(), loginUser);
					}
				}
			}
			// 判断 用户名密码，读取权限，返回权限数据
			pass = MD5Hash.getMD5AsHex(pass.getBytes());
			if (loginUser == null || !loginUser.getPASSWORD().equals(pass)) {// 用户不存在，未注册
				res[0] = "false";
				if (loginUser == null) {
					res[1] = "帐号不存在，请先注册。";
				} else {
					res[1] = "密码错误。";
				}
				return res;
			}
			res[0] = "登录成功";
			res[1] = loginUser.toMap();
			writeLog(loginUser.getUSER_ID(), map);
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			return new Object[] { "false", e.getMessage() };
		}
	}

	// 修改密码，重设密码
	@RemoteMethod
	public Object[] updatePass(String nbr, String oldPass, String newPass, int type) {
		try {
			Object[] res = new Object[4];
			UserInfoPO loginUser = UserInfoPO.UserNbrRels.get(nbr);
			UserInfoDAO udao = new UserInfoDAO();
			if (loginUser == null) {
				loginUser = udao.queryUser(nbr);
				if (loginUser != null) {
					synchronized (UserInfoPO.UserInfos) {
						UserInfoPO.UserNbrRels.put(nbr, loginUser);
						UserInfoPO.UserInfos.put(loginUser.getUSER_ID(), loginUser);
					}
				}
			}
			if (loginUser == null) {
				throw new Exception("手机号未注册");
			}
			oldPass = MD5Hash.getMD5AsHex(oldPass.getBytes());
			newPass = MD5Hash.getMD5AsHex(newPass.getBytes());
			if (type == 1) {// 修改
				if (!oldPass.equals(loginUser.getPASSWORD())) {
					throw new Exception("密码错误");
				}
				loginUser.setPASSWORD(newPass);
				udao.updatePO(loginUser);
				res[0] = "true";
				res[1] = "修改成功";
			} else if (type == 2) {
				loginUser.setPASSWORD(newPass);
				udao.updatePO(loginUser);
				res[0] = "true";
				res[1] = "重置成功";
			}
			return res;
		} catch (Exception e) {
			return new Object[] { "false", e.getMessage() };
		}

	}

}
