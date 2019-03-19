package com.sobey.mbserver.location;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sobey.base.BaseAction;
import com.sobey.base.Page;
import com.sobey.base.exception.POException;
import com.sobey.base.util.LngLatUtil;

public class MarkerLocInfoAction extends BaseAction {
	public static HashMap<Integer, HashMap<Long, MarkerCachePO>> markers = new HashMap();
	MarkerLocInfoDAO ComDao;

	public MarkerLocInfoAction() throws POException {
		this.ComDao = new MarkerLocInfoDAO();
	}

	public void setComDao(MarkerLocInfoDAO comDao) {
		this.ComDao = comDao;
	}

	static void addCache(MarkerLocInfoPO cpo) {
		HashMap list = null;
		synchronized (markers) {
			list = (HashMap) markers.get(Integer.valueOf(cpo.getMARKER_TYPE()));
			if (list == null) {
				list = new HashMap();
				markers.put(Integer.valueOf(cpo.getMARKER_TYPE()), list);
			}
		}
		long keyId = cpo.getMARKER_SRC_ID();
		if (cpo.getMARKER_TYPE() == 5)
			keyId = cpo.getMARKER_ID();
		MarkerCachePO mcpo = new MarkerCachePO();
		mcpo.mpo = cpo;
		mcpo.bound = LngLatUtil.getBounds(cpo.getLONGITUDE(), cpo.getLATITUDE(), cpo.getDISTANCE_RANGE());

		synchronized (list) {
			list.put(Long.valueOf(keyId), mcpo);
		}
	}

	void updateCache(MarkerLocInfoPO cpo) throws SQLException {
		HashMap list = null;
		synchronized (markers) {
			list = (HashMap) markers.get(Integer.valueOf(cpo.getMARKER_TYPE()));
			if (list == null) {
				list = new HashMap();
				markers.put(Integer.valueOf(cpo.getMARKER_TYPE()), list);
			}
		}
		long keyId = cpo.getMARKER_SRC_ID();
		if (cpo.getMARKER_TYPE() == 5)
			keyId = cpo.getMARKER_ID();
		MarkerCachePO mcpo = new MarkerCachePO();
		mcpo.mpo = query(keyId);
		mcpo.bound = LngLatUtil.getBounds(mcpo.mpo.getLONGITUDE(), mcpo.mpo.getLATITUDE(), mcpo.mpo.getDISTANCE_RANGE());
		synchronized (list) {
			list.put(Long.valueOf(keyId), mcpo);
		}
	}

	public boolean insert(MarkerLocInfoPO cpo) throws SQLException {
		int type = cpo.getMARKER_TYPE();
		if ((type > 0) && (type < 5)) {
			long sid = cpo.getMARKER_SRC_ID();
			MarkerLocInfoPO oldPO = this.ComDao.queryBySrcId(type, sid);
			if (oldPO != null) {
				cpo.setMARKER_ID(oldPO.MARKER_ID);
				return update(cpo);
			}
		}
		boolean res = this.ComDao.insertPO(cpo);
		if (res) {
			updateCache(cpo);
		}
		return res;
	}

	public boolean update(MarkerLocInfoPO cpo) throws SQLException {
		if (cpo.MARKER_ID == 0L) {
			return insert(cpo);
		}
		boolean res = this.ComDao.updatePO(cpo);
		if (res) {
			updateCache(cpo);
		}
		return res;
	}

	public boolean updateBatch(List<Map> list) throws SQLException {
		this.ComDao.updateLocBatch(list);
		return true;
	}

	public MarkerLocInfoPO query(long cmpId) throws SQLException {
		return (MarkerLocInfoPO) this.ComDao.queryPO(cmpId + "");
	}

	public Page queryPage(int posStart, int pageSize, boolean knowLoc, String keywords) {
		return this.ComDao.queryPage(posStart, pageSize, knowLoc, keywords);
	}

	public List<MarkerLocInfoPO> queryScope(double lng, double lat, double raidus, int type) {
		try {
			return this.ComDao.queryScope(lng, lat, raidus, type);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<MarkerLocInfoPO> queryByType(int type) {
		try {
			return this.ComDao.queryByType(type);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public MarkerLocInfoPO queryMarkerSrcId(long markerSrcId) {
		try {
			return this.ComDao.queryMarkerSrcId(markerSrcId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static class MarkerCachePO {
		public MarkerLocInfoPO mpo;
		public Bounds bound;
	}
}