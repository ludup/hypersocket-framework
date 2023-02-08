package com.hypersocket.messagedelivery;

import com.hypersocket.realm.MediaType;
import com.hypersocket.realm.Realm;

public interface MessageDeliveryProvider<B extends MessageDeliveryBuilder> {

	MediaType getSupportedMedia();
	
	default boolean isDefault() {
		return true;
	}
	
	String getResourceKey();
	
	B newBuilder(Realm realm);

	boolean isEnabled();

}
