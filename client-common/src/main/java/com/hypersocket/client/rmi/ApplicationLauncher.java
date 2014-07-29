package com.hypersocket.client.rmi;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.hypersocket.client.util.CommandExecutor;
import com.hypersocket.replace.ReplacementUtils;

public class ApplicationLauncher implements ResourceLauncher, Serializable {

	private static final long serialVersionUID = 4922604914995232181L;

	String[] ALLOWED_SYSTEM_PROPERTIES = { "user.name", "user.home", "user.dir" };
	
	NetworkResourceTemplate template;
	ApplicationLauncherTemplate launcher;
	String username;
	public ApplicationLauncher(String username, NetworkResourceTemplate template, ApplicationLauncherTemplate launcher) {
		this.username = username;
		this.template = template;
		this.launcher = launcher;
	}
	
	@Override
	public void launch() {
		
		Map<String,String> env = System.getenv();
		Map<String,String> properties = new HashMap<String,String>();
		
		properties.put("hostname", template.getHostname());
		properties.put("username", username);
		
		for(String prop : ALLOWED_SYSTEM_PROPERTIES) {
			properties.put(prop, System.getProperty(prop));
		}
		
		String exe = ReplacementUtils.processTokenReplacements(launcher.getExe(), env);
		exe = ReplacementUtils.processTokenReplacements(exe, properties);
		
		
		final CommandExecutor cmd = new CommandExecutor(exe);
		for(String arg : launcher.getArgs()) {
			arg = ReplacementUtils.processTokenReplacements(arg, env);
			arg = ReplacementUtils.processTokenReplacements(arg, properties);
			cmd.addArg(arg);
		}
		
		Thread t = new Thread() {
			public void run() {
				try {
					cmd.execute();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		
		t.start();
	}

}
