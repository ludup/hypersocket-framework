package com.hypersocket.attributes;

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.hypersocket.properties.NameValuePair;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.resource.AssignableResource;

@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@Table(name = "abstract_attributes")
public abstract class AbstractAttribute<C extends RealmAttributeCategory<?>> extends AssignableResource  {
	
	private static final long serialVersionUID = 8883188306254264069L;

	@Column(name="description")
	private String description;
	
	@Lob
	@Column(name="default_value", nullable=true)
	private String defaultValue;

	@Column(name="weight")
	private int weight;
	
	@Column(name="type")
	private AttributeType type;

	@Column(name="hidden")
	private Boolean hidden = false;
	
	@Column(name="display_mode")
	private String displayMode;
	
	@Column(name="read_only")
	private Boolean readOnly = false;
	
	@Column(name="encrypted")
	private Boolean encrypted = false;
	
	@Column(name="variable_name")
	private String variableName;
	
	@Lob
	@Column(name="options")
	private String options;

	@Column(name="linked_resource_id")
	private Long linkedResourceId;
	
	public abstract C getCategory();

	public abstract void setCategory(C category);

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

	public AttributeType getType() {
		return type;
	}

	public void setType(AttributeType type) {
		this.type = type;
	}

	public String generateMetaData() {
		return "{ \"inputType\": \"" + type.getInputType() + "\", \"filter\": \"custom\" }";
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

	public Collection<NameValuePair> getOptions() {
		return ResourceUtils.explodeNamePairs(options);
	}

	public void setOptions(Collection<NameValuePair> options) {
		if(options==null) {
			this.options = "";
			return;
		}
		this.options = ResourceUtils.implodeNamePairs(options);
	}

	
}
