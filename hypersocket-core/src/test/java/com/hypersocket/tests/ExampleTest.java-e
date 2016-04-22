package com.hypersocket.tests;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hypersocket.config.ConfigurationService;

@RunWith(SpringJUnit4ClassRunner.class)
public class ExampleTest extends ContextLoadingTest {

	@Autowired
	ConfigurationService configurationService; 
	
	@Test
	public void example() {
		configurationService.clearPrincipalContext();
	}
}
