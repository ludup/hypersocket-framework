package com.hypersocket.i18n;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.hypersocket.repository.AbstractEntity;

@Entity
@Table(name="i18n", uniqueConstraints={@UniqueConstraint(columnNames={"bundle", "name"})})
public class I18nOverride extends AbstractEntity<Long> {

	private static final long serialVersionUID = -1158209957523989316L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	@Column(name="id")
	private Long id;

	@Column(name="locale")
	private String locale;
	
	@Column(name="bundle", length=191)
	private String bundle;
	
	@Column(name="name", length=191)
	private String name;
	
	@Lob
	@Column(name="value")
	private String value;
	
	@Override
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getBundle() {
		return bundle;
	}

	public void setBundle(String bundle) {
		this.bundle = bundle;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	
}
