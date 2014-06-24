package com.hypersocket.template;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.hypersocket.resource.Resource;

@Entity
@Table(name="templates")
public class Template extends Resource {

	@Column(name="subject", length=512)
	String subject;
	
	@Column(name="template", length=8000 /*SQL server limit */)
	String template;

	@Column(name="template_type")
	String type;
	
	@Column(name="name_is_resource_key")
	boolean nameIsResourceKey;
	
	@Column(name="has_subject")
	boolean hasSubject;
	
	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isNameIsResourceKey() {
		return nameIsResourceKey;
	}

	public void setNameIsResourceKey(boolean nameIsResourceKey) {
		this.nameIsResourceKey = nameIsResourceKey;
	}

	public boolean isHasSubject() {
		return hasSubject;
	}

	public void setHasSubject(boolean hasSubject) {
		this.hasSubject = hasSubject;
	}
	
	
	
}
