package com.hypersocket.client.rmi;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

public class ApplicationLauncherTemplate implements Serializable {

	private static final long serialVersionUID = -1187629371260022723L;

	String name;
	String exe;
	String[] args = { };
	String startupScript;
	String shutdownScript;
	
	public ApplicationLauncherTemplate(String name, String exe, String startupScript, String shutdownScript, String... args) {
		this.name = name;
		this.exe = exe;
		this.args = args;
		this.startupScript = startupScript;
		this.shutdownScript = shutdownScript;
	}
	
	public ApplicationLauncherTemplate(String name, String exe, String startupScript, String shutdownScript, String args) {
		this.name = name;
		this.exe = exe;
		this.startupScript = startupScript;
		this.shutdownScript = shutdownScript;
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
	
	public String getStartupScript() {
		return startupScript;
	}
	
	public String getShutdownScript() {
		return shutdownScript;
	}
	
}
