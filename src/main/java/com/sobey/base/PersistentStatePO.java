package com.sobey.base;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sobey.base.util.ClassUtils;
import com.sobey.base.util.DataInputBuffer;
import com.sobey.base.util.DataOutputBuffer;
import com.sobey.base.util.DataSerializable;
import com.sobey.base.util.StringUtils;
import com.sobey.base.util.ToolUtil;
import com.sobey.jcg.support.jdbc.Column;
import com.sobey.jcg.support.log4j.LogUtils;

/**
 * 为数据更新提供状态，方便客户端只更新少量数据而使用PO对象
 * 
 * @author hans
 * 
 */
public abstract class PersistentStatePO {

	public static final byte SerializableTypeCode = 125;
	protected boolean isNew;
	protected BitSet dirtyBits;
	protected BitSet readableBits;
	protected Map<String, Integer> fieldsMap = null;
	protected String[] fieldNames = null;
	protected Field[] fields = null;
	protected Map<String, Method[]> fieldsMethod = null;
	protected Map<String, Integer> fieldsMethodRel = null;
	private static Class<Column> columnClazz = Column.class;
	public static long updateTime = System.currentTimeMillis();

	protected static Map<Class<?>, Map<String, Integer>> FILEDS_MAP = new HashMap<Class<?>, Map<String, Integer>>();
	protected static Map<Class<?>, String[]> FILEDS_NAME = new HashMap<Class<?>, String[]>();
	protected static Map<Class<?>, Field[]> FILEDS = new HashMap<Class<?>, Field[]>();
	public static Map<Class<?>, Map<String, Method[]>> FILED_METHOD = new HashMap<Class<?>, Map<String, Method[]>>();
	protected static Map<Class<?>, Map<String, Integer>> FILED_METHOD_REL = new HashMap<Class<?>, Map<String, Integer>>();
	protected static Map<Integer, Class<? extends PersistentStatePO>> TYPE_REL = new HashMap<Integer, Class<? extends PersistentStatePO>>();
	protected static Map<Class<? extends PersistentStatePO>, Integer> TYPE_NREL = new HashMap<Class<? extends PersistentStatePO>, Integer>();

	static {
		init();
	}

	private static void init() {
		List<Class<?>> clazzs = ClassUtils.getAllClassByInterface(PersistentStatePO.class, "com.tydic.angel");
		for (Class class1 : clazzs) {
			int typeCode = getMyType(class1);
			TYPE_NREL.put(class1, Integer.valueOf(typeCode));
			LogUtils.info("PO class " + class1.getName() + "   typeId: " + typeCode);
			putInitClass(class1);
		}
	}

	public static void putInitClass(Class<?> class1) {
		Map filedMap = new HashMap();
		Field[] _fields = class1.getDeclaredFields();

		int fieldLength = 0;
		List filedsName = new ArrayList();
		List fields = new ArrayList();
		Map filedMethod = new HashMap();
		Map filedMethodRel = new HashMap();
		for (Field fd : _fields)
			if (!Modifier.isStatic(fd.getModifiers())) {
				fd.setAccessible(true);
				String fieldName = fd.getName();
				String setmethodName = StringUtils
				        .join(new String[] { "set", String.valueOf(fieldName.charAt(0)).toUpperCase(), StringUtils.substring(fieldName, 1) });
				String getmethodName = StringUtils
				        .join(new String[] { "get", String.valueOf(fieldName.charAt(0)).toUpperCase(), StringUtils.substring(fieldName, 1) });
				if ((fieldName.startsWith("is")) && (fd.getType().equals(Boolean.TYPE)) && (Character.isUpperCase(fieldName.charAt(2)))) {
					setmethodName = "set" + fieldName.substring(2);
					getmethodName = fieldName;
				}
				Method[] method = new Method[2];
				method[0] = getMethod(class1, setmethodName, fd.getType());
				method[1] = getMethod(class1, getmethodName);
				if ((method[0] != null) && (method[1] != null)) {
					fields.add(fd);
					String columnName = null;
					if (fd.isAnnotationPresent(columnClazz))
						columnName = ((Column) fd.getAnnotation(columnClazz)).value().toUpperCase();
					else {
						columnName = fieldName.toUpperCase();
					}
					filedsName.add(columnName);
					filedMap.put(columnName, Integer.valueOf(fieldLength));
					filedMethod.put(columnName, method);
					filedMethodRel.put(setmethodName, Integer.valueOf(fieldLength));
					filedMethodRel.put(getmethodName, Integer.valueOf(fieldLength));
					fieldLength++;
				}
			}
		FILEDS_MAP.put(class1, filedMap);
		FILEDS_NAME.put(class1, (String[]) filedsName.toArray(new String[0]));
		FILED_METHOD.put(class1, filedMethod);
		FILED_METHOD_REL.put(class1, filedMethodRel);
		FILEDS.put(class1, (Field[]) fields.toArray(new Field[0]));
	}

	public PersistentStatePO() {
		if (!FILEDS_MAP.containsKey(getClass())) {
			putInitClass(getClass());
		}
		this.fieldsMap = ((Map) FILEDS_MAP.get(getClass()));
		this.fieldNames = ((String[]) FILEDS_NAME.get(getClass()));
		this.fieldsMethod = ((Map) FILED_METHOD.get(getClass()));
		this.fieldsMethodRel = ((Map) FILED_METHOD_REL.get(getClass()));
		this.fields = ((Field[]) FILEDS.get(getClass()));
		setManagedPersistent();
	}

	public static Map<String, Integer> getFieldNameRels(Class<? extends PersistentStatePO> po) {
		return (Map) FILEDS_MAP.get(po);
	}

	public static String[] getFieldNames(Class<? extends PersistentStatePO> po) {
		return (String[]) FILEDS_NAME.get(po);
	}

	public static Map<String, Method[]> getFieldSetGetMethods(Class<? extends PersistentStatePO> po) {
		return (Map) FILED_METHOD.get(po);
	}

	public static Map<String, Integer> getFieldSetGetMethodRel(Class<? extends PersistentStatePO> po) {
		return (Map) FILED_METHOD_REL.get(po);
	}

	public static Field[] getFields(Class<? extends PersistentStatePO> po) {
		return (Field[]) FILEDS.get(po);
	}

	static Method getMethod(Class<?> class1, String methodName) {
		Method method = null;
		try {
			method = class1.getMethod(methodName, new Class[0]);
		} catch (Exception e) {
			LogUtils.error(ToolUtil.getCallMethodName(1), e);
		}
		return method;
	}

	static Method getMethod(Class<?> class1, String methodName, Class<?> argType) {
		Method method = null;
		try {
			method = class1.getMethod(methodName, new Class[] { argType });
		} catch (Exception e) {
			LogUtils.error(ToolUtil.getCallMethodName(1), e);
		}
		return method;
	}

	public void setManagedPersistent() {
		dirtyBits = new BitSet(fieldNames.length);
		readableBits = new BitSet(fieldNames.length);
		isNew = true;
	}

	public String[] getFields() {
		return this.fieldNames;
	}

	public String getField(int index) {
		return this.fieldNames[index];
	}

	public int getFieldIndex(String field) {
		return fieldsMap.get(field);
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

	protected boolean setValue(Object obj) {
		String setMethodName = Thread.currentThread().getStackTrace()[2].getMethodName();
		Integer colIndex = fieldsMethodRel.get(setMethodName);
		Method[] getset = this.fieldsMethod.get(this.fieldNames[colIndex]);
		// System.out.println("colName=" + this.fieldNames[colIndex]);
		// System.out.println("getFieldIndex=" + colIndex);
		// System.out.println("setMethodName=" + setMethodName);
		try {
			Object old = getset[1].invoke(this);
			if (!isFieldEqual(old, obj)) {
				this.setDirty(colIndex);
				this.fields[colIndex].set(this, obj);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private static int getMyType(Class<? extends PersistentStatePO> clazz) {
		return markCode(clazz, clazz.getName().hashCode(), 0, 0, (String[]) FILEDS_NAME.get(clazz), (Field[]) FILEDS.get(clazz));
	}

	private static synchronized int markCode(Class<? extends PersistentStatePO> clazz, int code, int i, int type, String[] fieldNames, Field[] fields) {
		if (TYPE_REL.containsKey(Integer.valueOf(code))) {
			if (!clazz.equals(TYPE_REL.get(Integer.valueOf(code)))) {
				if (type == 0) {
					if (i >= fieldNames.length) {
						return markCode(clazz, code, 0, ++type, fieldNames, fields);
					}
					code ^= fieldNames[i].hashCode();
					return markCode(clazz, code, ++i, type, fieldNames, fields);
				}
				if (type == 1) {
					if (i >= fields.length) {
						return markCode(clazz, code, ++i, ++type, fieldNames, fields);
					}
					code ^= fields[i].hashCode();
					return markCode(clazz, code, ++i, type, fieldNames, fields);
				}
				code = code * i * type;
				return markCode(clazz, code, ++i, type, fieldNames, fields);
			}

			return code;
		}

		synchronized (TYPE_REL) {
			TYPE_REL.put(Integer.valueOf(code), clazz);
			return code;
		}
	}

	public String getClassName() {
		return getClass().getName();
	}

	public static Class<? extends PersistentStatePO> getTypeClass(int type) {
		return (Class) TYPE_REL.get(Integer.valueOf(type));
	}

	public static int getClassType(Class<?> PO) {
		return ((Integer) TYPE_NREL.get(PO)).intValue();
	}

	public static Map<Integer, Class<? extends PersistentStatePO>> getClassRel() {
		return TYPE_REL;
	}

	public void writeToBytes(DataOutputBuffer destBuffer) throws IOException {
		DataSerializable.writeToBytes(toMap(), destBuffer);
	}

	public void readFromBytes(DataInputBuffer srcBuffer) throws IOException {
		setFromMap((Map) DataSerializable.readFromBytes(srcBuffer));
	}

	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		for (String fieldName : fieldsMethod.keySet()) {
			Method[] getset = fieldsMethod.get(fieldName);
			Object old = null;
			try {
				old = getset[1].invoke(this);
			} catch (Exception e) {
			}
			map.put(fieldName, old);
		}
		return map;
	}

	// 需要设置标识位
	public void setFromMap(Map<String, Object> map) {
		for (String fieldName : fieldsMethod.keySet()) {
			Object val = map.get(fieldName);
			if (val != null) {
				Method[] getset = fieldsMethod.get(fieldName);
				Integer colIndex = fieldsMethodRel.get(getset[0].getName());
				try {
					Object old = getset[1].invoke(this);
					Class<?> cl = this.fields[colIndex].getType();
					val = BaseTool.castObject(cl, val);
					if (!isFieldEqual(old, val)) {
						this.setDirty(colIndex);
						this.fields[colIndex].set(this, val);
					}
				} catch (Exception e) {
				}
			}
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < this.fieldNames.length; i++) {
			String fieldName = this.fieldNames[i];
			Method[] setGet = (Method[]) this.fieldsMethod.get(fieldName);
			Object fieldVal = null;
			try {
				fieldVal = setGet[1].invoke(this);
			} catch (IllegalAccessException e) {
				LogUtils.error(getClassName() + " toString", e);
			} catch (IllegalArgumentException e) {
				LogUtils.error(getClassName() + " toString", e);
			} catch (InvocationTargetException e) {
				LogUtils.error(getClassName() + " toString", e);
			}
			sb.append(fieldName);
			sb.append("=");
			sb.append(fieldVal == null ? "null" : fieldVal.toString());
			sb.append(",");
		}
		return sb.toString();
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

	public static <T extends PersistentStatePO> T paseFromJSONObject(Map<String, Object> map, Class<T> PO) {
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

	public static class aaa extends PersistentStatePO {

		long COMPANY_ID;
		String COMPANY_NAME;

		public long getCOMPANY_ID() {
			return COMPANY_ID;
		}

		public void setCOMPANY_ID(long cOMPANY_ID) {
			setValue(cOMPANY_ID);
		}

		public String getCOMPANY_NAME() {
			return COMPANY_NAME;
		}

		public void setCOMPANY_NAME(String cOMPANY_NAME) {
			// COMPANY_NAME = cOMPANY_NAME;
			setValue(cOMPANY_NAME);
		}
	}

	public static void main(String args[]) {
		aaa a = new aaa();
		System.err.println(PersistentStatePO.class.isAssignableFrom(aaa.class));

		for (int i = 0; i < a.fieldNames.length; i++) {
			System.out.println(a.fieldNames[i] + ":" + i);
		}
		// a.getCOMPANY_ID(111);
		a.setCOMPANY_NAME("aaaaaaaaaaaa");
		System.out.println("getCOMPANY_NAME=" + a.getCOMPANY_NAME());

	}

}
