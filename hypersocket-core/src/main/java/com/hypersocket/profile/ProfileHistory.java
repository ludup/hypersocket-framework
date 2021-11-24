package com.hypersocket.profile;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.hypersocket.repository.AbstractEntity;

@Entity
@Table(name="profile_history")
public class ProfileHistory extends AbstractEntity<Long>{

	private static final long serialVersionUID = -5378742901637458377L;

	@Column(name = "realm_id")
	Long realmId;
	
	@Column(name = "report_date")
	@Temporal(TemporalType.DATE)
	Date reportDate;
	
	@Column(name="used_profiles")
	long profileCount;
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	@Column(name = "resource_id")
	private Long id;
	
	@Override
	public Long getId() {
		return id;
	}

	public Long getRealmId() {
		return realmId;
	}

	public void setRealmId(Long realmId) {
		this.realmId = realmId;
	}

	public Date getReportDate() {
		return reportDate;
	}

	public void setReportDate(Date reportDate) {
		this.reportDate = reportDate;
	}

	public long getProfileCount() {
		return profileCount;
	}

	public void setProfileCount(long profileCount) {
		this.profileCount = profileCount;
	}

}
