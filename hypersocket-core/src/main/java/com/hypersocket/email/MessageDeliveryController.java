package com.hypersocket.email;

import com.hypersocket.messagedelivery.MessageDeliveryProvider;

public interface MessageDeliveryController {

	boolean canSend(MessageDeliveryProvider<?> provider);
}
