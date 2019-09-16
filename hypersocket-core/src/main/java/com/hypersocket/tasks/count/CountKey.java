package com.hypersocket.tasks.count;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.hypersocket.resource.RealmResource;

@Entity
@Table(name="count_keys")
public class CountKey extends RealmResource {

	private static final long serialVersionUID = -2894955407636699115L;

	@Column(name="count_value")
	Long count;
		
	public void add(Long count) {
		this.count += count;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

}
