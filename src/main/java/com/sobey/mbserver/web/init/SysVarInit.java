package com.sobey.mbserver.web.init;

import java.io.File;
import java.util.Properties;

import javax.servlet.ServletContext;

import com.sobey.jcg.support.log4j.LogUtils;
import com.sobey.jcg.support.sys.SystemConstant;
import com.sobey.jcg.support.sys.SystemVariable;
import com.sobey.jcg.support.web.ISystemStart;
import com.sobey.jcg.support.web.init.SystemVariableInit;

public class SysVarInit implements ISystemStart {
	private ServletContext servletContext;

	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	@Override
	public void init() {
		// 加载webroot路径
		SystemVariableInit.WEB_ROOT_PATH = servletContext.getRealPath("/");
		// 加载系统classpath
		// CLASS_PATH = this.getClass().getResource("/").toString();
		// CLASS_PATH = java.net.URLDecoder.decode(CLASS_PATH);
		SystemVariableInit.CLASS_PATH = servletContext.getRealPath("/WEB-INF/classes") + File.separator;
		SystemConstant.setSYS_CONF_FILE("support.properties");
		SystemVariable.init();// 先加载
		LogUtils.info("WEB_ROOT_PATH=" + SystemVariableInit.WEB_ROOT_PATH);
		LogUtils.info("CLASS_PATH=" + SystemVariableInit.CLASS_PATH);
		// 加载properties配置文件
		String proFile = servletContext.getInitParameter("conf_props");
		if (proFile != null) {
			String[] props = proFile.split(",");
			for (String conf : props) {
				File file = new File(SystemVariableInit.CLASS_PATH, conf);
				SystemVariableInit.load(file);
			}
			SystemVariableInit.load(new File(System.getProperty("java.home"), "local.properties"));
		}
		// SystemVariable.init();
		SystemVariable.DSID = "" + SystemVariable.getDefaultDataSourceID();// 管理库DSID
		// 从zk获取
		// SysVar.zkClient=null;
		SysVar.setLogLevel();
	}

	@Override
	public void destory() {
		Properties conf = SystemVariable.getProperties();
		if (conf != null)
			conf.clear();
	}

}
