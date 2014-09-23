package com.hypersocket.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HypersocketUtils {

	static Logger log = LoggerFactory.getLogger(HypersocketUtils.class);

	static ThreadLocal<Long> times = new ThreadLocal<Long>();

	public static void resetInterval() {
		times.set(System.currentTimeMillis());
	}

	public static void logInterval(String msg) {

		log.info("REMOVE ME: " + msg + ": "
				+ (System.currentTimeMillis() - times.get()));
		resetInterval();
	}

	/**
	 * Encapsulate a part of a string by a given character.useful in hiding part of a password
	 * 
	 * @param original
	 *            Original string
	 * @param start
	 *            Start position of masking
	 * @param maskcharacter
	 *            masking character of the rest of the String
	 * @return if the given string length is shorter than start position then
	 *         return the original string, otherwise return original string with
	 *         replace masking character from the start position onward
	 */
	public static String maskingString(String original, int start,String maskcharacter) {
		String result = null;
		if (start < 0) {
			throw new IllegalArgumentException("Start position should be greater than 0");
		}
		if (original.length() > start) {
			result = original.substring(0, start)+ original.substring(start).replaceAll(".", maskcharacter);
		} else {
			result = original;
		}
		return result;
	}

}
