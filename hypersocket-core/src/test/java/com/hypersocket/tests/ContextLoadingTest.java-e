package com.hypersocket.tests;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(locations = { "/test-applicationContext.xml" })
public class ContextLoadingTest {

	@BeforeClass
	public static void before() throws IOException {
		File data = new File("data");
		FileUtils.deleteDirectory(data);
	}
}
