package com.hypersocket.resource;

/**
 * Exceptions of this type should be thrown all the way to the
 * controller and not intercepted or rethrown by the service layer or
 * anythinng else.
 *
 */
public class ResourcePassthroughException extends ResourceException {

	private static final long serialVersionUID = -1594953009953156880L;

	public ResourcePassthroughException(ResourceException e) {
		super(e);
	}

	public ResourcePassthroughException(String bundle, String resourceKey, Object... args) {
		super(bundle, resourceKey, args);
	}

	public ResourcePassthroughException(Throwable cause, String bundle, String resourceKey, Object... args) {
		super(cause, bundle, resourceKey, args);
	}

	public ResourcePassthroughException(Throwable e) {
		super(e);
	}
	
	public static void maybeRethrow(Throwable t) throws ResourcePassthroughException {
		if(t instanceof ResourcePassthroughException)
			throw (ResourcePassthroughException)t;
		if(t.getCause() instanceof ResourcePassthroughException) {
			throw (ResourcePassthroughException)t.getCause();
		}
	}

}
