package com.sobey.mbserver.web.init;

import com.alibaba.druid.filter.config.ConfigTools;
import com.sobey.jcg.support.sys.DataSourceManager;
import com.sobey.jcg.support.sys.SystemConstant;
import com.sobey.jcg.support.web.init.DataSourceManagerInit;
import com.sobey.mbserver.sql.CheckAndUpdateDb;

public class DbSourceInit extends DataSourceManagerInit {
	@Override
	public void init() {
		super.init();
		try {
			String pass;
			pass = ConfigTools.decrypt(
			        "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAJIy9Mn4iB6Ans6pr4GnJj3C3yorxUmI+LST+jJDGmEpArQJ5Clgbgb46nQWY1U8jAKQbZa9mUvijE0FuKhYc9MCAwEAAQ==",
			        "LpclXh9Er/5F55G2U5g9XCvEiGcmCagz6yNFDIohhpBeCzdW7K/LuE+8VhJ+erNXARyng4hq4ZycIhnHHB2fPA==");
			System.err.println(pass);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		SystemConstant.setDB_CONF_FILE("db.properties");
		DataSourceManager.dataSourceInit();
		CheckAndUpdateDb.checkAndUpdate();
	}
}
