package com.hypersocket.client.rmi;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="configuration")
public class ConfigurationItemImpl implements ConfigurationItem, Serializable {

	private static final long serialVersionUID = -3650734390706745660L;

	@Id
	@GeneratedValue
	Long id;
	
	@Column
	String name;
	
	@Column
	String value;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public Long getId() {
		return id;
	}
	
	
}
