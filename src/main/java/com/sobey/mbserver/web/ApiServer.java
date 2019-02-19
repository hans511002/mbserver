package com.sobey.mbserver.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import com.sobey.jcg.sobeyhive.main.DaemonMaster;
import com.sobey.jcg.support.log4j.LogUtils;
import com.sobey.mbserver.mgr.api.MgrServletContainer;
import com.sobey.mbserver.mgr.servlet.ZkctlServlet;
import com.sobey.mbserver.web.init.SysConfig;

public class ApiServer {

	private DaemonMaster master;
	private Server server;
	private boolean started;
	public boolean runned = false;
	WebAppContext web;
	WebAppContext mgr;
	public static String webApp;

	public ApiServer(DaemonMaster master) throws IOException {
		this.master = master;
		this.server = new Server();
		HttpConfiguration http_config = new HttpConfiguration();
		http_config.setSecureScheme("https");
		ServerConnector httpConnector = new ServerConnector(server, 10, 10);
		httpConnector.setPort(SysConfig.getUiPort());
		server.addConnector(httpConnector);

		webApp = this.getClass().getResource("/webapp").getPath();
		File file = new File(webApp);
		LogUtils.debug("webApp======" + webApp + "==file.exists=" + file.exists());

		if (!file.exists()) {
			if (webApp.startsWith("file:/")) {
				webApp = webApp.substring(5);
			}
			if (webApp.indexOf(".jar!/") > 0) {
				JarFile jar = new JarFile(webApp.substring(0, webApp.indexOf(".jar!/") + 4));
				Enumeration<JarEntry> entrys = jar.entries();
				String home = SysConfig.getInstallHome();
				while (entrys.hasMoreElements()) {
					JarEntry entry = entrys.nextElement();
					if (entry.getName().startsWith("webapp")) {
						File appDir = new File(home + File.separator + "tmp" + File.separator + entry.getName());
						if (entry.isDirectory()) {
							appDir.mkdirs();
						} else {
							InputStream input = jar.getInputStream(entry);
							byte[] buf = new byte[4096];
							int len = 0;
							FileOutputStream out = new FileOutputStream(appDir);
							while ((len = input.read(buf)) > 0) {
								out.write(buf, 0, len);
							}
							input.close();
							out.close();
						}
					}
				}
				webApp = home + File.separator + "tmp" + File.separator + "webapp";
			}
		}
		String webBase = webApp + File.separator + "web";
		web = new WebAppContext(webBase, "/web");
		web.setDisplayName("web");
		web.setContextPath("/web");
		web.setWelcomeFiles(new String[] { "index.html", "deployLog.html", "installLog.html" });
		web.setDescriptor("WEB-INF/web.xml");
		// web.setWar(webBase);
		web.setParentLoaderPriority(true);
		web.setAllowNullPathInfo(true);

		String mgrBase = webApp + File.separator + "mgr";
		mgr = new WebAppContext(mgrBase, "/mgr");
		mgr.setDisplayName("mgr");
		mgr.setWelcomeFiles(new String[] { "index.html" });
		mgr.setDescriptor(mgrBase + "/WEB-INF/web.xml");
		// mgr.setWar(mgrBase);

		web.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "true");
		mgr.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");
		HandlerCollection hc = new HandlerCollection();
		server.setHandler(hc);
		// ((HandlerCollection) server.getHandler()).addHandler(web);
		hc.addHandler(web);
		hc.addHandler(mgr);
		web.setTempDirectory(new File(SysConfig.getTempDir() + File.separator + "web"));
		mgr.setTempDirectory(new File(SysConfig.getTempDir() + File.separator + "mgr"));

		// mgr.setAttribute("org.eclipse.jetty.webapp.basetempdir", SysConfig.getTempDir());
		// mgr.setAttribute("javax.servlet.context.tempdir", SysConfig.getTempDir() + File.separator + "mgr");
		// mgr.setPersistTempDirectory(true);

		if (SysConfig.getHttpsPort() > 0 && new File(SysConfig.getInstallHome() + File.separator + "crt" + File.separator + "keystore").exists()) {
			HttpConfiguration https_config = new HttpConfiguration();
			https_config.setSecureScheme("https");
			// keytool -keystore keystore -alias jetty -genkey -keyalg RSA
			// keytool -export -alias jetty -file jetty.crt -keystore keystore
			// java -cp
			// /sobeyhive/app/installer-1.3.0/lib/jetty/*:/sobeyhive/app/installer-1.3.0/lib/jetty/jetty-alpn-server-9.2.14.v20151106.jar
			// org.eclipse.jetty.util.security.Password sobeyhive
			// sobeyhive
			// OBF:1vv11yew1ta01toc20zj1toi1tae1yfa1vu9
			// MD5:2472405f6ba2f9ea05328219915f27d7

			SslContextFactory sslContextFactory = new SslContextFactory();
			sslContextFactory.setKeyStorePath("crt" + File.separator + "keystore");
			sslContextFactory.setKeyStorePassword("OBF:1vv11yew1ta01toc20zj1toi1tae1yfa1vu9");// 私钥
			sslContextFactory.setKeyManagerPassword("OBF:1vv11yew1ta01toc20zj1toi1tae1yfa1vu9");// 公钥
			ServerConnector httpsConnector = new ServerConnector(server, new SslConnectionFactory(sslContextFactory, "http/1.1"), new HttpConnectionFactory(
			        https_config));
			// 设置访问端口
			httpsConnector.setPort(SysConfig.getHttpsPort());
			httpsConnector.setIdleTimeout(30000);
			server.addConnector(httpsConnector);
		}
	}

	public void start() {
		Thread thr = new Thread(new Runnable() {
			public void run() {
				try {
					initServlets();
					server.start();
					LogUtils.info("UI[http://" + SysConfig.getHostName() + ":" + SysConfig.getUiPort() + "]启动 OK!");
					started = true;
					// server.join();
					runned = true;
				} catch (Exception e) {
					runned = true;
					started = false;
					LogUtils.error("启动UI[" + SysConfig.getUiPort() + "]服务出错!" + e.getMessage(), e);
					stop();
				}
			}
		});
		thr.start();
		thr.setName("UIServerJoin");
	}

	public void closeDefaultWebPages() {
		String appFile = webApp + File.separator + "app";
		if (new File(appFile).exists()) {
			// String delFiles[] = SysConfig.getWebAppsDelFiles();
			// for (String string : delFiles) {
			// appFile = webApp + File.separator + string;
			// File f = new File(appFile);
			// if (f.exists()) {
			// try {
			// Process proc = ToolUtil.runProcess("rm -rf " + appFile);
			// int res = proc.waitFor();
			// LogUtils.info("删除安装界面文件：" + appFile + " res=" + res);
			// } catch (IOException | InterruptedException e) {
			// }
			// }
			// }
		}
	}

	public void stop() {
		started = false;
		if (server != null) {
			try {
				server.stop();
			} catch (Exception e) {
			}
			try {
				server.destroy();
			} catch (Exception e) {
			}
		}
		server = null;
	}

	private void initServlets() throws IOException {
		// web.addServlet(new ServletHolder(new ZkctlServlet(daemonMaster, "/zkctl/")), "/zkctl/*");
		web.addServlet(DeployWebSocketServer.class, "/deploy/LogsWS");
		// ServiceReqHandler.addMasterPath("/deploy/");
		ResourceConfig configuration = new ResourceConfig(MyObjectMapperProvider.class, JacksonFeature.class);
		configuration.packages("com.sobey.mbserver.web.api");
		ServletHolder sh = new ServletHolder(new ServletContainer(configuration));
		// sh.setInitParameter("jersey.config.server.provider.packages", "com.sobey.mbserver");
		web.addServlet(sh, "/api/*");

		mgr.addServlet(new ServletHolder(new ZkctlServlet("/zkctl/")), "/zkctl/*");
		// mgr.addServlet(ZkctlServlet.getCLass("/zkctl/"), "/zkctl/*");
		mgr.addServlet(MgrServletContainer.class, "/api/*");
	}

	public boolean isStarted() {
		return started;
	}
}
