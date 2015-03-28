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

import com.hypersocket.replace.ReplacementUtils;
import com.hypersocket.utils.CommandExecutor;

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
	public int launch() {
		
		Map<String,String> env = System.getenv();
		Map<String,String> properties = new HashMap<String,String>();
		
		properties.put("hostname", hostname);
		properties.put("username", username);
		
		for(String prop : ALLOWED_SYSTEM_PROPERTIES) {
			properties.put(prop.replace("user.", "client.user"), System.getProperty(prop));
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
			return Integer.MIN_VALUE;
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
		
		
		try {
			int exitCode = cmd.execute();
			
			if(log.isInfoEnabled()) {
				log.info("Script exited with exit code " + exitCode);
				log.info("---BEGIN CMD OUTPUT----");
				log.info(cmd.getCommandOutput());
				log.info("---END CMD OUTPUT----");
			}
			
			return exitCode;
		} catch (IOException e) {
			scriptFile.delete();
			log.error("Failed to execute script", e);
			return Integer.MIN_VALUE;
		}
				
	}

	private CommandExecutor executeBashScript(File scriptFile) {
		return new CommandExecutor("cmd.exe", "/C", scriptFile.getAbsolutePath());
	}

	private CommandExecutor executeWindowsScript(File scriptFile) {
		return new CommandExecutor("/bin/sh", scriptFile.getAbsolutePath());
	}

	private String getScriptSuffix() {
		return System.getProperty("os.name").toLowerCase().startsWith("windows") ? ".bat" : ".sh";
	}

}
