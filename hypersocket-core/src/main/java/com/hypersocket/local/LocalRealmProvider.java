/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.local;

import org.springframework.context.event.ContextStartedEvent;

import com.hypersocket.realm.RealmProvider;

public interface LocalRealmProvider extends RealmProvider {

	void started(ContextStartedEvent cse);
}
