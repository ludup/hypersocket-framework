package com.hypersocket.interfaceState.json;

public class UserInterfaceStateUpdate {

	Long top;
	Long leftpx;
	Long resourceId;

	public UserInterfaceStateUpdate() {

	}

	public UserInterfaceStateUpdate(Long top, Long leftpx, Long resourceId) {
		this.top = top;
		this.leftpx = leftpx;
		this.resourceId = resourceId;
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

}
