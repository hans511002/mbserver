package com.sobey.base;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sobey.base.annotation.DBName;
import com.sobey.base.annotation.DBTable;
import com.sobey.base.annotation.ID;
import com.sobey.base.annotation.KEY;
import com.sobey.base.annotation.Length;
import com.sobey.base.annotation.Seq;
import com.sobey.base.util.ClassUtils;
import com.sobey.base.util.DataInputBuffer;
import com.sobey.base.util.DataOutputBuffer;
import com.sobey.base.util.DataSerializable;
import com.sobey.base.util.ToolUtil;
import com.sobey.jcg.support.jdbc.Column;
import com.sobey.jcg.support.log4j.LogUtils;

/**
 * 为数据更新提供状态，方便客户端只更新少量数据而使用PO对象
 * 
 * @author hans
 * 
 */
public abstract class PersistentStatePO implements java.io.Serializable {
	public static final byte SerializableTypeCode = 125;
	protected boolean isNew;
	protected BitSet dirtyBits;
	protected BitSet readableBits;
	final PPOAttr attr;

	private static Class<DBName> dbClazz = DBName.class;
	private static Class<DBTable> tableClazz = DBTable.class;
	private static Class<ID> idClazz = ID.class;
	private static Class<Column> columnClazz = Column.class;
	private static Class<Seq> seqClazz = Seq.class;
	private static Class<KEY> keyClazz = KEY.class;
	private static Class<Length> lengthClazz = Length.class;
	public static long updateTime = System.currentTimeMillis();

	// protected static Map<Class<?>, Map<String, Integer>> FILEDS_MAP = new HashMap<Class<?>, Map<String, Integer>>();
	// protected static Map<Class<?>, String[]> FILEDS_NAME = new HashMap<Class<?>, String[]>();
	// protected static Map<Class<?>, Field[]> FILEDS = new HashMap<Class<?>, Field[]>();
	// public static Map<Class<?>, Map<String, Method[]>> FILED_METHOD = new HashMap<Class<?>, Map<String, Method[]>>();
	// protected static Map<Class<?>, Map<String, Integer>> FILED_METHOD_REL = new HashMap<Class<?>, Map<String,
	// Integer>>();
	//
	//
	final protected static Map<Long, PPOAttr> ppoUidMap = new HashMap<Long, PPOAttr>();
	final protected static Map<Class<?>, PPOAttr> ppoAttrMap = new HashMap<Class<?>, PPOAttr>();

	public static class PPOAttr {
		Class<? extends PersistentStatePO> clazz;
		long serialVersionUID = 0;
		String dbName = "";
		String tableName = "";
		// String keyColName = "";
		String seqName = "";
		PPOField key;
		List<PPOField> fields = new ArrayList<PPOField>();
		// fieldName
		Map<String, PPOField> fieldsRel = new HashMap<String, PPOField>();
		// colName
		Map<String, PPOField> colRel = new HashMap<String, PPOField>();
		// get set method name
		Map<String, PPOField> methodRel = new HashMap<String, PPOField>();
		String selectSQL = "";
		String insertSQL = "";
		String updateSQL = "";
		String deleteSQL = "";

		public Class<? extends PersistentStatePO> getClazz() {
			return clazz;
		}

		public long getSerialVersionUID() {
			return serialVersionUID;
		}

		public String getDbName() {
			return dbName;
		}

		public String getTableName() {
			return tableName;
		}

		public String getSeqName() {
			return seqName;
		}

		public PPOField getKey() {
			return key;
		}

		public List<PPOField> getFields() {
			return fields;
		}

		public Map<String, PPOField> getFieldsRel() {
			return fieldsRel;
		}

		public Map<String, PPOField> getColRel() {
			return colRel;
		}

		public Map<String, PPOField> getMethodRel() {
			return methodRel;
		}

		public String getSelectSQL() {
			return selectSQL;
		}

		public String getInsertSQL() {
			return insertSQL;
		}

		public String getUpdateSQL() {
			return updateSQL;
		}

		public String getDeleteSQL() {
			return deleteSQL;
		}

		public String getColNameList() {
			return colNameList;
		}

		public String getNames() {
			if (!colNameList.isEmpty())
				return colNameList;
			for (PPOField ppoField : fields) {
				if (colNameList.isEmpty())
					colNameList = ppoField.columnName;
				else
					colNameList += "," + ppoField.columnName;
			}
			return colNameList;
		}

		String colNameList = "";

		public String getKeyName() {
			return this.tableName + "." + key.columnName;
		}

		public String getFullKeyName() {
			return key.columnName;
		}

		public List<String> getNameList() {
			List<String> names = new ArrayList<String>();
			for (PPOField ppoField : fields) {
				names.add(ppoField.columnName);
			}
			return names;
		}
	}

	public static class PPOField {
		PPOAttr poAttr;
		String fieldName = "";
		String tableName = "";
		String columnName = "";
		Method getMethod;
		Method setMethod;
		String getMethodName;
		String setMethodName;
		int colIndex = -1;
		int length = 0;
		Field fd;
		boolean isId = false;

		public String toString() {
			return columnName + " " + toDbType();
		}

		public String toDbType() {
			Class<?> c = fd.getType();
			if (c.equals(boolean.class) || c.equals(Boolean.class)) {
				return "bit";
			} else if (c.equals(int.class) || c.equals(Integer.class)) {
				return "int";
			} else if (c.equals(long.class) || c.equals(Long.class)) {
				return "bigint";
			} else if (c.equals(Date.class)) {
				return "DATETIME";
			} else if (c.equals(String.class)) {
				if (length <= 0) {
					length = 64;
				} else if (length >= 16 * 1 << 20) {
					return "longtext";
				} else if (length >= 64 * 1024) {
					return "mediumtext";
				} else if (length > 4000) {
					return "TEXT";
				}
				return "VARCHAR(" + length + ")";
			} else if (c.equals(Byte[].class) || c.equals(byte[].class)) {
				if (length <= 0) {
					length = 255;
					return "tinyblob";
				} else if (length >= 16 * 1 << 20) {
					return "longblob";
				} else if (length >= 64 * 1024) {
					return "mediumblob";
				}
				return "blob";
			}
			return "";
		}
	}

	static {
		init();
	}

	private static void init() {
		List<Class<?>> clazzs = ClassUtils.getAllClassByInterface(PersistentStatePO.class, "com.sobey");
		for (Class<?> class1 : clazzs) {
			putInitClass(class1, false);
		}
	}

	public static void putInitClass(Class<?> class1, boolean check) {
		synchronized (ppoAttrMap) {
			if (ppoAttrMap.containsKey(class1)) {
				return;
			}
			PPOAttr poAttr = new PPOAttr();
			Field[] _fields = class1.getDeclaredFields();
			String keyColName = "";
			if (class1.isAnnotationPresent(dbClazz)) {
				poAttr.dbName = ((DBName) class1.getAnnotation(dbClazz)).value().toUpperCase();
			}
			if (class1.isAnnotationPresent(tableClazz)) {
				poAttr.tableName = ((DBTable) class1.getAnnotation(tableClazz)).value().toUpperCase();
			}
			if (class1.isAnnotationPresent(seqClazz)) {
				poAttr.seqName = ((Seq) class1.getAnnotation(seqClazz)).value().toUpperCase();
			}
			if (class1.isAnnotationPresent(keyClazz)) {
				keyColName = ((KEY) class1.getAnnotation(keyClazz)).value().toUpperCase();
			}
			for (Field fd : _fields) {
				fd.setAccessible(true);
				if (Modifier.isStatic(fd.getModifiers())) {
					if (fd.getName().equals("serialVersionUID")) {
						try {
							poAttr.serialVersionUID = fd.getLong(class1);
						} catch (IllegalArgumentException | IllegalAccessException e) {
							LogUtils.error(e);
							if (check)
								throw new RuntimeException(e);
						}
					}
					continue;
				}
				PPOField field = new PPOField();
				field.fieldName = fd.getName();
				field.getMethodName = "get" + field.fieldName.substring(0, 1).toUpperCase() + field.fieldName.substring(1);
				field.setMethodName = "set" + field.fieldName.substring(0, 1).toUpperCase() + field.fieldName.substring(1);
				field.getMethod = getMethod(class1, field.getMethodName);
				field.setMethod = getMethod(class1, field.setMethodName, fd.getType());
				if (field.getMethod == null) {
					if (fd.getType().equals(Boolean.TYPE) && field.fieldName.startsWith("is") && Character.isUpperCase(field.fieldName.charAt(2))) {
						field.getMethodName = field.fieldName;
						field.getMethod = getMethod(class1, field.getMethodName);
						if (field.setMethod == null) {
							field.setMethodName = "set" + field.fieldName.substring(2);
							field.setMethod = getMethod(class1, field.setMethodName, fd.getType());
						}
					}
				}
				if ((field.getMethod == null) || (field.setMethod == null)) {
					continue;
				}
				if (fd.isAnnotationPresent(columnClazz))
					field.columnName = ((Column) fd.getAnnotation(columnClazz)).value().toUpperCase();
				else {
					field.columnName = field.fieldName.toUpperCase();
				}
				if (keyColName.isEmpty() && fd.isAnnotationPresent(keyClazz)) {
					keyColName = ((KEY) fd.getAnnotation(keyClazz)).value().toUpperCase();
				}
				if (field.columnName.equals(keyColName)) {
					field.isId = true;
					poAttr.key = field;
				} else if (fd.isAnnotationPresent(idClazz)) {
					if (!keyColName.isEmpty()) {
						if (check) {
							throw new RuntimeException("double key field " + field.columnName + " and " + keyColName);
						}
					}
					keyColName = field.columnName;
					field.isId = true;
					poAttr.key = field;
				}
				if (poAttr.seqName.isEmpty() && fd.isAnnotationPresent(seqClazz)) {
					poAttr.seqName = ((Seq) fd.getAnnotation(seqClazz)).value().toUpperCase();
				}
				if (fd.isAnnotationPresent(tableClazz))
					field.tableName = ((Column) fd.getAnnotation(tableClazz)).value().toUpperCase();
				if (fd.isAnnotationPresent(lengthClazz))
					field.length = ((Length) fd.getAnnotation(lengthClazz)).value();
				field.poAttr = poAttr;
				field.fd = fd;
				field.colIndex = poAttr.fields.size();
				poAttr.fields.add(field);
				poAttr.fieldsRel.put(field.fieldName, field);
				poAttr.colRel.put(field.columnName, field);
				poAttr.methodRel.put(field.getMethodName, field);
				poAttr.methodRel.put(field.setMethodName, field);
			}
			if (poAttr.serialVersionUID == 0) {
				LogUtils.warn("PO class " + class1.getName() + " not define serialVersionUID ");
				if (check)
					throw new RuntimeException("PO class " + class1.getName() + " not define serialVersionUID ");
				return;
			}
			if (poAttr.tableName.isEmpty()) {
				for (PPOField field : poAttr.fields) {
					if (!field.tableName.isEmpty()) {
						poAttr.tableName = field.tableName;
						break;
					}
				}
			}
			if (poAttr.seqName.isEmpty()) {
				for (PPOField field : poAttr.fields) {
					if (field.isId && (field.fd.getType().equals(int.class) || field.fd.getType().equals(Integer.class) || field.fd.getType().equals(long.class)
					        || field.fd.getType().equals(Long.class))) {
						poAttr.seqName = "SEQ_" + field.columnName;
						break;
					}
				}
			}
			if (poAttr.tableName.isEmpty()) {
				if (check)
					throw new RuntimeException("PO class " + class1.getName() + " not define tableName ");
				LogUtils.warn("PO class " + class1.getName() + " not define tableName");
				return;
			}
			if (poAttr.key == null) {
				if (check)
					throw new RuntimeException("PO class " + class1.getName() + "   not define key ");
				LogUtils.warn("PO class " + class1.getName() + " not define key");
				return;
			}
			keyColName = poAttr.key.columnName;
			for (PPOField field : poAttr.fields) {
				if (field.tableName.isEmpty()) {
					field.tableName = poAttr.tableName;
				}
			}
			LogUtils.info("PO class " + class1.getName() + " typeId: " + poAttr.serialVersionUID + " colNames:" + poAttr.getNames());
			ppoAttrMap.put(class1, poAttr);
			ppoUidMap.put(poAttr.serialVersionUID, poAttr);
			///////// build sql//////////
			String tableKeyColName = poAttr.tableName + "." + keyColName;
			SystemSeqService.getSeq(poAttr.seqName, tableKeyColName);
			poAttr.insertSQL = ("insert into " + poAttr.tableName + "(");
			poAttr.updateSQL = ("update " + poAttr.tableName + " set  ");
			poAttr.deleteSQL = ("delete from " + poAttr.tableName + " where " + keyColName + " =?");
			poAttr.selectSQL = "select ";
			int index = 0;
			for (PPOField ppf : poAttr.fields) {
				if (!ppf.isId) {
					poAttr.insertSQL = (poAttr.insertSQL + ppf.columnName + ",");
					poAttr.selectSQL = (poAttr.selectSQL + ppf.columnName + ",");
					poAttr.updateSQL = (poAttr.updateSQL + ppf.columnName + "=? ,");
					index++;
				}
			}
			poAttr.updateSQL = poAttr.updateSQL.substring(0, poAttr.updateSQL.length() - 1);
			poAttr.insertSQL = (poAttr.insertSQL + keyColName + ") values(");
			for (int i = 0; i < index; i++)
				poAttr.insertSQL += "?,";
			poAttr.insertSQL += "?)";
			poAttr.updateSQL = (poAttr.updateSQL + " where " + keyColName + " =?");
			poAttr.selectSQL = (poAttr.selectSQL + keyColName + " from  " + poAttr.tableName);
		}
	}

	public PersistentStatePO() {
		if (!ppoAttrMap.containsKey(getClass())) {
			putInitClass(getClass(), true);
		}
		this.attr = ppoAttrMap.get(getClass());

		setManagedPersistent();
	}

	public static PPOAttr getPpoAttr(Class<? extends PersistentStatePO> po) {
		PPOAttr attr = ppoAttrMap.get(po);
		if (attr == null)
			putInitClass(po, true);
		return attr;
	}

	public static PPOAttr getPpoAttr(long type) {
		return ppoUidMap.get(type);
	}

	static Method getMethod(Class<?> class1, String methodName, Class<?>... parameterTypes) {
		Method method = null;
		try {
			method = class1.getMethod(methodName, parameterTypes);
		} catch (Exception e) {
			LogUtils.error(ToolUtil.getCallMethodName(1), e);
		}
		return method;
	}

	public void setManagedPersistent() {
		dirtyBits = new BitSet(attr.fields.size());
		readableBits = new BitSet(attr.fields.size());
		isNew = true;
	}

	public PPOAttr getPpoAttr() {
		return this.attr;
	}

	public PPOField getField(int index) {
		return this.attr.fields.get(index);
	}

	public PPOField getField(String field) {
		return this.attr.fieldsRel.get(field);
	}

	public PPOField getCol(String colName) {
		return this.attr.colRel.get(colName);
	}

	public boolean isNew() {
		return this.isNew;
	}

	public void setNew() {
		this.isNew = true;
	}

	public void clearNew() {
		this.isNew = false;
	}

	public void setDirty(int fieldIndex) {
		this.dirtyBits.set(fieldIndex);
		this.readableBits.set(fieldIndex);
	}

	public boolean isDirty(int fieldIndex) {
		return this.dirtyBits.get(fieldIndex);
	}

	public boolean isDirty(PPOField ppf) {
		return this.dirtyBits.get(ppf.colIndex);
	}

	public boolean isDirty() {
		return !this.dirtyBits.isEmpty();
	}

	public void setDirty() {
		this.dirtyBits.set(0, this.dirtyBits.size());
	}

	public void clearDirty(int fieldIndex) {
		this.dirtyBits.clear(fieldIndex);
	}

	public void clearDirty() {
		this.dirtyBits.clear();
	}

	public void setReadable(int fieldIndex) {
		this.readableBits.set(fieldIndex);
	}

	public boolean isReadable(int fieldIndex) {
		return this.readableBits.get(fieldIndex);
	}

	public void clearReadable(int fieldIndex) {
		this.readableBits.clear(fieldIndex);
	}

	public void clearReadable() {
		this.readableBits.clear();
	}

	protected boolean isFieldEqual(Object old, Object value) {
		if ((old == null) && (value == null))
			return true;
		if ((old == null) || (value == null))
			return false;
		return value.equals(old);
	}

	public Object getKey() {
		return get(this.attr.key);
	}

	public Object get(PPOField ppf) {
		try {
			return ppf.getMethod.invoke(this);
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new RuntimeException(e);
			}
		}
	}

	public boolean set(PPOField ppf, Object obj) {
		try {
			Object old = ppf.getMethod.invoke(this);
			obj = BaseTool.castObject(ppf.fd.getType(), obj);
			if (!isFieldEqual(old, obj)) {
				this.setDirty(ppf.colIndex);
				ppf.fd.set(this, obj);
				return true;
			}
			return false;
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new RuntimeException(e);
			}
		}

	}

	protected boolean set(Object obj) {
		try {
			String setMethodName = Thread.currentThread().getStackTrace()[2].getMethodName();
			PPOField ppf = attr.methodRel.get(setMethodName);
			Object old = ppf.getMethod.invoke(this);
			obj = BaseTool.castObject(ppf.fd.getType(), obj);
			if (!isFieldEqual(old, obj)) {
				this.setDirty(ppf.colIndex);
				ppf.fd.set(this, obj);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public String getClassName() {
		return getClass().getName();
	}

	public void writeToBytes(DataOutputBuffer destBuffer) throws IOException {
		DataSerializable.writeToBytes(toMap(), destBuffer);
	}

	public void readFromBytes(DataInputBuffer srcBuffer) throws IOException {
		setFromMap((Map) DataSerializable.readFromBytes(srcBuffer));
	}

	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		for (PPOField ppf : attr.fields) {
			try {
				Object old = ppf.fd.get(this);
				map.put(ppf.fieldName, old);
			} catch (Throwable e) {
				if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				} else {
					throw new RuntimeException(e);
				}
			}
		}
		return map;
	}

	public long getSerUid() {
		return this.attr.serialVersionUID;
	}

	// 需要设置标识位
	public void setFromMap(Map<String, Object> map) {
		for (PPOField ppf : attr.fields) {
			Object val = map.get(ppf.fieldName);
			if (val != null) {
				try {
					Object old = ppf.getMethod.invoke(this);
					val = BaseTool.castObject(ppf.fd.getType(), val);
					if (!isFieldEqual(old, val)) {
						this.setDirty(ppf.colIndex);
						ppf.fd.set(this, val);
					}
				} catch (Exception e) {
					if (e instanceof RuntimeException) {
						throw (RuntimeException) e;
					} else {
						throw new RuntimeException(e);
					}
				}
			}
		}
	}

	public String toString() {
		return toMap().toString();
	}

	public static <T extends PersistentStatePO> T paseFromMap(Map<String, Object> map, Class<T> PO) {
		try {
			PersistentStatePO t = (PersistentStatePO) PO.newInstance();
			t.setFromMap(map);
			return (T) t;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Map<Long, String> getPPoTypes() {
		Map<Long, String> rel = new HashMap<Long, String>();
		for (Class<?> cl : ppoAttrMap.keySet()) {
			rel.put(ppoAttrMap.get(cl).serialVersionUID, cl.getCanonicalName());
		}
		return rel;
	}

	@DBTable("com")
	public static class aaa extends PersistentStatePO {
		private static final long serialVersionUID = -6844553238036909809L;
		@ID
		long COMPANY_ID;
		String COMPANY_NAME;

		public long getCOMPANY_ID() {
			return COMPANY_ID;
		}

		public void setCOMPANY_ID(long cOMPANY_ID) {
			set(cOMPANY_ID);
		}

		public String getCOMPANY_NAME() {
			return COMPANY_NAME;
		}

		public void setCOMPANY_NAME(String cOMPANY_NAME) {
			set(cOMPANY_NAME);
		}
	}

	public static void main(String args[]) {
		aaa a = new aaa();
		System.err.println(PersistentStatePO.class.isAssignableFrom(aaa.class));

		for (PPOField ppf : a.attr.fields) {
			System.err.println(ppf);
		}
		// a.getCOMPANY_ID(111);
		a.setCOMPANY_NAME("aaaaaaaaaaaa");
		System.out.println("getCOMPANY_NAME=" + a.getCOMPANY_NAME());

	}

}
