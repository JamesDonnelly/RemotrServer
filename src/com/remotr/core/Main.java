package com.remotr.core;

import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class Main extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private Logger log;

	private static String versionNumber;
	private static String versionName;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		ApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());
		
		log = Logger.getLogger(this.getClass());
		
		try {
			Properties props = new Properties();
			props.load(this.getServletContext().getResourceAsStream("/META-INF/MANIFEST.MF"));

			versionNumber = (String) props.get("Implementation-Version");
			versionName = (String) props.get("Implementation-Title");
		} catch (Throwable e) {
			log.error("Error getting version details - responses will be missing information");
		}
	}
	
	public static synchronized String getVersionNumber(){
		return versionNumber;
	}
	
	public static synchronized String getVersionName(){
		return versionName;
	}

}
