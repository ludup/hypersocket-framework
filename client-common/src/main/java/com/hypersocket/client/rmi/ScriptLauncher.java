package com.hypersocket.client.rmi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.client.util.CommandExecutor;
import com.hypersocket.replace.ReplacementUtils;

public class ScriptLauncher implements ResourceLauncher, Serializable {

	static Logger log = LoggerFactory.getLogger(ScriptLauncher.class);
	
	private static final long serialVersionUID = 4922604914995232181L;

	String[] ALLOWED_SYSTEM_PROPERTIES = { "user.name", "user.home", "user.dir" };
	
	String hostname;
	String script;
	String username;
	
	public ScriptLauncher(String username, String hostname, String script) {
		this.username = username;
		this.hostname = hostname;
		this.script = script;
	}
	
	@Override
	public void launch() {
		
		Map<String,String> env = System.getenv();
		Map<String,String> properties = new HashMap<String,String>();
		
		properties.put("hostname", hostname);
		properties.put("username", username);
		
		for(String prop : ALLOWED_SYSTEM_PROPERTIES) {
			properties.put(prop, System.getProperty(prop));
		}
		
		script = ReplacementUtils.processTokenReplacements(script, env);
		script = ReplacementUtils.processTokenReplacements(script, properties);
		
		File scriptFile = null; 
		FileOutputStream out = null;
		try {
			scriptFile = File.createTempFile("tmp", getScriptSuffix());
			out = new FileOutputStream(scriptFile);
			IOUtils.write(script, out);
		} catch(IOException ex) { 
			log.error("Failed to create temporary script file", ex);
			scriptFile.delete();
			return;
		} finally {
			IOUtils.closeQuietly(out);
		}
		
		scriptFile.setExecutable(true);
		
		final CommandExecutor cmd;
		if(System.getProperty("os.name").toLowerCase().startsWith("windows")) {
			cmd = executeWindowsScript(scriptFile);
		} else {
			cmd = executeBashScript(scriptFile);
		}
		
		
		Thread t = new Thread() {
			public void run() {
				try {
					cmd.execute();
				} catch (IOException e) {
					log.error("Failed to launch application", e);
				} 
			}
		};
		
		t.start();
	}

	private CommandExecutor executeBashScript(File scriptFile) {
		return new CommandExecutor("cmd.exe", "/C", scriptFile.getAbsolutePath());
	}

	private CommandExecutor executeWindowsScript(File scriptFile) {
		return new CommandExecutor("/usr/bin/sh", scriptFile.getAbsolutePath());
	}

	private String getScriptSuffix() {
		return System.getProperty("os.name").toLowerCase().startsWith("windows") ? ".bat" : ".sh";
	}

}
