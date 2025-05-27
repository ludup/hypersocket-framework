package com.hypersocket.messagedelivery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.email.RecipientHolder;
import com.hypersocket.resource.ThrowingRunnable;

public class MessageDeliveryResult {
	
	static Logger LOG = LoggerFactory.getLogger(MessageDeliveryResult.class);

	public static MessageDeliveryResult ofNonFatalError(String nonFatalError, RecipientHolder... recipients) {
		return new MessageDeliveryResult(new Exception(nonFatalError), recipients);
	}

	private Optional<Throwable> exception;
	private final List<RecipientHolder> recipients = new ArrayList<>();
	private List<MessageDeliveryResult> details = new ArrayList<>();
	private Optional<Long> timeTaken = Optional.empty();
	private Optional<Double> cost = Optional.empty();
	private Optional<String> skippedReason = Optional.empty();
	private Optional<String> id = Optional.empty();

	public MessageDeliveryResult(RecipientHolder... recipients) {
		this(null, recipients);
	}

	public MessageDeliveryResult(Throwable exception, RecipientHolder... recipients) {
		this(exception, Arrays.asList(recipients));
	}

	public MessageDeliveryResult(Collection<RecipientHolder> recipients) {
		this(null, recipients);
	}
	
	public boolean isMultipleResults() {
		return details.size() > 1;
	}
	
	public boolean isSingleResult() {
		return details.size() == 1;
	}

	public MessageDeliveryResult(Throwable exception, Collection<RecipientHolder> recipients) {
		this.exception = exception == null ? Optional.empty() : Optional.of(exception);
		this.recipients.addAll(recipients);
	}

	public void wrap(ThrowingRunnable r) {
		var start = System.currentTimeMillis();
		try {
			r.run();
		} catch (Throwable t) {
			error(t);
			LOG.error("Failed to deliver message.", t);
		} finally {
			timeTaken = Optional.of(System.currentTimeMillis() - start);
		}
	}

	public <V> V wrapCallable(Callable<V> r) throws Exception {
		var start = System.currentTimeMillis();
		try {
			return r.call();
		} catch (Throwable t) {
			error(t);
			LOG.error("Failed to deliver message.", t);
			if (t instanceof Exception)
				throw (Exception) t;
			else
				throw new Exception("Failed to deliver message.", t);
		} finally {
			timeTaken = Optional.of(System.currentTimeMillis() - start);
		}
	}

	public <V> V wrapCallableAndCatchError(Callable<V> r) {
		var start = System.currentTimeMillis();
		try {
			return r.call();
		} catch (Throwable t) {
			error(t);
			LOG.error("Failed to deliver message.", t);
		} finally {
			timeTaken = Optional.of(System.currentTimeMillis() - start);
		}
		return null;
	}

	public int getSuccesses() {
		return details.isEmpty() ? (isSuccess() ? 1 : 0) : (int) details.stream().filter(d -> d.isSuccess()).count();
	}

	public int getFailures() {
		return details.isEmpty() ? (isFailure() ? 1 : 0) : (int) details.stream().filter(d -> d.isFailure()).count();
	}

	public int getSkipped() {
		return details.isEmpty() ? (isSkipped() ? 1 : 0) : (int) details.stream().filter(d -> d.isSkipped()).count();
	}

	public int getFailuresOrSkipped() {
		return details.isEmpty() ? (isSkipped() || isFailure() ? 1 : 0)
				: (int) details.stream().filter(d -> d.isSkipped() || d.isFailure()).count();
	}

	public Optional<String> getId() {
		return id;
	}

	public void setId(Optional<String> id) {
		this.id = id;
	}

	public Optional<Long> getTimeTaken() {
		return timeTaken;
	}

	public void setTimeTaken(Optional<Long> timeTaken) {
		this.timeTaken = timeTaken;
	}

	public Optional<Double> getCost() {
		return cost;
	}

	public void setCost(Optional<Double> cost) {
		this.cost = cost;
	}

	public List<RecipientHolder> getRecipients() {
		return recipients;
	}

	public Optional<Throwable> getException() {
		return exception;
	}

	public List<MessageDeliveryResult> getDetails() {
		return details;
	}

	public void error(Throwable exception) {
		this.exception = Optional.of(exception);
	}

	public void skip(String reason) {
		skippedReason = Optional.of(reason);
	}

	public boolean isEmpty() {
		return details.isEmpty();
	}

	public boolean isSuccess() {
		return !isFailure() && !isSkipped() && !isEmpty();
	}

	public boolean isSkipped() {
		return skippedReason.isPresent()
				|| !details.stream().filter(m -> m.isSkipped()).collect(Collectors.toList()).isEmpty();
	}

	public boolean isFailure() {
		return exception.isPresent()
				|| !details.stream().filter(m -> m.isFailure()).collect(Collectors.toList()).isEmpty();
	}

	public boolean isFailureOrSkipped() {
		return isSkipped() || isFailure();
	}

	public boolean isPartialFailure() {
		return details.size() > 1
				&& !details.stream().filter(m -> m.isFailure()).collect(Collectors.toList()).isEmpty();
	}

	public MessageDeliveryResult newResult(RecipientHolder... recipients) {
		var res = new MessageDeliveryResult(recipients);
		details.add(res);
		return res;
	}

}
