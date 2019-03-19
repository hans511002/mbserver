package com.sobey.mbserver.user;

import com.sobey.base.BaseDao;
import com.sobey.base.exception.POException;

/**
 * 
 * @author hans
 * 
 */
public class UserLoginLogDAO extends BaseDao<UserLoginLogPO> {

	public UserLoginLogDAO() throws POException {
		super(UserLoginLogPO.class, "C_USER_LOGIN_LOG", "LOGIN_LOG_ID");
	}

}
