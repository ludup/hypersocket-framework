package com.hypersocket.template;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.hypersocket.resource.Resource;

@Entity
@Table(name="templates")
public class Template extends Resource {

	@Column(name="template", length=8000 /*SQL server limit */)
	String template;

	@Column(name="template_type")
	String type;
	
	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	
}
