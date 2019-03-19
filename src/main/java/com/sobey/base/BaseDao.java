package com.sobey.base;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.List;

import com.sobey.base.SingleTableDAO.SeqValue;
import com.sobey.base.exception.POException;
import com.sobey.jcg.support.jdbc.DataAccess;

public abstract class BaseDao<T> extends SingleTableDAO<T> {
	public BaseDao(Class<T> PO, String tableName, String keyName) throws POException {
		super(PO, tableName, keyName);
	}

	public BaseDao(Class<T> PO, String tableName, String keyName, String seqName) throws POException {
		super(PO, tableName, keyName, seqName);
	}

	public Class<?> getGenericsType() {
		Type[] types = getClass().getGenericInterfaces();
		for (Type type : types) {
			if ((type instanceof ParameterizedType)) {
				ParameterizedType ptype = (ParameterizedType) type;
				if (Comparator.class.equals(ptype.getRawType())) {
					Type cmpType = ptype.getActualTypeArguments()[0];
					if ((cmpType instanceof ParameterizedType)) {
						return (Class) ((ParameterizedType) cmpType).getRawType();
					}
					return (Class) ptype.getActualTypeArguments()[0];
				}
			}
		}

		return Object.class;
	}

	public static long createAndGetSeq(String seqName, int step) {
		if (step < 1) {
			step = 1;
		}
		SingleTableDAO.SeqValue seqv = null;
		synchronized (seqMutex) {
			seqv = (SingleTableDAO.SeqValue) seqMutex.get(seqName);
			if (seqv == null) {
				seqv = new SingleTableDAO.SeqValue();
				seqMutex.put(seqName, seqv);
			}
		}
		synchronized (seqv) {
			seqv.seq += step;
			return seqv.seq;
		}
	}

	protected Page queryForPageList(int posStart, int pageSize, String sql, Object[] params) {
		return queryForPageList(posStart, pageSize, sql, null, params);
	}

	protected Page queryForPageList(int posStart, int pageSize, String sql, String countSql, Object[] params) {
		DataAccess dataAccess = getDataAccess();
		if ((countSql == null) || ("".equals(countSql.trim()))) {
			int fromIndex = sql.toUpperCase().indexOf(" FROM ");
			int orderIndex = sql.toUpperCase().indexOf(" ORDER ");
			if (-1 == orderIndex) {
				orderIndex = sql.length();
			}
			countSql = "select count(*) " + sql.substring(fromIndex, orderIndex);
		}
		int total = dataAccess.queryForInt(countSql, params);

		sql = sql + " LIMIT " + pageSize + " offset " + posStart;
		List list = getDataAccess().queryForList(sql, params);

		Page page = new Page();
		page.setPosStart(posStart);
		page.setTotal(total);
		page.setCount(pageSize);
		page.setList(list);
		return page;
	}
}