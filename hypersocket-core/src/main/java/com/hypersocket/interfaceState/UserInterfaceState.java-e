package com.hypersocket.interfaceState;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.hypersocket.resource.RealmResource;

@Entity
@Table(name = "user_interface_state")
public class UserInterfaceState extends RealmResource {

	@Column(name = "top")
	Long top;

	@Column(name = "leftpx")
	Long leftpx;

	@Column(name = "resourceId")
	Long resourceId;

	@Column(name = "principalId")
	Long principalId;

	public UserInterfaceState() {
	}

	public UserInterfaceState(Long top, Long leftpx, Long resourceId,
			Long principalId) {

		this.top = top;
		this.leftpx = leftpx;
		this.resourceId = resourceId;
		this.principalId = principalId;
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

	public Long getPrincipalId() {
		return principalId;
	}

	public void setPrincipalId(Long principalId) {
		this.principalId = principalId;
	}

}
