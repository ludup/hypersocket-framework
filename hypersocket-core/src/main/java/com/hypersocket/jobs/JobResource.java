package com.hypersocket.jobs;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.hypersocket.realm.Realm;
import com.hypersocket.resource.RealmResource;

@Entity
@Table(name = "jobs")
public class JobResource extends RealmResource {

	private static final long serialVersionUID = 5348826725565924228L;

	@OneToOne
	private JobResource parentJob;

	@Column(name = "state")
	private JobState state;

	@Column(name = "result")
	@Lob
	private String result;
	@ManyToOne
	@JoinColumn(name = "realm_id", foreignKey = @ForeignKey(name = "jobs_cascade_1"))
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

	public JobResource getParentJob() {
		return parentJob;
	}

	public void setParentJob(JobResource parentJob) {
		this.parentJob = parentJob;
	}

	public JobState getState() {
		return state;
	}

	public void setState(JobState state) {
		this.state = state;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

}
