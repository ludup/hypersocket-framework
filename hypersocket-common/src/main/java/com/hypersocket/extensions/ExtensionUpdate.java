package com.hypersocket.extensions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExtensionUpdate {

	private String currentVersion;
	private String latestVersion;
	private String phase;
	private String[] repos;
	private boolean upgrade;
	private boolean canUpdate;
	private int missingComponentCount;
	private String customer;
	private String product; 
	
	public ExtensionUpdate() {
		
	}

	public String getCurrentVersion() {
		return currentVersion;
	}

	public void setCurrentVersion(String currentVersion) {
		this.currentVersion = currentVersion;
	}

	public String getLatestVersion() {
		return latestVersion;
	}

	public void setLatestVersion(String latestVersion) {
		this.latestVersion = latestVersion;
	}

	public String getPhase() {
		return phase;
	}

	public void setPhase(String phase) {
		this.phase = phase;
	}

	public void setUpgrade(boolean upgrade) {
		this.upgrade = upgrade;
	}

	public boolean isUpgrade() {
		return upgrade;
	}

	public String[] getRepos() {
		return repos;
	}

	public void setRepos(String[] repos) {
		this.repos = repos;
	}

	public void setMissingComponents(int missingComponentCount) {
		this.missingComponentCount = missingComponentCount;
	}
	
	public int getMissingComponentCount() {
		return missingComponentCount;
	}

	public void setCanUpdate(boolean canUpdate) {
		this.canUpdate = canUpdate;
	}
	
	public boolean getCanUpdate() {
		return canUpdate;
	}

	public String getCustomer() {
		return customer;
	}

	public void setCustomer(String customer) {
		this.customer = customer;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}
	
	
}
