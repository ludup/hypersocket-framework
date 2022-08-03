/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.upgrade;

import com.hypersocket.json.version.Version;

public class UpgradeOp<C> implements Comparable<UpgradeOp<C>> {

    private Version version;
    private C callable;
    private String module;
    private String language;

    UpgradeOp(Version version, C callable, String module, String language) {
        this.version = version;
        this.callable = callable;
        this.module = module;
        this.language = language;
    }

    public int compareTo(UpgradeOp<C> o) {
        int i = getVersion().compareTo(o.getVersion());
        if (i == 0) {
            i = getLanguage().compareTo(o.getLanguage());
            if (i == 0) {
                i = callable.toString().compareTo(callable.toString());
            }
        }
        return i;
    }

    public String getLanguage() {
        return language;
    }

    public String getModule() {
        return module;
    }

    public C getCallable() {
        return callable;
    }

    public Version getVersion() {
        return version;
    }

}
