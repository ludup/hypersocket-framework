package com.hypersocket.resource;

import com.hypersocket.repository.AbstractEntity;

import javax.persistence.*;

@MappedSuperclass
public abstract class AbstractResource extends AbstractEntity<Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	@Column(name="resource_id")
	Long id;

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

}
