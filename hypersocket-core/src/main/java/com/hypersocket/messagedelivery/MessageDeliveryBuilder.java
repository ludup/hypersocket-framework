package com.hypersocket.messagedelivery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hypersocket.email.RecipientHolder;
import com.hypersocket.realm.Realm;
import com.hypersocket.triggers.ValidationException;

public abstract class MessageDeliveryBuilder {

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
		for(var recipientAddress : recipientAddresses) {
			this.recipients.add(parseRecipient(recipientAddress));	
		}
		return this;
	}
	
	public boolean validate(String... addressSpecs) {
		for(var addressSpec : addressSpecs) {
			try {
				parseRecipient(addressSpec);
			}
			catch(ValidationException ve) {
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
		var res = sendImpl();
		if(res.isEmpty()) {
			throw new MessageDeliveryException("Nothing was sent.");
		}
		else if(res.isPartialFailure()) {
			if(partialDeliveryIsException)
				throw new MessageDeliveryException(res);
		}
		
		if(res.isSingleResult())
			res = res.getDetails().get(0);

		if(res.isFailure())
			throw new MessageDeliveryException(res);
		
		return res;
	}

	protected abstract MessageDeliveryResult sendImpl() throws MessageDeliveryException;

}
