package com.sobey.mbserver.web.init;

import com.sobey.jcg.support.sys.DataSourceManager;
import com.sobey.jcg.support.sys.SystemConstant;
import com.sobey.jcg.support.web.init.DataSourceManagerInit;
import com.sobey.mbserver.db.CheckAndUpdateDb;

public class DbSourceInit extends DataSourceManagerInit {
	@Override
	public void init() {
		super.init();
		SystemConstant.setDB_CONF_FILE("db.properties");
		DataSourceManager.dataSourceInit();
		CheckAndUpdateDb.checkAndUpgrade();
	}
}
