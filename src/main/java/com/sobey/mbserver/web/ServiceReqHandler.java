package com.sobey.mbserver.web;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.net.util.Base64;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.sobey.jcg.sobeyhive.main.DaemonMaster;
import com.sobey.jcg.support.log4j.LogUtils;
import com.sobey.jcg.support.utils.Convert;
import com.sobey.mbserver.util.DateUtils;
import com.sobey.mbserver.util.DeflateUtils;
import com.sobey.mbserver.util.GZIPUtils;
import com.sobey.mbserver.util.HttpHeaders;
import com.sobey.mbserver.web.init.InstallConstant;
import com.sobey.mbserver.web.init.SysConfig;

/**
 * 
 * 
 * @description 服务请求处理类
 * @date 14-1-15 -
 * @modify
 * @modifyDate -
 */
public class ServiceReqHandler extends HttpServlet implements HttpHeaders {

	private static final long serialVersionUID = 5268008595106647694L;
	public DaemonMaster master;
	static boolean lockStarted = false;

	public static PoolingClientConnectionManager poolingClientConnectionManager = new PoolingClientConnectionManager();
	public static DefaultHttpClient httpClient = new DefaultHttpClient(poolingClientConnectionManager);
	public static ArrayList<BasicHeader> defaultHttpHeaders = null;
	public static int timeout = 30000;
	public static final int BUFFER_SIZE = 8 * 1024;
	public static int maxThreadsTotal = 200;
	public static int defaultThreadsTotal = 100;
	public static String acceptLanguage = "en-us,en-gb,en;q=0.7,*;q=0.3";
	public static String accept = "text/html,application/xml;q=0.9,application/xhtml+xml,text/xml;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5";
	public static String acceptCharset = "utf-8,ISO-8859-1;q=0.7,*;q=0.7";
	public static int maxContent = 64 * 1024 * 1024;
	public static final int CHUNK_SIZE = 2000;
	public static Pattern metaPattern = Pattern.compile("<meta\\s+([^>]*http-equiv=(\"|')?content-type(\"|')?[^>]*)>", Pattern.CASE_INSENSITIVE);
	public static Pattern charsetPattern = Pattern.compile("charset=\\s*([a-z][_\\-0-9a-z]*)", Pattern.CASE_INSENSITIVE);

	public static void configureClient() {
		if (defaultHttpHeaders == null) {
			defaultHttpHeaders = new ArrayList<BasicHeader>();
			HttpParams httpParams = httpClient.getParams();
			httpClient.getParams().setParameter("http.protocol.cookie-policy", CookiePolicy.BROWSER_COMPATIBILITY);
			httpClient.getParams().setParameter("http.protocol.content-charset", HTTP.UTF_8);
			httpParams.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout);
			httpParams.setParameter(CoreConnectionPNames.SO_TIMEOUT, timeout * 3);
			httpParams.setParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, BUFFER_SIZE);
			poolingClientConnectionManager.setMaxTotal(maxThreadsTotal);
			poolingClientConnectionManager.setDefaultMaxPerRoute(defaultThreadsTotal);
			// Set up an HTTPS socket factory that accepts self-signed certs.
			// Set the User Agent in the header
			// headers.add(new BasicHeader("User-Agent", userAgent));
			// prefer English
			defaultHttpHeaders.add(new BasicHeader("Accept-Language", acceptLanguage));
			// prefer UTF-8
			defaultHttpHeaders.add(new BasicHeader("Accept-Charset", acceptCharset));
			// prefer understandable formats
			defaultHttpHeaders.add(new BasicHeader("Accept", accept));
			// accept gzipped content
			defaultHttpHeaders.add(new BasicHeader("Accept-Encoding", "x-gzip, gzip, deflate"));
			defaultHttpHeaders.add(new BasicHeader("User-Agent",
			        "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36"));
			httpParams.setParameter(ClientPNames.DEFAULT_HEADERS, defaultHttpHeaders);
		}
	}

	public static boolean isRunning() {
		return lockStarted;
	}

	public static void lockRuuning() {
		lockStarted = true;
	}

	public static void resRuuning() {
		lockStarted = false;
	}

	void lock() {
		lockStarted = true;
	}

	void unlock() {
		lockStarted = false;
	}

	static boolean checkRuuning() {
		return lockStarted;
	}

	public static String getMapValue(Map<String, Object> m, String key, String defaultS) {
		Object v = m.get(key);
		if (v == null) {
			return defaultS;
		}
		return v.toString();
	}

	public Object getMapObject(Map<String, Object> m, String key) {
		Object v = m.get(key);
		if (v == null) {
			return null;
		}
		return v;
	}

	public ServiceReqHandler() {
	}

	// 获取编码类型
	public String getCharset() {
		return "UTF-8";
	}

	/**
	 * http服务。处理接口
	 * 
	 * @param request
	 * @param response
	 * @return 将页面内容按字符串返回
	 */
	public String doHttp(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// 期待HttpCollectServer处理时子类实现
		return null;
	}

	public String proxyHttp(String url, String body, boolean isPost) throws IOException {
		return proxyHttp(url, body, isPost, null);
	}

	public String proxyHttp(String url, String body, boolean isPost, HttpServletResponse response) throws IOException {
		configureClient();
		HttpRequestBase httpRequest = null;
		HttpResponse httpresponse = null;
		try {
			if (isPost) {
				HttpPost httpPost = new HttpPost(url.toString());
				if (body != null && !body.isEmpty()) {
					List<NameValuePair> nvps = new ArrayList<NameValuePair>();
					nvps.add(new BasicNameValuePair("body", body));
					httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
				}
				httpRequest = httpPost;
			} else {
				httpRequest = new HttpGet(url.toString());
			}
			httpresponse = httpClient.execute(httpRequest);
			if (response != null) {
				for (Header head : httpresponse.getAllHeaders()) {// "Content-Type"
					((HttpServletResponse) response).setHeader(head.getName(), head.getValue());
				}
			}
			return packageResponse(url, httpRequest, httpresponse);
		} catch (HttpException e) {
			LogUtils.warn("proxyHttp:" + url, e);
			throw new IOException(e);
		} finally { // 关闭连接
			if (null != httpRequest) {
				httpRequest.releaseConnection();
			}
		}
	}

	public String proxyHttp(String url, String userName, String userPass, String body, boolean isPost) throws IOException {
		configureClient();
		HttpRequestBase httpRequest = null;
		HttpResponse httpresponse = null;
		try {
			// 使用base64进行加密
			byte[] tokenByte = Base64.encodeBase64((userName + ":" + userPass).getBytes());
			// 将加密的信息转换为string
			String tokenStr = new String(tokenByte, 0, tokenByte.length);
			// Basic YFUDIBGDJHFK78HFJDHF== token的格式
			String token = "Basic " + tokenStr;
			System.err.println(token);
			if (isPost || !body.isEmpty()) {
				HttpPost httpPost = new HttpPost(url.toString());
				if (body != null && !body.isEmpty()) {
					StringEntity postingString = new StringEntity(body, "UTF-8");
					httpPost.setEntity(postingString);
				}
				httpRequest = httpPost;
			} else {
				httpRequest = new HttpGet(url.toString());
			}
			// 把认证信息发到header中
			httpRequest.setHeader("Authorization", token);
			httpresponse = httpClient.execute(httpRequest);
			return packageResponse(url, httpRequest, httpresponse);
		} catch (HttpException e) {
			LogUtils.warn("proxyHttp:" + url, e);
			throw new IOException(e);
		} finally { // 关闭连接
			if (null != httpRequest) {
				httpRequest.releaseConnection();
			}
		}
	}

	public static String proxyHttpPost(String url, String json) throws IOException {
		configureClient();
		HttpRequestBase httpRequest = null;
		HttpResponse httpresponse = null;
		try {
			HttpPost httpPost = new HttpPost(url.toString());
			StringEntity postingString = new StringEntity(json, "UTF-8");
			httpPost.setEntity(postingString);
			httpPost.setHeader("Content-type", "application/json");
			httpRequest = httpPost;
			httpresponse = httpClient.execute(httpRequest);
			return packageResponse(url, httpRequest, httpresponse);
		} catch (HttpException e) {
			LogUtils.warn("proxyHttp:" + url, e);
			throw new IOException(e);
		} finally { // 关闭连接
			if (null != httpRequest) {
				httpRequest.releaseConnection();
			}
		}
	}

	public static byte[] processGzipEncoded(byte[] compressed) throws IOException {
		byte[] content;
		if (maxContent > 0) {
			content = GZIPUtils.unzipBestEffort(compressed, maxContent);
		} else {
			content = GZIPUtils.unzipBestEffort(compressed);
		}
		if (content == null)
			throw new IOException("unzipBestEffort returned null");
		return content;
	}

	public static byte[] processDeflateEncoded(byte[] compressed) throws IOException {
		byte[] content;
		if (maxContent > 0)
			content = DeflateUtils.inflateBestEffort(compressed, maxContent);
		else
			content = DeflateUtils.inflateBestEffort(compressed);
		if (content == null)
			throw new IOException("inflateBestEffort returned null");
		return content;
	}

	public static String parseCharacterEncoding(String contentType) {
		if (contentType == null)
			return (null);
		int start = contentType.indexOf("charset=");
		if (start < 0)
			return (null);
		String encoding = contentType.substring(start + 8);
		int end = encoding.indexOf(';');
		if (end >= 0)
			encoding = encoding.substring(0, end);
		encoding = encoding.trim();
		if ((encoding.length() > 2) && (encoding.startsWith("\"")) && (encoding.endsWith("\"")))
			encoding = encoding.substring(1, encoding.length() - 1);
		return (encoding.trim());

	}

	private static String packageResponse(String url, HttpRequestBase httpRequest, HttpResponse response) throws HttpException, IOException {
		int code = response.getStatusLine().getStatusCode();
		Header[] heads = response.getAllHeaders();
		Map<String, String> headers = new HashMap<String, String>();
		for (int i = 0; i < heads.length; i++) {
			headers.put(heads[i].getName(), heads[i].getValue());
		}
		HttpEntity entity = response.getEntity();
		long contentLength = maxContent;
		if (entity != null) {
			if (entity.getContentLength() > 0) {
				if (entity.getContentLength() < contentLength)
					contentLength = entity.getContentLength();
			} else {
				contentLength = Long.MAX_VALUE;
			}
		}
		String contentType = headers.get("Content-Type");
		String charSet = parseCharacterEncoding(contentType);
		InputStream in = entity.getContent();
		byte[] content = null;
		try {
			byte[] buffer = new byte[BUFFER_SIZE];
			int bufferFilled = 0;
			int totalRead = 0;
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			while ((bufferFilled = in.read(buffer, 0, buffer.length)) > 0 && totalRead + bufferFilled <= contentLength) {
				totalRead += bufferFilled;
				out.write(buffer, 0, bufferFilled);
			}
			content = out.toByteArray();
		} catch (SocketTimeoutException e) {
			if (code == 200) {
				throw new IOException(e.toString());
			}
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				EntityUtils.consume(entity);
				if (null != httpRequest) {
					httpRequest.abort();
				}
			} catch (Exception e) {
			}
		}
		StringBuilder fetchTrace = null;
		// Trace message
		if (LogUtils.traceEnabled()) {
			fetchTrace = new StringBuilder("url: " + url + "; status code: " + code + "; bytes received: " + content.length);
			if (headers.get("Content-Length") != null)
				fetchTrace.append("; Content-Length: " + headers.get("Content-Length"));
			if (headers.get("Location") != null)
				fetchTrace.append("; Location: " + headers.get("Location"));
		}
		// Extract gzip, x-gzip and deflate content
		if (content != null) {
			// check if we have to uncompress it
			String contentEncoding = headers.get(CONTENT_ENCODING);
			// System.out.println("content.length=" + this.content.length + "  contentEncoding:" + contentEncoding);
			if (contentEncoding != null && LogUtils.traceEnabled())
				fetchTrace.append("; Content-Encoding: " + contentEncoding);
			if ("gzip".equals(contentEncoding) || "x-gzip".equals(contentEncoding)) {
				content = processGzipEncoded(content);
				if (LogUtils.traceEnabled())
					fetchTrace.append("; extracted to " + content.length + " bytes");
			} else if ("deflate".equals(contentEncoding)) {
				content = processDeflateEncoded(content);
				if (LogUtils.traceEnabled())
					fetchTrace.append("; extracted to " + content.length + " bytes");
			}
		}
		if (LogUtils.traceEnabled()) {
			LogUtils.trace(fetchTrace.toString());
		}
		if (charSet == null) {
			charSet = sniffCharacterEncoding(content);
			if (charSet != null) {
				headers.put(CONTENT_TYPE, headers.get(CONTENT_TYPE) + ";charset=" + charSet);
				contentType = headers.get(CONTENT_TYPE);
			} else {
				String encoding = "windows-1252";
				headers.put(CONTENT_TYPE, headers.get(CONTENT_TYPE) + ";charset=" + charSet);
				contentType = headers.get(CONTENT_TYPE);
				charSet = encoding;
			}
		}
		if (charSet != null && !charSet.equals("utf-8")) {
			if (contentType != null)
				headers.put(CONTENT_TYPE, contentType.replace(charSet, "utf-8"));
			if (null == content) {
				content = new byte[0];
				return "";
			}
			String txt = new String(content, charSet);
			return txt;
		}
		return new String(content, charSet);
	}

	public static String sniffCharacterEncoding(byte[] content) {
		int length = content.length < CHUNK_SIZE ? content.length : CHUNK_SIZE;
		String str = "";
		try {
			str = new String(content, 0, length, Charset.forName("ASCII").toString());
		} catch (UnsupportedEncodingException e) {
			return null;
		}
		Matcher metaMatcher = metaPattern.matcher(str);
		String encoding = null;
		if (metaMatcher.find()) {
			Matcher charsetMatcher = charsetPattern.matcher(metaMatcher.group(1));
			if (charsetMatcher.find())
				encoding = new String(charsetMatcher.group(1));
		}
		return encoding;
	}

	static Map<String, String> baseUrl = new HashMap<String, String>();
	static {
		baseUrl.put("/deploy/get/master", "/deploy/get/master");
		baseUrl.put("/deploy/get/zkBaseNode", "/deploy/get/zkBaseNode");
		baseUrl.put("/deploy/refreashHostDockerContainer", "/deploy/refreashHostDockerContainer");
	}
	static ArrayList<String> masterPaths = new ArrayList<String>();

	static void addMasterPath(String pathRoot) {
		masterPaths.add(pathRoot);
	}

	boolean checkMasterPath(String path) {
		for (String proot : masterPaths) {
			if (path.startsWith(proot))
				return true;
		}
		return checkPathOnMaster(path);
	}

	protected boolean checkPathOnMaster(String path) {
		return false;
	}

	protected boolean checkPathOnLocal(String path, HttpServletRequest request) {
		return true;
	}

	// 不走master, 直接代理
	protected boolean checkPathOnLocal(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String path = request.getRequestURI();
		if (!checkPathOnLocal(path, request)) {
			String hostName = request.getParameter("hostName");
			String config = request.getParameter("body");
			if (config == null || config.equals("")) {
				StringBuffer sb = new StringBuffer();
				BufferedReader bufReader = request.getReader();
				String buf = bufReader.readLine();
				while (buf != null) {
					sb.append(buf);
					sb.append("\n");
					buf = bufReader.readLine();
				}
				bufReader.close();
				config = sb.toString();
			}
			if (hostName == null && config != null) {
				Map<String, Object> tmp = InstallConstant.objectMapper.readValue(config, Map.class);
				hostName = SysConfig.getMapValue(tmp, "hostName", "null");
			}
			// zk hostlist exists
			// if (!master.zkClusterNodeTracker.clusterNodes.containsKey(hostName)) {
			// throw new ParamsException("客户端:" + request.getRemoteAddr() + "请求参数hostName对应的主机\"" + hostName +
			// "\"不存在\nurl:" + request.getRequestURL() + "?"
			// + request.getQueryString());
			// }
			String url = "http://" + hostName + ":" + SysConfig.getUiPort() + path;
			String queryString = request.getQueryString();
			if (queryString != null && !queryString.isEmpty()) {
				url += "?" + queryString;
			}
			// 直接代理
			String cnt = proxyHttp(url, config, true, response);
			PrintWriter out = response.getWriter();
			out.write(cnt);
			// 跳转
			// if (config != null && !config.trim().equals("")) {
			// url += "?body=" + java.net.URLEncoder.encode(config, "utf-8");
			// }
			// LogUtils.info("sendRedirect : " + url);
			// response.sendRedirect(url);
			return false;
		}
		return true;
	}

	// 非master进行跳转
	boolean checkMaster(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String path = request.getRequestURI();
		boolean isBaseUrl = baseUrl.containsKey(path);
		if (!isBaseUrl && checkMasterPath(path) && !master.isMaster() && master.haveMaster()) {
			String masterIp = master.getMasterHostIp();
			if (masterIp == null || masterIp.trim().equals("")) {
				masterIp = master.getMasterHostName();
			}
			if (masterIp.equals("")) {
				throw new IOException("只能在master上执行");
			}
			String url = "http://" + masterIp + ":" + SysConfig.getUiPort() + path;
			String config = request.getParameter("body");
			if (config == null || config.equals("")) {
				StringBuffer sb = new StringBuffer();
				BufferedReader bufReader = request.getReader();
				String buf = bufReader.readLine();
				while (buf != null) {
					sb.append(buf);
					sb.append("\n");
					buf = bufReader.readLine();
				}
				bufReader.close();
				config = sb.toString();
			}
			String queryString = request.getQueryString();
			if (queryString != null && !queryString.isEmpty()) {
				url += "?" + queryString;
			}
			// 直接代理
			String cnt = proxyHttp(url, config, true, response);
			PrintWriter out = response.getWriter();
			out.write(cnt);
			// 跳转
			// if (config != null && !config.trim().equals("")) {
			// url += "?body=" + java.net.URLEncoder.encode(config, "utf-8");
			// }
			// LogUtils.info("sendRedirect : " + url);
			// response.sendRedirect(url);
			return false;
		}
		return true;
	}

	public String getErrorMsg(Throwable e) {
		String str = "";
		while (e != null) {
			str += "\tcaused by:" + e.getMessage() + "\n";
			StackTraceElement[] els = e.getStackTrace();
			if (els != null) {
				int linex = 0;
				for (StackTraceElement el : els) {
					str += "\t\t" + el.getMethodName() + "(" + el.getFileName() + ":" + el.getLineNumber() + ")\n";
					if (linex++ > 3) {
						break;
					}
				}
			}
			e = e.getCause();
		}
		return str;
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) {
		String charset = "UTF-8";
		response.setContentType("text/html;charset=" + charset);
		PrintWriter out = null;
		String str = "";
		try {
			request.setCharacterEncoding("UTF-8");
			response.setHeader("Access-Control-Allow-Origin", "*");
			response.setHeader("Access-Control-Allow-Methods", "GET,POST");
			response.setHeader("Access-Control-Allow-Headers", "*");
			String httpMethod = ((HttpServletRequest) request).getMethod();
			if ("OPTIONS".equals(httpMethod)) {
				// this.doOptions(request, response);
				return;
			} else if ("GET".equals(httpMethod) || "POST".equals(httpMethod)) {
				out = response.getWriter();
				if (checkMaster(request, response) && checkPathOnLocal((HttpServletRequest) request, (HttpServletResponse) response)) {
					str = doHttp(request, response);
					out.write(str);
				}
			} else {
				throw new IOException("不支持的方法:" + httpMethod);
			}
		} catch (Throwable e) {
			if (out != null) {
				LogUtils.error("URL:[" + ((HttpServletRequest) request).getRequestURI() + "]error:" + e.getMessage());
				// String stackStr[] = StringUtils.printStackTrace(e).split("\\\n");
				str = "URL:[" + ((HttpServletRequest) request).getRequestURI() + "]  error:\n" + getErrorMsg(e);
				String path = ((HttpServletRequest) request).getRequestURI();
				if (checkMasterPath(path)) {
					((HttpServletResponse) response).setHeader("Content-Type", "application/json;charset=UTF-8");
					Map<String, Object> res = new HashMap<String, Object>();
					res.put("code", 1);
					res.put("msg", str);
					res.put("time", DateUtils.format(new Date()));
					try {
						out.print(InstallConstant.objectMapper.writeValueAsString(res));
					} catch (IOException e1) {
					}
				} else {
					out.print(new String(str.getBytes()).replaceAll("\n", "<br/>"));
				}
			}
		} finally {
			if (out != null) {
				out.flush();
				out.close();
			}
			// DataSourceManager.destroy();
		}

	}

	protected boolean parserSend(String taskInfo, String content, boolean isBtech, String srcClient) throws JsonMappingException, IOException {
		return isBtech;
	}

	public Map<String, Object> getParams(ServletRequest request) throws IOException {
		Map<String, Object> par = getBody(request);
		if (par == null || par.isEmpty()) {
			Enumeration<String> enums = request.getParameterNames();
			while (enums.hasMoreElements()) {
				par = new HashMap<String, Object>();
				String name = enums.nextElement();
				par.put(name, request.getParameter(name));
			}
		}
		return par;
	}

	public Map<String, Object> getBody(ServletRequest request) throws IOException {
		String config = request.getParameter("body");
		if (config == null || config.equals("")) {
			config = Convert.toString(request.getAttribute("body"), "");
		}
		if (config == null || config.trim().isEmpty()) {
			StringBuffer sb = new StringBuffer();
			BufferedReader bufReader = request.getReader();
			String buf = bufReader.readLine();
			while (buf != null) {
				sb.append(buf);
				sb.append("\n");
				buf = bufReader.readLine();
			}
			bufReader.close();
			config = sb.toString();
			if (config == null || config.trim().equals(""))
				return null;
		}
		LogUtils.debug("submit config: " + config);
		return InstallConstant.objectMapper.readValue(config, Map.class);
	}
}
