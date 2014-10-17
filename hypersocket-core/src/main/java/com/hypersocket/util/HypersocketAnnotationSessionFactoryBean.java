package com.hypersocket.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;

public class HypersocketAnnotationSessionFactoryBean extends
		AnnotationSessionFactoryBean {

	static Logger log = LoggerFactory.getLogger(HypersocketAnnotationSessionFactoryBean.class);
	
	@Override
	public void setPackagesToScan(String[] packagesToScan) {

		PathMatchingResourcePatternResolver matcher = new PathMatchingResourcePatternResolver();
		ArrayList<String> finalPackages = new ArrayList<String>(Arrays.asList(packagesToScan));
		
		try {
			Resource[] packages = matcher.getResources("classpath*:hibernate-ext.properties");
		
			for(Resource r : packages) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(r.getInputStream()));
				
				try {
					String line;
					
					if(log.isInfoEnabled()) {
						log.info("Processing hibernate-ext.properties from " + r.getURI().toASCIIString());
					}
					while((line = reader.readLine())!=null) {
						line = line.trim();
						if(!line.startsWith("#")) {
							if(line.startsWith("scanPackage=")) {
								String pkgName = line.substring(12);
								if(log.isInfoEnabled()) {
									log.info("Will scan package " + pkgName);
								}
								finalPackages.add(pkgName + ".**");
							}
						}
					}
				} finally {
					reader.close();
				}
				
			}
		} catch (IOException e) {
		}
		
		
		super.setPackagesToScan(finalPackages.toArray(new String[0]));
	}

	
}
