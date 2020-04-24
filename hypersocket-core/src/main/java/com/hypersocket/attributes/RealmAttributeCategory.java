package com.hypersocket.attributes;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.hypersocket.realm.Realm;
import com.hypersocket.resource.RealmResource;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "realm_attribute_categories")
public abstract class RealmAttributeCategory<A extends AbstractAttribute<?>> extends RealmResource
		implements AttributeCategory<A> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -682595785528401400L;

	@ManyToOne
	@JoinColumn(name = "realm_id", foreignKey = @ForeignKey(name = "realm_attribute_categories_cascade_1"))
	@OnDelete(action = OnDeleteAction.CASCADE)
	protected Realm realm;

	@Override
	protected Realm doGetRealm() {
		return realm;
	}

	@Override
	public void setRealm(Realm realm) {
		this.realm = realm;
	}

	abstract public void setWeight(Integer weight);

	abstract public Set<A> getAttributes();
}
