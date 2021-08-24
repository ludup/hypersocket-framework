package com.hypersocket.dashboard;

public abstract class OverviewWidget implements Comparable<OverviewWidget>{

	private int weight;
	private String resourceKey;
	private String contentPath;
	private boolean large;
	private boolean visible;
	private Long column;
	private Long position;
	private boolean system;
	private boolean decorate = true;
	public OverviewWidget() {

	}

	public OverviewWidget(int weight, String resourceKey, String contentPath,
			boolean large) {

		this.weight = weight;
		this.resourceKey = resourceKey;
		this.contentPath = contentPath;
		this.large = large;
		this.visible = true;
		this.column = Long.valueOf(1);
		this.position = Long.valueOf(-1);
		this.system = false;
	}

	public OverviewWidget(boolean visible, int weight, String resourceKey, String contentPath,
			boolean large) {

		this.weight = weight;
		this.resourceKey = resourceKey;
		this.contentPath = contentPath;
		this.large = large;
		this.visible = visible;
		this.column = Long.valueOf(1);
		this.position = Long.valueOf(-1);
		this.system = false;
	}
	
	public OverviewWidget(boolean visible, int weight, String resourceKey, String contentPath,
			boolean large, boolean decorate) {

		this.weight = weight;
		this.resourceKey = resourceKey;
		this.contentPath = contentPath;
		this.large = large;
		this.visible = visible;
		this.column = Long.valueOf(1);
		this.position = Long.valueOf(-1);
		this.system = false;
		this.decorate = decorate;
	}
	
	public OverviewWidget(boolean visible, int weight, String resourceKey, String contentPath,
			boolean large, Long column, Long position, boolean system) {

		this.weight = weight;
		this.resourceKey = resourceKey;
		this.contentPath = contentPath;
		this.large = large;
		this.visible = visible;
		this.column = column;
		this.position = position;
		this.system = system;
	}
	
	public int getWeight() {
		return weight;
	}
	
	public boolean getDecorate() {
		return decorate;
	}
	
	public void setDecorate(boolean decorate) {
		this.decorate = decorate;
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

	public Long getColumn() {
		return column;
	}

	public void setColumn(Long column) {
		this.column = column;
	}

	public Long getPosition() {
		return position;
	}

	public void setPosition(Long position) {
		this.position = position;
	}

	public abstract boolean hasContent();

	public boolean isSystem() {
		return system;
	}

	public void setSystem(boolean system) {
		this.system = system;
	}
}
