package com.sobey.mbserver.user;

import com.sobey.base.BaseAction;
import com.sobey.base.exception.POException;

public class UserInfoAction extends BaseAction {
	UserInfoDAO dao;

	public UserInfoAction() throws POException {
		this.dao = new UserInfoDAO();
	}

	public void setComDao(UserInfoDAO uDao) {
		this.dao = uDao;
	}

	public boolean updateUserInfo(UserInfoPO upo) {
		return this.dao.updatePO(upo);
	}
}