package com.sobey.mbserver.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;

public class SystemUtil {

	/**
	 * 获取错误流的信息
	 * 
	 * @param command
	 */
	public static void exece(String command) {
		if (null == command || command.trim().length() <= 0) {
			return;
		}

		try {
			Process proc = Runtime.getRuntime().exec(command);
			exece(proc);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取错误流的信息
	 * 
	 * @param command
	 */
	public static void exece(Process proc) {
		if (null == proc) {
			return;
		}

		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			String text = null;
			while ((text = in.readLine()) != null) {
				if (text == null || text.trim().equals("")) {
					continue;
				}
				System.out.println(text);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 获取流的信息
	 * 
	 * @param command
	 */
	public static void exec(String command) {
		if (null == command || command.trim().length() <= 0) {
			return;
		}
		try {
			Process proc = Runtime.getRuntime().exec(command);
			exec(proc);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static int exec(String workDir, String... command) {
		if (null == command || command.length <= 0) {
			return -2;
		}
		Process proc = null;
		try {
			ProcessBuilder pb = new ProcessBuilder(command);
			Map env = pb.environment();
			if (workDir != null && !workDir.trim().equals("")) {
				File dir = new File(workDir);
				if (dir.exists()) {
					pb = pb.directory(dir);
					env.put("USER.DIR", workDir);
				}
			}
			proc = pb.start();
			getShellOut(proc);
			proc.waitFor();
			return proc.exitValue();
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return -1;
		} finally {
			if (proc != null) {
				proc.destroy();
			}
		}

	}

	/**
	 * 获取流的信息
	 * 
	 * @param command
	 */
	public static void exec(Process proc) {
		if (null == proc) {
			return;
		}

		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String text = null;
			while ((text = in.readLine()) != null) {
				if (text == null || text.trim().equals("")) {
					continue;
				}
				System.out.println(text);
			}
			proc.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 获取流的信息
	 * 
	 * @param command
	 */
	public static String execResult(String workDir, String... command) {
		if (null == command || command.length <= 0) {
			return null;
		}
		Process proc = null;
		try {
			ProcessBuilder pb = new ProcessBuilder(command);
			if (workDir != null && !workDir.equals("")) {
				File dir = new File(workDir);
				if (dir.exists())
					pb.directory(dir);
			}
			proc = pb.start();
			String res = getShellOut(proc);
			return res;
		} catch (IOException e) {
			e.printStackTrace();
			return StringUtils.printStackTrace(e);
		} finally {
			if (proc != null) {
				proc.destroy();
			}
		}
	}

	/**
	 * 读取输出流数据
	 * 
	 * @param p
	 *            进程
	 * @return 从输出流中读取的数据
	 * @throws IOException
	 */
	public static final String getShellOut(Process p) throws IOException {

		StringBuilder sb = new StringBuilder();
		BufferedInputStream in = null;
		BufferedReader br = null;

		try {
			in = new BufferedInputStream(p.getInputStream());
			br = new BufferedReader(new InputStreamReader(in));
			String s;
			while ((s = br.readLine()) != null) {
				// 追加换行符
				sb.append("\n");
				sb.append(s);
			}
			p.waitFor();
			System.out.println(sb.toString());
		} catch (IOException e) {
			throw e;
		} catch (InterruptedException e) {
			throw new IOException(e);
		} finally {
			br.close();
			in.close();
		}

		return sb.toString();
	}

	public static int executeLinux(String... cmds) {
		return executeCommand(true, cmds);
	}

	public static int executeCommand(boolean isLinux, String... cmds) {
		ProcessBuilder pb = null;
		Process proc = null;
		try {
			// 创建一个进程示例
			if (isLinux) {
				pb = new ProcessBuilder("/bin/bash");
			} else {
				pb = new ProcessBuilder("cmd.exe");
			}
			// 设置工作目录
			proc = pb.start();
			// 将要执行的Windows命令写入
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
			InputStream is = proc.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			// '/r/n'是必须写入的
			for (String string : cmds) {
				bw.write("echo \"" + string + " \"\n");
				bw.write(string + " \n");
				if (isLinux) {
					bw.write("res=$? && [ \"$res\" != \"0\" ] && exit $res \n");
				}
			}
			bw.write("exit 0 \n");
			// for (int i = 0; i < cmds.length; i++) {
			// if (i > 0)
			// bw.write(" && ");
			// bw.write(cmds[i]);
			// }
			// bw.write("exit $? \n");
			bw.flush();
			// 将执行结果打印显示
			String line;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
			int res = proc.waitFor();
			return res;
		} catch (Exception e) {
			return -1;
		} finally {
			if (proc != null) {
				proc.destroy();
			}
		}
	}

}
