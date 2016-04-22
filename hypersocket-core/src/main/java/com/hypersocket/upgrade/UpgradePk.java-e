/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.upgrade;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class UpgradePk implements Serializable {

	private static final long serialVersionUID = 3316065208271057468L;

	@Column(name = "module", nullable = false)
    private String module;

    @Column(name = "language", nullable = false)
    private String language;
    
    public UpgradePk() {
    }
    
    public UpgradePk(String module, String language) {
        this.language = language;
        this.module = module;
    }

    public final String getLanguage() {
        return language;
    }

    public final void setLanguage(String language) {
        this.language = language;
    }

    public final String getModule() {
        return module;
    }

    public final void setModule(String module) {
        this.module = module;
    }
    
    public int hashCode() {
        return ( getModule() + "." + getLanguage() ).hashCode(); 
    }
    
    public boolean equals(Object o) {
        return o != null && o instanceof UpgradePk && module.equals(((UpgradePk)o).getModule()) && language.equals(((UpgradePk)o).getLanguage());
    }

}
