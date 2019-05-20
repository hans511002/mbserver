package com.sobey.base;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sobey.base.PersistentStatePO.PPOAttr;
import com.sobey.base.PersistentStatePO.PPOField;
import com.sobey.base.exception.POException;
import com.sobey.base.util.ToolUtil;
import com.sobey.jcg.support.jdbc.DataAccess;
import com.sobey.jcg.support.sys.podo.BaseDAO;

public class SingleTableDAO<T extends PersistentStatePO> extends BaseDAO {
	final PPOAttr attr;

	// SingleTableDAO<T> stDao = null;
	// Class<T> PO;
	// String tableName;
	// String keyName;
	// String seqName;
	// private Map<String, Method[]> colFields = new HashMap<String, Method[]>();
	// private Class<Column> columnClazz = Column.class;
	// String insertSQL;
	// String updateSQL;
	// String deleteSQL;
	// String selectSQL;
	// static HashMap<String, SingleTableDAO> cacheDao = new HashMap<String, SingleTableDAO>();
	public DataAccess getDataAccess() {
		return super.getDataAccess();
	}

	public PPOAttr getPPOAttr() {
		return this.attr;
	}

	public String getTableName() {
		return this.attr.tableName;
	}

	public String getSeqName() {
		return this.attr.seqName;
	}

	public String getUpdateSQL() {
		return this.attr.updateSQL;
	}

	public String getSelectSQL() {
		return this.attr.selectSQL;
	}

	protected SingleTableDAO(Class<T> PO) throws POException {
		this.attr = PersistentStatePO.getPpoAttr(PO);
	}

	public boolean insertPO(T po) {
		Object[] params = new Object[po.attr.fields.size()];
		int index = 0;
		for (PPOField ppf : po.attr.fields) {
			if (!ppf.isId) {
				Object obj = po.get(ppf);// invokeMethod(po, ppf.getMethod);
				if ((obj instanceof Date))
					params[(index++)] = new Timestamp(((Date) obj).getTime());
				else
					params[(index++)] = obj;
			}
		}
		if (!po.attr.seqName.isEmpty()) {
			long id = SystemSeqService.createAndGetSeq(po.attr.seqName, 1, po.attr.getFullKeyName());
			params[(index++)] = Long.valueOf(id);
			po.set(po.attr.key, id);
		} else {
			params[(index++)] = po.get(po.attr.key);
		}
		return getDataAccess().execNoQuerySql(po.attr.insertSQL, params);
	}

	public boolean insertPO(T po, long id) throws SQLException {
		Object[] params = new Object[this.attr.fields.size()];
		int index = 0;
		for (PPOField ppf : this.attr.fields) {
			if (!ppf.isId) {
				Object obj = po.get(ppf);// invokeMethod(po, ppf.getMethod);
				if ((obj instanceof Date))
					params[(index++)] = new Timestamp(((Date) obj).getTime());
				else
					params[(index++)] = obj;
			}
		}
		params[(index++)] = Long.valueOf(id);
		po.set(po.attr.key, id);
		return getDataAccess().execNoQuerySql(this.attr.insertSQL, params);
	}

	public boolean updatePO(T po) {
		return updatePO(po, false);
	}

	public boolean updatePO(T po, boolean allField) {
		if (!po.isDirty())
			return true;
		String updateSQL = allField ? po.attr.updateSQL : "update " + this.attr.tableName + " set  ";
		List<Object> paramsList = new ArrayList<Object>();
		boolean haveUpdate = allField;
		for (PPOField ppf : po.attr.fields) {
			if (!ppf.isId) {
				if (allField) {
					Object obj = po.get(ppf);
					if ((obj instanceof Date))
						paramsList.add(new Timestamp(((Date) obj).getTime()));
					else
						paramsList.add(obj);
				} else if (po.isDirty(ppf)) {
					if (haveUpdate) {
						updateSQL = updateSQL + "," + ppf.columnName + "=?";
					} else {
						updateSQL = updateSQL + ppf.columnName + "=?";
					}
					Object obj = po.get(ppf);
					if ((obj instanceof Date))
						paramsList.add(new Timestamp(((Date) obj).getTime()));
					else
						paramsList.add(obj);
					haveUpdate = true;
				}
			}
		}
		if (haveUpdate) {
			paramsList.add(po.getKey());
			if (!allField)
				updateSQL = updateSQL + " where " + po.attr.key.columnName + " =?";
			return getDataAccess().execNoQuerySql(updateSQL, paramsList.toArray());
		}
		return true;
	}

	public boolean deletePO(T po) throws SQLException {
		return getDataAccess().execNoQuerySql(this.attr.deleteSQL, new Object[] { po.getKey() });
	}

	public boolean deletePO(String id) throws SQLException {
		return getDataAccess().execNoQuerySql(this.attr.deleteSQL, new Object[] { id });
	}

	public boolean deletePO(long id) throws SQLException {
		return getDataAccess().execNoQuerySql(this.attr.deleteSQL, new Object[] { id });
	}

	public T queryPO(String id) throws SQLException {
		T tpo = (T) getDataAccess().queryForBean(this.attr.selectSQL + " where " + this.attr.key.columnName + " =?", this.attr.clazz, new Object[] { id });
		if (tpo != null)
			tpo.clearDirty();
		return tpo;
	}

	public List<T> queryPOList(long... ids) throws SQLException {
		String _selectSQL = this.attr.selectSQL + " where " + this.attr.getKeyName() + " in( " + ToolUtil.join(ids, ",") + " )";
		List<T> listPO = (List<T>) getDataAccess().queryForBeanList(_selectSQL, this.attr.clazz, new Object[0]);
		if (listPO != null)
			for (T tpo : listPO) {
				tpo.clearDirty();
			}
		return listPO;
	}

	public List<T> queryPOList() {
		List<T> listPO = (List<T>) getDataAccess().queryForBeanList(this.attr.selectSQL, this.attr.clazz, new Object[0]);
		if (listPO != null)
			for (T tpo : listPO) {
				tpo.clearDirty();
			}
		return listPO;
	}

	public List<T> queryPOList(String where, Object... params) {
		String _selectSQL = this.attr.selectSQL;
		if (where != null) {
			_selectSQL = _selectSQL + " where " + where;
		}
		List<T> listPO = null;
		if ((where != null) && (params != null) && (params.length > 0))
			listPO = (List<T>) getDataAccess().queryForBeanList(_selectSQL, this.attr.clazz, params);
		else
			listPO = (List<T>) getDataAccess().queryForBeanList(_selectSQL, this.attr.clazz, new Object[0]);
		if (listPO != null)
			for (T tpo : listPO) {
				tpo.clearDirty();
			}
		return listPO;
	}

	public List<T> queryPOList(String where, String orderstr, Object... params) throws SQLException {
		String _selectSQL = this.attr.selectSQL;
		if ((where != null) && (!where.trim().equals(""))) {
			_selectSQL = _selectSQL + " where " + where;
		}
		if ((orderstr != null) && (!orderstr.trim().equals(""))) {
			_selectSQL = _selectSQL + " order by " + orderstr;
		}
		List<T> listPO = null;
		if ((where != null) && (params != null) && (params.length > 0))
			listPO = (List<T>) getDataAccess().queryForBeanList(_selectSQL, this.attr.clazz, params);
		else
			listPO = (List<T>) getDataAccess().queryForBeanList(_selectSQL, this.attr.clazz, new Object[0]);
		if (listPO != null)
			for (T tpo : listPO) {
				tpo.clearDirty();
			}
		return listPO;
	}

}