package com.matt.remotr.main;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class Main extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		ApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());
			
		//DeviceCoordinator deviceCoordinator = (DeviceCoordinator) context.getBean("deviceCoordinator");
		//TcpCoordinator tcpCoordinator = (TcpCoordinator) context.getBean("tcpCoordinator");
	}

}
