package com.hypersocket.interfaceState.json;

public class UserInterfaceStateUpdate {

	Long top;
	Long leftpx;
	Long resourceId;
	String name;
	boolean specific;

	public UserInterfaceStateUpdate() {

	}

	public UserInterfaceStateUpdate(Long top, Long leftpx, Long resourceId,
			String name, boolean specific) {
		this.top = top;
		this.leftpx = leftpx;
		this.resourceId = resourceId;
		this.name = name;
		this.specific = specific;
	}

	public Long getTop() {
		return top;
	}

	public void setTop(Long top) {
		this.top = top;
	}

	public Long getLeftpx() {
		return leftpx;
	}

	public void setLeftpx(Long leftpx) {
		this.leftpx = leftpx;
	}

	public Long getResourceId() {
		return resourceId;
	}

	public void setResourceId(Long resourceId) {
		this.resourceId = resourceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isSpecific() {
		return specific;
	}

	public void setSpecific(boolean specific) {
		this.specific = specific;
	}

}
