package com.sobey.mbserver.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sobey.jcg.support.jdbc.DataAccess;
import com.sobey.jcg.support.jdbc.DataAccessFactory;
import com.sobey.jcg.support.log4j.LogUtils;
import com.sobey.jcg.support.sys.DataSourceManager;
import com.sobey.mbserver.exception.ConfigException;
import com.sobey.mbserver.exception.DbException;

public class CheckAndUpdateDb {

	static final Object mutex = new Object();

	public static void checkAndUpdate() {
		try {
			DataAccess access = DataAccessFactory.getInstance(DataSourceManager.getConnection());
			synchronized (mutex) {
				checkDb(access);
				checkTable(access);
			}
		} catch (Throwable e) {
			LogUtils.error("check db error", e);
			System.exit(1);
		} finally {
			DataSourceManager.destroy();
		}
	}

	static void checkTable(DataAccess access) {
		String sql = "SELECT COUNT(0) FROM `information_schema`.`TABLES` t WHERE t.`TABLE_SCHEMA`='mbserver' AND t.`TABLE_NAME`='mbserver_version';";
		if (access.queryForInt(sql) == 0) {
			execSqlFile(access, "dbinit");
		}
		sql = "SELECT t.`id`,t.`mb_version`,t.`update_time` FROM  " + Version.dbName + ".`mbserver_version` t order by t.id";
		List<String> verions = new ArrayList<String>();
		for (Map<String, Object> map : access.queryForList(sql)) {
			verions.add(map.get("mb_version").toString());
		}
	}

	static void execSqlFile(DataAccess access, String version) {

	}

	static void checkDb(DataAccess access) {
		String connDataBaseName = access.queryForString("SELECT DATABASE()");
		if (Version.dbName.isEmpty()) {
			Version.dbName = connDataBaseName;
		}
		Version.dbName = Version.dbName.toLowerCase();
		if (Version.dbName.contentEquals("mysql")) {
			throw new ConfigException("config error,dbName not use mysql ");
		}
		List<Map<String, Object>> list = access.queryForList("SHOW DATABASES");
		boolean dbExists = false;
		for (Map<String, Object> map : list) {
			if (Version.dbName.equals(map.get("Database"))) {
				dbExists = true;
				break;
			}
		}
		if (!dbExists) {
			String sql = "CREATE DATABASE " + Version.dbName + " DEFAULT CHARSET utf8";
			if (!access.execNoQuerySql(sql)) {
				throw new DbException("execNoQuerySql:" + sql);
			}
		}
	}
}
