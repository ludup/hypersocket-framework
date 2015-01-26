/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.upgrade;

import java.net.URL;

import com.hypersocket.Version;

public class UpgradeOp implements Comparable<UpgradeOp> {

    private Version version;
    private URL url;
    private String module;
    private String language;

    UpgradeOp(Version version, URL url, String module, String language) {
        this.version = version;
        this.url = url;
        this.module = module;
        this.language = language;
    }

    public int compareTo(UpgradeOp o) {
        int i = getVersion().compareTo(o.getVersion());
        if (i == 0) {
            i = getLanguage().compareTo(o.getLanguage());
            if (i == 0) {
                i = getUrl().toExternalForm().compareTo(o.getUrl().toExternalForm());
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

    public URL getUrl() {
        return url;
    }

    public Version getVersion() {
        return version;
    }

}
