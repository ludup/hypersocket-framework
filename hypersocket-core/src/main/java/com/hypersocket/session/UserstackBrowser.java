package com.hypersocket.session;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserstackBrowser implements Serializable {

	private static final long serialVersionUID = 4208593517044810498L;

	String name;
	String version;
	String version_major;
	String engine;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getVersion_major() {
		return version_major;
	}
	public void setVersion_major(String version_major) {
		this.version_major = version_major;
	}
	public String getEngine() {
		return engine;
	}
	public void setEngine(String engine) {
		this.engine = engine;
	}
	
	
}
