package com.hypersocket.messagedelivery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.email.RecipientHolder;
import com.hypersocket.messagedelivery.MessageDeliveryPreSendCheck.MessageDeliveryPreSendCheckException;
import com.hypersocket.messagedelivery.MessageDeliveryRetry.Result;
import com.hypersocket.messagedelivery.MessageDeliveryRetry.RetryableCheckedExceptionWrapper;
import com.hypersocket.realm.Realm;
import com.hypersocket.triggers.ValidationException;
import com.hypersocket.util.SpringApplicationContextProvider;

public abstract class MessageDeliveryBuilder {
	
	private static Logger log = LoggerFactory.getLogger(MessageDeliveryBuilder.class);

	private final List<RecipientHolder> recipients = new ArrayList<>();
	private String text;
	private int delay;
	private String context;
	private Realm realm;
	private boolean partialDeliveryIsException;

	protected MessageDeliveryBuilder() {
	}

	public boolean partialDeliveryIsException() {
		return partialDeliveryIsException;
	}

	public MessageDeliveryBuilder partialDeliveryIsException(boolean partialDeliveryIsException) {
		this.partialDeliveryIsException = partialDeliveryIsException;
		return this;
	}

	public List<RecipientHolder> recipients() {
		return recipients;
	}

	public MessageDeliveryBuilder recipient(RecipientHolder recipient) {
		this.recipients.clear();
		addRecipients(Arrays.asList(recipient));
		return this;
	}

	public MessageDeliveryBuilder recipients(RecipientHolder[] recipients) {
		this.recipients.clear();
		if (recipients != null)
			this.recipients.addAll(Arrays.asList(recipients));
		return this;
	}

	public MessageDeliveryBuilder recipients(List<RecipientHolder> recipients) {
		this.recipients.clear();
		this.recipients.addAll(recipients);
		return this;
	}

	public MessageDeliveryBuilder addRecipients(RecipientHolder... recipients) {
		return addRecipients(Arrays.asList(recipients));
	}

	public MessageDeliveryBuilder addRecipients(List<RecipientHolder> recipients) {
		this.recipients.addAll(recipients);
		return this;
	}

	public MessageDeliveryBuilder addRecipientAddresses(String... recipients) throws ValidationException {
		return addRecipientAddresses(Arrays.asList(recipients));
	}

	public MessageDeliveryBuilder addRecipientAddresses(List<String> recipientAddresses) throws ValidationException {
		for (var recipientAddress : recipientAddresses) {
			this.recipients.add(parseRecipient(recipientAddress));
		}
		return this;
	}

	public boolean validate(String... addressSpecs) {
		for (var addressSpec : addressSpecs) {
			try {
				parseRecipient(addressSpec);
			} catch (ValidationException ve) {
				return false;
			}
		}
		return true;
	}

	public RecipientHolder parseRecipient(String addressSpec) throws ValidationException {
		return RecipientHolder.ofGeneric(addressSpec);
	}

	public String getName(String addressSpec) {
		try {
			return parseRecipient(addressSpec).getName();
		} catch (ValidationException e) {
			return "";
		}
	}

	public String getAddress(String addressSpec) {
		try {
			return parseRecipient(addressSpec).getAddress();
		} catch (ValidationException e) {
			return addressSpec;
		}
	}

	public String text() {
		return text;
	}

	public MessageDeliveryBuilder text(String text) {
		this.text = text;
		return this;
	}

	public int delay() {
		return delay;
	}

	public MessageDeliveryBuilder delay(int delay) {
		this.delay = delay;
		return this;
	}

	public String context() {
		return context;
	}

	public MessageDeliveryBuilder context(String context) {
		this.context = context;
		return this;
	}

	public Realm realm() {
		return realm;
	}

	public MessageDeliveryBuilder realm(Realm realm) {
		this.realm = realm;
		return this;
	}

	public final MessageDeliveryResult send() throws MessageDeliveryException {

		Objects.requireNonNull(this.realm);

		var messageDeliveryPreSendCheck = SpringApplicationContextProvider.getApplicationContext()
				.getBean(MessageDeliveryPreSendCheck.class);

		Objects.requireNonNull(messageDeliveryPreSendCheck);

		if (!messageDeliveryPreSendCheck.canSend(this.realm)) {
			throw new MessageDeliveryPreSendCheckException(String
					.format("Invalid license found for realm: '%s' during Message Delivery", this.realm.getName()));
		}

		var messageDeliveryRetry = SpringApplicationContextProvider.getApplicationContext()
				.getBean(MessageDeliveryRetry.class);

		Objects.requireNonNull(messageDeliveryRetry);

		var result = messageDeliveryRetry.retry(() -> {
			try {
				var res = sendImpl();
				
				if (res == null || res.isEmpty()) {
					throw new MessageDeliveryException("Nothing was sent.");
				} else if (res.isPartialFailure()) {
					if (partialDeliveryIsException)
						throw new MessageDeliveryException(res);
				}

				if (res.isSingleResult())
					res = res.getDetails().get(0);

				if (res.isFailure())
					throw new MessageDeliveryException(res);

				return res;
				
			} catch (MessageDeliveryException e) {
				throw new RetryableCheckedExceptionWrapper(e);
			}
		}, String.format("Realm:%s", this.realm.getName()));

		var reason = result.getReason();

		switch (reason) {
			case Success: return handleSuccess(result);
			case CountComplete: handleCountComplete(result);break;
			case ExceptionNoMatch: handleExceptionNoMatch(result);break;
			case UnRecoverable: handleUnRecoverable(result);break;
		}
		
		throw new IllegalStateException("No matching path during retry, flow should not have reached here, most likely a bug!");

	}

	private void handleUnRecoverable(Result<MessageDeliveryResult> result) {
		
		log.info("Result for retry with tag '{}' ended up in handleUnRecoverable", result.getTag());
		
		// unlikely case still handled
		throw new IllegalStateException("Unrecoverable throwable caught during retry!");
		
	}

	private void handleExceptionNoMatch(Result<MessageDeliveryResult> result) {
		log.info("Result for retry with tag '{}' ended up in handleExceptionNoMatch", result.getTag());
		
		// There was an exception but it was not the one we retry on
		// need to re throw back, may be a null pointer, let caller handle it
		var lastException = result.getLastException();
		if (lastException.isPresent()) {
			throw new IllegalStateException(lastException.get());
		}
	}

	private void handleCountComplete(Result<MessageDeliveryResult> result) throws MessageDeliveryException {
		
		log.info("Result for retry with tag '{}' ended up in handleCountComplete", result.getTag());
		
		var lastException = result.getLastException();
		// all try complete and we have exception, throw it again, as if exception was
		// thrown on first go,
		// this for calling program to react to exception
		if (lastException.isPresent()) {
			// special handling as calling program does expects exception of this type
			throw (MessageDeliveryException) lastException.get();
		}
		
		throw new IllegalStateException(lastException.get());
		
	}

	private MessageDeliveryResult handleSuccess(Result<MessageDeliveryResult> result) throws MessageDeliveryException {
		
		log.info("Result for retry with tag '{}' ended up in handleSuccess", result.getTag());
		
		if (result.getResult().isEmpty()) {
			throw new IllegalStateException("No result found on sucess flow!");
		}
		
		return result.getResult().get();
		
	}

	protected abstract MessageDeliveryResult sendImpl() throws MessageDeliveryException;

}
