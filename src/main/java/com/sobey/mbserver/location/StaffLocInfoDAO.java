package com.sobey.mbserver.location;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.sobey.base.BaseDao;
import com.sobey.base.exception.POException;
import com.sobey.mbserver.location.HistoryPO.Locus;

/**
 * 针对M_COMPANY的操作，需要序列 SEQ_COMPANY_ID
 * 
 * @author hans
 * 
 */
public class StaffLocInfoDAO extends BaseDao<StaffLocInfoPO> {

	public StaffLocInfoDAO() throws POException {
		super(StaffLocInfoPO.class);
	}

	static java.text.SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	static java.text.SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public List<StaffLocusInfoPO> queryStaffLocusList(long staffId, Date time) throws SQLException {
		String sql = "SELECT  l.LOCINFO_ID,l.STAFF_ID,l.MIN_MARKER_ID1,l.MIN_MARKER_ID2,l.MIN_MARKER_ID3,l.STAFF_MOBILE,l.LONGITUDE,l.LATITUDE,l.ELEVATION, "
		        + "l.FEEDBACK_TIME,l.STAFF_ACTION,l.MARKER_ID1_DISTANCE,l.MARKER_ID2_DISTANCE,l.MARKER_ID3_DISTANCE,l.ADDRESS_DESC,l.ERROR_MSG"
		        + ",m1.MARKER_TYPE MARKER_TYPE1,m1.MARKER_NAME MARKER_NAME1,m1.MARKER_SRC_ID MARKER_SRC_ID1,m1.MARKER_DESC MARKER_DESC1,m1.DISTANCE_RANGE DISTANCE_RANGE1,m1.LONGITUDE LONGITUDE1,m1.LATITUDE LATITUDE1           ,m1.ELEVATION ELEVATION1"
		        + ",m2.MARKER_TYPE MARKER_TYPE2,m2.MARKER_NAME MARKER_NAME2,m2.MARKER_SRC_ID MARKER_SRC_ID2,m2.MARKER_DESC MARKER_DESC2,m2.DISTANCE_RANGE DISTANCE_RANGE2,m2.LONGITUDE LONGITUDE2,m2.LATITUDE LATITUDE2           ,m2.ELEVATION ELEVATION2"
		        + ",m3.MARKER_TYPE MARKER_TYPE3,m3.MARKER_NAME MARKER_NAME3,m3.MARKER_SRC_ID MARKER_SRC_ID3,m3.MARKER_DESC MARKER_DESC3,m3.DISTANCE_RANGE DISTANCE_RANGE3,m3.LONGITUDE LONGITUDE3,m3.LATITUDE LATITUDE3           ,m3.ELEVATION ELEVATION3  "
		        + "  FROM  M_COMPANY_STAFF_LOCINFO l left join M_MARKER_LOCATION m1 on l.MIN_MARKER_ID1=m1.MARKER_ID"
		        + " left join M_MARKER_LOCATION m2 on l.MIN_MARKER_ID2=m2.MARKER_ID "
		        + " left join M_MARKER_LOCATION m3 on l.MIN_MARKER_ID3=m3.MARKER_ID       "
		        + "  WHERE  l.staff_id=?  and DATE_FORMAT(FEEDBACK_TIME,'%Y%m%d') =? order by FEEDBACK_TIME";
		return (List<StaffLocusInfoPO>) getDataAccess().queryForBeanList(sql, StaffLocusInfoPO.class, staffId, sdf.format(time));
	}

	/**
	 * 查询最近的位置信息
	 * 
	 * @param staffId
	 * @param time
	 * @return
	 * @throws SQLException
	 */
	public List<StaffLocusInfoPO> queryLastLocus(long staffId, Date time) throws SQLException {
		String sql = "SELECT  l.LOCINFO_ID,l.STAFF_ID,l.MIN_MARKER_ID1,l.MIN_MARKER_ID2,l.MIN_MARKER_ID3,l.STAFF_MOBILE,l.LONGITUDE,l.LATITUDE,l.ELEVATION, "
		        + "l.FEEDBACK_TIME,l.STAFF_ACTION,l.MARKER_ID1_DISTANCE,l.MARKER_ID2_DISTANCE,l.MARKER_ID3_DISTANCE,l.ADDRESS_DESC,l.ERROR_MSG"
		        + ",m1.MARKER_TYPE MARKER_TYPE1,m1.MARKER_NAME MARKER_NAME1,m1.MARKER_SRC_ID MARKER_SRC_ID1,m1.MARKER_DESC MARKER_DESC1,m1.DISTANCE_RANGE DISTANCE_RANGE1,m1.LONGITUDE LONGITUDE1,m1.LATITUDE LATITUDE1           ,m1.ELEVATION ELEVATION1"
		        + ",m2.MARKER_TYPE MARKER_TYPE2,m2.MARKER_NAME MARKER_NAME2,m2.MARKER_SRC_ID MARKER_SRC_ID2,m2.MARKER_DESC MARKER_DESC2,m2.DISTANCE_RANGE DISTANCE_RANGE2,m2.LONGITUDE LONGITUDE2,m2.LATITUDE LATITUDE2           ,m2.ELEVATION ELEVATION2"
		        + ",m3.MARKER_TYPE MARKER_TYPE3,m3.MARKER_NAME MARKER_NAME3,m3.MARKER_SRC_ID MARKER_SRC_ID3,m3.MARKER_DESC MARKER_DESC3,m3.DISTANCE_RANGE DISTANCE_RANGE3,m3.LONGITUDE LONGITUDE3,m3.LATITUDE LATITUDE3           ,m3.ELEVATION ELEVATION3  "
		        + "  FROM  M_COMPANY_STAFF_LOCINFO l left join M_MARKER_LOCATION m1 on l.MIN_MARKER_ID1=m1.MARKER_ID"
		        + " left join M_MARKER_LOCATION m2 on l.MIN_MARKER_ID2=m2.MARKER_ID "
		        + " left join M_MARKER_LOCATION m3 on l.MIN_MARKER_ID3=m3.MARKER_ID       "
		        + "  WHERE  l.staff_id=?  and FEEDBACK_TIME>? order by FEEDBACK_TIME desc";
		return this.getDataAccess().queryForBeanList(sql, StaffLocusInfoPO.class, staffId, sdf2.format(time));
	}

	public List<Locus> queryLocInfoAddres() {
		String sql = "SELECT LOCINFO_ID,LONGITUDE,LATITUDE ,ELEVATION,FEEDBACK_TIME,STAFF_ACTION, ADDRESS_DESC,ERROR_MSG from M_COMPANY_STAFF_LOCINFO where ADDRESS_DESC is null and  LONGITUDE!=0 and LATITUDE!=0 ";
		return this.getDataAccess().queryForBeanList(sql, Locus.class);
	}

	public void updateLocu(Locus ls) {
		String sql = "update M_COMPANY_STAFF_LOCINFO set ADDRESS_DESC=? where LOCINFO_ID=?";
		getDataAccess().execNoQuerySql(sql, ls.ADDRESS_DESC, ls.LOCINFO_ID);
	}

	public void updateLocus(List<Locus> list) {
		String sql = "update M_COMPANY_STAFF_LOCINFO set ADDRESS_DESC=? where LOCINFO_ID=?";
		List<String> desc = new ArrayList<String>();
		List<Long> ids = new ArrayList<Long>();
		for (Locus ls : list) {
			if (ls.ADDRESS_DESC != null && !ls.ADDRESS_DESC.equals("")) {
				desc.add(ls.ADDRESS_DESC);
				ids.add(ls.LOCINFO_ID);
			}
		}
		Object[][] params = new Object[desc.size()][2];
		for (int i = 0; i < params.length; i++) {
			params[i][0] = desc.get(i);
			params[i][1] = ids.get(i);
		}
		getDataAccess().execUpdateBatch(sql, params);
	}

	public Locus queryLastLoc(long staffId) {
		String sql = "SELECT LOCINFO_ID,LONGITUDE,LATITUDE,ELEVATION,FEEDBACK_TIME,STAFF_ACTION, ADDRESS_DESC,ERROR_MSG FROM M_COMPANY_STAFF_LOCINFO WHERE LONGITUDE>0 and STAFF_ID=? ORDER BY FEEDBACK_TIME DESC LIMIT 0,1";
		return this.getDataAccess().queryForBean(sql, Locus.class, staffId);
	}

	public StaffLocusInfoPO queryLastVaildLocu(long staffId) throws SQLException {
		List<StaffLocusInfoPO> locuInfos = this.queryLastLocus(staffId, new Date(System.currentTimeMillis() - 33 * 60 * 1000));
		if (null == locuInfos || locuInfos.size() == 0) {
			return null;
		}
		StaffLocusInfoPO validLocuInfo = null; // 有效位置信息
		for (int i = 0; i < locuInfos.size(); i++) {
			StaffLocusInfoPO info = locuInfos.get(i);
			if (info.getLONGITUDE() != 0 && info.getLATITUDE() != 0) { // 经纬度不为0
				if (i == 0) { // 最新一条
					validLocuInfo = info;
					break;
				} else if (info.getFEEDBACK_TIME().getTime() > (System.currentTimeMillis() - 10 * 60 * 1000)) { // 不是最新的但是是最近10分钟
					validLocuInfo = info;
					break;
				}
			}
		}
		if (null == validLocuInfo && locuInfos.get(0).getFEEDBACK_TIME().getTime() > (System.currentTimeMillis() - 5 * 60 * 1000)) { // 5分钟内
			validLocuInfo = locuInfos.get(0);
		}
		return validLocuInfo;
	}

	public List<Map<String, Object>> queryStaffLocus4Day(long staffId, Date time) throws SQLException {
		String sql = "SELECT l.LOCINFO_ID,l.LONGITUDE,l.LATITUDE,l.ELEVATION,l.FEEDBACK_TIME,l.STAFF_ACTION, l.ADDRESS_DESC,l.ERROR_MSG,m.MARKER_NAME FROM M_COMPANY_STAFF_LOCINFO l "
		        + "left join M_MARKER_LOCATION m on l.MIN_MARKER_ID1=m.MARKER_ID "
		        + "WHERE l.LONGITUDE>0 AND l.LATITUDE>0 AND l.staff_id=? AND DATE_FORMAT(l.FEEDBACK_TIME,'%Y%m%d') =? order by l.FEEDBACK_TIME";
		return this.getDataAccess().queryForList(sql, staffId, sdf.format(time));
	}

	public List<StaffLocusInfoPO> queryStaffLocusList(long staffId, Date stime, Date etime) throws SQLException {
		String sql = "SELECT  l.LOCINFO_ID,l.STAFF_ID,l.MIN_MARKER_ID1,l.MIN_MARKER_ID2,l.MIN_MARKER_ID3,l.STAFF_MOBILE,l.LONGITUDE,l.LATITUDE,l.ELEVATION, "
		        + "l.FEEDBACK_TIME,l.STAFF_ACTION,l.MARKER_ID1_DISTANCE,l.MARKER_ID2_DISTANCE,l.MARKER_ID3_DISTANCE,l.ADDRESS_DESC,l.ERROR_MSG "
		        + " FROM  M_COMPANY_STAFF_LOCINFO l   " + "  WHERE  l.staff_id=?  and FEEDBACK_TIME >=? and FEEDBACK_TIME <=? order by FEEDBACK_TIME";
		return this.getDataAccess().queryForBeanList(sql, StaffLocusInfoPO.class, staffId, new Timestamp(stime.getTime()), new Timestamp(etime.getTime()));
	}
}
