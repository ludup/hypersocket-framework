package com.hypersocket.attributes;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.hypersocket.repository.AbstractEntity;

@Entity
@Table(name="attribute_categories")
public class AttributeCategory extends AbstractEntity<Long>  {

	@Column(name="context")
	String context;
	
	@Column(name="name")
	String name;
	
	@Column(name="weight", nullable=false)
	Integer weight = new Integer(0);

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	@Column(name="category_id")
	Long id;

	@OneToMany(mappedBy = "category", fetch = FetchType.EAGER)
	protected Set<Attribute> attributes;
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public Integer getWeight() {
		return weight;
	}
	
	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Set<Attribute> getAttributes() {
		return attributes;
	}
}
