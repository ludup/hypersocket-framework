package com.hypersocket.messagedelivery;

import com.hypersocket.realm.Realm;

/**
 * Interface to check and support different message delivery interfaces whether it can send message or not.
 * 
 * <br />
 * <strong>Note:</strong> Do not register implementations as Spring bean make use of Java SPI,
 * check {@link MessageDeliveryPreSendCheckConfiguration} for more details.
 */
@FunctionalInterface
public interface MessageDeliveryPreSendCheck {
	
	boolean canSend(Realm realm);
	
	
	public static class MessageDeliveryPreSendCheckException extends RuntimeException {
		public MessageDeliveryPreSendCheckException(String msg) {
			super(msg);
		}

		private static final long serialVersionUID = 5832276272056229851L;
	}
}
