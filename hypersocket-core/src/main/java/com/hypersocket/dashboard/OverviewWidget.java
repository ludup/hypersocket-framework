package com.hypersocket.dashboard;

public class OverviewWidget implements Comparable<OverviewWidget>{

	int weight;
	String resourceKey;
	String contentPath;
	boolean large;

	public OverviewWidget() {

	}

	public OverviewWidget(int weight, String resourceKey, String contentPath,
			boolean large) {

		this.weight = weight;
		this.resourceKey = resourceKey;
		this.contentPath = contentPath;
		this.large = large;
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
}
