package com.hypersocket.remoteservices;

import com.hypersocket.realm.Realm;

public interface RemoteServiceIdentificationProvider {

	String getAccountName(Realm realm);

	String getAccountID(Realm realm);

	String getAccountUser(Realm realm);

	String getAccountEmail(Realm realm);
}
