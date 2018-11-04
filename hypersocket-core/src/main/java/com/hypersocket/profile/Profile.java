package com.hypersocket.profile;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.repository.AbstractEntity;
import com.hypersocket.utils.HypersocketUtils;

@Entity
@Table(name="profiles")
public class Profile extends AbstractEntity<Long>{

	private static final long serialVersionUID = 6673975856205519969L;
	
	@Id
	@Column(name="principal_id")
	Long id;
	
	@OneToOne
	Realm realm;
	
	@OneToMany(orphanRemoval=true, fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	@JoinColumn(name="profile_principal_id")
	List<ProfileCredentials> credentials;
	
	@Column(name="state")
	ProfileCredentialsState state;
	
	@Column(name="completed")
	@Temporal(TemporalType.DATE)
	Date completed;
	
	@Override
	public Long getId() {
		return id;
	}

	public void setId(Principal principal) {
		this.id = principal.getId();
	}

	public List<ProfileCredentials> getCredentials() {
		return credentials;
	}

	public void setCredentials(List<ProfileCredentials> credentials) {
		this.credentials = credentials;
	}

	public Realm getRealm() {
		return realm;
	}

	public void setRealm(Realm realm) {
		this.realm = realm;
	}

	public ProfileCredentialsState getState() {
		return state;
	}

	public void setState(ProfileCredentialsState state) {
		if(state == ProfileCredentialsState.COMPLETE && this.state != ProfileCredentialsState.COMPLETE) {
			completed = HypersocketUtils.today();
		} else if(state != ProfileCredentialsState.COMPLETE) {
			completed = null;
		}
		this.state = state;
	}

	public Date getCompleted() {
		return completed;
	}

	public void setCompleted(Date completed) {
		this.completed = completed;
	}
	
	
}
