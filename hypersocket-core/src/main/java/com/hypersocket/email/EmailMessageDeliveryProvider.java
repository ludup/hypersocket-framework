package com.hypersocket.email;

import com.hypersocket.messagedelivery.MessageDeliveryBuilder;
import com.hypersocket.messagedelivery.MessageDeliveryProvider;

public interface EmailMessageDeliveryProvider<B extends MessageDeliveryBuilder> extends MessageDeliveryProvider<B> {

}
