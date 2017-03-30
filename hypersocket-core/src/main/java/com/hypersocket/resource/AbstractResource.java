package com.hypersocket.resource;

import com.hypersocket.repository.AbstractEntity;

import javax.persistence.*;

@MappedSuperclass
public abstract class AbstractResource extends AbstractEntity<Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	@Column(name="resource_id")
	Long id;

	@Column(name="legacy_id")
	Long legacyId;

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	public Long getLegacyId() {
		return legacyId == null ? id : legacyId;
	}

	public void setLegacyId(Long legacyId) {
		this.legacyId = legacyId;
	}
}
