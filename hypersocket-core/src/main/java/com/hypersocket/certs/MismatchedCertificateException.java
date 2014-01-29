/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.certs;

public class MismatchedCertificateException extends Exception {

	private static final long serialVersionUID = -3898335439807988224L;

	public MismatchedCertificateException() {
	}

	public MismatchedCertificateException(String message) {
		super(message);
	}

	public MismatchedCertificateException(Throwable cause) {
		super(cause);
	}

	public MismatchedCertificateException(String message, Throwable cause) {
		super(message, cause);
	}

}
