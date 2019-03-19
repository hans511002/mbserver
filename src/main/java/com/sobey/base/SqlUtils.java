package com.sobey.base;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sobey.base.util.ToolUtil;
import com.sobey.jcg.support.log4j.LogUtils;

/**
 * Copyrights @ 2011,Tianyuan DIC Information Co.,Ltd. All rights reserved.<br>
 * 
 * @author 张伟
 * @description 作用:SQL工具类，提供常用的SQL工具访问方法 <br>
 * @date 2011-09-16
 */
public class SqlUtils {
	/**
	 * SQL常用一元操作比较符
	 */
	private final static String[] sqlOneOpers = new String[] { "=", ">", "<", "!=", ">=", "<=", "<>", "!>", "!<", "not\\s*in", "in", "like", "not\\s*like",
	        "is\\s*null", "is\\s*not\\s*null,in,not\\s*in" };

	/**
	 * sql格式化，将Sql中以#key格式的键值格式化，并从object中获取指定键值的索引，放置合适的索引位。 如:"INSERT INTO USERS(ID,NAME,SEX)
	 * VALUES(#id,#name,#sex)",那么调用方法有两个作用，首先 将其中格式为#key形式的参数转为JDBC正常格式的参数占位符？，其次根据键值在object中寻找，如果是javabean
	 * 根据反射获取其值，如果是map直接get取值。
	 * 
	 * @param sql
	 *            sql 包含#key格式的键值
	 * @param params
	 *            map中包含的参数。
	 * @param nullEnable
	 *            如果params未包含指定键值的参数，是否设置为null，true表示设置为null，false 表示不设置值，SQL中对应索引位将取消。调用此函数时注意索引位取消引起的语法问题
	 *            方法已尽量努力做到由于键值取消而引发的SQL语法问题，请调用者自己检查注意
	 * @return 长度为2的数组，object[0]是将#key格式化为？的sql，object[2]是从map中获取到的参数。
	 */

	public static Object[] sqlFormat(String sql, Object params, boolean nullEnable) {
		sql += " ";
		Pattern p = Pattern.compile("#(\\w*)");
		Matcher m = p.matcher(sql);
		List sqlParams = new ArrayList();
		Map<String, PropertyDescriptor> propertys = null;
		StringBuffer keys = new StringBuffer();
		while (m.find()) {
			boolean isfindParam = false; // 是否找到指定键值的参数
			String key = m.group(1);
			// 从object中获取数据
			if (Map.class.isAssignableFrom(params.getClass())) {
				sqlParams.add(((Map) params).get(key));// get null if not found
				isfindParam = ((Map) params).containsKey(key);
			} else {// 看成java bean
				try {
					// 读取属性，获取值。
					if (propertys == null) {
						propertys = new HashMap<String, PropertyDescriptor>();
						PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(params.getClass()).getPropertyDescriptors();
						for (PropertyDescriptor descriptor : propertyDescriptors) {
							if (key.equals(descriptor.getName()) && descriptor.getReadMethod() != null) {
								propertys.put(key, descriptor);
							}
						}
					}
					if (propertys.containsKey(key)) {
						try {
							sqlParams.add(propertys.get(key).getReadMethod().invoke(params));
							isfindParam = true;
						} catch (IllegalAccessException e) {
							LogUtils.error(null, e);
							sqlParams.add(null);
						} catch (InvocationTargetException e) {
							LogUtils.error(null, e);
							sqlParams.add(null);
						}
					} else {
						sqlParams.add(null);
					}

				} catch (IntrospectionException e) {
					LogUtils.error(null, e);
				}
			}
			// 如果不允许null处理且有未找到的键值
			if (sqlParams.get(sqlParams.size() - 1) == null && !nullEnable && !isfindParam) {
				/*
				 * 如果有未找到的键值，如果设置nullEnable=true，进行条件表达式匹配，取消与此键值 有关的表达式，本方法已尽量努力做到由于键值取消而引发的SQL语法问题，请调用者自己 检查注意
				 */
				keys.append("#" + key + "|");
				sqlParams.remove(sqlParams.size() - 1);
			}
		}
		Object[] res = new Object[2];
		if (!nullEnable) {
			sql = sql.replaceAll("(?i)([^\\w])(where|on)([^\\w])", "$1$2 1=1 AND $3");// 条件表单式加占位符消除语法问题
			sql = sql.replaceAll("(#\\w*)", " $1 ");// 关键字加入空格
			sql = sql.replaceAll("(?i)([^\\w]and[^\\w]|[^\\w]or[^\\w])", "  $1  ");// and or关键字加入空格
			String sqloneStr = ToolUtil.join(sqlOneOpers, "|");
			// 一次性完全替换没有找到指定关键字的子查询
			String key = keys.substring(0, keys.length() - 1);
			int i = 0;
			p = Pattern.compile("(?i)((,)|[^\\w]and[^\\w]|[^\\w]or[^\\w])?\\s*\\w*\\s*(?:" + sqloneStr + ")?\\s*(?:" + key + ")(?:[^\\w])(\\s*,)*");
			while (i++ < 2) {
				// 关键字在后匹配
				StringBuffer sb = new StringBuffer();
				m = p.matcher(sql);
				while (m.find()) {
					String rep = " ";
					if (m.group(1) != null && m.group(3) != null) {// 前后都有","
						rep = " $1 ";
					} else if (m.group(1) == null && m.group(2) == null && m.group(3) == null) {
						// 如果三个组都为null，则此条件位于句末，如果前面有",",一起干掉
						rep = " _replace__$0__replace ";
					}
					m.appendReplacement(sb, rep);
				}
				m.appendTail(sb);
				if (sb.length() > 0) {
					sql = sb.toString();
					sql = sql.replaceAll("\\s*,*\\s*_replace__.+__replace\\s*", " ");
				}
				if (i == 1) {// 关键字在前匹配
					p = Pattern.compile("(?i)((,)|[^\\w]and[^\\w]|[^\\w]or[^\\w])?\\s*(?:" + key + ")(?:[^\\w])\\s*(?:" + sqloneStr + ")?\\s*\\w*(\\s*,)*");
				}

			}
			// 二元操作符匹配
			sql = sql.replaceAll("(?:(?i)(?:not)?\\s*between)\\s*(?:" + key + ")\\s*and", " ");
			sql = sql.replaceAll("(?:(?i)(?:not)?\\s*between)\\s*\\w*\\s*(?:and)\\s*(?:" + key + ")([^\\w])", " $1 ");

			// 解决由于条件取消引起的SQL语法问题);
			// 条件表单式取消引起的语法问题
			// 条件表达式的多个and or 问题
			p = Pattern.compile("(?i)[^\\w]((and|or)|\\()\\s*((and|or)|\\))[^\\w]");
			m = p.matcher(sql);
			while (m.find()) {
				String replace = " $2 ";
				if (m.group(2) != null && m.group(4) == null) { // 前面是and or 后面是括号
					replace = " $4 ";
				}
				if (m.group(2) == null && m.group(4) == null) { // 前后都是括号
					replace = "##";
				}

				sql = m.replaceFirst(replace);
				m = p.matcher(sql);
			}
			sql = sql.replaceAll("((?i)[^\\w]and|or)\\s*##", " ");
		}
		sql = sql.replaceAll("#\\w*", " ? ");
		sql = sql.replaceAll("\\s+", " ");// 多个空格合并成一个。
		res[0] = sql;
		res[1] = sqlParams.toArray();
		return res;
	}

	/**
	 * 删除SQL中宏变量条件
	 * 
	 * @param sql
	 * @param key
	 * @return
	 */
	public static String delSQLTermFromKey(String sql, String key) {
		key = "'?\\{" + key + "\\}'?";
		sql = sql.replaceAll("(" + key + ")", " $1 ");// 关键字加入空格
		// sql = sql.replaceAll("(?i)([^\\w]and[^\\w]|[^\\w]or[^\\w])", " $1 ");//and or关键字加入空格
		String sqloneStr = ToolUtil.join(sqlOneOpers, "|");
		// 一次性完全替换没有找到指定关键字的子查询
		int i = 0;
		Pattern p = Pattern.compile(
		        "(?i)((,)|[^\\w]and[^\\w]|[^\\w]or[^\\w])?\\s*\\w+\\(?\\w*(\\.\\w+)?\\)?\\s*(?:" + sqloneStr + ")?\\s*(?:" + key + ")(?:[^\\w])(\\s*,)*");
		Matcher m;
		while (i++ < 2) {
			// 关键字在后匹配
			StringBuffer sb = new StringBuffer();
			m = p.matcher(sql);
			while (m.find()) {
				String rep = " ";
				if (m.group(1) != null && m.group(4) != null) {// 前后都有","
					rep = " $1 ";
				} else if (m.group(1) == null && m.group(2) == null && m.group(4) == null) {
					// 如果三个组都为null，则此条件位于句末，如果前面有",",一起干掉
					rep = " _replace__$0__replace ";
				}
				m.appendReplacement(sb, rep);
			}
			m.appendTail(sb);
			if (sb.length() > 0) {
				sql = sb.toString();
				sql = sql.replaceAll("\\s*,*\\s*_replace__.+?__replace\\s*", " ");
			}
			if (i == 1) {// 关键字在前匹配
				p = Pattern.compile("(?i)((,)|[^\\w]and[^\\w]|[^\\w]or[^\\w])?\\s*(?:" + key + ")(?:[^\\w])\\s*(?:" + sqloneStr + ")?\\s*\\w*(\\s*,)*");
			}

		}
		// 二元操作符匹配
		sql = sql.replaceAll("(?:(?i)(?:not)?\\s*between)\\s*(?:" + key + ")\\s*and", " ");
		sql = sql.replaceAll("(?:(?i)(?:not)?\\s*between)\\s*\\w*\\s*(?:and)\\s*(?:" + key + ")([^\\w])", " $1 ");

		// 解决由于条件取消引起的SQL语法问题);
		// 条件表单式取消引起的语法问题
		// 条件表达式的多个and or 问题
		p = Pattern.compile("(?i)[^\\w]((and|or)\\s*(and|or))[^\\w]");
		m = p.matcher(sql);
		while (m.find()) {
			String replace = " $2 ";
			if (m.group(2) != null && m.group(4) == null) { // 前面是and or 后面是括号
				if (")".equals(m.group(3))) {
					replace = " $3 ";
				} else {
					replace = " $4 ";
				}
			}
			if (m.group(2) == null && m.group(4) == null) { // 前后都是括号
				replace = " ";
			}
			sql = m.replaceFirst(replace);
			m = p.matcher(sql);
		}
		// 替换AND/OR{}情况
		sql = sql.replaceAll("((?i)[^\\w]and|or)\\s*\\(\\s*\\)", " ");
		sql = sql.replaceAll("((?i)[^\\w]and|or)\\s*##", " ");
		sql = sql.replaceAll("((?i)[^\\w]where)\\s*\\)\\s", ") ");
		sql = sql.replaceAll("((?i)[^\\w]where)\\s*((?i)\\)|and|or)\\s", " $1 ");
		sql = sql.replaceAll("((?i)[^\\w]where)\\s*((?i)(\\)|group\\s+by\\s+|order\\s*by\\s+|$))", " $2 ");
		sql = sql.replaceAll("((?i)[^\\w]and|or)\\s*((?i)(\\)|group\\s+by\\s+|order\\s*by\\s+|$))", " $2 ");
		// sql = sql.replaceAll("((?i)[^\\w]where)\\s*$", " ");
		sql = sql.replaceAll("#\\w*", " ? ");
		sql = sql.replaceAll("\\s+", " ");// 多个空格合并成一个。
		// sql = sql.replaceAll("((?i)[^\\w]where)\\s*\\(\\s*\\)", " ");
		sql = sql.replaceAll("((?i)[^\\w](where|and|or))\\s*\\(\\s*\\)", " $1 1=1 ");
		sql = sql.replaceAll("\\(\\s*\\)", " ");
		sql = sql.replaceAll("((?i)\\s+where\\s+(and|or)\\s+)", " WHERE ");
		return sql;
	}

	/**
	 * 对传入sql 进行处理分页，该方法为查询大数据量时使用，无排序
	 * 
	 * @param sql
	 *            原始sql
	 * @param countSql
	 *            总数sql
	 * @param page
	 *            页数
	 * @return
	 */
	public static String wrapPagingSql(String sql, String countSql, Page page) {
		sql = sql.trim();// 去除字符串后面的空字符，便于正则表达式匹配
		sql = "SELECT INNERTABLE_.*,ROWNUM RN_,(" + countSql + ") TOTAL_COUNT_ " + "FROM (" + sql + ") INNERTABLE_ WHERE rownum <="
		        + (page.getPosStart() + page.getCount());
		sql = "SELECT INNER_.* FROM (" + sql + ") INNER_ WHERE INNER_.RN_>=" + (page.getPosStart() + 1);
		return sql;
	}

	/**
	 * 分页查询
	 * 
	 * @param sql
	 * @param page
	 * @param isTotal
	 * @return
	 */
	public static String wrapPagingSql(String sql, Page page) {
		return wrapPagingSql(sql, page, true);
	}

	/**
	 * 对SQL进行处理，封装分页逻辑，注意SQL的排序逻辑，SQL一定要根据主键或者唯一键值进行排序， 该SQL查询出的字段中已经有查询总数的判断逻辑，字段TOTAL_COUNT_即为查询总记录。 此函数对SQl进行简单的order
	 * by处理。
	 * 
	 * @return
	 */
	public static String wrapPagingSql(String sql, Page page, boolean isTotal) {
		sql = sql.trim();// 去除字符串后面的空字符，便于正则表达式匹配
		// 匹配order by结尾的正则表达式
		Pattern pattern = Pattern.compile(".+order\\s+by\\s+(.+)", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(sql);
		// 是否找到order by
		boolean isfindOrderBy = false;
		String continueSql = "";
		while (matcher.find()) {
			isfindOrderBy = true;
			// 对order by后面的语句持续匹配以便找到order by后最后面的语句。
			continueSql = matcher.group(1);
			matcher = pattern.matcher(continueSql);
		}
		boolean isAutoOrderBy = true;// 是否自动追加order by 语句
		if (isfindOrderBy) {
			isAutoOrderBy = false;
			// 分析order by是否处于SQL语句句末,判断标准如果order by后的语句其左括号(与右括号)的个数相当，即为句末。
			try {
				if (!(continueSql.split("\\(").length == continueSql.split("\\)").length)) {
					isAutoOrderBy = true;
				}
			} catch (Exception e) {
				if (continueSql.trim().equals("") || continueSql.indexOf("(") != -1 || continueSql.indexOf(")") != -1) {
					isAutoOrderBy = true;
				}
			}
		}
		if (isAutoOrderBy) {// 没有做Order by 处理，简单将所有的查询字段作为order by语句
			throw new IllegalStateException("SQL:" + sql + " 没有做排序处理，不能保证其分页的正确性，不能做分页！");
			// 取消自动追加Oeder——by
			/*
			 * int selectIndex=sql.toUpperCase().indexOf("SELECT"); int fromIndex=sql.toUpperCase().indexOf("FROM");
			 * String queryColumns=sql.substring(selectIndex+6,fromIndex); if (queryColumns.contains("*")){
			 * Log.error("SQL语句没有ORDER BY语句进行排序，查询列中包含[*]匹配符，" + "为SQL添加ORDER BY 语句失败！"); throw new
			 * IllegalStateException(); } //去除AS语句
			 * queryColumns=queryColumns.replaceAll("(?i)\\s+as\\s+\\w+"," ").trim(); //去除，号前后空格
			 * queryColumns=queryColumns.replaceAll("\\s*,\\s*",","); //去除没有AS的别命名
			 * queryColumns=queryColumns.replaceAll("\\s+\\w+(,|$)","$1"); //加上order by 子句
			 * sql+=" ORDER BY "+queryColumns;
			 */

		}
		// 封装分页逻辑
		// 嵌套子查询，用于计算rowNum和计算最大rowNum，即总数目
		sql = "SELECT INNERTABLE_.*,ROWNUM RN_" + (isTotal ? (",DECODE(ROWNUM," + (page.getPosStart() + 1) + ",MAX(ROWNUM) OVER(),0) TOTAL_COUNT_") : "")
		        + " FROM (" + sql + ") INNERTABLE_ ";
		// 加入分页逻辑
		sql = "SELECT INNER_.* FROM (" + sql + ") INNER_ WHERE INNER_.RN_>=" + (page.getPosStart() + 1) + " AND INNER_.RN_<="
		        + (page.getPosStart() + page.getCount());
		return sql;
	}

	/**
	 * SQL LIKE 参数前后模糊匹配参数处理
	 * 
	 * @param param
	 * @return 返回"%"+param+"%"的字符串
	 */
	public static String allLikeParam(String param) {
		if (param == null) {
			throw new NullPointerException("LIKE 参数为NULL");
		}
		param = param.replaceAll("_", "/_").replaceAll("%", "/%");
		return "'%" + param + "%' escape '/' ";
	}

	/**
	 * 相识绑定参数封装，无"'"号
	 * 
	 * @param param
	 * @return
	 */
	public static String allLikeBindParam(String param) {
		if (param == null) {
			throw new NullPointerException("LIKE 参数为NULL");
		}
		param = param.replaceAll("_", "/_").replaceAll("%", "/%");
		return "%" + param + "%";
	}

	/**
	 * SQl LIKE参数前模糊匹配处理。
	 * 
	 * @param param
	 * @return 返回"%"+param的字符串
	 */
	public static String prefixLikeParam(String param) {
		if (param == null) {
			throw new NullPointerException("LIKE 参数为NULL");
		}
		param = param.replaceAll("_", "/_").replaceAll("%", "/%");
		return "'%" + param + "' escape '/' ";
	}

	/**
	 * SQl LIKE参数后模糊匹配处理。
	 * 
	 * @param param
	 * @return 返回 param+"%"的字符串
	 */
	public static String afterLikeParam(String param) {
		if (param == null) {
			throw new NullPointerException("LIKE 参数为NULL");
		}
		param = param.replaceAll("_", "/_").replaceAll("%", "/%");
		return "'" + param + "'% escape '/' ";
	}

	/**
	 * SQL IN参数处理，将一个数组转换为IN格式的参数，参数长度不能大于1000
	 * 
	 * @return 返回类似(1, 2, , 3)的字符串
	 */
	public static String inParamDeal(int[] params) {
		if (params == null) {
			throw new NullPointerException("IN 参数为空");
		}
		if (params.length > 1000) {
			throw new IllegalStateException("参数长度超过了1000个");
		}
		String inParams = "(";
		int count = 0;
		for (int param : params) {
			inParams += param;
			inParams += (++count == params.length) ? "" : ",";
		}
		inParams += ")";
		return inParams;
	}

	/**
	 * SQL IN参数处理，将一个数组转换为IN格式的参数，参数长度不能大于1000
	 * 
	 * @return 返回类似(1, 2, , 3)的字符串
	 */
	public static String inParamDeal(long[] params) {
		if (params == null) {
			throw new NullPointerException("IN 参数为空");
		}
		if (params.length > 1000) {
			throw new IllegalStateException("参数长度超过了1000个");
		}
		String inParams = "(";
		int count = 0;
		for (long param : params) {
			inParams += param;
			inParams += (++count == params.length) ? "" : ",";
		}
		inParams += ")";
		return inParams;
	}

	/**
	 * SQL IN参数处理，将一个数组转换为IN格式的参数，参数长度不能大于1000
	 * 
	 * @return 返回类似(1, 2, , 3)的字符串
	 */
	public static String inParamDeal(float[] params) {
		if (params == null) {
			throw new NullPointerException("IN 参数为空");
		}
		if (params.length > 1000) {
			throw new IllegalStateException("参数长度超过了1000个");
		}
		String inParams = "(";
		int count = 0;
		for (float param : params) {
			inParams += param;
			inParams += (++count == params.length) ? "" : ",";
		}
		inParams += ")";
		return inParams;
	}

	/**
	 * SQL IN参数处理，将一个数组转换为IN格式的参数，参数长度不能大于1000
	 * 
	 * @return 返回类似(1, 2, , 3)的字符串
	 */
	public static String inParamDeal(double[] params) {
		if (params == null) {
			throw new NullPointerException("IN 参数为空");
		}
		if (params.length > 1000) {
			throw new IllegalStateException("参数长度超过了1000个");
		}
		String inParams = "(";
		int count = 0;
		for (double param : params) {
			inParams += param;
			inParams += (++count == params.length) ? "" : ",";
		}
		inParams += ")";
		return inParams;
	}

	/**
	 * SQL IN参数处理，将一个数组转换为IN格式的参数，参数长度不能大于1000
	 * 
	 * @return 返回类似(1, 2, , 3)的字符串
	 */
	public static String inParamDeal(short[] params) {
		if (params == null) {
			throw new NullPointerException("IN 参数为空");
		}
		if (params.length > 1000) {
			throw new IllegalStateException("参数长度超过了1000个");
		}
		String inParams = "(";
		int count = 0;
		for (short param : params) {
			inParams += param;
			inParams += (++count == params.length) ? "" : ",";
		}
		inParams += ")";
		return inParams;
	}

	/**
	 * SQL IN参数处理，将一个数组转换为IN格式的参数，参数长度不能大于1000
	 * 
	 * @return 返回类似(1, 2, , 3)的字符串
	 */
	public static String inParamDeal(Object[] params) {
		if (params == null) {
			throw new NullPointerException("IN 参数为空");
		}
		if (params.length > 1000) {
			throw new IllegalStateException("参数长度超过了1000个");
		}
		String inParams = "(";
		int count = 0;
		for (Object param : params) {
			boolean isNumber = BaseTool.isNumeric(param);
			inParams += (isNumber ? "" : "'");
			inParams += param;
			inParams += (isNumber ? "" : "'");
			inParams += (++count == params.length) ? "" : ",";
		}
		inParams += ")";
		return inParams;
	}

	/**
	 * SQL IN参数处理，将一个数组转换为IN格式的参数，参数长度不能大于1000
	 * 
	 * @return 返回类似(1, 2, , 3)的字符串
	 */
	public static String inParamDeal(String[] params) {
		if (params == null) {
			throw new NullPointerException("IN 参数为空");
		}
		if (params.length > 1000) {
			throw new IllegalStateException("参数长度超过了1000个");
		}
		String inParams = "(";
		int count = 0;
		for (String param : params) {
			boolean isNumber = BaseTool.isNumeric(param);
			inParams += (isNumber ? "" : "'");
			inParams += param;
			inParams += (isNumber ? "" : "'");
			inParams += (++count == params.length) ? "" : ",";
		}
		inParams += ")";
		return inParams;
	}

	/**
	 * SQL IN参数处理，将一个数组转换为IN格式的参数，参数长度不能大于1000
	 * 
	 * @return 返回类似(1, 2, , 3)的字符串
	 */
	public static String inParamDeal(Collection params) {
		if (params == null) {
			throw new NullPointerException("IN 参数为空");
		}
		if (params.size() > 1000) {
			throw new IllegalStateException("参数长度超过了1000个");
		}
		String inParams = "(";
		int count = 0;
		for (Object param : params) {
			boolean isNumber = BaseTool.isNumeric(param);
			inParams += (isNumber ? "" : "'");
			inParams += param;
			inParams += (isNumber ? "" : "'");
			inParams += (++count == params.size()) ? "" : ",";
		}
		inParams += ")";
		return inParams;
	}
}
