package com.sobey.mbserver.sql;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.ery.base.support.utils.Convert;
import com.sobey.jcg.support.jdbc.DataAccess;
import com.sobey.jcg.support.jdbc.DataAccessFactory;
import com.sobey.jcg.support.log4j.LogUtils;
import com.sobey.jcg.support.sys.DataSourceManager;
import com.sobey.mbserver.exception.ConfigException;
import com.sobey.mbserver.exception.DbException;
import com.sobey.mbserver.util.LineProcess;
import com.sobey.mbserver.web.init.SysConfig;

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

	final static String fileFormat = "db-(\\d+\\.\\d+\\.\\d+)\\.sql";

	static void checkTable(DataAccess access) {
		URL res = CheckAndUpdateDb.class.getResource(".");
		File rsql = new File(res.getPath());
		List<File> updateFiles = new ArrayList<File>();
		if (rsql.exists() && rsql.isDirectory()) {
			File sqlFiles[] = rsql.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.equals("dbinit.sql") || name.matches(fileFormat);
				}
			});
			for (File file : sqlFiles) {
				updateFiles.add(file);
			}
		}
		File dbsql = new File("dbsql");
		if (dbsql.exists() && dbsql.isDirectory()) {
			File sqlFiles[] = dbsql.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.equals("dbinit.sql") || name.matches(fileFormat);
				}
			});
			for (File file : sqlFiles) {
				updateFiles.add(file);
			}
		}
		File sqlFiles[] = updateFiles.toArray(new File[0]);
		Arrays.sort(sqlFiles, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				String name1 = o1.getName();
				String name2 = o2.getName();
				if (name1.equals("dbinit.sql")) {
					return -1;
				}
				String ver1 = getVersion(name1);
				String ver2 = getVersion(name2);
				return comparVersion(ver1, ver2);
			}
		});
		List<File> listFiles = uniq(Arrays.asList(sqlFiles));
		String sql = "SELECT COUNT(0) FROM `information_schema`.`TABLES` t WHERE t.`TABLE_SCHEMA`='" + Version.dbName + "' AND t.`TABLE_NAME`='"
		        + Version.sqlVerTableName + "' ";
		if (access.queryForInt(sql) == 1) {
			sql = "SELECT t.`id`,t.`version`,t.`update_time` FROM  " + Version.dbName + ".`" + Version.sqlVerTableName + "` t order by t.id";
			List<String> verions = new ArrayList<String>();
			for (Map<String, Object> map : access.queryForList(sql)) {
				verions.add(map.get("version").toString());
			}
			if (verions.size() == 0) {
				systemError("table " + Version.dbName + ".`" + Version.sqlVerTableName + "` is null ,please check database");
			}
			List<File> del = new ArrayList<File>();
			String lastVersion = verions.get(verions.size() - 1);
			for (File file : listFiles) {
				String ver1 = getVersion(file);
				if (verions.contains(ver1)) {
					del.add(file);
				} else if (comparVersion(ver1, lastVersion) <= 0) {
					del.add(file);
				}
			}
			listFiles.removeAll(del);
		} else {
			sql = "CREATE TABLE " + Version.sqlVerTableName
			        + "(id INT AUTO_INCREMENT,`version` VARCHAR(20), update_time DATETIME DEFAULT NOW(), PRIMARY KEY (`id`) )";
			if (!access.execNoQuerySql(sql)) {
				systemError("exec sql failed:" + sql);
			}
		}
		execSqlFile(access, listFiles);
	}

	public static String getVersion(File file) {
		return getVersion(file.getName());
	}

	public static String getVersion(String fileName) {
		String ver = fileName.replaceAll(fileFormat, "$1");
		if (ver.equals("dbinit.sql")) {
			return "init";
		} else {
			return ver;
		}
	}

	public static <CLASSNAME> List<CLASSNAME> uniq(List<CLASSNAME> list) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		List<CLASSNAME> res = new ArrayList<CLASSNAME>();
		for (CLASSNAME object : list) {
			if (object != null) {
				if (!map.containsKey(object.toString())) {
					map.put(object.toString(), object.hashCode());
					res.add(object);
				}
			} else if (!map.containsKey(null)) {
				map.put(null, 0);
				res.add(object);
			}
		}
		return res;
	}

	public static int comparVersion(String v1, String v2) {
		if (v1 == null && v2 == null)
			return 0;
		if (v1 == null)
			return -1;
		if (v2 == null)
			return 1;
		String v1s[] = v1.split("\\.");
		String v2s[] = v2.split("\\.");
		int i = 0;
		while (true) {
			if (v1s.length > i && v2s.length > i) {
				int res = Convert.toInt(v1s[i]) - Convert.toInt(v2s[i]);
				if (res != 0)
					return res;
			} else if (v1s.length > i) {
				return 1;
			} else if (v2s.length > i) {
				return -1;
			} else {
				return 0;
			}
		}
	}

	static void execSqlFile(DataAccess access, List<File> verions) {
		for (File file : verions) {
			execSqlFile(access, file);
		}
	}

	public static void systemError(String msg) {
		systemError(msg, null);
	}

	public static void systemError(String msg, Throwable e) {
		if (e == null)
			LogUtils.error(msg);
		else
			LogUtils.error(msg, e);
		LogUtils.error("system serious error,exit 1");
		System.exit(1);
	}

	static String getCurrentDbName(DataAccess access) {
		return access.queryForString("SELECT DATABASE()");
	}

	static void checkDb(DataAccess access) {
		String connDataBaseName = getCurrentDbName(access);
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

	static void execSqlFile(DataAccess access, File file) {
		try {
			String connDataBaseName = getCurrentDbName(access);
			if (!connDataBaseName.equalsIgnoreCase(Version.dbName)) {
				boolean res = access.execNoQuerySql("use " + Version.dbName);
				if (!res) {
					systemError("use " + Version.dbName + " failed, please check database/system config");
				}
			}
			String fileVer = getVersion(file);
			List<String> sqls = parseSqlFile(file);
			if (sqls != null && !sqls.isEmpty()) {
				for (String sql : sqls) {
					LogUtils.info(fileVer + " sql:" + sql);
					if (!access.execNoQuerySql(sql)) {
						systemError(fileVer + " exec sql failed with " + file + "\n" + sql);
					}
				}
			} else {
				systemError("not parse sql in file:" + file);
			}
			String sql = "insert into " + Version.sqlVerTableName + "(`version`)values(?)";
			if (!access.execNoQuerySql(sql, fileVer)) {
				systemError("exec sql failed:" + sql);
			}
		} catch (Exception e) {
			systemError("", e);
		} finally {
		}
	}

	private static List<String> parseSqlFile(File file) throws IOException {
		List<String> sqls = new ArrayList<String>();
		StringBuffer sb = new StringBuffer();
		Map<String, Integer> keys = new HashMap<String, Integer>() {
			{
				put("create", 1);
				put("drop", 1);
				put("alter", 1);
				put("insert", 2);
				put("delete", 2);
				put("update", 2);
			}
		};
		Map<Character, Integer> count = new HashMap<Character, Integer>() {
			{
				put('\"', 0);
				put('\'', 0);
				put('`', 0);
			}
		};
		AtomicBoolean newSql = new AtomicBoolean(false);
		AtomicBoolean mulLineComment = new AtomicBoolean(false);
		String fileVer = getVersion(file);
		LineProcess call = new LineProcess() {

			@Override
			public String processLine(String line) {
				if (line == null)
					return null;
				line = line.trim();
				if (line.isEmpty())
					return null;
				if (mulLineComment.get()) {
					LogUtils.info("in multiline comment: " + line);
					if (line.endsWith("*/")) {
						mulLineComment.set(false);
					}
				}
				if (line.startsWith("--") || line.startsWith("//")) {
					return null;
				}
				String key = line.split(" ", 2)[0].toLowerCase();
				if (key.equals("use")) {
					return null;
				}
				if (keys.containsKey(key)) {
					newSql.set(true);
					if (sb.length() > 10) {
						sqls.add(sb.toString());
					}
					sb.setLength(0);
					for (Character c : count.keySet()) {
						count.put(c, 0);
					}
				}
				if (!newSql.get()) {
					LogUtils.info("not in sql:" + line);
					return null;
				}
				int len = line.length();
				for (int i = 0; i < len; i++) {
					char c = line.charAt(i);
					if (c == '"' || c == '\'' || c == '`') {
						count.put(c, count.get(c) + 1);
					} else if (c == ';') {
						boolean newsql = true;
						for (Character ct : count.keySet()) {
							int ctv = count.get(ct);
							if (ctv % 2 == 1) {
								newsql = false;
								break;
							}
						}
						if (newsql) {
							sb.append(line.substring(0, i));
							newSql.set(false);
							String subLine = line.substring(i + 1);
							return processLine(subLine);
						}
					} else if (c == '-') {
						if (i + 1 < len && line.charAt(i + 1) == '-') {
							sb.append(line.substring(0, i));
							return null;
						}
					} else if (c == '/') {
						if (i + 1 < len && line.charAt(i + 1) == '*') {
							sb.append(line.substring(0, i));
							String subLine = line.substring(i + 2);
							mulLineComment.set(true);
							return processLine(subLine);
						}
					}
				}
				sb.append(line + " ");
				return null;
			}
		};
		if (sb.length() > 10) {
			sqls.add(sb.toString());
		}
		SysConfig.readFile(file.getAbsolutePath(), call);
		return sqls;
	}
}
