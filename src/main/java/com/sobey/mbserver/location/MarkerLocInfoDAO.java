package com.sobey.mbserver.location;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sobey.base.BaseDao;
import com.sobey.base.Page;
import com.sobey.base.exception.POException;
import com.sobey.base.util.LngLatUtil;

public class MarkerLocInfoDAO extends BaseDao<MarkerLocInfoPO> {
	public MarkerLocInfoDAO() throws POException {
		super(MarkerLocInfoPO.class, "M_MARKER_LOCATION", "MARKER_ID");
	}

	public MarkerLocInfoPO queryBySrcId(int type, long srcId) {
		String sql = "select MARKER_ID,MARKER_TYPE,MARKER_SRC_ID,MARKER_NAME,LONGITUDE ,LATITUDE,ELEVATION,DISTANCE_RANGE,MARKER_DESC from M_MARKER_LOCATION a where  a.MARKER_TYPE=? and ";

		if ((type > 0) && (type < 5))
			sql = sql + " a.MARKER_SRC_ID=?";
		else {
			sql = sql + " a.MARKER_ID=?";
		}
		return (MarkerLocInfoPO) this.access.queryForBean(sql, MarkerLocInfoPO.class, new Object[] { Integer.valueOf(type), Long.valueOf(srcId) });
	}

	public void initLoad() {
		String sql = "select MARKER_ID,MARKER_TYPE,MARKER_SRC_ID,MARKER_NAME,LONGITUDE ,LATITUDE,ELEVATION,DISTANCE_RANGE,MARKER_DESC from M_MARKER_LOCATION a ";

		List<MarkerLocInfoPO> list = this.access.queryForBeanList(sql, MarkerLocInfoPO.class, new Object[0]);
		synchronized (MarkerLocInfoAction.markers) {
			for (MarkerLocInfoPO m : list)
				MarkerLocInfoAction.addCache(m);
		}
	}

	public Page queryPage(int posStart, int pageSize, boolean knowLoc, String keywords) {
		String sql = "select * from M_MARKER_LOCATION where";

		if (knowLoc)
			sql = sql + " LONGITUDE>0 ";
		else {
			sql = sql + " LONGITUDE=0 ";
		}
		if ((keywords != null) && (!"".equals(keywords.trim()))) {
			sql = sql + " AND MARKER_NAME like ? ORDER BY MARKER_ID DESC";
			return queryForPageList(posStart, pageSize, sql, new Object[] { "%" + keywords + "%" });
		}
		sql = sql + " ORDER BY MARKER_ID DESC";
		return queryForPageList(posStart, pageSize, sql, new Object[0]);
	}

	public void updateLocBatch(List<Map> list) {
		String sql = "UPDATE M_MARKER_LOCATION SET LONGITUDE=?,LATITUDE=?,ELEVATION=?,DISTANCE_RANGE=? WHERE MARKER_ID=?";
		Object[][] param = new Object[list.size()][5];
		for (int i = 0; i < list.size(); i++) {
			Map map = (Map) list.get(i);
			int index = 0;
			param[i][(index++)] = map.get("LONGITUDE");
			param[i][(index++)] = map.get("LATITUDE");
			param[i][(index++)] = map.get("ELEVATION");
			param[i][(index++)] = map.get("DISTANCE_RANGE");
			param[i][(index++)] = map.get("MARKER_ID");
		}
		getDataAccess().execUpdateBatch(sql, param);
	}

	public List<MarkerLocInfoPO> queryScope(double lng, double lat, double raidus, int type) {
		List params = new ArrayList();
		Bounds bounds = LngLatUtil.getBounds(lng, lat, raidus);
		String sql = "select MARKER_ID,MARKER_TYPE,MARKER_SRC_ID,MARKER_NAME,LONGITUDE,LATITUDE,ELEVATION,DISTANCE_RANGE,MARKER_DESC from M_MARKER_LOCATION WHERE ?<LONGITUDE and LONGITUDE<? and ?<LATITUDE and LATITUDE<? ";

		params.add(Double.valueOf(bounds.getMinLng()));
		params.add(Double.valueOf(bounds.getMaxLng()));
		params.add(Double.valueOf(bounds.getMinLat()));
		params.add(Double.valueOf(bounds.getMaxLat()));

		if (-1 != type) {
			params.add(Integer.valueOf(type));
			sql = sql + " AND MARKER_TYPE =?";
		}
		return this.access.queryForBeanList(sql, MarkerLocInfoPO.class, params.toArray());
	}

	public List<MarkerLocInfoPO> queryByType(int type) {
		List params = new ArrayList();
		String sql = "select MARKER_ID,MARKER_TYPE,MARKER_SRC_ID,MARKER_NAME,LONGITUDE,LATITUDE,ELEVATION,DISTANCE_RANGE,MARKER_DESC from M_MARKER_LOCATION ";
		if (-1 != type) {
			params.add(Integer.valueOf(type));
			sql = sql + "WHERE MARKER_TYPE=?";
		}
		return this.access.queryForBeanList(sql, MarkerLocInfoPO.class, params.toArray());
	}

	public MarkerLocInfoPO queryMarkerSrcId(long markerSrcId) {
		String sql = "select MARKER_ID,MARKER_TYPE,MARKER_SRC_ID,MARKER_NAME,LONGITUDE,LATITUDE,ELEVATION,DISTANCE_RANGE,MARKER_DESC from M_MARKER_LOCATION WHERE MARKER_SRC_ID=? ";

		return (MarkerLocInfoPO) this.access.queryForBean(sql, MarkerLocInfoPO.class, new Object[] { Long.valueOf(markerSrcId) });
	}
}