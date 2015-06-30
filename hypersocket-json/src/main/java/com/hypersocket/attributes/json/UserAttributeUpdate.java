package com.hypersocket.attributes.json;

public class UserAttributeUpdate {

	private Long id;
	private String name;
	private Long category;
	private String description;
	private String defaultValue;
	private int weight;
	private String type;
	private String displayMode;
	private Boolean readOnly;
	private Boolean encrypted;
	private String variableName;
	Long[] roles;
	
	public UserAttributeUpdate() {

	}

	public UserAttributeUpdate(Long id, String name, Long category,
			String description, String defaultValue, int weight, String type,
			String displayMode, Boolean readOnly, Boolean encrypted, String variableName) {
		this.id = id;
		this.name = name;
		this.category = category;
		this.description = description;
		this.defaultValue = defaultValue;
		this.weight = weight;
		this.type = type;
		this.displayMode = displayMode;
		this.readOnly = readOnly;
		this.encrypted = encrypted;
		this.variableName = variableName;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long[] getRoles() {
		return roles;
	}

	public void setRoles(Long[] roles) {
		this.roles = roles;
	}
	
	public Long getCategory() {
		return category;
	}

	public void setCategory(Long category) {
		this.category = category;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Boolean getReadOnly() {
		return readOnly;
	}

	public void setReadOnly(Boolean readOnly) {
		this.readOnly = readOnly;
	}

	public Boolean getEncrypted() {
		return encrypted;
	}

	public void setEncrypted(Boolean encrypted) {
		this.encrypted = encrypted;
	}

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	public String getDisplayMode() {
		return displayMode;
	}

	public void setDisplayMode(String displayMode) {
		this.displayMode = displayMode;
	}
	
	
}
