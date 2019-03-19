package com.sobey.mbserver.location;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sobey.base.util.LngLatUtil;
import com.sobey.mbserver.location.HistoryPO.Locus;

public class LocationAction {
	public static final Log LOG = LogFactory.getLog(LocationAction.class.getName());
	StaffLocInfoDAO sldao;

	public void setSldao(StaffLocInfoDAO sldao) {
		this.sldao = sldao;
	}

	HashMap<Long, Integer> findSleep(HashMap<Long, Integer> lastMarker, int points) {
		HashMap<Long, Integer> res = new HashMap<Long, Integer>();
		for (Long l : lastMarker.keySet()) {
			if (lastMarker.get(l) >= points) {
				res.put(l, lastMarker.get(l));
			}
		}
		return res;
	}

	Locus getLocus(StaffLocusInfoPO slpo) {
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

	void addMarkerCount(HashMap<Long, Integer> lastMarker, StaffLocusInfoPO slpo) {
		if (slpo.MIN_MARKER_ID1 != 0) {
			if (!lastMarker.containsKey(slpo.MIN_MARKER_ID1)) {// 判断是否存在第一个标识
				lastMarker.put(slpo.MIN_MARKER_ID1, 1);
			} else {
				lastMarker.put(slpo.MIN_MARKER_ID1, lastMarker.get(slpo.MIN_MARKER_ID1) + 1);
			}
		}
		if (slpo.MIN_MARKER_ID2 != 0) {
			if (!lastMarker.containsKey(slpo.MIN_MARKER_ID2)) {// 判断是否存在第一个标识
				lastMarker.put(slpo.MIN_MARKER_ID2, 1);
			} else {
				lastMarker.put(slpo.MIN_MARKER_ID2, lastMarker.get(slpo.MIN_MARKER_ID2) + 1);
			}
		}
		if (slpo.MIN_MARKER_ID3 != 0) {
			if (!lastMarker.containsKey(slpo.MIN_MARKER_ID3)) {// 判断是否存在第一个标识
				lastMarker.put(slpo.MIN_MARKER_ID3, 1);
			} else {
				lastMarker.put(slpo.MIN_MARKER_ID3, lastMarker.get(slpo.MIN_MARKER_ID3) + 1);
			}
		}
	}

	void addLocus(HistoryPO hpo, StaffLocusInfoPO slpo) {
		if (hpo.startTime == null) {
			hpo.startTime = slpo.FEEDBACK_TIME;
		}
		hpo.endTime = slpo.FEEDBACK_TIME;
		if (slpo.LATITUDE != 0 && slpo.LONGITUDE != 0)
			hpo.period.add(getLocus(slpo));
		hpo.periodLen++;
		hpo.ERROR_MSG = slpo.ERROR_MSG;
	}

	void removeLocus(HistoryPO hpo, StaffLocusInfoPO slpo, HashMap<Long, StaffLocusInfoPO> loclRel, HashMap<Long, Integer> lastMarker) {
		if (hpo.period.size() < 2)
			return;
		for (int index = hpo.period.size() - 1; index >= 0; index--) {
			Locus ls = hpo.period.get(index);
			if (ls.LOCINFO_ID == slpo.LOCINFO_ID) {
				hpo.period.remove(ls);
				removeMarkerCount(lastMarker, slpo);
				break;
			}
		}
		hpo.endTime = loclRel.get(hpo.period.get(hpo.period.size() - 1).LOCINFO_ID).FEEDBACK_TIME;
	}

	void removeMarkerCount(HashMap<Long, Integer> lastMarker, StaffLocusInfoPO slpo) {
		if (slpo.MIN_MARKER_ID1 != 0 && lastMarker.containsKey(slpo.MIN_MARKER_ID1)) {
			lastMarker.put(slpo.MIN_MARKER_ID1, lastMarker.get(slpo.MIN_MARKER_ID1) - 1);
		}
		if (slpo.MIN_MARKER_ID2 != 0 && lastMarker.containsKey(slpo.MIN_MARKER_ID2)) {// 判断是否存在第一个标识
			lastMarker.put(slpo.MIN_MARKER_ID2, lastMarker.get(slpo.MIN_MARKER_ID2) - 1);
		}
		if (slpo.MIN_MARKER_ID3 != 0 && lastMarker.containsKey(slpo.MIN_MARKER_ID3)) {// 判断是否存在第一个标识
			lastMarker.put(slpo.MIN_MARKER_ID3, lastMarker.get(slpo.MIN_MARKER_ID3) - 1);
		}
	}

	boolean haveJoin(StaffLocusInfoPO slpo, StaffLocusInfoPO lslpo) {
		if (slpo.MIN_MARKER_ID1 != 0) {
			if (slpo.MIN_MARKER_ID1 == lslpo.MIN_MARKER_ID1 || slpo.MIN_MARKER_ID1 == lslpo.MIN_MARKER_ID2 || slpo.MIN_MARKER_ID1 == lslpo.MIN_MARKER_ID3) {
				return true;
			}
		}

		if (slpo.MIN_MARKER_ID2 != 0) {
			if (slpo.MIN_MARKER_ID2 == lslpo.MIN_MARKER_ID1 || slpo.MIN_MARKER_ID2 == lslpo.MIN_MARKER_ID2 || slpo.MIN_MARKER_ID2 == lslpo.MIN_MARKER_ID3) {
				return true;
			}
		}
		if (slpo.MIN_MARKER_ID3 != 0) {
			if (slpo.MIN_MARKER_ID3 == lslpo.MIN_MARKER_ID1 || slpo.MIN_MARKER_ID3 == lslpo.MIN_MARKER_ID2 || slpo.MIN_MARKER_ID3 == lslpo.MIN_MARKER_ID3) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param staffId
	 * @param time
	 * @param points
	 *            连续点位精度，越小越细,最小为1
	 * @param includePeriod
	 * @return
	 */
	public List<HistoryPO> getHistory(long staffId, Date time, int points, boolean includePeriod) {
		List<HistoryPO> res = new ArrayList<HistoryPO>();
		try {
			List<StaffLocusInfoPO> list = sldao.queryStaffLocusList(staffId, time);
			int len = list.size();
			if (len == 0)
				return res;
			HashMap<Long, StaffLocusInfoPO> loclRel = new HashMap<Long, StaffLocusInfoPO>();
			for (StaffLocusInfoPO slpo : list) {
				loclRel.put(slpo.LOCINFO_ID, slpo);
			}
			HistoryPO hpo = new HistoryPO();
			HashMap<Long, Integer> lastMarker = new HashMap<Long, Integer>();
			boolean haveSleep = false;
			int lastAddIndex = 0;
			boolean lastHaveJoin = false;
			// 计算当前三个标识物是否与上一次有交集
			StaffLocusInfoPO lslpo = null;
			StaffLocusInfoPO llslpo = null;
			for (int i = 0; i < len; i++) {
				StaffLocusInfoPO lspo = list.get(i);
				// 独立特殊事件 1：开机 2：关机 3：启动应用 4：关闭应用
				if (lspo.STAFF_ACTION == 1 || lspo.STAFF_ACTION == 2 || lspo.STAFF_ACTION == 3 || lspo.STAFF_ACTION == 4) {
					if (hpo.period.size() > 0) {// 先前存在未添加合并的
						HashMap<Long, Integer> sls = findSleep(lastMarker, points);
						comSum(res, hpo, lastMarker, sls, loclRel, haveSleep);
						lastAddIndex = i - 1;
					}
					addLocus(hpo, lspo);
					comSum(res, hpo, lastMarker, new HashMap<Long, Integer>(), loclRel, haveSleep);
					lastAddIndex = i;
					hpo = new HistoryPO();
					continue;
				}
				if (i == 0) {
					addLocus(hpo, lspo);
					addMarkerCount(lastMarker, lspo);
					lslpo = lspo;
					continue;
				}
				if (lspo.LATITUDE == 0 || lspo.LONGITUDE == 0) {
					// addLocus(hpo, lspo);// 全部向上合
					if (i > 0 && !lastHaveJoin) {// 只合中间无效数据
						addLocus(hpo, lspo);
						// hpo.endTime = lspo.FEEDBACK_TIME;
					}
					continue;
				}
				boolean hjoin = false;
				if (lslpo != null)
					hjoin = haveJoin(lspo, lslpo);
				lastHaveJoin = hjoin;
				if (hjoin) {
					// if (i >= lastAddIndex + 2) {
					if (hpo.period.size() >= 2 && llslpo != null) {
						if (haveJoin(lslpo, llslpo) == false) {
							// 上二个之间无交集,需要添加无交集集合
							removeLocus(hpo, lslpo, loclRel, lastMarker);
							HashMap<Long, Integer> sls = findSleep(lastMarker, points);// 查找是否存在有满足次数的标识物
							comSum(res, hpo, lastMarker, sls, loclRel, false);
							lastAddIndex = i - 2;
							hpo = new HistoryPO();
							addLocus(hpo, lslpo);
							addMarkerCount(lastMarker, lslpo);
						}
					}
					addLocus(hpo, lspo);
					addMarkerCount(lastMarker, lspo);
					haveSleep = true;
				} else {// 无交集
					// 判断是否有满足次数的标识物，存在回写添加，清空，重新new 不存在直接清空 重新new
					HashMap<Long, Integer> sls = findSleep(lastMarker, points);// 查找是否存在有满足次数的标识物
					if (sls.size() > 0 && haveSleep) {// 有满足次数的标识物
						// 需要计算汇总合并值
						comSum(res, hpo, lastMarker, sls, loclRel, haveSleep);
						lastAddIndex = i - 1;
						hpo = new HistoryPO();
						addLocus(hpo, lspo);
						addMarkerCount(lastMarker, lspo);
					} else {// 没有满足的
						addLocus(hpo, lspo);
						addMarkerCount(lastMarker, lspo);
					}
					haveSleep = false;
				}
				llslpo = lslpo;
				lslpo = lspo;
			}
			if (!res.contains(hpo)) {
				// 合并
				HashMap<Long, Integer> sls = findSleep(lastMarker, points);
				comSum(res, hpo, lastMarker, sls, loclRel, haveSleep);
			}
			if (!includePeriod) {
				for (HistoryPO his : res) {
					his.period.clear();
				}
			}
			// 判断最后时间是否在列表中
			if (res.size() > 0) {
				HistoryPO lpo = res.get(res.size() - 1);
				if (lpo != null) {
					StaffLocusInfoPO slpo = list.get(list.size() - 1);
					if (lpo.endTime != slpo.FEEDBACK_TIME) {
						for (int i = list.size() - 1; i >= 0; i--) {
							slpo = list.get(i);
							if (lpo.endTime == slpo.FEEDBACK_TIME) {
								lpo = new HistoryPO();
								for (int l = i + 1; l < list.size(); l++) {
									slpo = list.get(l);
									addLocus(lpo, slpo);
								}
								// lpo.period.clear();
								comSum(res, lpo, lastMarker, null, loclRel, haveSleep);
								break;
							}
						}
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// list.add(new HistoryPO(getDate("08:30"), null, "开工 万科", true));
		// list.add(new HistoryPO(getDate("08:30"), getDate("09:30"), "位置在不断移动", false));
		// list.add(new HistoryPO(getDate("9:30"), getDate("12:00"), "光华杏林（工地）", true));
		LOG.info(res);
		return res;
	}

	static String actionNames[] = new String[] { "(开机)", "(关机)", "(启动应用)", "(关闭应用)" };

	void comSum(List<HistoryPO> res, HistoryPO hpo, HashMap<Long, Integer> lastMarker, HashMap<Long, Integer> sls, HashMap<Long, StaffLocusInfoPO> loclRel,
	        boolean haveSleep) {
		if (hpo.period.size() > 1 && sls != null && sls.size() > 0 && haveSleep) {// 多个取距离小的
			int maxTimes = 0;
			long maxMarkerId = 0;
			String moreDesc = "轨迹：";
			int descLen = moreDesc.length();
			for (Long markerId : sls.keySet()) {
				if (sls.get(markerId) > maxTimes) {
					maxTimes = sls.get(markerId);
					maxMarkerId = markerId;
				}
			}
			for (Locus ls : hpo.period) {
				StaffLocusInfoPO slpo = loclRel.get(ls.LOCINFO_ID);
				if (sls.containsKey(slpo.MIN_MARKER_ID1) && (slpo.MARKER_ID2_DISTANCE == 0 || slpo.MARKER_ID1_DISTANCE <= slpo.MARKER_ID2_DISTANCE)
				        && (slpo.MARKER_ID3_DISTANCE == 0 || slpo.MARKER_ID1_DISTANCE <= slpo.MARKER_ID3_DISTANCE)) {
					moreDesc += slpo.MARKER_NAME1 + "(" + slpo.MARKER_ID1_DISTANCE + "m)->";
				} else if (sls.containsKey(slpo.MIN_MARKER_ID2) && (slpo.MARKER_ID1_DISTANCE == 0 || slpo.MARKER_ID2_DISTANCE <= slpo.MARKER_ID1_DISTANCE)
				        && (slpo.MARKER_ID3_DISTANCE == 0 || slpo.MARKER_ID2_DISTANCE <= slpo.MARKER_ID3_DISTANCE)) {
					moreDesc += slpo.MARKER_NAME2 + "(" + slpo.MARKER_ID2_DISTANCE + "m)->";
				} else if (sls.containsKey(slpo.MIN_MARKER_ID3) && (slpo.MARKER_ID2_DISTANCE == 0 || slpo.MARKER_ID3_DISTANCE <= slpo.MARKER_ID2_DISTANCE)
				        && (slpo.MARKER_ID1_DISTANCE == 0 || slpo.MARKER_ID3_DISTANCE <= slpo.MARKER_ID1_DISTANCE)) {
					moreDesc += slpo.MARKER_NAME3 + "(" + slpo.MARKER_ID3_DISTANCE + "m)->";
				}
			}
			if (moreDesc.length() > descLen)
				moreDesc = moreDesc.substring(0, moreDesc.length() - 2);
			hpo.markerId = maxMarkerId;
			hpo.MARKER_LONGITUDE = 0;
			hpo.MARKER_LATITUDE = 0;
			hpo.MARKER_ELEVATION = 0;
			hpo.maxMarkerDistance = 0;
			hpo.minMarkerDistance = 0;
			hpo.markerName = null;
			for (Locus ls : hpo.period) {
				StaffLocusInfoPO slpo = loclRel.get(ls.LOCINFO_ID);
				if (slpo.MIN_MARKER_ID1 == maxMarkerId) {
					if (hpo.markerName == null) {
						hpo.markerName = slpo.MARKER_NAME1;
						hpo.markerDesc = slpo.MARKER_DESC1;
						if (hpo.markerDesc == null || hpo.markerDesc.trim().equals("")) {
							hpo.markerDesc = moreDesc;
						}
						hpo.MARKER_LONGITUDE = slpo.LONGITUDE1;
						hpo.MARKER_LATITUDE = slpo.LATITUDE1;
						hpo.MARKER_ELEVATION = slpo.ELEVATION1;
					}
					if (slpo.MARKER_ID1_DISTANCE > hpo.maxMarkerDistance) {
						hpo.maxMarkerDistance = slpo.MARKER_ID1_DISTANCE;
					}
					if (slpo.MARKER_ID1_DISTANCE < hpo.minMarkerDistance || hpo.minMarkerDistance == 0) {
						hpo.minMarkerDistance = slpo.MARKER_ID1_DISTANCE;
					}
				} else if (slpo.MIN_MARKER_ID2 == maxMarkerId) {
					if (hpo.markerName == null) {
						hpo.markerName = slpo.MARKER_NAME2;
						hpo.markerDesc = slpo.MARKER_DESC2;
						if (hpo.markerDesc == null || hpo.markerDesc.trim().equals("")) {
							hpo.markerDesc = moreDesc;
						}
						hpo.MARKER_LONGITUDE = slpo.LONGITUDE2;
						hpo.MARKER_LATITUDE = slpo.LATITUDE2;
						hpo.MARKER_ELEVATION = slpo.ELEVATION2;
					}
					if (slpo.MARKER_ID2_DISTANCE > hpo.maxMarkerDistance) {
						hpo.maxMarkerDistance = slpo.MARKER_ID2_DISTANCE;
					}
					if (slpo.MARKER_ID2_DISTANCE < hpo.minMarkerDistance || hpo.minMarkerDistance == 0) {
						hpo.minMarkerDistance = slpo.MARKER_ID2_DISTANCE;
					}
				} else if (slpo.MIN_MARKER_ID3 == maxMarkerId) {
					if (hpo.markerName == null) {
						hpo.markerName = slpo.MARKER_NAME3;
						hpo.markerDesc = slpo.MARKER_DESC3;
						if (hpo.markerDesc == null || hpo.markerDesc.trim().equals("")) {
							hpo.markerDesc = moreDesc;
						}
						hpo.MARKER_LONGITUDE = slpo.LONGITUDE3;
						hpo.MARKER_LATITUDE = slpo.LATITUDE3;
						hpo.MARKER_ELEVATION = slpo.ELEVATION3;
					}
					if (slpo.MARKER_ID3_DISTANCE > hpo.maxMarkerDistance) {
						hpo.maxMarkerDistance = slpo.MARKER_ID3_DISTANCE;
					}
					if (slpo.MARKER_ID3_DISTANCE < hpo.minMarkerDistance || hpo.minMarkerDistance == 0) {
						hpo.minMarkerDistance = slpo.MARKER_ID3_DISTANCE;
					}
				}
			}
		} else {
			hpo.markerId = 0;
			if (hpo.periodLen > 1) {
				hpo.markerName = "移动中";
				hpo.markerDesc = "移动过程中，在以下标识点出现过：";
			} else {
				hpo.markerName = "非兴趣点";
				hpo.markerDesc = "不在特殊标识物附近";
			}

			int descLen = hpo.markerDesc.length();
			boolean inMark = false;
			for (Locus ls : hpo.period) {
				StaffLocusInfoPO slpo = loclRel.get(ls.LOCINFO_ID);
				String desc = "(";
				if (slpo.MIN_MARKER_ID1 != 0) {
					desc += slpo.MARKER_NAME1 + "(" + slpo.MARKER_ID1_DISTANCE + "m),";
				}
				if (slpo.MIN_MARKER_ID2 != 0) {
					desc += slpo.MARKER_NAME2 + "(" + slpo.MARKER_ID2_DISTANCE + "m),";
				}
				if (slpo.MIN_MARKER_ID3 != 0) {
					desc += slpo.MARKER_NAME3 + "(" + slpo.MARKER_ID3_DISTANCE + "m),";
				}
				if (desc.length() > 1) {
					desc = desc.substring(0, desc.length() - 1) + ")";
					hpo.markerDesc += desc + "->";
				}
				if (ls.ADDRESS_DESC != null && !ls.ADDRESS_DESC.equals(""))
					inMark = true;
			}

			if (hpo.ERROR_MSG != null && !hpo.ERROR_MSG.equals("")) {
				hpo.markerName = hpo.ERROR_MSG;// "未获取到位置";
				hpo.markerDesc = hpo.ERROR_MSG;// "GPS";
			} else if (!inMark) {
				hpo.markerName = "非兴趣点";
				hpo.markerDesc = "不在特殊标识物附近";
			}
			if (hpo.markerDesc.length() > descLen)
				hpo.markerDesc = hpo.markerDesc.substring(0, hpo.markerDesc.length() - 2);
			hpo.MARKER_LONGITUDE = 0;
			hpo.MARKER_LATITUDE = 0;
			hpo.MARKER_ELEVATION = 0;
			hpo.maxMarkerDistance = 0;
			hpo.minMarkerDistance = 0;
			if (hpo.period.size() == 1) {
				StaffLocusInfoPO slpo = loclRel.get(hpo.period.get(0).LOCINFO_ID);
				if (slpo.MIN_MARKER_ID1 != 0) {
					hpo.markerId = slpo.MIN_MARKER_ID1;
					hpo.markerName = slpo.MARKER_NAME1;
					if (slpo.MARKER_DESC1 != null && !slpo.MARKER_DESC1.equals(""))
						hpo.markerDesc = slpo.MARKER_DESC1;
				} else if (slpo.MIN_MARKER_ID2 != 0) {
					hpo.markerId = slpo.MIN_MARKER_ID2;
					hpo.markerName = slpo.MARKER_NAME2;
					if (slpo.MARKER_DESC2 != null && !slpo.MARKER_DESC2.equals(""))
						hpo.markerDesc = slpo.MARKER_DESC2;
				} else if (slpo.MIN_MARKER_ID3 != 0) {
					hpo.markerName = slpo.MARKER_NAME3;
					hpo.markerId = slpo.MIN_MARKER_ID3;
					if (slpo.MARKER_DESC3 != null && !slpo.MARKER_DESC3.equals(""))
						hpo.markerDesc = slpo.MARKER_DESC3;
				}
				if (slpo.STAFF_ACTION > 0 && slpo.STAFF_ACTION < 5) {
					hpo.markerDesc += actionNames[slpo.STAFF_ACTION - 1];
				}
			}
		}
		lastMarker.clear();
		res.add(hpo);
	}

	public static class LocusPO {
		public long S_LOCINFO_ID;
		public double S_LONGITUDE;
		public double S_LATITUDE;
		public Date S_FEEDBACK_TIME;

		public long E_LOCINFO_ID;
		public double E_LONGITUDE;
		public double E_LATITUDE;
		public Date E_FEEDBACK_TIME;
		public String ADDRESS_DESC;

		public long getS_LOCINFO_ID() {
			return S_LOCINFO_ID;
		}

		public void setS_LOCINFO_ID(long s_LOCINFO_ID) {
			S_LOCINFO_ID = s_LOCINFO_ID;
		}

		public double getS_LONGITUDE() {
			return S_LONGITUDE;
		}

		public void setS_LONGITUDE(double s_LONGITUDE) {
			S_LONGITUDE = s_LONGITUDE;
		}

		public double getS_LATITUDE() {
			return S_LATITUDE;
		}

		public void setS_LATITUDE(double s_LATITUDE) {
			S_LATITUDE = s_LATITUDE;
		}

		public Date getS_FEEDBACK_TIME() {
			return S_FEEDBACK_TIME;
		}

		public void setS_FEEDBACK_TIME(Date s_FEEDBACK_TIME) {
			S_FEEDBACK_TIME = s_FEEDBACK_TIME;
		}

		public long getE_LOCINFO_ID() {
			return E_LOCINFO_ID;
		}

		public void setE_LOCINFO_ID(long e_LOCINFO_ID) {
			E_LOCINFO_ID = e_LOCINFO_ID;
		}

		public double getE_LONGITUDE() {
			return E_LONGITUDE;
		}

		public void setE_LONGITUDE(double e_LONGITUDE) {
			E_LONGITUDE = e_LONGITUDE;
		}

		public double getE_LATITUDE() {
			return E_LATITUDE;
		}

		public void setE_LATITUDE(double e_LATITUDE) {
			E_LATITUDE = e_LATITUDE;
		}

		public Date getE_FEEDBACK_TIME() {
			return E_FEEDBACK_TIME;
		}

		public void setE_FEEDBACK_TIME(Date e_FEEDBACK_TIME) {
			E_FEEDBACK_TIME = e_FEEDBACK_TIME;
		}

		public String getADDRESS_DESC() {
			return ADDRESS_DESC;
		}

		public void setADDRESS_DESC(String aDDRESS_DESC) {
			ADDRESS_DESC = aDDRESS_DESC;
		}

	}

	/**
	 * 
	 * @param staffId
	 * @param stime
	 * @param etime
	 * @param time
	 * @param disct
	 *            间隔距离在多少内算做同一点
	 * @return
	 */
	public List<LocusPO> calcStaffLocusIgnore(long staffId, Date stime, Date etime, long time, long disct) {
		List<LocusPO> res = new ArrayList<LocusPO>();
		try {
			List<StaffLocusInfoPO> list = sldao.queryStaffLocusList(staffId, stime, etime);
			int len = list.size();
			if (len == 0)
				return res;
			LocusPO hpo = new LocusPO();
			StaffLocusInfoPO lslpo = list.get(0);
			for (int i = 1; i < len; i++) {
				StaffLocusInfoPO lspo = list.get(i);
				if (lspo.LATITUDE > 0 && lspo.LONGITUDE > 0 && lslpo.LATITUDE > 0 && lslpo.LONGITUDE > 0 && lspo.ADDRESS_DESC != null
				        && lslpo.ADDRESS_DESC != null && !lspo.ADDRESS_DESC.equals("未知") && !lslpo.ADDRESS_DESC.equals("未知")) {
					boolean isSame = false;
					if (lspo.ADDRESS_DESC.trim().equals(lslpo.ADDRESS_DESC.trim())) {
						isSame = true;
					}
					if (!isSame && disct > 0) {
						double lastDis = LngLatUtil.getDistance(lspo.LONGITUDE, lspo.LATITUDE, lslpo.LONGITUDE, lslpo.LATITUDE);
						double firstDis = 0;
						if (hpo.S_LOCINFO_ID > 0) {
							firstDis = LngLatUtil.getDistance(hpo.S_LONGITUDE, hpo.S_LATITUDE, lspo.LONGITUDE, lspo.LATITUDE);
						}
						if (lspo.ADDRESS_DESC.trim().equals(lslpo.ADDRESS_DESC.trim()) || (lastDis < disct && firstDis < disct)) {
							isSame = true;
						}
					}
					if (isSame) {
						if (hpo.S_LOCINFO_ID == 0) {
							hpo.S_LOCINFO_ID = lslpo.LOCINFO_ID;
							hpo.S_LONGITUDE = lslpo.LONGITUDE;
							hpo.S_LATITUDE = lslpo.LATITUDE;
							hpo.S_FEEDBACK_TIME = lslpo.FEEDBACK_TIME;
							hpo.ADDRESS_DESC = lslpo.ADDRESS_DESC;
						}
						hpo.E_LOCINFO_ID = lspo.LOCINFO_ID;
						hpo.E_LONGITUDE = lspo.LONGITUDE;
						hpo.E_LATITUDE = lspo.LATITUDE;
						hpo.E_FEEDBACK_TIME = lspo.FEEDBACK_TIME;
					} else if (hpo.S_LOCINFO_ID > 0) {
						res.add(hpo);
						hpo = new LocusPO();
					}
					lslpo = lspo;
				} else {
					lslpo = lspo;
				}
			}
			if (hpo.S_LOCINFO_ID > 0 && !res.contains(hpo)) {
				res.add(hpo);
			}
			if (time > 0) {
				List<LocusPO> del = new ArrayList<LocusPO>();
				for (LocusPO locusPO : res) {
					if (locusPO.E_FEEDBACK_TIME.getTime() - locusPO.S_FEEDBACK_TIME.getTime() < time) {
						del.add(locusPO);
					}
				}
				res.removeAll(del);
			}
		} catch (SQLException e) {
		}
		return res;

	}

	/**
	 * 
	 * @param staffId
	 * @param stime
	 * @param etime
	 * @param time
	 *            间隔时间，毫秒 ms
	 */
	public List<LocusPO> calcStaffLocus(long staffId, Date stime, Date etime, long time) {
		return calcStaffLocusIgnore(staffId, stime, etime, time, 0);
	}
}
