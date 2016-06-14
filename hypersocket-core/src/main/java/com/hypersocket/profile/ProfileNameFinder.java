package com.hypersocket.profile;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

public class ProfileNameFinder {
	
	public static String[] findProfiles(){
		Set<String> profiles = new HashSet<>();
		BeanDefinitionRegistry bdr = new SimpleBeanDefinitionRegistry();
		ClassPathBeanDefinitionScanner s = new ClassPathBeanDefinitionScanner(bdr);

		TypeFilter tf = new AssignableTypeFilter(ProfileNameProvider.class);
		s.addIncludeFilter(tf);
		s.setIncludeAnnotationConfig(false);
		s.scan("com.hypersocket.profile");       
		String[] beans = bdr.getBeanDefinitionNames();
		for (String string : beans) {
			String beanClassName = bdr.getBeanDefinition(string).getBeanClassName();
			profiles.add(beanClassName.substring(beanClassName.lastIndexOf(".") + 1));
		}
		
		return profiles.toArray(new String[0]);
	}

}
