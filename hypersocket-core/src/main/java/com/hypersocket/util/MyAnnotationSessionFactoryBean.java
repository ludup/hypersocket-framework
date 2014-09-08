package com.hypersocket.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;

public class MyAnnotationSessionFactoryBean extends
		AnnotationSessionFactoryBean {

	@Override
	public void setPackagesToScan(String[] packagesToScan) {

		PathMatchingResourcePatternResolver matcher = new PathMatchingResourcePatternResolver();
		ArrayList<String> finalPackages = new ArrayList<String>(Arrays.asList(packagesToScan));
		
		try {
			Resource[] packages = matcher.getResources("classpath*:*.hibernateScan");
		
			for(Resource r : packages) {
				finalPackages.add(r.getFilename().substring(0, r.getFilename().length()-14) + ".**");
			}
		} catch (IOException e) {
		}
		
		
		super.setPackagesToScan(finalPackages.toArray(new String[0]));
	}

	
}
