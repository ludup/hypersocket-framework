package com.hypersocket.util;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.hypersocket.util.TextProcessor.Resolver;

public class TextProcessorTest extends AbstractTest {
	@Test
	public void testScript() {
		TextProcessor tp = new TextProcessor();
		tp.setEvaluateScripts(true);
		tp.addVariable("var1", "val1");
		Assert.assertEquals("This is a bit of script Some value val1",
				tp.process("This is a bit of script $(script)'Some value ' + attributes.var1$(/script)"));
	}

	@Test
	public void testScriptBindings() {
		TextProcessor tp = new TextProcessor();
		tp.setEvaluateScripts(true);
		tp.addBindings("bnd", "val1");
		Assert.assertEquals("This is a bit of script Some value val1",
				tp.process("This is a bit of script $(script)'Some value ' + bnd$(/script)"));
	}

	@Test(expected = RuntimeException.class)
	public void testDangerousClasses() throws IOException {
		TextProcessor tp = new TextProcessor();
		tp.setCaptureScriptErrors(false);
		tp.setEvaluateScripts(true);
		File tf = File.createTempFile("tff", ".tmp");
		Assert.assertEquals(tf.lastModified(), Long
				.parseLong(tp.process("$(script)new java.io.File('" + tf.getPath() + "').lastModified()$(/script)")));
	}

	public void testDangerousClassesWhitelisted() throws IOException {
		TextProcessor tp = new TextProcessor();
		tp.setCaptureScriptErrors(false);
		tp.setEvaluateScripts(true);
		tp.whitelistClassName("java.io.File");
		File tf = File.createTempFile("tff", ".tmp");
		Assert.assertEquals(tf.lastModified(), Long
				.parseLong(tp.process("$(script)new java.io.File('" + tf.getPath() + "').lastModified()$(/script)")));
	}

	@Test(expected = RuntimeException.class)
	public void testDangerousClassesBlacklisted() throws IOException {
		TextProcessor tp = new TextProcessor();
		tp.setCaptureScriptErrors(false);
		tp.setEvaluateScripts(true);
		tp.allowAllClasses();
		tp.blacklistClassName("java.io.File");
		File tf = File.createTempFile("tff", ".tmp");
		Assert.assertEquals(tf.lastModified(), Long
				.parseLong(tp.process("$(script)new java.io.File('" + tf.getPath() + "').lastModified()$(/script)")));
	}

	@Test
	public void testVariables() {
		TextProcessor tp = new TextProcessor();
		tp.addVariable("var1", "val1");
		tp.addVariable("var2", "val2");
		tp.addVariable("var3", "val3");
		Assert.assertEquals("this is var1: val1  and this is var2: val2  and this is var3: val3",
				tp.process("this is var1: ${var1}  and this is var2: ${var2}  and this is var3: ${var3}"));
	}

	@Test
	public void testResolver() {
		TextProcessor tp = new TextProcessor();
		tp.addResolver(new Resolver() {
			@Override
			public String evaluate(String variable) {
				return "val" + variable.substring(3);
			}
		});
		Assert.assertEquals("this is var1: val1  and this is var2: val2  and this is var3: val3",
				tp.process("this is var1: ${var1}  and this is var2: ${var2}  and this is var3: ${var3}"));
	}
}
