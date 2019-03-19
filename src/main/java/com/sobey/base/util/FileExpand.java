package com.sobey.base.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class FileExpand extends File {

	private static final long serialVersionUID = 7614276874261286743L;

	public FileExpand(String path) {
		super(path);
	}

	public FileExpand(URI path) {
		super(path);
	}

	public FileExpand(String parent, String child) {
		super(parent, child);
	}

	public FileExpand(File parent, String child) {
		super(parent, child);
	}

	public boolean copyTo(String newFile) throws IOException {
		File newCfgFile = new File(newFile);
		return copyTo(newCfgFile);
	}

	public boolean copyTo(File newFile) throws IOException {
		return copyTo(newFile, false);
	}

	public boolean copyTo(File newFile, boolean force) throws IOException {
		if (!force) {
			if (newFile.exists()) {
				return false;
			}
		}
		FileInputStream in = null;
		FileOutputStream wr = null;
		try {
			in = new FileInputStream(this);
			wr = new FileOutputStream(newFile);
			byte[] buf = new byte[4096];
			int len = 0;
			while ((len = in.read(buf)) > 0) {
				wr.write(buf, 0, len);
			}
			in.close();
			wr.close();
			wr = null;
			in = null;
			return true;
		} finally {
			if (wr != null)
				wr.close();
			if (in != null)
				in.close();
		}
	}

	public static String parseFileCode(String file) throws IOException {
		return parseFileCode(file, "utf8");
	}

	public static String parseFileCode(String file, String def) throws IOException {
		return parseFileCode(new File(file), def);
	}

	public static String parseFileCode(InputStream fis) throws IOException {
		return parseFileCode(fis, "utf8");
	}

	public static String parseFileCode(InputStream fis, String def) throws IOException {
		if (fis == null)
			return def;
		byte[] bb = new byte[4];
		fis.read(bb);
		fis.close();
		String code = def;
		int bom[] = new int[4];
		for (int i = 0; i < bom.length; i++) {
			if (bb[i] > 0) {
				bom[i] = bb[i];
			} else {
				bom[i] = bb[i] + 256;
			}
		}
		if (bom[0] == 0xEF && bom[1] == 0xBB && bom[2] == 0xBF) {
			code = "UTF-8";
		} else if ((bom[0] == 0xFE && bom[1] == 0xFF) || (bom[1] == 0xFE && bom[0] == 0xFF)) {
			code = "UTF-16";
		} else if ((bom[0] == 0x00 && bom[1] == 0x00 && bom[2] == 0xFE && bom[3] == 0xFF)
		        || (bom[0] == 0x00 && bom[1] == 0x00 && bom[2] == 0xFF && bom[3] == 0xFE)) {
			code = "UTF-32";
		}
		return code;
	}

	public static String parseFileCode(File file, String def) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		return parseFileCode(fis, def);
	}

	public static BufferedReader getFileBufferedReader(String file, String code) throws IOException {
		return getFileBufferedReader(new File(file), code);
	}

	public static BufferedReader getFileBufferedReader(File file) throws IOException {
		String code = parseFileCode(file, "utf8");
		BufferedReader f = new BufferedReader(new InputStreamReader(new FileInputStream(file), code));
		return f;
	}

	public static BufferedReader getFileBufferedReader(File file, String code) throws IOException {
		BufferedReader f = new BufferedReader(new InputStreamReader(new FileInputStream(file), code));
		return f;
	}

	public static List<String> getSubFiles(File file) throws IOException {
		List<String> files = new ArrayList<String>();
		getSubFiles(files, file);
		return files;
	}

	public static void getSubFiles(List<String> files, File file) throws IOException {
		File[] subFile = file.listFiles();
		for (File sfile : subFile) {
			if (!sfile.isDirectory()) {
				files.add(sfile.getCanonicalPath());
			} else {
				getSubFiles(files, sfile);
			}
		}
	}

	public String[] getAllFiles() throws IOException {
		List<String> files = new ArrayList<String>();
		getSubFiles(files, this);
		return files.toArray(new String[0]);
	}
}
