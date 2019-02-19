package com.sobey.mbserver.mgr.api;

import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import com.sobey.mbserver.web.MyObjectMapperProvider;

@WebServlet(initParams = @WebInitParam(name = "jersey.config.server.provider.packages", value = "com.sobey.mbserver.web.service"), urlPatterns = "/api/*", loadOnStartup = 1)
public class MgrServletContainer extends ServletContainer {
	private static final long serialVersionUID = -8508621619879507167L;

	MgrServletContainer() {
		super();
		this.reload(new ResourceConfig(MyObjectMapperProvider.class, JacksonFeature.class));
	}

	// PackageNamesScanner scanner = new PackageNamesScanner(new String[] {
	// MgrServletContainer.class.getPackage().getName() }, true);

}
