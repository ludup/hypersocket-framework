package com.hypersocket.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HypersocketUtils {

	static Logger log = LoggerFactory.getLogger(HypersocketUtils.class);

	static ThreadLocal<Long> times = new ThreadLocal<Long>();

	static Map<String,SimpleDateFormat> dateFormats = new HashMap<String,SimpleDateFormat>();
	
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
	
	/**
	 * Format a date with a given format. Formats are cached to prevent excessive use of 
	 * DateFormat.
	 * @param date
	 * @param format
	 * @return
	 */
	public static String formatDate(Date date, String format) {
		
		if(!dateFormats.containsKey(format)) {
			dateFormats.put(format, new SimpleDateFormat(format));
		}
		
		return dateFormats.get(format).format(date);
	}
	
	public static String formatDate(Date date) {
		return formatDate(date, "EEE, d MMM yyyy HH:mm:ss");
	}
	
	/**
	 * Parse a date on a given format. 
	 * @param date
	 * @param format
	 * @return
	 * @throws ParseException
	 */
	public static Date parseDate(String date, String format) throws ParseException {
		if(!dateFormats.containsKey(format)) {
			dateFormats.put(format, new SimpleDateFormat(format));
		}
		
		return dateFormats.get(format).parse(date);
	}
	

	/**
	 * Strip the port from a host header.
	 * @param hostHeader
	 * @return
	 */
	public static String stripPort(String hostHeader) {
		int idx = hostHeader.indexOf(':');
		if(idx > -1) {
			return hostHeader.substring(0, idx);
		}
		return hostHeader;
	}
}
