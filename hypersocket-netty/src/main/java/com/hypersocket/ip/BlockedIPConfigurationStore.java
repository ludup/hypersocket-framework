package com.hypersocket.ip;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import com.hypersocket.properties.PropertyStore;
import com.hypersocket.properties.PropertyTemplate;
import com.hypersocket.properties.ResourceUtils;

@Component
public class BlockedIPConfigurationStore implements PropertyStore {

	File confFile;
	String cachedValue = null;
	
	PropertyTemplate template;
	
	public BlockedIPConfigurationStore() {
		confFile =  new File(System.getProperty("hypersocket.conf", "conf"), "blockedIPs.txt");
	}

	public boolean isDefaultStore() {
		return false;
	}
	
	@Override
	public synchronized String getPropertyValue(PropertyTemplate template) {
		if(!confFile.exists()) {
			return "";
		}
		if(cachedValue==null) {
			try {
				StringTokenizer t = new StringTokenizer(FileUtils.readFileToString(confFile), System.getProperty("line.separator"));
				Set<String> uniqueValues = new HashSet<String>();
				while(t.hasMoreTokens()) {
					String ip = t.nextToken();
					if(!uniqueValues.contains(ip)) {
						uniqueValues.add(ip);
					}
				}
				
				cachedValue = ResourceUtils.implodeValues(uniqueValues.toArray(new String[0]));
			} catch (IOException e) {
				throw new IllegalStateException("Failed to read to blockedIPs.txt");
			}
		}
		return cachedValue;
	}

	@Override
	public synchronized void setProperty(PropertyTemplate property, String value) {
		
		String[] ips = ResourceUtils.explodeValues(value);
		Set<String> uniqueValues = new HashSet<String>();
		String data = "";
		cachedValue = "";
		for(String ip : ips) {
			if(!uniqueValues.contains(ip)) {
				data += ip + System.getProperty("line.separator");
				uniqueValues.add(ip);
			}
		}
		cachedValue = ResourceUtils.implodeValues(uniqueValues);
		try {
			FileUtils.writeStringToFile(confFile, data);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to write to blockedIPs.txt");
		}
	}

	@Override
	public void registerTemplate(PropertyTemplate template, String module) {
		if(!template.getResourceKey().equals("server.blockIPs")) {
			throw new IllegalStateException("BlockedIPConfigurationStore implementation is exclusivley for the use of server.blockedIPs property");
		}	
		this.template = template;
	}

	@Override
	public PropertyTemplate getPropertyTemplate(String resourceKey) {
		return template;
	}

}
