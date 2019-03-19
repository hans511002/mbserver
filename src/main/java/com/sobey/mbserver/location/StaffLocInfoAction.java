package com.sobey.mbserver.location;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sobey.base.BaseAction;
import com.sobey.base.exception.POException;
import com.sobey.base.util.LngLatUtil;
import com.sobey.mbserver.location.HistoryPO.Locus;
import com.sobey.mbserver.location.MarkerLocInfoAction.MarkerCachePO;

public class StaffLocInfoAction extends BaseAction {
	public static final Log LOG = LogFactory.getLog(StaffLocInfoAction.class.getName());

	StaffLocInfoDAO ComDao;

	public StaffLocInfoAction() {
		// ComDao = new StaffLocInfoDAO();
	}

	public void setComDao(StaffLocInfoDAO comDao) {
		ComDao = comDao;
	}

	public long receive(long staffId, String mobileNum, int action, long timeMillis, double lng, double lat, int radius, String errMsg) throws SQLException {
		StaffLocInfoPO cpo = new StaffLocInfoPO();
		cpo.setSTAFF_ID(staffId);
		cpo.setSTAFF_MOBILE(mobileNum);
		cpo.setSTAFF_ACTION(action);
		cpo.setLONGITUDE(lng);
		cpo.setLATITUDE(lat);
		cpo.setLOCATE_RADIUS(radius);
		cpo.setLOCATE_TYPE(radius < 100 ? 0 : 1);
		cpo.setFEEDBACK_TIME(new Date(timeMillis));
		cpo.setERROR_MSG(errMsg);
		return insert(cpo);
	}

	public static class MarkerDistance {
		long markerId;
		double markerDis;
	}

	public long insert(StaffLocInfoPO cpo) throws SQLException {
		cpo.MIN_MARKER_ID1 = 0;
		cpo.MIN_MARKER_ID2 = 0;
		cpo.MIN_MARKER_ID3 = 0;

		if (cpo.LATITUDE != 0 && cpo.LONGITUDE != 0) {
			for (Integer type : MarkerLocInfoAction.markers.keySet()) {
				HashMap<Long, MarkerCachePO> list = MarkerLocInfoAction.markers.get(type);
				if (list == null)
					continue;
				for (Long id : list.keySet()) {
					MarkerCachePO m = list.get(id);
					if (m.bound.maxLng < cpo.LONGITUDE || cpo.LONGITUDE < m.bound.minLng || m.bound.maxLat < cpo.LATITUDE || cpo.LATITUDE < m.bound.minLat) {
						// LOG.info("不在" + m.mpo.MARKER_NAME + m.mpo.DISTANCE_RANGE + "m有效范围内");
						continue;
					}
					double dis = LngLatUtil.getDistance(cpo.LONGITUDE, cpo.LATITUDE, m.mpo.LONGITUDE, m.mpo.LATITUDE);
					// LOG.info("在" + m.mpo.MARKER_NAME + m.mpo.DISTANCE_RANGE + "m有效范围内 距离：" + dis + "m");

					if (dis < m.mpo.DISTANCE_RANGE) {
						if (cpo.MIN_MARKER_ID1 == 0) {
							cpo.MIN_MARKER_ID1 = m.mpo.MARKER_ID;
							cpo.MARKER_ID1_DISTANCE = dis;
						} else if (cpo.MIN_MARKER_ID2 == 0) {
							cpo.MIN_MARKER_ID2 = m.mpo.MARKER_ID;
							cpo.MARKER_ID2_DISTANCE = dis;
						} else if (cpo.MIN_MARKER_ID3 == 0) {
							cpo.MIN_MARKER_ID3 = m.mpo.MARKER_ID;
							cpo.MARKER_ID3_DISTANCE = dis;
						} else if (cpo.MARKER_ID1_DISTANCE >= cpo.MARKER_ID2_DISTANCE && cpo.MARKER_ID1_DISTANCE >= cpo.MARKER_ID3_DISTANCE
						        && dis <= cpo.MARKER_ID1_DISTANCE) {
							cpo.MIN_MARKER_ID1 = m.mpo.MARKER_ID;
							cpo.MARKER_ID1_DISTANCE = dis;
						} else if (cpo.MARKER_ID2_DISTANCE >= cpo.MARKER_ID1_DISTANCE && cpo.MARKER_ID2_DISTANCE >= cpo.MARKER_ID3_DISTANCE
						        && dis <= cpo.MARKER_ID2_DISTANCE) {
							cpo.MIN_MARKER_ID2 = m.mpo.MARKER_ID;
							cpo.MARKER_ID2_DISTANCE = dis;
						} else if (cpo.MARKER_ID3_DISTANCE >= cpo.MARKER_ID1_DISTANCE && cpo.MARKER_ID3_DISTANCE >= cpo.MARKER_ID2_DISTANCE
						        && dis <= cpo.MARKER_ID3_DISTANCE) {
							cpo.MIN_MARKER_ID3 = m.mpo.MARKER_ID;
							cpo.MARKER_ID3_DISTANCE = dis;
						}
					}
				}
			}
			LOG.info("MIN_MARKER_ID1=" + cpo.MIN_MARKER_ID1 + ":" + cpo.MARKER_ID1_DISTANCE + " MIN_MARKER_ID2=" + cpo.MIN_MARKER_ID2 + ":"
			        + cpo.MARKER_ID2_DISTANCE + " MIN_MARKER_ID3=" + cpo.MIN_MARKER_ID3 + ":" + cpo.MARKER_ID3_DISTANCE);
			// 排序，可使用对象合并MarkerId及MarkerDis 交换排序，需要特殊处理 0 值，当前未处理
			MarkerDistance minMarks[] = new MarkerDistance[3];
			minMarks[0] = new MarkerDistance();
			minMarks[1] = new MarkerDistance();
			minMarks[2] = new MarkerDistance();
			minMarks[0].markerId = cpo.MIN_MARKER_ID1;
			minMarks[1].markerId = cpo.MIN_MARKER_ID2;
			minMarks[2].markerId = cpo.MIN_MARKER_ID3;
			minMarks[0].markerDis = cpo.MARKER_ID1_DISTANCE;
			minMarks[1].markerDis = cpo.MARKER_ID2_DISTANCE;
			minMarks[2].markerDis = cpo.MARKER_ID3_DISTANCE;

			for (int i = 1; i < minMarks.length; i++) {// min
				if (cpo.MARKER_ID1_DISTANCE > minMarks[i].markerDis && minMarks[i].markerDis != 0) {
					cpo.MARKER_ID1_DISTANCE = minMarks[i].markerDis;
					cpo.MIN_MARKER_ID1 = minMarks[i].markerId;
				}
			}
			if (cpo.MIN_MARKER_ID3 != 0)
				for (int i = 0; i < minMarks.length - 1; i++) {// max
					if (cpo.MARKER_ID3_DISTANCE < minMarks[i].markerDis) {
						cpo.MARKER_ID3_DISTANCE = minMarks[i].markerDis;
						cpo.MIN_MARKER_ID3 = minMarks[i].markerId;
					}
				}
			if (cpo.MIN_MARKER_ID2 == cpo.MIN_MARKER_ID1 || cpo.MIN_MARKER_ID2 == cpo.MIN_MARKER_ID3) {
				for (int i = 0; i < minMarks.length; i++) {// mid
					if (minMarks[i].markerId != cpo.MIN_MARKER_ID1 && minMarks[i].markerId != cpo.MIN_MARKER_ID3) {
						cpo.MARKER_ID2_DISTANCE = minMarks[i].markerDis;
						cpo.MIN_MARKER_ID2 = minMarks[i].markerId;
						break;
					}
				}
			}

			// long[] minMarkerIds = new long[3];
			// double[] minMarkerDis = new double[3];
			// if (cpo.MARKER_ID1_DISTANCE >= cpo.MARKER_ID2_DISTANCE && cpo.MARKER_ID1_DISTANCE >=
			// cpo.MARKER_ID3_DISTANCE) {
			// minMarkerIds[2] = cpo.MIN_MARKER_ID1;
			// minMarkerDis[2] = cpo.MARKER_ID1_DISTANCE;
			// if (cpo.MARKER_ID2_DISTANCE > cpo.MARKER_ID3_DISTANCE) {
			// minMarkerIds[0] = cpo.MIN_MARKER_ID3;
			// minMarkerDis[0] = cpo.MARKER_ID3_DISTANCE;
			// minMarkerIds[1] = cpo.MIN_MARKER_ID2;
			// minMarkerDis[1] = cpo.MARKER_ID2_DISTANCE;
			// } else {
			// minMarkerIds[0] = cpo.MIN_MARKER_ID2;
			// minMarkerDis[0] = cpo.MARKER_ID2_DISTANCE;
			// minMarkerIds[1] = cpo.MIN_MARKER_ID3;
			// minMarkerDis[1] = cpo.MARKER_ID3_DISTANCE;
			// }
			// } else if (cpo.MARKER_ID2_DISTANCE >= cpo.MARKER_ID1_DISTANCE && cpo.MARKER_ID2_DISTANCE >=
			// cpo.MARKER_ID3_DISTANCE) {
			// minMarkerIds[2] = cpo.MIN_MARKER_ID2;
			// minMarkerDis[2] = cpo.MARKER_ID2_DISTANCE;
			// if (cpo.MARKER_ID1_DISTANCE > cpo.MARKER_ID3_DISTANCE) {
			// minMarkerIds[0] = cpo.MIN_MARKER_ID3;
			// minMarkerDis[0] = cpo.MARKER_ID3_DISTANCE;
			// minMarkerIds[1] = cpo.MIN_MARKER_ID1;
			// minMarkerDis[1] = cpo.MARKER_ID1_DISTANCE;
			// } else {
			// minMarkerIds[0] = cpo.MIN_MARKER_ID1;
			// minMarkerDis[0] = cpo.MARKER_ID1_DISTANCE;
			// minMarkerIds[1] = cpo.MIN_MARKER_ID3;
			// minMarkerDis[1] = cpo.MARKER_ID3_DISTANCE;
			// }
			// } else {
			// minMarkerIds[2] = cpo.MIN_MARKER_ID3;
			// minMarkerDis[2] = cpo.MARKER_ID3_DISTANCE;
			// if (cpo.MARKER_ID1_DISTANCE > cpo.MARKER_ID2_DISTANCE) {
			// minMarkerIds[0] = cpo.MIN_MARKER_ID2;
			// minMarkerDis[0] = cpo.MARKER_ID2_DISTANCE;
			// minMarkerIds[1] = cpo.MIN_MARKER_ID1;
			// minMarkerDis[1] = cpo.MARKER_ID1_DISTANCE;
			// } else {
			// minMarkerIds[0] = cpo.MIN_MARKER_ID1;
			// minMarkerDis[0] = cpo.MARKER_ID1_DISTANCE;
			// minMarkerIds[1] = cpo.MIN_MARKER_ID2;
			// minMarkerDis[1] = cpo.MARKER_ID2_DISTANCE;
			// }
			// }
			// cpo.MIN_MARKER_ID1 = minMarkerIds[0];
			// cpo.MARKER_ID1_DISTANCE = minMarkerDis[0];
			// cpo.MIN_MARKER_ID2 = minMarkerIds[1];
			// cpo.MARKER_ID2_DISTANCE = minMarkerDis[1];
			// cpo.MIN_MARKER_ID3 = minMarkerIds[2];
			// cpo.MARKER_ID3_DISTANCE = minMarkerDis[2];
			LOG.info("MIN_MARKER_ID1=" + cpo.MIN_MARKER_ID1 + ":" + cpo.MARKER_ID1_DISTANCE + " MIN_MARKER_ID2=" + cpo.MIN_MARKER_ID2 + ":"
			        + cpo.MARKER_ID2_DISTANCE + " MIN_MARKER_ID3=" + cpo.MIN_MARKER_ID3 + ":" + cpo.MARKER_ID3_DISTANCE);
		}
		boolean res = ComDao.insertPO(cpo);
		// synchronized (StaffLocInfoAddrServer.needUpdateLocus) {
		// Locus ls = getLocus(cpo);
		// StaffLocInfoAddrServer.needUpdateLocus.add(ls);
		// }
		if (res) {
			if (cpo.MIN_MARKER_ID1 != 0 || cpo.MIN_MARKER_ID2 != 0 || cpo.MIN_MARKER_ID3 != 0) {
				// if (cpo.MIN_MARKER_ID1 != 0)
				// return cpo.MIN_MARKER_ID1;
				// else if (cpo.MIN_MARKER_ID2 != 0)
				// return cpo.MIN_MARKER_ID2;
				// else
				// return cpo.MIN_MARKER_ID3;
				return 1;
			} else {
				return 0;
			}
		} else {
			return -1;
		}
	}

	Locus getLocus(StaffLocInfoPO slpo) {
		Locus ls = new Locus();
		ls.LOCINFO_ID = slpo.LOCINFO_ID;
		ls.LONGITUDE = slpo.LONGITUDE;
		ls.LATITUDE = slpo.LATITUDE;
		ls.ELEVATION = slpo.ELEVATION;
		ls.FEEDBACK_TIME = slpo.FEEDBACK_TIME;
		ls.STAFF_ACTION = slpo.STAFF_ACTION;
		ls.ADDRESS_DESC = slpo.ADDRESS_DESC;
		ls.ERROR_MSG = slpo.ERROR_MSG;
		return ls;
	}

	public boolean update(StaffLocInfoPO cpo) throws SQLException {
		return ComDao.updatePO(cpo);
	}

	public StaffLocInfoPO query(long cmpId) throws SQLException {
		return ComDao.queryPO(cmpId + "");
	}

	public List<StaffLocInfoPO> queryList(long... ids) throws SQLException {
		return ComDao.queryPOList(ids);
	}

	/**
	 * 获取最新有效位置
	 * 
	 * @return
	 * @throws SQLException
	 */
	public Locus queryLastVaildLocu(int staffId) throws SQLException {
		StaffLocusInfoPO locuInfo = ComDao.queryLastVaildLocu(staffId);
		if (null != locuInfo) {
			Locus loc = new Locus();
			loc.LOCINFO_ID = locuInfo.LOCINFO_ID;
			loc.LONGITUDE = locuInfo.LONGITUDE;
			loc.LATITUDE = locuInfo.LATITUDE;
			loc.ELEVATION = locuInfo.ELEVATION;
			loc.FEEDBACK_TIME = locuInfo.FEEDBACK_TIME;
			loc.STAFF_ACTION = locuInfo.STAFF_ACTION;
			loc.ADDRESS_DESC = locuInfo.ADDRESS_DESC;
			loc.ERROR_MSG = locuInfo.ERROR_MSG;
			return loc;
		}
		return null;
	}

	public List<Map<String, Object>> queryTodayStaffLocus(long staffId) {
		try {
			return ComDao.queryStaffLocus4Day(staffId, new Date());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean insertTestData(Date dt) throws POException, SQLException {
		MarkerLocInfoDAO mkdao = new MarkerLocInfoDAO();
		List<MarkerLocInfoPO> mklist = mkdao.queryPOList("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18");
		LOG.info(mklist.size() + "  ：" + mklist);
		if (mklist.size() < 10)
			throw new RuntimeException("Marker结果数据不正确");
		StaffLocInfoPO cpo = new StaffLocInfoPO();
		cpo.STAFF_ID = 1;
		cpo.STAFF_MOBILE = "13990909500";
		dt.setHours(9);
		dt.setMinutes(0);
		dt.setSeconds(0);
		long st = dt.getTime();
		int step = 5 * 60 * 1000;
		long et = st + 9 * 60 * 60 * 1000;
		for (long now = st; now <= et; now += step) {
			cpo.FEEDBACK_TIME = new Date(now);
			// 标识点
			int mkd = (int) (Math.random() * 18) + 1;
			if (mkd < 10) {
				mkd += (int) (Math.random() * (18 - mkd));
				if (mkd < 10)
					mkd += (int) (Math.random() * (18 - mkd));
			}
			mkd = mkd % 18;
			MarkerLocInfoPO mpo = mklist.get(mkd);
			double lng = Math.random() * Math.random() * 0.0015;// 控制在0.15内
			double lat = Math.random() * Math.random() * Math.random() * 0.0015;
			// // 30%几率不在标识物附近
			// if (Math.random() < 0.3) {
			// lng += Math.random();
			// lat += Math.random();
			// }

			cpo.LONGITUDE = mpo.LONGITUDE + lng;
			cpo.LATITUDE = mpo.LATITUDE + lat;
			cpo.ELEVATION = mpo.ELEVATION;
			cpo.MIN_MARKER_ID1 = 0;
			cpo.MIN_MARKER_ID2 = 0;
			cpo.MIN_MARKER_ID3 = 0;
			cpo.MARKER_ID1_DISTANCE = 0;
			cpo.MARKER_ID2_DISTANCE = 0;
			cpo.MARKER_ID3_DISTANCE = 0;

			// 用户事件
			int action = (int) (Math.random() * 10000);
			cpo.STAFF_ACTION = 0;
			if (action < 10) {
				cpo.STAFF_ACTION = action & 3;
				if (cpo.STAFF_ACTION == 0) {
					cpo.STAFF_ACTION = action & 1;
				}
				if (cpo.STAFF_ACTION == 2) {
					cpo.STAFF_ACTION = action & 2;
				}
				if (cpo.STAFF_ACTION == 4) {
					cpo.STAFF_ACTION = action & 4;
				}
			}
			insert(cpo);
		}
		return true;
	}
}
