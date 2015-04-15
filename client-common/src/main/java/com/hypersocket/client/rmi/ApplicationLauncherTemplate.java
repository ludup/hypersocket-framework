package com.hypersocket.client.rmi;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class ApplicationLauncherTemplate implements Serializable {

	private static final long serialVersionUID = -1187629371260022723L;

	String name;
	String exe;
	String[] args = { };
	String startupScript;
	String shutdownScript;
	Map<String,String> variables;
	
	public ApplicationLauncherTemplate(String name, String exe, String startupScript, String shutdownScript, Map<String,String> variables, String... args) {
		this.name = name;
		this.exe = exe;
		this.args = args;
		this.startupScript = startupScript;
		this.shutdownScript = shutdownScript;
		this.variables = variables;
	}
	
	public ApplicationLauncherTemplate(String name, String exe, String startupScript, String shutdownScript, Map<String,String> variables, String args) {
		this.name = name;
		this.exe = exe;
		this.startupScript = startupScript;
		this.shutdownScript = shutdownScript;
		this.variables = variables;
		if(!StringUtils.isEmpty(args)) {
			this.args = args.split("\\]\\|\\[");
		}
	}
	
	public String getName() {
		return name;
	}
	
	public String getExe() {
		return exe;
	}
	
	public String[] getArgs() {
		return args;
	}
	
	public Map<String,String> getVariables() {
		return variables;
	}
	
	public String getStartupScript() {
		return startupScript;
	}
	
	public String getShutdownScript() {
		return shutdownScript;
	}
	
}
