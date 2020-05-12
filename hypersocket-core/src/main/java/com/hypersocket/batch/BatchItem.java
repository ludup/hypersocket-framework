package com.hypersocket.batch;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.hypersocket.realm.Realm;
import com.hypersocket.repository.AbstractEntity;

@Entity
@Table(name = "batch_processing_items")
public class BatchItem extends AbstractEntity<Long> {

	private static final long serialVersionUID = -6097805172247469296L;

	@Column(name = "resource_key")
	private String resourceKey;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	@Column(name="resource_id")
	private Long id;
	
	@ManyToOne
	@JoinColumn(name= "realm_id", foreignKey = @ForeignKey(name = "batch_processing_items_cascade_1"))
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Realm realm;
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Realm getRealm() {
		return realm;
	}

	public void setRealm(Realm realm) {
		this.realm = realm;
	}

	public String getResourceKey() {
		return resourceKey;
	}

	public void setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
	}

}
