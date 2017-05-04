package com.hypersocket.dashboard;

public abstract class OverviewWidget implements Comparable<OverviewWidget>{

	int weight;
	String resourceKey;
	String contentPath;
	boolean large;
	boolean visible = true;
	boolean fix;
	public OverviewWidget() {

	}

	public OverviewWidget(int weight, String resourceKey, String contentPath,
			boolean large) {

		this.weight = weight;
		this.resourceKey = resourceKey;
		this.contentPath = contentPath;
		this.large = large;
		this.fix = true;
	}

	public OverviewWidget(boolean visible, int weight, String resourceKey, String contentPath,
			boolean large) {

		this.weight = weight;
		this.resourceKey = resourceKey;
		this.contentPath = contentPath;
		this.large = large;
		this.visible = visible;
	}
	
	public OverviewWidget(boolean visible, int weight, String resourceKey, String contentPath,
			boolean large, boolean fix) {

		this.weight = weight;
		this.resourceKey = resourceKey;
		this.contentPath = contentPath;
		this.large = large;
		this.visible = visible;
		this.fix = fix;
	}
	
	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public String getResourceKey() {
		return resourceKey;
	}

	public void setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
	}

	public String getContentPath() {
		return contentPath;
	}

	public void setContentPath(String contentPath) {
		this.contentPath = contentPath;
	}

	public boolean isLarge() {
		return large;
	}

	public void setLarge(boolean large) {
		this.large = large;
	}

	@Override
	public int compareTo(OverviewWidget overviewWidget) {
		return weight<overviewWidget.getWeight()?-1:weight>overviewWidget.getWeight()?1:0;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public boolean isFix() {
		return fix;
	}

	public void setFix(boolean fix) {
		this.fix = fix;
	}

	public abstract boolean hasContent();
	
	
}
