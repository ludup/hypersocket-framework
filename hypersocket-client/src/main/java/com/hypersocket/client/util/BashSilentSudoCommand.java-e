/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.client.util;

import com.hypersocket.utils.CommandExecutor;

public class BashSilentSudoCommand extends CommandExecutor {

	char[] password;
	
	public BashSilentSudoCommand(char[] password, String cmd, String... cmdline) {
		super("/bin/bash", "-c", createSudoCommand(password, cmd, cmdline));	
	}
	
	private static String createSudoCommand(char[] password, String cmd, String[] cmdline) {
		
		StringBuffer buf = new StringBuffer();
		buf.append("echo ");
		buf.append(password);
		buf.append("|");
		buf.append("sudo -S");
		buf.append(" ");
		if(cmd.contains(" ")) {
			buf.append('\'');
			buf.append(cmd);
			buf.append('\'');
		} else {
			buf.append(cmd);
		}
		for(String s : cmdline) {
			buf.append(" ");
			if(s.contains(" ")) {
				buf.append('\'');
				buf.append(s);
				buf.append('\'');
			} else {
				buf.append(s);
			}
			
		}
		return buf.toString();
	}

}
