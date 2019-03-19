package com.sobey.mbserver.user;

import java.util.List;

import com.sobey.base.BaseDao;
import com.sobey.base.exception.POException;

public class UserInfoDAO extends BaseDao<UserInfoPO> {
	public UserInfoDAO() throws POException {
		super(UserInfoPO.class, "C_USER_INFO", "USER_ID", "SEQ_USER_ID");
	}

	public UserInfoPO queryUser(String nbr) {
		List us = queryPOList("MOBILE=? and STATE=1", new Object[] { nbr });
		if (us.size() == 1) {
			return (UserInfoPO) us.get(0);
		}
		return null;
	}

}