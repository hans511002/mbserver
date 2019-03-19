package com.sobey.base;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.directwebremoting.WebContextFactory;

public abstract class BaseAction {
	
	/**
	 * 获得HttpServletRequest对象
	 * @return
	 */
	protected HttpServletRequest getHttpRequest() {
		return WebContextFactory.get().getHttpServletRequest();
	}
	
	/**
	 * 获得HttpServletResponse对象
	 * @return
	 */
	protected HttpServletResponse getHttpResponse() {
		return WebContextFactory.get().getHttpServletResponse();
	}
	
	/**
	 * 获得ServletContext对象
	 * @return
	 */
	protected ServletContext getServletContext() {
		return WebContextFactory.get().getServletContext();
	}

	/**
	 * 获得HttpSession
	 * @return
	 */
	protected HttpSession getSession() {
		return this.getHttpRequest().getSession();
	}
}
