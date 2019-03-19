package com.sobey.base.dwr.test;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.sobey.jcg.support.utils.FileUtils;

public class GenerateActionJsFile {
	public static final Log LOG = LogFactory.getLog(GenerateActionJsFile.class.getName());

	public static String BASE_PATH = "http://192.168.160.1:8080/EAngel";
	// public static String BASE_PATH = "http://192.168.10.146:8080/mopt";
	public static String DIR_PATH = "E:\\Android\\workspace\\EAngel\\client\\assets\\www\\js\\action";

	public static void main(String[] args) {
		HttpClient client = null;
		try {
			client = new DefaultHttpClient();
			String url = BASE_PATH + "/dwr/index.html";
			HttpGet get = new HttpGet(url);
			HttpResponse response = client.execute(get);
			if (200 == response.getStatusLine().getStatusCode()) {
				String content = EntityUtils.toString(response.getEntity());
				Matcher matcher = Pattern.compile(".*?/dwr/test/(.*?)'>").matcher(content);
				while (matcher.find()) {
					downloadFile(matcher.group(1));
					LOG.info("生成JS：" + matcher.group(1));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			HttpClientUtils.closeQuietly(client);
		}
	}

	public static void downloadFile(String action) {
		HttpClient client = null;
		try {
			client = new DefaultHttpClient();
			String url = BASE_PATH + "/dwr/interface/" + action + ".js";
			HttpGet get = new HttpGet(url);
			HttpResponse response = client.execute(get);
			if (200 == response.getStatusLine().getStatusCode()) {
				String content = EntityUtils.toString(response.getEntity());
				content = content.replaceAll(action + "\\._path.*?/dwr';", "");
				content = content.replaceAll(action + "\\._path", "httpDwrServerpath");

				content = content.replaceAll("= function\\((.*), callback\\) \\{",
				        "= function(successCallback, errorCallback, $1){\n\tvar dwrFun=new DwrFunction(successCallback, errorCallback);");
				content = content.replaceAll("= function\\(callback\\) \\{",
				        "= function(successCallback, errorCallback){\n\tvar dwrFun=new DwrFunction(successCallback, errorCallback);");
				content = content.replaceAll("dwr\\.engine\\._execute\\((.*), callback\\);", "dwr.engine._execute($1, dwrFun.fun);");

				File file = new File(DIR_PATH + "\\" + action + ".js");
				if (file.exists()) {
					file.delete();
				}
				FileUtils.writeStringToFile(file, content);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			HttpClientUtils.closeQuietly(client);
		}
	}
}
