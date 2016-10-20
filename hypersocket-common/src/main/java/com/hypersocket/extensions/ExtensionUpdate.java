package com.hypersocket.extensions;

public class ExtensionUpdate {

	String currentVersion;
	String latestVersion;
	String phase;
	String[] repos;
	boolean upgrade;
	boolean canUpdate;
	int missingComponentCount;
	
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
}
