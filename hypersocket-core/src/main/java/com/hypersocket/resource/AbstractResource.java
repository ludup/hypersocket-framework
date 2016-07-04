package com.hypersocket.resource;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hypersocket.repository.AbstractEntity;

@MappedSuperclass
public abstract class AbstractResource extends AbstractEntity<Long> {

	private static final long serialVersionUID = 306989572401186385L;
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	@Column(name="resource_id")
	Long id;

	@Column(name = "reference", updatable=false)
	String reference = UUID.randomUUID().toString();
	
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
	
	
}
