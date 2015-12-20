package com.hypersocket.attributes.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.hypersocket.resource.AssignableResource;

@Entity
@Table(name="user_attributes")
public class UserAttribute extends AssignableResource  {

	@ManyToOne
	UserAttributeCategory category;
	
	@Column(name="description")
	String description;
	
	@Column(name="default_value", nullable=true, length=8000 /*SQL server limit */)
	String defaultValue;

	@Column(name="weight")
	int weight;
	
	@Column(name="type")
	UserAttributeType type;

	@Column(name="hidden")
	Boolean hidden = false;
	
	@Column(name="display_mode")
	String displayMode;
	
	@Column(name="read_only")
	Boolean readOnly = false;
	
	@Column(name="encrypted")
	Boolean encrypted = false;
	
	@Column(name="variable_name")
	String variableName;

	@Column(name="linked_resource_id")
	Long linkedResourceId;
	
	public UserAttributeCategory getCategory() {
		return category;
	}

	public void setCategory(UserAttributeCategory category) {
		this.category = category;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public UserAttributeType getType() {
		return type;
	}

	public void setType(UserAttributeType type) {
		this.type = type;
	}

	public String generateMetaData() {
		return "{ \"inputType\": \"" + type.toString().toLowerCase() + "\", \"filter\": \"custom\" }";
	}

	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getHidden() {
		return hidden;
	}

	public void setHidden(Boolean hidden) {
		this.hidden = hidden == null ? false : hidden;
	}

	public Boolean getReadOnly() {
		return readOnly;
	}

	public void setReadOnly(Boolean readOnly) {
		this.readOnly = readOnly==null ? false : readOnly;
	}

	public void setEncrypted(boolean encrypted) {
		this.encrypted = encrypted;
	}
	public boolean getEncrypted() {
		return encrypted!=null && encrypted;
	}

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	public String getDisplayMode() {
		return displayMode==null ? "" : displayMode;
	}

	public void setDisplayMode(String displayMode) {
		this.displayMode = displayMode;
	}

	public Long getLinkedResourceId() {
		return linkedResourceId;
	}

	public void setLinkedResourceId(Long linkedResourceId) {
		this.linkedResourceId = linkedResourceId;
	}

	
}
