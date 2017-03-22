package com.niit.blogmiddleend.config;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import com.niit.blogmiddleend.controller.JerseyService;


public class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer{

	@Override
	protected Class<?>[] getRootConfigClasses() {
		
		return new Class[]{ AppConfig.class, WebSocketConfig.class,JerseyService.class,FileUpload.class };
	}

	@Override
	protected Class<?>[] getServletConfigClasses() {
	
		return new Class[] { WebConfig.class };
	}

	@Override
	protected String[] getServletMappings() {
		
		return new String[]{"/"};
	}

}
