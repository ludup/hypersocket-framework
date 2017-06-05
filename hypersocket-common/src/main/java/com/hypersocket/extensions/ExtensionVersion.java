package com.hypersocket.extensions;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hypersocket.json.JsonResource;

@JsonIgnoreProperties(ignoreUnknown=true)
public class ExtensionVersion extends JsonResource implements Comparable<ExtensionVersion>, Serializable {

	private static final long serialVersionUID = -906138696866340097L;

	Long size;
	String version;
	String hash;
	String url;
	String extensionName;
	String repositoryDescription;
	String filename;
	String description;
	String extensionId;
	String repository;
	String target;
	Long modifiedDate;
	String featureGroup;
	String tab;
	boolean mandatory;
	String[] dependsOn;
	int weight;
	ExtensionState state;
	
	public ExtensionState getState() {
		return state;
	}
	public void setState(ExtensionState state) {
		this.state = state;
	}
	public Long getSize() {
		return size;
	}
	public void setSize(Long size) {
		this.size = size;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getExtensionName() {
		return extensionName;
	}
	public void setExtensionName(String extensionName) {
		this.extensionName = extensionName;
	}
	public String getRepositoryDescription() {
		return repositoryDescription;
	}
	public void setRepositoryDescription(String repositoryDescription) {
		this.repositoryDescription = repositoryDescription;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getExtensionId() {
		return extensionId;
	}
	public void setExtensionId(String extensionId) {
		this.extensionId = extensionId;
	}
	public String getRepository() {
		return repository;
	}
	public void setRepository(String repository) {
		this.repository = repository;
	}
	public Long getModifiedDate() {
		return modifiedDate;
	}
	public void setModifiedDate(Long modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	public boolean isMandatory() {
		return mandatory;
	}
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}
	public String[] getDependsOn() {
		return dependsOn;
	}
	public void setDependsOn(String[] dependsOn) {
		this.dependsOn = dependsOn;
	}
	public int getWeight() {
		return weight;
	}
	public void setWeight(int weight) {
		this.weight = weight;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public String getFeatureGroup() {
		return featureGroup;
	}
	public void setFeatureGroup(String featureGroup) {
		this.featureGroup = featureGroup;
	}
	public String getTab() {
		return tab;
	}
	public void setTab(String tab) {
		this.tab = tab;
	}
	@Override
	public int compareTo(ExtensionVersion o) {
		return new Integer(weight).compareTo(o.getWeight());
	}
	
	
}
