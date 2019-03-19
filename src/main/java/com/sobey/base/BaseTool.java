package com.sobey.base;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sobey.base.util.StringUtils;
import com.sobey.jcg.support.jdbc.JdbcException;
import com.sobey.jcg.support.log4j.LogUtils;
import com.sobey.jcg.support.utils.Convert;
import com.sobey.jcg.support.utils.MapUtils;

/**
 */
public class BaseTool {
	public static java.text.SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * 按照JAVA命名规范将数据库字段名替换成JAVA风格的字段名。例如：THIS_IS_A_STR==>thisIsAStr
	 * 
	 * @param columnName
	 * @return
	 */
	public static String tranColumnToJavaName(String columnName) {
		// 匹配XXX_格式的命名
		Pattern p = Pattern.compile("([A-Za-z_])([A-Za-z0-9]*)_?");
		Matcher m = p.matcher(columnName);
		int count = 0;
		StringBuffer javaName = new StringBuffer();
		while (m.find()) {
			// java变量第一个字母小写
			if (count++ == 0) {
				javaName.append(m.group(1).toLowerCase());
			} else { // 以下划线分割的第一个字母大写
				javaName.append(m.group(1).toUpperCase());
			}
			javaName.append(m.group(2).toLowerCase());
		}
		return javaName.toString();
	}

	/**
	 * 格式化JAVA键值成JAVA风格
	 * 
	 * @return
	 */
	public static Map<String, Object> formatMapKeyToJavaStyle(Map<String, Object> data) {
		Map<String, Object> tableDataFormat = new HashMap<String, Object>();
		for (Map.Entry<String, Object> entry : data.entrySet()) {
			tableDataFormat.put(BaseTool.tranColumnToJavaName(entry.getKey()), entry.getValue());
		}
		return tableDataFormat;
	}

	/**
	 * 格式化LIST<Map></Map>
	 * 
	 * @param datas
	 * @return
	 */
	public static List<Map<String, Object>> formatMapKeyToJavaStyle(List<Map<String, Object>> datas) {
		List<Map<String, Object>> columnDataFormat = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> data : datas) {
			columnDataFormat.add(formatMapKeyToJavaStyle(data));
		}
		return columnDataFormat;
	}

	/**
	 * 按照数据库字段命名规范将JAVA风格变量替换成数据库字段名。例如：thisIsAStr/ThisIsAStr==>THIS_IS_A_STR。
	 * 该方法的不足：参数只能接受形如thisIsAStr/ThisIsAStr的标准java命名，否则将不会得到预期的结果
	 * 
	 * @param javaName
	 * @return
	 * @author 熊小平
	 * @date 2011-11-03
	 */
	public static String tranJavaNameToColumn(String javaName) {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < javaName.length(); i++) {
			if (i != 0 && Character.isUpperCase(javaName.charAt(i))) {
				// 如果是大写字符且不是首字母，则添加下划线
				result.append("_");
			}
			result.append(Character.toUpperCase(javaName.charAt(i)));
		}
		return result.toString();
	}

	/**
	 * 格式化Date数据
	 * 
	 * @param date
	 *            要格式化的Date
	 * @param formatStr
	 *            格式
	 * @return
	 */
	public static String formatDate(Date date, String formatStr) {
		SimpleDateFormat dateToStr = new SimpleDateFormat(formatStr);
		return dateToStr.format(date);
	}

	/**
	 * 从字符串解析Date
	 * 
	 * @param date
	 * @param formatStr
	 * @return
	 */
	public static Date parseDate(String date, String formatStr) {
		SimpleDateFormat strToDate = new SimpleDateFormat(formatStr);
		try {
			return strToDate.parse(date);
		} catch (ParseException e) {
			LogUtils.error(null, e);
			return null;
		}
	}

	/**
	 * 删除整型数组中的某个元素，返回新的数组
	 * 
	 * @param oldArray
	 *            原数组
	 * @param index
	 *            被删除数据的序列号
	 * @return
	 */
	public static int[] removeArrayElement(int[] oldArray, int index) {
		try {
			if (oldArray.length > 1) {
				int[] newArray = new int[oldArray.length - 1];
				int newIndex = 0;
				for (int i = 0; i < oldArray.length; i++) {
					if (i != index) {
						newArray[newIndex] = oldArray[i];
						newIndex++;
					}
				}
				return newArray;
			} else if (index == 0) {
				return new int[0];
			} else {
				return oldArray;
			}
		} catch (Exception e) {
			return oldArray;
		}
	}

	// 汉字问题
	public static String parseChinese(String in) {

		String s = null;
		byte temp[];
		if (in == null) {
			LogUtils.warn("Warn:Chinese null founded!");
			return new String("");
		}
		try {
			temp = in.getBytes("iso-8859-1");
			s = new String(temp, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			LogUtils.warn("汉字编码转换出错：" + e.toString());
		}
		return s;
	}

	/**
	 * 返回Integer对象
	 * 
	 * @param obj
	 * @return
	 */
	public static Integer parseInt(Object obj) {
		try {
			return Integer.parseInt(obj.toString());
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 判断某个JDBCException是否SQLException
	 * 
	 * @param e
	 *            JdbcException
	 * @return
	 */
	public static boolean isSQLException(JdbcException e) {
		boolean rtn = false;
		if (e != null && e.getException() instanceof SQLException) {
			rtn = true;
		}
		return rtn;
	}

	/**
	 * 从JdbcException中抽取SQL错误信息
	 * 
	 * @param e
	 *            JdbcException
	 * @return
	 */
	public static String getSQLMessage(JdbcException e) {
		String msg = null;
		if (e != null) {
			Exception exception = e.getException();
			if (exception != null && exception instanceof SQLException) {
				msg = exception.getMessage();
			}
		}

		return msg;
	}

	/**
	 * 判断一个对象是否是数字 "33" "+33" "033.30" "-.33" ".33" " 33." " 000.000 "
	 * 
	 * @param obj
	 * @return boolean
	 */
	public static boolean isNumeric(Object obj) {
		Class objClass = obj.getClass();
		if (objClass.isPrimitive()) {
			// 是基本类型的时候，long,short,int,double算作数字
			if (objClass == Short.TYPE || objClass == Long.TYPE || objClass == Integer.TYPE || objClass == Float.TYPE || objClass == Double.TYPE
			        || objClass == Byte.TYPE) {
				return true;
			}
		} else { // 不是基本类型，判断是否是数字类型
			return Number.class.isAssignableFrom(objClass);
		}
		return false;
	}

	/**
	 * 替换Map中空值
	 * 
	 * @param map
	 * @return
	 */
	public static Map<String, Object> replaceNullOfMap(Map<String, Object> map) {
		Set<String> keys = map.keySet();
		for (Iterator<String> it = keys.iterator(); it.hasNext();) {
			String key = it.next();
			if (map.get(key) == null)
				map.put(key, "");
		}
		return map;
	}

	/**
	 * 获取嵌套MAP的值，比如传入参数key=a.b,就是获取键值=a的Map的键值=b的值
	 * 
	 * @param map
	 * @param key
	 * @param params
	 * @return
	 */
	public static Object getLinkMapVal(Map<String, Object> map, String key, String... params) {
		Object obj = null;
		String[] keys = key.split("\\.");
		if (keys.length == 1) {
			obj = map.get(key);
			if (params.length == 0)
				return obj;
			for (int i = 0; i < params.length; i++) {
				if (obj == null)
					return null;
				obj = ((Map<String, Object>) obj).get(params[i]);
			}
		} else {
			obj = map.get(keys[0]);
			for (int i = 1; i < keys.length; i++) {
				if (obj == null)
					return null;
				obj = ((Map<String, Object>) obj).get(keys[i]);
			}
		}
		return obj;
	}

	/**
	 * 将数据维度打横的函数，此函数先支持一个维度打横
	 * 
	 * @param header
	 *            表头，如['时间','地域','产品类型','指标1','指标2'....]
	 * @param data
	 *            要进行转换的数据,二维数组。
	 * @param rule
	 *            转换的规则，需要的规则如下： { dimDataIndex:要进行转换的维度列索引,数组格式 dimIndex:[]--所有的维度列 tranHeader:转换维度的列表头
	 *            tranGdls:[],要进行转换的指标列索引，数组，如果没有，默认为所有维度列全转， }
	 * @return 数据转换之后的数据，第一行为表头信息，从第二行始为对应数据信息。
	 */
	// public static Object[][] tranDimData(String[] header, Object[] data, Map<String, Object> rule) {
	//
	// }

	/**
	 * 将维度数据打横的函数，此函数只支持一个维度打横
	 * 
	 * @param colNames
	 *            列集合，如['时间','地域','产品类型','指标1','指标2'....]，必填
	 * @param rows
	 *            要进行转换的数据，二维数组，必填
	 * @param groupCol
	 *            原生数据分组的列信息。必填
	 * @param transCol
	 *            要进行打横的列,必填
	 * @param gdlCols
	 *            要进行打纵的指标列信息,必填
	 * @return 转换之后的数据，数组第一行为表头信息，从第二行始为对应转换之后的数据,第三行为维度转换列的位置
	 */
	public static Object[] tranDimData(String[] colNames, Object[][] rows, String[] groupCol, String transCol, String[] gdlCols) {
		// 进行参数检查
		if (colNames == null || colNames.length == 0) {
			throw new IllegalArgumentException("所传列集合为空！");
		}
		if (groupCol == null || groupCol.length == 0) {
			throw new IllegalArgumentException("所传分组列为空，转换失败！");
		}
		if (StringUtils.isEmpty(transCol)) {
			throw new IllegalArgumentException("转换列为空！");
		}
		if (gdlCols == null || gdlCols.length == 0) {
			throw new IllegalArgumentException("转换的指标为空，转换失败！");
		}
		// 所有的GROUP BY列序号
		List<Integer> groupIndexs = new ArrayList<Integer>();
		// 要进行转换的列索引
		int dimDataIndex = -1;
		// 要进行转换的指标索引
		Map<String, Integer> tranGdls = new HashMap<String, Integer>();
		Map<String, Integer> columnIndex = new HashMap<String, Integer>();
		for (int i = 0; i < colNames.length; i++) {
			columnIndex.put(colNames[i], i);
		}
		// 计算GROUP BY 索引
		boolean isFindTranCode = false;// 是否找到要分组的CODE
		for (int j = 0; j < groupCol.length; j++) {
			groupIndexs.add(columnIndex.get(groupCol[j]));
			if (groupCol[j].equals(transCol)) {
				isFindTranCode = true;
			}
		}
		if (!isFindTranCode) {
			String[] temp = new String[groupCol.length + 1];
			System.arraycopy(groupCol, 0, temp, 0, groupCol.length);
			groupCol = temp;
			groupCol[groupCol.length - 1] = transCol;
			groupIndexs.add(columnIndex.get(transCol));
		}
		// 计算指标索引
		for (int j = 0; j < gdlCols.length; j++) {
			tranGdls.put(gdlCols[j], columnIndex.get(gdlCols[j]));
		}
		dimDataIndex = columnIndex.get(transCol);
		if (rows == null || rows.length == 0) {
			String[] tranColumns = new String[groupCol.length];
			int count = 0;
			// 如果无数据
			for (String columnName : groupCol) {
				if (!columnName.equals(transCol)) {
					tranColumns[count++] = columnName;
				}
			}
			tranColumns[count] = transCol;
			return new Object[] { tranColumns, new Object[0][0], count };
		} else {
			// 遍历数据，进行分析
			// 所有的维度组合,缓存列
			Set<List> allDimData = new LinkedHashSet<List>();
			// 要转换的维度值
			Set tranCode = new LinkedHashSet();
			// 维度和指标对应的值，
			// 比如维度1,维度2，维度3，指标1==Value
			Map<String, Object> tempMappingData = new HashMap<String, Object>();
			for (int i = 0; i < rows.length; i++) {
				// 所有的GROUP BY的维度和标识的集合，除需要列转换的
				List list = new ArrayList();
				// 所有的维度和标识的集合
				String allDims = "";
				Object tempTranCode = "";
				for (int count = 0; count < groupIndexs.size(); count++) {
					int index = groupIndexs.get(count);
					Object dimValue = rows[i][index] != null ? rows[i][index] : "";
					if (index != dimDataIndex) {
						list.add(dimValue);
						allDims += dimValue.toString() + ",";
					} else {// 记录的维度值
						tranCode.add(dimValue);
						tempTranCode = dimValue;
					}
				}
				allDims += tempTranCode + ",";
				allDimData.add(list);
				for (Map.Entry<String, Integer> entry : tranGdls.entrySet()) {
					String allDimsTemp = allDims + entry.getKey();
					Object value = rows[i][entry.getValue()];
					if (tempMappingData.containsKey(allDimsTemp)) {
						value = sum(value, tempMappingData.get(allDimsTemp));
					}
					tempMappingData.put(allDimsTemp, value);
				}
			}
			// 声明一个转换后的二维数组，其行长度等于GROUP BY的长度*维度转换指标的长度
			int rowLength = allDimData.size() * gdlCols.length;
			// 二维数组列的长度=保留维度长度 -1+转换的CODE长度。
			int colLength = groupIndexs.size() + tranCode.size();
			Object[][] data = new Object[rowLength][colLength];
			// 分析转换后的列
			String[] tranColumns = new String[colLength];
			int count = 0;
			for (String columnName : groupCol) {
				if (!columnName.equals(transCol)) {
					tranColumns[count++] = columnName;
				}
			}
			tranColumns[count++] = transCol;
			for (Object code : tranCode) {
				tranColumns[count++] = code.toString();
			}
			// 计算转换后的数据
			count = 0;
			for (List groupByDim : allDimData) {
				int j = 0;
				for (Map.Entry<String, Integer> entry : tranGdls.entrySet()) {
					int z = 0;
					String dimString = "";
					// 先赋值维度和标识值
					for (z = 0; z < groupByDim.size(); z++) {
						String value = groupByDim.get(z).toString();
						data[count * tranGdls.size() + j][z] = value;
						dimString += value + ",";
					}
					// 赋值转换后的维度列
					data[count * tranGdls.size() + j][z] = entry.getKey();
					// 赋值转换后的维度指标
					int k = 0;
					for (Object code : tranCode) {
						String key = dimString + code + "," + entry.getKey();
						data[count * tranGdls.size() + j][z + 1 + k++] = tempMappingData.get(key);
					}
					j++;
				}
				count++;
			}
			Object[] result = new Object[3];
			result[0] = tranColumns;
			result[1] = data;
			result[2] = groupCol.length - 1;
			return result;
		}
	}

	/**
	 * 两个数字相加
	 * 
	 * @param data1
	 * @param data2
	 * @return
	 */
	public static Object sum(Object data1, Object data2) {
		try {
			// 将所有的NUMBER转换为BIGDEMAIL进行加减
			BigDecimal big1 = null;
			BigDecimal big2 = null;
			Number num1 = null;
			Number num2 = null;
			if (data1 instanceof String) {
				big1 = new BigDecimal(data1.toString());
			} else {
				num1 = (Number) data1;
			}
			if (data2 instanceof String) {
				big2 = new BigDecimal(data2.toString());
			} else {
				num2 = (Number) data2;
			}
			if (num1 != null) {
				if (num1 instanceof BigDecimal) {
					big1 = (BigDecimal) num1;
				} else {
					big1 = new BigDecimal(num1.toString());
				}
			}
			if (num2 != null) {
				if (num2 instanceof BigDecimal) {
					big2 = (BigDecimal) num2;
				} else {
					big2 = new BigDecimal(num2.toString());
				}
			}
			return big1.add(big2);
		} catch (Exception e) {
			if (data1 == null) {
				return data2;
			}
			if (data2 == null) {
				return data1;
			}
			return data1.toString() + data2;
		}
	}

	/**
	 * 格式化数据
	 * 
	 * @param value
	 * @param partten
	 *            格式为####.###.##.##的格式其中#号代表数字
	 * @return
	 */
	public final static String formatValue(Object value, String partten) {
		if (value == null) {
			return null;
		}
		try {
			if (StringUtils.isNotEmpty(partten) && !partten.equals("0") && !value.toString().equals("0")) {
				// 格式开始字符
				String begin = partten.substring(0, partten.indexOf("#"));
				// 数字格式
				String dataParttern = partten.substring(partten.indexOf("#"), partten.lastIndexOf("#") + 1);
				// 数字结束字符
				String end = partten.substring(partten.lastIndexOf("#") + 1, partten.length());
				// 计算数字小数点位数。
				String data = "";
				int decimalLegth = 0;
				if (dataParttern.indexOf(".") != -1) {
					decimalLegth = dataParttern.length() - dataParttern.lastIndexOf(".") - 1;
				}
				double doubleData = Double.parseDouble(value.toString());
				// 四舍五入
				doubleData = roundDouble(doubleData, decimalLegth);
				String tempPartten = "0" + (decimalLegth == 0 ? "" : ".");
				for (int i = 0; i < decimalLegth; i++) {
					tempPartten += "0";
				}
				DecimalFormat df2 = new DecimalFormat(tempPartten);
				df2.setGroupingUsed(false);
				data = df2.format(doubleData);
				data = data + "";
				String integerParttern = decimalLegth != 0 ? dataParttern.substring(0, dataParttern.lastIndexOf(".")) : dataParttern;
				String formatValue = decimalLegth == 0 ? "" : data.substring(data.lastIndexOf("."), data.length());
				String integerValue = decimalLegth == 0 ? data : data.substring(0, data.lastIndexOf("."));
				int count = 0;
				int abstant = integerValue.length() - integerParttern.length();
				int i = 0;
				for (i = integerParttern.length(); i > 0; i--) {
					String tempFormat = integerParttern.substring(i - 1, i);
					if (i + count + abstant - 1 < 0) {
						break;
					}
					String tempValue = integerValue.substring(i + count + abstant - 1, i + count + abstant);
					if (tempValue.equals("-")) {
						formatValue = tempValue + formatValue;
						continue;
					}
					if (tempFormat.equals("#")) {
						formatValue = tempValue + "" + formatValue;
					} else {
						formatValue = tempFormat + "" + formatValue;
						count++;
					}
				}
				if (i + count + abstant > 0) {
					formatValue = integerValue.substring(0, i + count + abstant) + formatValue;
				}
				formatValue = begin + formatValue + end;
				return formatValue;
			} else {
				return value.toString();
			}
		} catch (Exception e) {
			return value.toString();
		}
	}

	/**
	 * double四舍五入
	 * 
	 * @param val
	 * @param precision
	 * @return
	 */
	public static Double roundDouble(double val, int precision) {
		Double ret = null;
		try {
			double factor = Math.pow(10, precision);
			ret = Math.floor(val * factor + 0.5) / factor;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}

	/**
	 * 判断以某个字符分割的字符串中是否存在指定某个字符串，如"1,2,13"存在字符1,13,不存在13.
	 * 
	 * @param strs
	 * @param str
	 * @param split
	 *            分隔符，默认为","
	 * @return
	 */
	public static boolean isExistsStr(String strs, String str, String split) {
		if (!StringUtils.isNotEmpty(split)) {
			split = ",";
		}
		if (StringUtils.isEmpty(strs) || StringUtils.isEmpty(str)) {
			return false;
		}
		if (strs.equals(str)) {
			return true;
		}
		if (!strs.contains(str)) {
			return false;
		} else {
			String[] splitStr = strs.split(split);
			for (String tempStr : splitStr) {
				if (tempStr.equals(str)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isExistsStr(String strs, String str) {
		return isExistsStr(strs, str, ",");
	}

	/**
	 * 将数组中的Map根据关键字做关联，将src的属性拷贝到desc中，重复的覆盖属性
	 * 
	 * @param desc
	 * @param src
	 * @param key
	 * @return
	 */
	public static List<Map<String, Object>> listMapExtend(List<Map<String, Object>> desc, List<Map<String, Object>> src, String key) {
		Map<Object, Integer> srcKeyIndexMapping = new HashMap<Object, Integer>();
		int count = 0;
		for (Map<String, Object> srcMap : src) {
			Object keyValue = MapUtils.getObject(srcMap, key);
			srcKeyIndexMapping.put(keyValue, count++);
		}
		for (Map<String, Object> descMap : desc) {
			Object keyValue = MapUtils.getObject(descMap, key);
			if (srcKeyIndexMapping.containsKey(keyValue)) {
				Map<String, Object> srcMap = src.get(srcKeyIndexMapping.get(keyValue));
				srcKeyIndexMapping.remove(keyValue);
				descMap.putAll(srcMap);
			}
		}
		if (MapUtils.isNotEmpty(srcKeyIndexMapping)) {
			for (Map.Entry<Object, Integer> mapp : srcKeyIndexMapping.entrySet()) {
				desc.add(src.get(mapp.getValue()));
			}
		}
		return desc;
	}

	/**
	 * 排序字符
	 * 
	 * @param str
	 * @param splitCh
	 * @param orderMode
	 *            0升序，1降序
	 * @return
	 */
	public static String orderStr(String str, String splitCh, final int orderMode) {
		String[] arr = str.split(splitCh);
		Arrays.sort(arr, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return o1.compareTo(o2) * (orderMode > 0 ? -1 : 1);
			}
		});
		return StringUtils.join(arr, splitCh);
	}

	// 排序字符
	public static String orderStr(String str, String splitCh) {
		return orderStr(str, splitCh, 0);
	}

	/**
	 * 获取指定的jar文件中所有继承了clazz的实现类
	 * 
	 * @param clazz
	 * @return
	 * @throws IOException
	 */
	public static <T> List<Class<? extends T>> getJarClasses(File file, ClassLoader loader, Class<T> clazz) throws IOException {
		List<Class<? extends T>> list = new ArrayList<Class<? extends T>>();
		JarFile jar = new JarFile(file);
		Enumeration<JarEntry> entries = jar.entries();
		while (entries.hasMoreElements()) {
			JarEntry entry = entries.nextElement();
			String name = entry.getName();
			if (name.endsWith(".class") && !entry.isDirectory()) {
				String className = name.replaceAll("/", "\\.").replaceAll("\\.class", "");
				try {
					Class<?> tempClazz = loader.loadClass(className);
					if (!tempClazz.isInterface() && !Modifier.isAbstract(tempClazz.getModifiers())) {
						list.add(tempClazz.asSubclass(clazz));
					}
				} catch (Exception e) {
				}
			}
		}
		return list;
	}

	/**
	 * 获取ListMap中指定键值的数据，放入到一个容器中
	 * 
	 * @param mapList
	 * @param key
	 * @param type
	 * @param <T>
	 * @return
	 */
	public static <T> Set<T> getListMapColumnValue(List<Map<String, Object>> mapList, String key, Class<T> type) {
		if (mapList == null || mapList.size() == 0) {
			return Collections.EMPTY_SET;
		}
		Set<T> set = new HashSet<T>();
		for (Map<String, Object> tempMap : mapList) {
			set.add(Convert.convert(tempMap.get(key), type));
		}
		return set;
	}

	public static Object castObject(Class<?> c, Object o) {
		try {
			if (o == null) {
				if (c.equals(int.class)) {
					return (int) 0;
				} else if (c.equals(long.class)) {
					return (int) 0;
				} else if (c.equals(char.class)) {
					return (char) 0;
				} else if (c.equals(byte.class)) {
					return (byte) 0;
				} else if (c.equals(float.class)) {
					return (float) 0.0;
				} else if (c.equals(double.class)) {
					return (double) 0.0;
				} else if (c.equals(Date.class)) {
					return new Date(0);
				}
				return o;
			}
			Class<?> oc = o.getClass();
			if (oc.equals(c) || c.isInstance(o)) {
				return o;
			} // 需要特殊处理
			if (c.equals(int.class) || c.equals(Integer.class)) {
				if (oc.equals(Integer.class)) {
					return o;
				} else if (oc.equals(Long.class)) {
					return ((Long) o).intValue();
				} else if (oc.equals(Double.class)) {
					return ((Double) o).intValue();
				} else if (oc.equals(Float.class)) {
					return ((Float) o).intValue();
				} else if (oc.equals(Character.class)) {
					return (int) ((Character) o).charValue();
				} else if (oc.equals(Byte.class)) {
					return ((Byte) o).intValue();
				} else if (oc.equals(Date.class)) {
					return (int) (((Date) o).getTime() / 1000);
				} else if (oc.equals(String.class)) {
					return Convert.toInt(o, 0);
				}
			} else if (c.equals(long.class) || c.equals(Long.class)) {
				if (oc.equals(Long.class)) {
					return o;
				} else if (oc.equals(Integer.class)) {
					return ((Integer) o).longValue();
				} else if (oc.equals(Double.class)) {
					return ((Double) o).longValue();
				} else if (oc.equals(Float.class)) {
					return ((Float) o).longValue();
				} else if (oc.equals(Character.class)) {
					return (long) ((Character) o).charValue();
				} else if (oc.equals(Byte.class)) {
					return ((Byte) o).longValue();
				} else if (oc.equals(Date.class)) {
					return ((Date) o).getTime();
				} else if (oc.equals(String.class)) {
					return Convert.toLong(o, 0);
				}
			} else if (c.equals(char.class) || c.equals(Character.class)) {
				if (oc.equals(Character.class)) {
					return o;
				} else if (oc.equals(Integer.class)) {
					return (char) ((Integer) o).intValue();
				} else if (oc.equals(Double.class)) {
					return (char) ((Double) o).longValue();
				} else if (oc.equals(Float.class)) {
					return (char) ((Float) o).longValue();
				} else if (oc.equals(Long.class)) {
					return (char) ((Long) o).intValue();
				} else if (oc.equals(Byte.class)) {
					return (char) ((Byte) o).longValue();
				} else if (oc.equals(String.class)) {
					if (o.toString().length() > 0) {
						return o.toString().charAt(0);
					} else {
						return (char) 0;
					}
				}
			} else if (c.equals(byte.class) || c.equals(Byte.class)) {
				if (oc.equals(Byte.class)) {
					return o;
				} else if (oc.equals(Integer.class)) {
					return ((Integer) o).byteValue();
				} else if (oc.equals(Double.class)) {
					return ((Double) o).byteValue();
				} else if (oc.equals(Float.class)) {
					return ((Float) o).byteValue();
				} else if (oc.equals(Long.class)) {
					return ((Long) o).byteValue();
				} else if (oc.equals(Character.class)) {
					return (byte) ((int) ((Character) o).charValue());
				} else if (oc.equals(String.class)) {
					if (o.toString().length() > 0) {
						return o.toString().getBytes()[0];
					} else {
						return (char) 0;
					}
				}
			} else if (c.equals(float.class) || c.equals(Float.class)) {
				if (oc.equals(Byte.class)) {
					return o;
				} else if (oc.equals(Integer.class)) {
					return ((Integer) o).floatValue();
				} else if (oc.equals(Double.class)) {
					return ((Double) o).floatValue();
				} else if (oc.equals(Long.class)) {
					return ((Long) o).floatValue();
				} else if (oc.equals(Byte.class)) {
					return ((Byte) o).floatValue();
				} else if (oc.equals(Character.class)) {
					return (float) ((int) ((Character) o).charValue());
				} else if (oc.equals(String.class)) {
					return Convert.toFloat(o, 0.0f);
				}
			} else if (c.equals(double.class) || c.equals(Double.class)) {
				if (oc.equals(Byte.class)) {
					return o;
				} else if (oc.equals(Integer.class)) {
					return ((Integer) o).doubleValue();
				} else if (oc.equals(Float.class)) {
					return ((Float) o).doubleValue();
				} else if (oc.equals(Long.class)) {
					return ((Long) o).doubleValue();
				} else if (oc.equals(Byte.class)) {
					return ((Byte) o).doubleValue();
				} else if (oc.equals(Character.class)) {
					return (double) ((int) ((Character) o).charValue());
				} else if (oc.equals(String.class)) {
					return Convert.toDouble(o, 0.0f);
				}
			} else if (c.equals(Date.class)) {
				if (oc.equals(Integer.class)) {
					return new Date(((Integer) o).longValue() * 1000);
				} else if (oc.equals(Long.class)) {
					return new Date((Long) o);
				} else if (oc.equals(String.class)) {
					return sdf.parse(o.toString());
				}
			}
		} catch (Exception e) {
		}
		return o;
	}
}
