package com.sobey.mbserver.web.init;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sobey.jcg.support.utils.Convert;
import com.sobey.mbserver.util.DateUtils;

public class InstallConstant {

	// 必选配置项
	public static final String ZK_CONNECT_KEY = "zk.connect";// zk连接
	public static final String ZK_URL_TYPE_KEY = "zk.url.type";// zk地址类型
	public static final String ZK_BASE_ZONE_KEY = "zk.base.node";
	public static final String CLUSTER_NAME_KEY = "cluster.name";
	public static final String DEPLOY_HOST_ETC_KEY = "deploy.host.etc.dir";

	// 可选配置项
	public static final String HOST_NAME_KEY = "host.name";// 集群当前机器IP
	public static final String HOST_IP_KEY = "host.ip";// 集群当前机器IP
	public static final String UI_ENABLE_KEY = "ui.enable";// 是否提供ui服务
	public static final String UI_PORT_KEY = "ui.port";// ui端口http://host_name:port
	public static final String UI_HTTPS_PORT_KEY = "ui.https.port";// ui端口http://host_name:port
	public static final String PROXY_PORT_KEY = "proxy.port";

	public static final String INSTALL_LANG = "installer.lang";
	public static final String ZK_CONNECT_TIMEOUT_MS_KEY = "zk.connect.timeout.ms";
	public static final String ZK_SESSION_TIMEOUT_MS_KEY = "zk.session.timeout.ms";
	public static final String ZK_DATA_COMPRESSION_TYPE_KEY = "zk.data.compression.type";
	public static final String ZK_DATA_URLENCODE_ENABLED_KEY = "zk.data.urlencode.enabled";

	public static final String DEPLOY_ZKCHECK_STATUS_TIMEOUT_KEY = "deployactor.zkcheck.status.timeout";

	public static final String INSTALL_CONFIG_PATH_KEY = "install.config.path";
	public static final String DEPLOY_LOCAL_APPSRC_SYNC_FLAG_KEY = "deployactor.cluster.local.appsrc.sync.flag";
	public static final String DEPLOY_LOCAL_APPSRC_SYNC_SCP_KEY = "deployactor.cluster.local.appsrc.sync.scp";

	public static final String DEPLOY_SYSTEM_WEB_DELFILES_KEY = "deployactor.system.web.delfiles";
	public static final String DEPLOY_CLUSTER_NODE_MANAGER_TYPE_KEY = "deployactor.cluster.node.manager.type";
	public static final String DEPLOY_ADD_HOST_RERUN_DOCKER_FALG_KEY = "deployactor.addnode.docker.rerun.flag";
	public static final String DEPLOY_CLUSTER_NODESTATUS_IMITATE_SWARM_KEY = "deployactor.cluster.nodestatus.imite_swarm";
	public static final String DEPLOY_CLUSTER_ADD_NODE_FAILED_FLAG_KEY = "deployactor.cluster.add.node.failed.delete.flag";
	public static final String DEPLOY_BACK_SYNC_THREAD_NUM_KEY = "deployactor.back.sync.threads";
	public static final String DEPLOY_INSTALL_ENV_FLUSH_INTERVAL_KEY = "deployactor.install.env.flush.interval";
	public static final String DEPLOY_SERVICE_FLUSH_INTERVAL_KEY = "deployactor.service.flush.interval";
	public static final String DEPLOY_APPSTATUSE_FLUSH_INTERVAL_KEY = "deployactor.appstatus.flush.interval";
	public static final String DEPLOY_DOCKER_PS_ENABLE_KEY = "deployactor.docker.ps.enable";
	public static final String DEPLOY_SYSTEM_RESOURCES_FLUSH_INTERVAL_KEY = "deployactor.system.resources.flush.interval";
	public static final String DEPLOY_APPRESOURCES_LEVEL_KEY = "deployactor.app.resource.monitor.type";
	public static final String DEPLOY_APPRESOURCES_ZK_SIZE_KEY = "deployactor.resource.monitor.zk.size";
	public static final String DEPLOY_RESOURCE_DOCKER_STATS_KEY = "deployactor.resource.docker.stats.flag";

	public static final String DEPLOY_PAASLOG_DBSUMMARY_DAYS_KEY = "deployactor.paaslog.dbsummary.days";
	public static final String DEPLOY_APPRESOURCES_API_SZIE_KEY = "deployactor.resource.monitor.api.size";
	public static final String DEPLOY_APPSTATUSE_FLUSH_TIMEOUT_KEY = "deployactor.appstatus.flush.timout";
	public static final String DEPLOY_NODEINFO_FLUSH_INTERVAL_KEY = "deployactor.nodeinfo.flush.interval";
	public static final String DEPLOY_LB_SUMMARY_INTERVAL_KEY = "deployactor.lb.summary.flush.interval";
	public static final String DEPLOY_LB_DETAIL_INTERVAL_KEY = "deployactor.lb.detail.flush.interval";
	public static final String DEPLOY_LB_APP_SUMMARY_LOGOUT_KEY = "deployactor.lb.app.summary.outlog.flag";
	public static final String DEPLOY_LBS_LISTEN_INTERVAL_KEY = "deployactor.lbs.listen.interval";
	public static final String DEPLOY_SYSTEM_RESOURCES_REALTIME_INTERVAL_KEY = "deployactor.system.resources.realtime.interval";

	public static final String DEPLOY_CLUSTER_INIT_APP_ILIST_KEY = "deployactor.firstinit.app.list";
	public static final String DEPLOY_CLUSTER_ADDNODE_APP_ILIST_KEY = "deployactor.addhost.app.list";
	public static final String DEPLOY_GOBAL_CONFIG_FILELS_KEY = "deployactor.gobal.config.files";
	public static final String DEPLOY_SYSTEM_ROOT_SUDO_APPS_KEY = "deployactor.system.root.apps";
	public static final String DEPLOY_INSTALL_TMP_DIR_KEY = "deployactor.install.temp.dir";
	public static final String DEPLOY_SHARE_ROOTPATH_KEY = "deployactor.share.root.path";

	public static final String DEPLOY_ELASTIC_FLAG_KEY = "deployactor.elastic.flag";
	public static final String DEPLOY_ELASTIC_INTERVAL_KEY = "deployactor.elastic.interval";
	public static final String DEPLOY_ELASTIC_APP_ALERTTYPE_KEY = "deployactor.elastic.app.alert.type";
	public static final String DEPLOY_ELASTIC_HOST_SCORE_DISPERSE_KEY = "deployactor.elastic.host.score.disperse";
	public static final String DEPLOY_ELASTIC_APP_SCORE_DISPERSE_KEY = "deployactor.elastic.app.score.disperse";
	public static final String DEPLOY_ELASTIC_APP_ALERT_DISPERSE_KEY = "deployactor.elastic.app.alert.disperse";

	public static final String DEPLOY_ELASTIC_IAAS_CLASS_KEY = "deployactor.elastic.iaas.class";
	public static final String DEPLOY_ELASTIC_REDUCE_TYPE_KEY = "deployactor.elastic.reduce.type";
	public static final String DEPLOY_ELASTIC_REDUCE_BASE_SIZE_KEY = "deployactor.elastic.reduce.base.size";
	public static final String DEPLOY_ELASTIC_HOST_ALERT_SCORES_KEY = "deployactor.elastic.host.alert.scores";
	public static final String DEPLOY_ELASTIC_APP_ALERT_SCORES_KEY = "deployactor.elastic.app.alert.scores";
	public static final String DEPLOY_ELASTIC_REFRESH_HAPROXY_FLAG_KEY = "deployactor.elastic.refresh.haproxy.flag";

	public static final String DEPLOY_COPY_APP_FALG_KEY = "deployactor.app.copyflag";
	public static final String DEPLOY_COPY_APP_DISABLED_KEY = "deployactor.copy.app.disabled";

	public static final String DEPLOY_APP_MANUAL_START_KEY = "deployactor.app.manual.start";

	public static ObjectMapper objectMapper = new ObjectMapper();

	// public static java.text.SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	// public static java.text.SimpleDateFormat yyyy = new SimpleDateFormat("yyyy");
	// public static java.text.SimpleDateFormat yyyymm = new SimpleDateFormat("yyyyMM");
	// public static java.text.SimpleDateFormat yyyymmdd = new SimpleDateFormat("yyyyMMdd");
	// public static long utsTiime = new Date(70, 0, 1).getTime();
	// public static String utsTiimeString = sdf.format(new Date(70, 0, 1));

	public static <T> T clone(T obj, Class<T> clz) throws IOException {
		if (obj == null)
			return (T) null;
		return objectMapper.readValue(objectMapper.writeValueAsString(obj), clz);
	}

	public static Date getNow() {
		return new Date(System.currentTimeMillis());
	}

	public static String getNowString() {
		return DateUtils.format(new Date(System.currentTimeMillis()));
	}

	public static String getNowYM() {
		return DateUtils.format(DateUtils.yyyymm, new Date(System.currentTimeMillis()));
	}

	public static String getDayNo() {
		return DateUtils.format(DateUtils.yyyymmdd, new Date(System.currentTimeMillis()));
	}

	// 2017-07-17T02:57:47.05841808Z
	static Pattern utcDateReg = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2})((\\.\\d+)Z)?");

	public static long convertDateLong(String str) {
		Matcher m = utcDateReg.matcher(str);
		if (m.find()) {
			int year = Convert.toInt(m.group(1), 0);
			int month = Convert.toInt(m.group(2), 0) - 1;
			int day = Convert.toInt(m.group(3), 0);
			int hh = Convert.toInt(m.group(4), 0);
			int mi = Convert.toInt(m.group(5), 0);
			int ss = Convert.toInt(m.group(6), 0);
			String sss = m.group(8);
			double ssss = Convert.toDouble(sss, 0);
			Calendar c = new GregorianCalendar(year, month, day, hh, mi, ss);
			c.add(Calendar.HOUR_OF_DAY, 8);
			return (long) (c.getTimeInMillis() + ssss * 1000);
		} else {
			return 0;
		}
	}
}
