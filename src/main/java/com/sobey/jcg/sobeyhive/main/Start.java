package com.sobey.jcg.sobeyhive.main;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import com.sobey.jcg.support.log4j.LogUtils;
import com.sobey.jcg.support.sys.SystemConstant;
import com.sobey.jcg.support.sys.SystemVariable;
import com.sobey.jcg.support.web.SystemInitListener;
import com.sobey.mbserver.util.HasThread;
import com.sobey.mbserver.util.ToolUtil;
import com.sobey.mbserver.web.init.SysVar;

public class Start {

	public static CommandLine buildCommandline(String[] args) {
		final Options options = new Options();
		Option opt = new Option("h", "help", false, "打印帮助");
		opt.setRequired(false);
		options.addOption(opt);
		opt = new Option("d", "debug", false, "调试运行，更多休眠和等待");
		opt.setRequired(false);
		options.addOption(opt);

		opt = new Option("start", null, false, "启动，不可与stop同时出现");
		opt.setRequired(false);
		options.addOption(opt);

		opt = new Option("stop", null, true, "停止，不可与start同时出现 all表示所有");
		opt.setRequired(false);
		options.addOption(opt);
		opt = new Option("export", null, true, "导出配置文件,传入文件路径名");
		opt.setRequired(false);
		options.addOption(opt);

		PosixParser parser = new PosixParser();
		HelpFormatter hf = new HelpFormatter();
		hf.setWidth(110);
		CommandLine commandLine = null;
		try {
			commandLine = parser.parse(options, args);
			if (commandLine.hasOption('h')) {
				hf.printHelp("queue", options, true);
				return null;
			}
			int cmdType = 0;
			cmdType += commandLine.hasOption("start") ? 1 : 0;
			cmdType += commandLine.hasOption("stop") ? 1 : 0;
			cmdType += commandLine.hasOption("export") ? 1 : 0;
			cmdType += commandLine.hasOption("expand") ? 1 : 0;
			cmdType += commandLine.hasOption("upgrade") ? 1 : 0;
			cmdType += commandLine.hasOption("install") ? 1 : 0;
			cmdType += commandLine.hasOption("reinstall") ? 1 : 0;
			if (cmdType != 1) {
				hf.printHelp("queue", options, true);
				return null;
			}
		} catch (ParseException e) {
			hf.printHelp("queue", options, true);
			LogUtils.error("param parse error:", e);
			return null;
		}
		return commandLine;
	}

	// 采集程序信号捕获接口
	static class ProcessSignal implements SignalHandler {

		private DaemonMaster daemonMaster;

		ProcessSignal(DaemonMaster daemonMaster) {
			this.daemonMaster = daemonMaster;
		}

		public void handle(Signal signal) {
			String nm = signal.getName();
			if (nm.equals("TERM") || nm.equals("INT") || nm.equals("KILL")) {
				LogUtils.info("程序捕获到[" + nm + "]信号,即将停止!");
				try {
					new HasThread() {
						@Override
						public void run() {
							daemonMaster.stop();
						}
					}.setDaemon(true).start();
				} catch (Throwable e) {
				} finally {
					int time = 0;
					while (time < 1000) {
						if (daemonMaster.stopSuccess) {
							System.exit(1);
						}
						time += 100;
						ToolUtil.sleep(100);
					}
					LogUtils.error("未能正确关闭");
					ToolUtil.sleep(1000);
					System.exit(1);
				}
			}
		}
	}

	public static void main(String[] args) throws IOException {
		SystemInitListener.inWebApp = true;
		Properties conf = new Properties();
		conf.load(SystemVariable.getResourceAsStream("server.properties"));
		System.getProperties().putAll(conf);
		SystemVariable.getConf().putAll(conf);
		SystemConstant.setSYS_CONF_FILE("server.properties");
		System.setProperty("logFileName", "deployactor");
		CommandLine cmd = buildCommandline(args);
		if (cmd == null) {
			throw new IOException("参数错误");
		}
		// String debug = System.getenv("DEBUG");
		// if (debug != null) {
		// if (debug.indexOf("dt_socket,server=y,suspend=n,address") >= 0) {
		// LogUtils.info("sleep 15000ms for debug link");
		// ToolUtil.sleep(15000);
		// }
		// }
		if (cmd.hasOption("d")) {
			SysVar.isDebug = true;
		}
		boolean isStart = cmd.hasOption("start");
		boolean isStop = cmd.hasOption("stop");
		boolean isInstall = cmd.hasOption("install");
		DaemonMaster daemonMaster = new DaemonMaster(isInstall || cmd.hasOption("noui"));// 注册捕获kill信号
		ProcessSignal processSignal = new ProcessSignal(daemonMaster);
		Signal.handle(new Signal("TERM"), processSignal);// kill命令
		Signal.handle(new Signal("INT"), processSignal);// ctrl+c
		if (isStart) {// 启动服务配置界面
			try {
				daemonMaster.start();
			} catch (Throwable e) {
				LogUtils.error("发生错误:" + e.getMessage() + " --程序即将退出!", e);
			} finally {
				if (daemonMaster != null) {
					daemonMaster.stop();
				}
			}
		}
	}
}
