package com.sobey.mbserver.db.dao;

import java.util.List;

import com.sobey.base.BaseDao;
import com.sobey.base.exception.POException;
import com.sobey.mbserver.db.po.MBUserPO;

public class UserDAO extends BaseDao<MBUserPO> {

	public UserDAO() throws POException {
		super(MBUserPO.class);
	}

	public List<MBUserPO> queryForList() {
		return this.queryPOList();
		// getDataAccess().queryForBeanList(SqlTool.buildQuerySql(MBUserPO.class), MBUserPO.class);
	}

}
