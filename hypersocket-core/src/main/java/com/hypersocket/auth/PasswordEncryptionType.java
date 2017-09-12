/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

public enum PasswordEncryptionType {

	PBKDF2_SHA1_1000("PBKDF2WithHmacSHA1", 160, 10000),
	PBKDF2_SHA1_20000("PBKDF2WithHmacSHA1", 160, 20000),
	PBKDF2_SHA256_20000("PBKDF2WithHmacSHA256", 256, 20000),
	PBKDF2_SHA256_50000("PBKDF2WithHmacSHA256", 256, 50000),
	PBKDF2_SHA512_50000("PBKDF2WithHmacSHA512", 512, 50000),
	PBKDF2_SHA512_100000("PBKDF2WithHmacSHA512", 512, 100000);
	
	private final String val;
	private final int keyLength;
	private final int iterations;
	
	private PasswordEncryptionType(final String val, int keyLength, int iterations) {
		this.val = val;
		this.keyLength = keyLength;
		this.iterations = iterations;
	}
	
	public String toString() {
		return val;
	}
	
	public int getKeyLength() {
		return keyLength;
	}
	
	public int getIterations() {
		return iterations;
	}
}
