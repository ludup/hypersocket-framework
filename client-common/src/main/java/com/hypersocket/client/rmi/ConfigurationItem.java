package com.hypersocket.client.rmi;

public interface ConfigurationItem {

	public abstract String getName();

	public abstract void setName(String name);

	public abstract String getValue();

	public abstract void setValue(String value);

	public abstract Long getId();

}