package com.sobey.mbserver.location;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sobey.base.PersistentStatePO;

public class HistoryPO {
	public Date startTime;
	public Date endTime;
	public long markerId;
	String markerName;
	public String markerDesc;
	public double MARKER_LONGITUDE;
	public double MARKER_LATITUDE;
	public double MARKER_ELEVATION;
	public double maxMarkerDistance;
	public double minMarkerDistance;
	public List<Locus> period = new ArrayList();
	public int periodLen = 0;
	public String ERROR_MSG;

	public String getERROR_MSG() {
		return this.ERROR_MSG;
	}

	public void setERROR_MSG(String eRROR_MSG) {
		this.ERROR_MSG = eRROR_MSG;
	}

	public int getPeriodLen() {
		return this.periodLen;
	}

	public void setPeriodLen(int periodLen) {
		this.periodLen = periodLen;
	}

	public Date getStartTime() {
		return this.startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return this.endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getMarkerName() {
		return this.markerName;
	}

	public void setMarkerName(String markerName) {
		this.markerName = markerName;
	}

	public String getMarkerDesc() {
		return this.markerDesc;
	}

	public void setMarkerDesc(String markerDesc) {
		this.markerDesc = markerDesc;
	}

	public long getMarkerId() {
		return this.markerId;
	}

	public void setMarkerId(long markerId) {
		this.markerId = markerId;
	}

	public double getMARKER_LONGITUDE() {
		return this.MARKER_LONGITUDE;
	}

	public void setMARKER_LONGITUDE(double mARKER_LONGITUDE) {
		this.MARKER_LONGITUDE = mARKER_LONGITUDE;
	}

	public double getMARKER_LATITUDE() {
		return this.MARKER_LATITUDE;
	}

	public void setMARKER_LATITUDE(double mARKER_LATITUDE) {
		this.MARKER_LATITUDE = mARKER_LATITUDE;
	}

	public double getMARKER_ELEVATION() {
		return this.MARKER_ELEVATION;
	}

	public void setMARKER_ELEVATION(double mARKER_ELEVATION) {
		this.MARKER_ELEVATION = mARKER_ELEVATION;
	}

	public double getMaxMarkerDistance() {
		return this.maxMarkerDistance;
	}

	public void setMaxMarkerDistance(double maxMarkerDistance) {
		this.maxMarkerDistance = maxMarkerDistance;
	}

	public double getMinMarkerDistance() {
		return this.minMarkerDistance;
	}

	public void setMinMarkerDistance(double minMarkerDistance) {
		this.minMarkerDistance = minMarkerDistance;
	}

	public List<Locus> getPeriod() {
		return this.period;
	}

	public void setPeriod(List<Locus> period) {
		this.period = period;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("startTime=" + this.startTime.toLocaleString());
		sb.append("  endTime=" + this.endTime.toLocaleString());
		sb.append("  markerId=" + this.markerId);
		sb.append("  markerName=" + this.markerName);
		sb.append("  markerDesc=" + this.markerDesc);
		sb.append("  MARKER_LONGITUDE=" + this.MARKER_LONGITUDE);
		sb.append("  MARKER_LATITUDE=" + this.MARKER_LATITUDE);
		sb.append("  MARKER_ELEVATION=" + this.MARKER_ELEVATION);
		sb.append("  maxMarkerDistance=" + this.maxMarkerDistance);
		sb.append("  period={");
		for (Locus l : this.period) {
			sb.append("  " + l.toString());
		}
		sb.append("}\n");
		return sb.toString();
	}

	public static class Locus extends PersistentStatePO {
		public long LOCINFO_ID;
		public double LONGITUDE;
		public double LATITUDE;
		public double ELEVATION;
		public Date FEEDBACK_TIME;
		public int STAFF_ACTION;
		public String ADDRESS_DESC;
		public String ERROR_MSG;

		public String getERROR_MSG() {
			return this.ERROR_MSG;
		}

		public void setERROR_MSG(String eRROR_MSG) {
			set(eRROR_MSG);
		}

		public long getLOCINFO_ID() {
			return this.LOCINFO_ID;
		}

		public void setLOCINFO_ID(long lOCINFO_ID) {
			set(Long.valueOf(lOCINFO_ID));
		}

		public double getLONGITUDE() {
			return this.LONGITUDE;
		}

		public void setLONGITUDE(double lONGITUDE) {
			set(Double.valueOf(lONGITUDE));
		}

		public double getLATITUDE() {
			return this.LATITUDE;
		}

		public void setLATITUDE(double lATITUDE) {
			set(Double.valueOf(lATITUDE));
		}

		public double getELEVATION() {
			return this.ELEVATION;
		}

		public void setELEVATION(double eLEVATION) {
			set(Double.valueOf(eLEVATION));
		}

		public Date getFEEDBACK_TIME() {
			return this.FEEDBACK_TIME;
		}

		public void setFEEDBACK_TIME(Date fEEDBACK_TIME) {
			set(fEEDBACK_TIME);
		}

		public int getSTAFF_ACTION() {
			return this.STAFF_ACTION;
		}

		public void setSTAFF_ACTION(int sTAFF_ACTION) {
			set(Integer.valueOf(sTAFF_ACTION));
		}

		public String getADDRESS_DESC() {
			return this.ADDRESS_DESC;
		}

		public void setADDRESS_DESC(String aDDRESS_DESC) {
			set(aDDRESS_DESC);
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("LOCINFO_ID=" + this.LOCINFO_ID);
			sb.append(",STAFF_ACTION=" + this.STAFF_ACTION);
			sb.append(",LONGITUDE=" + this.LONGITUDE);
			sb.append(",LATITUDE=" + this.LATITUDE);
			sb.append(",ELEVATION=" + this.ELEVATION);
			sb.append(",FEEDBACK_TIME=" + this.FEEDBACK_TIME.toLocaleString());
			sb.append(",ERROR_MSG=" + this.ERROR_MSG);
			return sb.toString();
		}
	}
}