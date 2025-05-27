package com.hypersocket.messagedelivery;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.simplejavamail.MailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.config.SystemConfigurationService;

@Component
public class MessageDeliveryRetry {

	private static Logger log = LoggerFactory.getLogger(MessageDeliveryRetry.class);

	public enum RetryCompleteReason {
		Success, CountComplete, ExceptionNoMatch, UnRecoverable
	}

	public static class Result<T> {

		private Stack<Exception> exceptions = new Stack<>();
		private T result;
		private RetryCompleteReason reason;
		private final String tag;
		
		public Result(String tag) {
			this.tag = tag;
		}

		public void addException(Exception e) {
			Objects.requireNonNull(e);
			
			if (e instanceof RetryableCheckedExceptionWrapper) {
				this.exceptions.add(((RetryableCheckedExceptionWrapper) e).getWrapped());
				return;
			}
			
			this.exceptions.add(e);
		}

		public Optional<Exception> getLastException() {
			return this.exceptions.isEmpty() ? Optional.empty() : Optional.of(this.exceptions.peek());
		}

		public Optional<T> getResult() {
			return Optional.ofNullable(this.result);
		}

		public void setResult(T result) {
			this.result = result;
		}

		public RetryCompleteReason getReason() {
			return reason;
		}

		public void setReason(RetryCompleteReason reason) {
			this.reason = reason;
		}

		public String getTag() {
			return tag;
		}

	}

	@Autowired
	private SystemConfigurationService systemConfigurationService;

	public <T> Result<T> retry(Supplier<T> logic, String tag, boolean shouldRetry) {

		var maxRetries = systemConfigurationService.getIntValue("message.delivery.retry.number");

		Objects.requireNonNull(maxRetries);

		var baseDelaySeconds = systemConfigurationService.getIntValue("message.delivery.retry.time");

		Objects.requireNonNull(baseDelaySeconds);

		// if by configuration or defined by implementation, no retry
		// configuration is dynamic, implementation is hardcoded
		if (baseDelaySeconds == 0 || !shouldRetry) {
			maxRetries = 0; // marks no retires
		}

		var baseDelayMillis = (long) baseDelaySeconds * 1000;

		return retry(logic, maxRetries, baseDelayMillis, Set.of(IOException.class, MailException.class), null, tag);

	}

	public <T> Result<T> retry(Supplier<T> logic, int maxRetries, long baseDelayMillis,
			Set<Class<? extends Exception>> retryOn, Predicate<T> isResultValid, String tag) {

		int attempt = 0;

		Result<T> resultHolder = new Result<T>(String.format("%s:%s", tag, logic.hashCode()));

		while (true) {
			try {

				T result = logic.get();

				if (isResultValid != null && !isResultValid.test(result)) {
					throw new RetryableResultException("Invalid result, retrying...");
				}

				resultHolder.setResult(result);
				resultHolder.setReason(RetryCompleteReason.Success);

				break;

			} catch (Exception e) {
				
				attempt++;
				
				log.error("Exception while trying, attempting retry for logic with tag '{}' and logic supplier tag {}.", tag, logic.hashCode(), e);
				
				var ex = (e instanceof RetryableCheckedExceptionWrapper) ? ((RetryableCheckedExceptionWrapper) e).getWrapped() : e;

				boolean shouldRetryForException = retryOn.stream().anyMatch(clazz -> clazz.isInstance(ex));
				
				resultHolder.addException(e);

				if (!shouldRetryForException) {
					log.info("Exception caught is not marked for retry, no further attempts for tag '{}' and logic supplier tag {}.", tag, logic.hashCode());
					resultHolder.setReason(RetryCompleteReason.ExceptionNoMatch);
					break;
				}
				
				if (attempt > maxRetries) {
					log.info("All attempts tried, no further attempts for tag '{}' and logic supplier tag {}.", tag, logic.hashCode());
					resultHolder.setReason(RetryCompleteReason.CountComplete);
					break;
				}

				long delay = calculateDelay(baseDelayMillis, attempt);
				sleepSafely(delay);

			} catch (Throwable t) {
				log.error("Unrecoverable error, caught throwable for logic with tag '{}' and logic supplier tag '{}'.", tag, logic.hashCode(), t);
				resultHolder.setReason(RetryCompleteReason.UnRecoverable);
				break;
			}
		}

		return resultHolder;
	}

	private static long calculateDelay(long baseDelayMillis, int attempt) {
		return baseDelayMillis;
	}

	private static void sleepSafely(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Retry interrupted", ie);
		}
	}

	private static class RetryableResultException extends RuntimeException {
		private static final long serialVersionUID = 6214248407374727600L;

		public RetryableResultException(String message) {
			super(message);
		}
	}
	
	public static class RetryableCheckedExceptionWrapper extends RuntimeException {
		
		private static final long serialVersionUID = 6214248407974727600L;
		private final Exception wrapped;

		public RetryableCheckedExceptionWrapper(Exception wrapped) {
			super(String.format("Wrapping exception of class : %s.", wrapped.getClass()));
			this.wrapped = wrapped;
		}
		
		public Exception getWrapped() {
			return wrapped;
		}
		
	}

}
