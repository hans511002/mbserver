package com.sobey.mbserver.sql;

import com.sobey.mbserver.web.init.SysVar;

public class Version {
	public static final String version = "1.0.0";
	public static String dbName = SysVar.getValue("db.databaseName", "mbserver");
}
