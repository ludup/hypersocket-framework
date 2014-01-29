/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.realm;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import com.hypersocket.resource.Resource;

@Entity
@Table(name="realms", uniqueConstraints = {@UniqueConstraint(columnNames={"name"})})
@XmlRootElement(name="realm")
public class Realm extends Resource {
	
}
