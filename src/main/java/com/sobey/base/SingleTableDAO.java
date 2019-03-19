package com.sobey.base;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sobey.base.exception.NoMatchException;
import com.sobey.base.exception.POException;
import com.sobey.base.util.StringUtils;
import com.sobey.base.util.ToolUtil;
import com.sobey.jcg.support.jdbc.Column;
import com.sobey.jcg.support.jdbc.DataAccess;
import com.sobey.jcg.support.jdbc.JdbcException;
import com.sobey.jcg.support.sys.podo.BaseDAO;
import com.sobey.jcg.support.utils.Convert;

public class SingleTableDAO<T> extends BaseDAO {
	protected DataAccess access = null;

	SingleTableDAO<T> stDao = null;
	Class<T> PO;
	String tableName;
	String keyName;
	String seqName;
	private Map<String, Method[]> colFields = new HashMap<String, Method[]>();
	private Class<Column> columnClazz = Column.class;
	String insertSQL;
	String updateSQL;
	String deleteSQL;
	String selectSQL;
	static HashMap<String, SingleTableDAO> cacheDao = new HashMap();

	public static Map<String, SingleTableDAO.SeqValue> seqMutex = new HashMap();

	public static class SeqValue {
		public long seq;
		public List<String> USETABLE_FIELDS;
	}

	public DataAccess getDataAccess() {
		if (this.access == null) {
			this.access = super.getDataAccess();
		}
		return this.access;
	}

	public void setAccess(DataAccess access) {
		this.access = access;
	}

	public String getTableName() {
		return this.stDao.tableName;
	}

	public String getSeqName() {
		return this.stDao.seqName;
	}

	public String getUpdateSQL() {
		return this.stDao.updateSQL;
	}

	public String getSelectSQL() {
		return this.stDao.selectSQL;
	}

	public SingleTableDAO(Class<T> PO, String tableName, String keyName) throws POException {
		this(PO, tableName, keyName, "SEQ_" + keyName);
	}

	protected SingleTableDAO(Class<T> PO, String tableName, String keyName, String seqName) throws POException {
		tableName = tableName.toUpperCase();
		keyName = keyName.toUpperCase();
		synchronized (cacheDao) {
			this.stDao = ((SingleTableDAO) cacheDao.get(tableName + "." + keyName));
		}
		if (this.stDao == null) {
			synchronized (cacheDao) {
				this.stDao = this;
				cacheDao.put(tableName, this.stDao);
			}
			synchronized (this.stDao) {
				SingleTableDAO.SeqValue seqv = null;
				synchronized (seqMutex) {
					seqv = (SingleTableDAO.SeqValue) seqMutex.get(seqName);
					if (seqv == null) {
						seqv = new SingleTableDAO.SeqValue();
						seqMutex.put(seqName, seqv);
					}
				}
				synchronized (seqv) {
					if (!seqv.USETABLE_FIELDS.contains(tableName + "." + keyName)) {
						seqv.USETABLE_FIELDS.add(tableName + "." + keyName);
					}
				}
				this.stDao.PO = PO;
				this.stDao.tableName = tableName.toUpperCase();
				this.stDao.keyName = keyName.toUpperCase();
				this.stDao.seqName = seqName.toUpperCase();
				if (PersistentStatePO.class.isAssignableFrom(PO)) {
					this.colFields = ((Map) PersistentStatePO.FILED_METHOD.get(PO));
					if (this.colFields == null)
						try {
							PO.newInstance();
							this.colFields = ((Map) PersistentStatePO.FILED_METHOD.get(PO));
						} catch (InstantiationException e) {
							throw new POException(e);
						} catch (IllegalAccessException e) {
							throw new POException(e);
						}
				} else {
					Field[] fields = PO.getDeclaredFields();
					for (Field field : fields)
						if (!Modifier.isStatic(field.getModifiers())) {
							field.setAccessible(true);
							String fieldName = field.getName();
							String setmethodName = StringUtils
							        .join(new String[] { "set", String.valueOf(fieldName.charAt(0)).toUpperCase(), StringUtils.substring(fieldName, 1) });
							String getmethodName = StringUtils
							        .join(new String[] { "get", String.valueOf(fieldName.charAt(0)).toUpperCase(), StringUtils.substring(fieldName, 1) });
							if ((fieldName.startsWith("is")) && (field.getType().equals(Boolean.TYPE)) && (Character.isUpperCase(fieldName.charAt(2)))) {
								setmethodName = "set" + fieldName.substring(2);
								getmethodName = fieldName;
							}
							Method[] method = new Method[2];
							method[0] = getMethod(setmethodName, field.getType());
							method[1] = getMethod(getmethodName);
							if ((method[0] != null) && (method[1] != null)) {
								String columnName = null;
								if (field.isAnnotationPresent(this.columnClazz))
									columnName = ((Column) field.getAnnotation(this.columnClazz)).value().toUpperCase();
								else {
									columnName = field.getName().toUpperCase();
								}
								if ((method[0] != null) || (method[1] != null))
									this.colFields.put(columnName, method);
							}
						}
				}
				if (!this.colFields.containsKey(keyName)) {
					throw new POException("初始化失败：Bean" + PO.getCanonicalName() + "中不包含主键ID" + keyName);
				}
				this.insertSQL = ("insert into " + this.stDao.tableName + "(");
				this.updateSQL = ("update " + this.stDao.tableName + " set  ");
				this.deleteSQL = ("delete from " + this.stDao.tableName + " where " + this.stDao.keyName + " =?");
				this.selectSQL = "select ";
				int index = 0;
				for (String fname : this.colFields.keySet())
					if (!fname.equals(this.stDao.keyName)) {
						this.insertSQL = (this.insertSQL + fname + ",");
						this.selectSQL = (this.selectSQL + fname + ",");
						this.updateSQL = (this.updateSQL + fname + "=? ,");
						index++;
					}
				this.updateSQL = this.updateSQL.substring(0, this.updateSQL.length() - 1);
				this.insertSQL = (this.insertSQL + this.stDao.keyName + ") values(");
				for (int i = 0; i < index; i++) {
					this.insertSQL += "?,";
				}
				this.insertSQL += "?)";
				this.updateSQL = (this.updateSQL + " where " + this.stDao.keyName + " =?");
				this.selectSQL = (this.selectSQL + this.stDao.keyName + " from  " + this.stDao.tableName);
			}
		}
	}

	private Method getMethod(String methodName) {
		Method method = null;
		try {
			method = this.stDao.PO.getMethod(methodName, new Class[0]);
		} catch (Exception e) {
			throw new JdbcException(e);
		}
		return method;
	}

	private Method getMethod(String methodName, Class<?> argType) {
		Method method = null;
		try {
			method = this.stDao.PO.getMethod(methodName, new Class[] { argType });
		} catch (Exception e) {
			throw new JdbcException(e);
		}
		return method;
	}

	public boolean insertPO(T po) {
		Object[] params = new Object[this.stDao.colFields.size()];
		int index = 0;
		for (String fname : this.stDao.colFields.keySet()) {
			if (!fname.equals(this.stDao.keyName)) {
				Object obj = invokeMethod(po, ((Method[]) this.stDao.colFields.get(fname))[1]);
				if ((obj instanceof Date))
					params[(index++)] = new Timestamp(((Date) obj).getTime());
				else
					params[(index++)] = obj;
			}
		}
		long id = BaseDao.createAndGetSeq(this.stDao.seqName, 1);
		params[(index++)] = Long.valueOf(id);
		invokeMethod(po, ((Method[]) this.stDao.colFields.get(this.stDao.keyName))[0], Long.valueOf(id));
		return getDataAccess().execNoQuerySql(this.stDao.insertSQL, params);
	}

	public boolean insertPO(T po, long id) throws SQLException {
		Object[] params = new Object[this.stDao.colFields.size()];
		int index = 0;
		for (String fname : this.stDao.colFields.keySet()) {
			if (!fname.equals(this.stDao.keyName)) {
				Object obj = invokeMethod(po, ((Method[]) this.colFields.get(fname))[1]);
				if ((obj instanceof Date))
					params[(index++)] = new Timestamp(((Date) obj).getTime());
				else
					params[(index++)] = obj;
			}
		}
		params[(index++)] = Long.valueOf(id);
		invokeMethod(po, ((Method[]) this.stDao.colFields.get(this.stDao.keyName))[0], Long.valueOf(id));

		return getDataAccess().execNoQuerySql(this.stDao.insertSQL, params);
	}

	public boolean updatePO(T po) {
		boolean s;
		if ((po instanceof PersistentStatePO)) {
			PersistentStatePO spo = (PersistentStatePO) po;
			if (!spo.isDirty())
				return true;
			String updateSQL = "update " + this.stDao.tableName + " set  ";
			List paramsList = new ArrayList();
			s = true;
			boolean haveUpdate = false;
			for (int i = 0; i < spo.fieldNames.length; i++) {
				if (!spo.fieldNames[i].equals(this.stDao.keyName)) {
					if (spo.isDirty(i)) {
						haveUpdate = true;
						if (s) {
							updateSQL = updateSQL + spo.fieldNames[i] + "=?";
							s = false;
						} else {
							updateSQL = updateSQL + "," + spo.fieldNames[i] + "=?";
						}
						Object obj = invokeMethod(po, ((Method[]) this.stDao.colFields.get(spo.fieldNames[i]))[1]);
						if ((obj instanceof Date))
							paramsList.add(new Timestamp(((Date) obj).getTime()));
						else
							paramsList.add(obj);
					}
				}
			}
			if (haveUpdate) {
				paramsList.add(invokeMethod(po, ((Method[]) this.stDao.colFields.get(this.stDao.keyName))[1]));
				updateSQL = updateSQL + " where " + this.stDao.keyName + " =?";
				return getDataAccess().execNoQuerySql(this.stDao.updateSQL, paramsList.toArray());
			}
			return true;
		}

		Object[] params = new Object[this.stDao.colFields.size()];
		int index = 0;
		for (String fname : this.stDao.colFields.keySet()) {
			if (!fname.equals(this.stDao.keyName)) {
				Object obj = invokeMethod(po, ((Method[]) this.stDao.colFields.get(fname))[1]);
				if ((obj instanceof Date))
					params[(index++)] = new Timestamp(((Date) obj).getTime());
				else
					params[(index++)] = obj;
			}
		}
		params[(index++)] = invokeMethod(po, ((Method[]) this.stDao.colFields.get(this.stDao.keyName))[1]);
		return getDataAccess().execNoQuerySql(this.stDao.updateSQL, params);
	}

	public boolean deletePO(T po) throws SQLException {
		return getDataAccess().execNoQuerySql(this.stDao.deleteSQL,
		        new Object[] { invokeMethod(po, ((Method[]) this.stDao.colFields.get(this.stDao.keyName))[1]) });
	}

	public boolean deletePO(String id) throws SQLException {
		return getDataAccess().execNoQuerySql(this.stDao.deleteSQL, new Object[] { id });
	}

	public boolean deletePO(long id) throws SQLException {
		return getDataAccess().execNoQuerySql(this.stDao.deleteSQL, new Object[] { Long.valueOf(id) });
	}

	public T queryPO(String id) throws SQLException {
		T tpo = getDataAccess().queryForBean(this.stDao.selectSQL + " where " + this.stDao.keyName + " =?", this.stDao.PO, new Object[] { id });
		if ((PersistentStatePO.class.isAssignableFrom(this.stDao.PO)) && (tpo != null)) {
			PersistentStatePO stpo = (PersistentStatePO) tpo;
			stpo.clearDirty();
		}
		return tpo;
	}

	public List<T> queryPOList(long[] ids) throws SQLException {
		String _selectSQL = this.stDao.selectSQL + " where " + this.stDao.keyName + " in( " + ToolUtil.join(ids, ",") + " )";
		List listPO = getDataAccess().queryForBeanList(_selectSQL, this.stDao.PO, new Object[0]);
		if (PersistentStatePO.class.isAssignableFrom(this.stDao.PO)) {
			for (Object tpo : listPO) {
				PersistentStatePO stpo = (PersistentStatePO) tpo;
				stpo.clearDirty();
			}
		}
		return listPO;
	}

	public List<T> queryAll() throws SQLException {
		List listPO = getDataAccess().queryForBeanList(this.stDao.selectSQL, this.stDao.PO, new Object[0]);
		if (PersistentStatePO.class.isAssignableFrom(this.stDao.PO)) {
			for (Object tpo : listPO) {
				PersistentStatePO stpo = (PersistentStatePO) tpo;
				stpo.clearDirty();
			}
		}
		return listPO;
	}

	public List<T> queryPOList(String where, Object... params) {
		String _selectSQL = this.stDao.selectSQL;
		if (where != null) {
			_selectSQL = _selectSQL + " where " + where;
		}
		List listPO = null;
		if ((where != null) && (params != null) && (params.length > 0))
			listPO = getDataAccess().queryForBeanList(_selectSQL, this.stDao.PO, params);
		else
			listPO = getDataAccess().queryForBeanList(_selectSQL, this.stDao.PO, new Object[0]);
		if (PersistentStatePO.class.isAssignableFrom(this.stDao.PO)) {
			for (Object tpo : listPO) {
				PersistentStatePO stpo = (PersistentStatePO) tpo;
				stpo.clearDirty();
			}
		}
		return listPO;
	}

	public List<T> queryOrderList(String orderstr, String where, Object[] params) throws SQLException {
		String _selectSQL = this.stDao.selectSQL;
		if ((where != null) && (!where.trim().equals(""))) {
			_selectSQL = _selectSQL + " where " + where;
		}
		if ((orderstr != null) && (!orderstr.trim().equals(""))) {
			_selectSQL = _selectSQL + " order by " + orderstr;
		}
		List listPO = null;
		if ((where != null) && (params != null) && (params.length > 0))
			listPO = getDataAccess().queryForBeanList(_selectSQL, this.stDao.PO, params);
		else
			listPO = getDataAccess().queryForBeanList(_selectSQL, this.stDao.PO, new Object[0]);
		if (PersistentStatePO.class.isAssignableFrom(this.stDao.PO)) {
			for (Object tpo : listPO) {
				PersistentStatePO stpo = (PersistentStatePO) tpo;
				stpo.clearDirty();
			}
		}
		return listPO;
	}

	public static Object invokeMethod(Object obj, Method method) {
		try {
			int plen = method.getParameterTypes().length;
			if (plen == 0) {
				return method.invoke(obj, new Object[0]);
			}
			throw new NoMatchException("反射调用方法与参数不匹配，对象：" + obj + "  方法：" + method);
		} catch (Exception e) {
			throw new NoMatchException(e);
		}
	}

	public static Object invokeMethod(Object obj, Method method, Object arg) {
		try {
			int plen = method.getParameterTypes().length;
			if (plen == 1) {
				Class clazz = method.getParameterTypes()[0];
				if (clazz.equals(Integer.TYPE))
					arg = Integer.valueOf(arg == null ? 0 : Convert.toInt(arg));
				else if (clazz.equals(Long.TYPE))
					arg = Long.valueOf(arg == null ? 0L : Convert.toLong(arg));
				else if (clazz.equals(Double.TYPE))
					arg = Double.valueOf(arg == null ? 0.0D : Convert.toDouble(arg));
				else if (clazz.equals(Float.TYPE))
					arg = Float.valueOf(arg == null ? 0.0F : Convert.toFloat(arg));
				else if (clazz.equals(Short.TYPE))
					arg = Short.valueOf(arg == null ? 0 : Convert.toShort(arg));
				else if (clazz.equals(Character.TYPE))
					arg = Character.valueOf(arg == null ? '\000' : Convert.toChar(arg));
				else if (clazz.equals(Byte.TYPE))
					arg = Byte.valueOf(arg == null ? 0 : Convert.toByte(arg));
				else if (clazz.equals(Boolean.TYPE))
					arg = arg == null ? Integer.valueOf(0) : Boolean.valueOf(Convert.toBool(arg));
				else {
					arg = arg == null ? null : Convert.convert(arg, clazz);
				}
				return method.invoke(obj, new Object[] { arg });
			}
			throw new NoMatchException("反射调用方法与参数不匹配，对象：" + obj + "  方法：" + method);
		} catch (Exception e) {
			throw new NoMatchException(e);
		}
	}
}