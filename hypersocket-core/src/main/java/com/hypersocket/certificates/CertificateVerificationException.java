package com.hypersocket.certificates;

import javax.net.ssl.SSLSession;

public class CertificateVerificationException extends RuntimeException {

	public enum Type {
		SIGNATURE_UNKNOWN, SIGNATURE_CHANGED, SIGNATURE_INVALID
	}

	private static final long serialVersionUID = 1L;

	private SSLSession session;
	private String hostname;
	private Type type;

	public CertificateVerificationException(Type type, String hostname, SSLSession session) {
		super(getMessageForType(type, hostname));
		this.hostname = hostname;
		this.session = session;
		this.type = type;
	}

	private static String getMessageForType(Type type, String hostname) {
		switch (type) {
		case SIGNATURE_INVALID:
			return "The signature for '" + hostname + "' is invalid.";
		case SIGNATURE_CHANGED:
			return "The signature for '" + hostname + "' has changed. ";
		default:
			return "The signature for '" + hostname + "' is unknown.";
		}
	}

	public Type getType() {
		return type;
	}

	public SSLSession getSession() {
		return session;
	}

	public String getHostname() {
		return hostname;
	}

}
