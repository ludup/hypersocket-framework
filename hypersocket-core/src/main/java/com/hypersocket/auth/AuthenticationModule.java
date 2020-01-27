/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.hypersocket.repository.AbstractEntity;

@Entity
@Table(name = "auth_scheme_modules")
public class AuthenticationModule extends AbstractEntity<Long> {

	private static final long serialVersionUID = 1206533209868713440L;

	@Id
	@GeneratedValue(strategy=GenerationType.TABLE)
	@Column(name = "module_id")
	private Long id;

	@Column(name = "template")
	private String template;

	@OneToOne
	@JoinColumn(name = "scheme_id")
	private AuthenticationScheme scheme;

	@Column(name = "idx")
	private Integer idx;

	public AuthenticationModule() {

	}

	public AuthenticationModule(String template, Integer idx) {
		this.template = template;
		this.idx = idx;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public AuthenticationScheme getScheme() {
		return scheme;
	}

	public void setScheme(AuthenticationScheme scheme) {
		this.scheme = scheme;
	}

	public Integer getIndex() {
		return idx;
	}

	public void setIndex(Integer idx) {
		this.idx = idx;
	}

}
