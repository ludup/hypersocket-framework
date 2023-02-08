package com.hypersocket.messagedelivery;

import java.io.IOException;
import java.util.Optional;

@SuppressWarnings("serial")
public class MessageDeliveryException extends IOException {

	private final Optional<MessageDeliveryResult> result;

	public MessageDeliveryException(String message, Throwable cause) {
		super(message, cause);
		this.result = Optional.empty();
	}

	public MessageDeliveryException(String message) {
		super(message);
		this.result = Optional.empty();
	}

	public MessageDeliveryException(Throwable cause) {
		super(cause);
		this.result = Optional.empty();
	}

	public MessageDeliveryException(MessageDeliveryResult result) {
		super(buildExceptionText(result), result.getException().get());
		this.result = Optional.empty();
	}

	public Optional<MessageDeliveryResult> getResult() {
		return result;
	}

	private static String buildExceptionText(MessageDeliveryResult result) {
		if (result.isSuccess())
			throw new IllegalArgumentException(String.format("Cannot construct a %s with a %s that is a success.",
					MessageDeliveryException.class.getName(), MessageDeliveryResult.class.getName()));
		if (result.isPartialFailure()) {
			throw new IllegalArgumentException(String.format(
					"The message delivery was only partially successful. %d succeeded and %d failed out of a total of %d recipients.",
					MessageDeliveryException.class.getName(), MessageDeliveryResult.class.getName()));
		}

		return "Failed to deliver message.";
	}

}
