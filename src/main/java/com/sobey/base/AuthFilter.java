package com.sobey.base;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AuthFilter implements Filter {
	public static final Log LOG = LogFactory.getLog(AuthFilter.class.getName());

	@Override
	public void init(FilterConfig arg0) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain chain) throws IOException, ServletException {
		long start = System.currentTimeMillis();
		HttpServletRequest req = (HttpServletRequest) arg0;
		HttpServletResponse res = (HttpServletResponse) arg1;
		chain.doFilter(req, res);
		res.setHeader("Access-Control-Allow-Origin", "*");
		long end = System.currentTimeMillis();
		LOG.info(req.getRequestURL() + "：" + (end - start));
	}

	@Override
	public void destroy() {

	}

}
