package com.hypersocket.resource;


import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PreRemove;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hypersocket.ApplicationContextServiceImpl;
import com.hypersocket.properties.PropertyRepository;
import com.hypersocket.repository.AbstractEntity;

@MappedSuperclass
public abstract class SimpleResource extends AbstractEntity<Long> {

	private static final long serialVersionUID = 306989572401186385L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	@Column(name="resource_id")
	private Long id;


	@Column(name = "reference", updatable=false)
	protected String reference = UUID.randomUUID().toString();
	

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	
	@JsonIgnore
	public String getUUID() {
		return reference;
	}
	
	@JsonIgnore
	public UUID toUUID() {
		return UUID.fromString(reference);
	}
	
	public abstract String getName();

	@PreRemove
	public void onDelete() {
		ApplicationContextServiceImpl.getInstance().getBean(PropertyRepository.class).deleteProperties(this);
	}
}
