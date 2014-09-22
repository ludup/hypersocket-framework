package com.hypersocket.attributes;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.hypersocket.repository.AbstractEntity;

@Entity
@Table(name="attributes")
public class Attribute extends AbstractEntity<Long>  {

	@ManyToOne
	AttributeCategory category;

	@Column(name="name")
	String name;

	@Column(name="description")
	String description;
	
	@Column(name="default_value", nullable=true, length=8000 /*SQL server limit */)
	String defaultValue;

	@Column(name="weight")
	int weight;
	
	@Column(name="type")
	AttributeType type;

	@Column(name="hidden")
	Boolean hidden = false;
	
	@Column(name="read_only")
	Boolean readOnly = false;
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	@Column(name="attribute_id")
	Long id;

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	public AttributeCategory getCategory() {
		return category;
	}

	public void setCategory(AttributeCategory category) {
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

	public AttributeType getType() {
		return type;
	}

	public void setType(AttributeType type) {
		this.type = type;
	}

	public String generateMetaData() {
		return "{ \"inputType\": \"" + type.toString().toLowerCase() + "\" }";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	
}
