package com.hypersocket.messagedelivery;

import com.hypersocket.realm.Realm;

public interface SenderContext {

	String getAccountName(Realm realm);

	String getAccountID(Realm realm);
}
