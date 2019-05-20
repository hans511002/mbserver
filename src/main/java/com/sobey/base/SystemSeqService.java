package com.sobey.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sobey.base.util.HasThread;
import com.sobey.base.util.ToolUtil;
import com.sobey.jcg.support.jdbc.DataAccess;
import com.sobey.jcg.support.sys.DataSourceManager;
import com.sobey.jcg.support.sys.SystemVariable;
import com.sobey.jcg.support.utils.Convert;

//集群需要使用外部缓存
public class SystemSeqService extends HasThread {
	public static final Log LOG = LogFactory.getLog(SystemSeqService.class.getName());

	public static class SeqValue {
		public long seq = 0;
		public List<String> usedTableFields;
	}

	public static Map<String, SeqValue> seqMutex = new HashMap<String, SeqValue>();

	// 集群需要使用外部缓存
	public static SystemSeqService.SeqValue getSeq(String seqName, String... tableKeyColName) {
		SystemSeqService.SeqValue seqv = SystemSeqService.seqMutex.get(seqName);
		if (seqv == null) {
			synchronized (SystemSeqService.seqMutex) {
				seqv = (SystemSeqService.SeqValue) SystemSeqService.seqMutex.get(seqName);
				if (seqv == null) {
					seqv = new SystemSeqService.SeqValue();
					SystemSeqService.seqMutex.put(seqName, seqv);
					for (String string : tableKeyColName) {
						if (!seqv.usedTableFields.contains(string))
							seqv.usedTableFields.add(string);
					}
				}
			}
		} else {
			if (tableKeyColName.length > 0) {
				synchronized (seqv) {
					for (String string : tableKeyColName) {
						if (!seqv.usedTableFields.contains(string))
							seqv.usedTableFields.add(string);
					}
				}
			}
		}
		return seqv;
	}

	// 集群需要使用外部缓存
	public static long createAndGetSeq(String seqName, int step, String... tableKeyColName) {
		if (step < 1) {
			step = 1;
		}
		SeqValue seqv = getSeq(seqName, tableKeyColName);
		synchronized (seqv) {
			seqv.seq += step;
			return seqv.seq;
		}
	}

	public static SystemSeqService.SeqValue addSeqTableField(SeqValue seqv, String... tableKeyColName) {
		if (tableKeyColName != null && tableKeyColName.length > 0) {
			synchronized (seqv) {
				for (String string : tableKeyColName) {
					if (!seqv.usedTableFields.contains(string))
						seqv.usedTableFields.add(string);
				}
			}
		}
		return seqv;
	}

	public SystemSeqService() {
		initSeq();
	}

	// 初始时读取序列入内存
	public void initSeq() {
		synchronized (SystemSeqService.seqMutex) {
			try {
				DataAccess access = new DataAccess(DataSourceManager.getConnection(SystemVariable.getString("db.default", "config1")));
				List<Map<String, Object>> seqs = access.queryForList("SELECT SEQ,SEQ_NAME,USETABLE_FIELD FROM SYS_SEQ  ");
				for (Map<String, Object> map : seqs) {
					String seqName = map.get("SEQ_NAME").toString();
					long seq = Convert.toLong(map.get("SEQ"), 1);
					String useTableField = Convert.toString(map.get("USETABLE_FIELD"), "");
					String useTableFields[] = useTableField.split(",");
					if (!useTableField.isEmpty()) {// 从数据库读取初始化值 ,解决内存中刷新不及时的问题
						for (String tableField : useTableFields) {
							String sql = "select max(";
							String tmp[] = tableField.split("\\.");
							sql += tmp[1] + ") MAX_ID from " + tmp[0];
							long sq = access.queryForLongByNvl(sql, 1l);
							if (seq < sq) {
								seq = sq;
							}
						}
					}
					SystemSeqService.SeqValue seqv = new SeqValue();
					seqv.seq = seq;
					for (String string : useTableFields) {
						seqv.usedTableFields.add(string);
					}
					SystemSeqService.seqMutex.put(seqName, seqv);
				}
			} catch (Exception e) {
				LOG.error("初始化序列异常", e);
				throw e;
			} finally {
				DataSourceManager.destroy();
			}
		}
	}

	@Override
	public void run() {
		while (SystemInit.isRuning) {
			flushSeq();
			ToolUtil.sleep(600000);
		}
	}

	public void flushSeq() {
		try {
			DataAccess access = new DataAccess(DataSourceManager.getConnection(SystemVariable.getString("db.default", "config1")));
			List<Map<String, Object>> seqs = access.queryForList("SELECT SEQ,SEQ_NAME,USETABLE_FIELD FROM SYS_SEQ  ");
			Map<String, Boolean> existSeqs = new HashMap<String, Boolean>();
			for (Map<String, Object> map : seqs) {
				existSeqs.put(map.get("SEQ_NAME").toString(), true);
			}
			for (String seqName : SystemSeqService.seqMutex.keySet()) {
				SystemSeqService.SeqValue seqv = SystemSeqService.seqMutex.get(seqName);
				if (existSeqs.containsKey(seqName)) {
					String sql = "update SYS_SEQ set seq=? , USETABLE_FIELD=? where SEQ_NAME=?";
					access.execUpdate(sql, seqv.seq, ToolUtil.Join(seqv.usedTableFields), seqName);
				} else {
					String sql = "insert into SYS_SEQ (seq,USETABLE_FIELD,SEQ_NAME) values(?,?,?)";
					access.execUpdate(sql, seqv.seq, ToolUtil.Join(seqv.usedTableFields), seqName);
				}
			}
		} catch (Exception e) {
			LOG.error("更新内存中的序列值到数据库表异常", e);
		} finally {
			DataSourceManager.destroy();
		}
	}

	public void destory() {
		flushSeq();
	}
}
