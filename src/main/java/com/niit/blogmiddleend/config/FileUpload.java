package com.niit.blogmiddleend.config;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import com.niit.blogmiddleend.controller.JerseyService;

@ApplicationPath("/rest")
public class FileUpload extends Application{
	@Override
	public Set<Class<?>> getClasses() {
		
		Set<Class<?>> s=new HashSet<Class<?>>();
		s.add(JerseyService.class);
		
		return s;
		
	}
	
	

}