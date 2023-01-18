package com.hypersocket.email;

import java.util.List;

import javax.mail.Message.RecipientType;

import com.hypersocket.messagedelivery.MessageDeliveryBuilder;
import com.hypersocket.messagedelivery.MessageDeliveryProvider;
import com.hypersocket.triggers.ValidationException;

public interface EmailMessageDeliveryProvider<B extends MessageDeliveryBuilder> extends MessageDeliveryProvider<B> {

	@Deprecated
	default boolean validateEmailAddress(String email) {
		return newBuilder(null).validate(email);
	}

	@Deprecated
	default boolean validateEmailAddresses(String[] emails) {
		return newBuilder(null).validate(emails);
	}

	@Deprecated
	default String populateEmailList(String[] emails, List<RecipientHolder> recipients,
			RecipientType type) throws ValidationException {
		var ret = new StringBuffer();

		for (var email : emails) {
			if (ret.length() > 0) {
				ret.append(", ");
			}
			ret.append(email);
			try {
				recipients.add(newBuilder(null).parseRecipient(email));
			}
			catch(ValidationException iae) {
			}
		}

		return ret.toString();
	}
	@Deprecated
	default String getEmailName(String addressSpec) {
		return newBuilder(null).getName(addressSpec);
	}

	@Deprecated
	default String getEmailAddress(String addressSpec) {
		return newBuilder(null).getAddress(addressSpec);
	}
}
