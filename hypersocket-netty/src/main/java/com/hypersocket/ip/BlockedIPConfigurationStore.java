package com.hypersocket.ip;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

	@Override
	public String getPropertyValue(PropertyTemplate template) {
		if(!confFile.exists()) {
			return "";
		}
		if(cachedValue==null) {
			try {
				StringTokenizer t = new StringTokenizer(FileUtils.readFileToString(confFile), System.getProperty("line.separator"));
				List<String> data = new ArrayList<String>();
				while(t.hasMoreTokens()) {
					data.add(t.nextToken());
				}
				
				cachedValue = ResourceUtils.implodeValues(data.toArray(new String[0]));
			} catch (IOException e) {
				throw new IllegalStateException("Failed to read to blockedIPs.txt");
			}
		}
		return cachedValue;
	}

	@Override
	public void setProperty(PropertyTemplate property, String value) {
		
		String[] ips = ResourceUtils.explodeValues(value);
		String data = "";
		for(String ip : ips) {
			data += ip + System.getProperty("line.separator");
		}
		cachedValue = value;
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
