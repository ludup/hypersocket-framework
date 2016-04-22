/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.upgrade;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "upgrade")
public class Upgrade {

    @Id
    private UpgradePk pk;

    @Column(name = "version", nullable = false)
    private String version;
    
    public Upgrade() {
    }
    
    public Upgrade(String version, String module, String language) {
        this.version = version;
        this.pk = new UpgradePk(module, language);
    }

    public final String getVersion() {
        return version;
    }

    public final void setVersion(String version) {
        this.version = version;
    }

    public final UpgradePk  getPk() {
        return pk;
    }

    public final void setPk(UpgradePk pk) {
        this.pk = pk;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[pk='").append(getPk());
        builder.append("', version='").append(getVersion());
        builder.append("']");
        return builder.toString();
    }
}
