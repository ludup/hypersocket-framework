package com.hypersocket.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HypersocketUtils {

	public static final int ONE_MINUTE = 60000;
	public static final int ONE_HOUR = ONE_MINUTE * 60;
	public static final int ONE_DAY = ONE_HOUR * 24;

	static Logger log = LoggerFactory.getLogger(HypersocketUtils.class);

	private static ThreadLocal<Long> times = new ThreadLocal<Long>();

	/* BUG: Date formatters are not thread safe. */
	//static Map<String,SimpleDateFormat> dateFormats = new HashMap<String,SimpleDateFormat>();

	private static SecureRandom random = new SecureRandom();
	
	private static String ALLOWED_CHARACTERS = " ,.-_";
	
	public static String normaliseName(String name) {
		
		if(Objects.isNull(name)) {
			return null;
		}
		
		StringBuffer buf = new StringBuffer();
		for(char c : name.toCharArray()) {
			if(!Character.isAlphabetic(c) && ALLOWED_CHARACTERS.indexOf(c) == -1) {
				continue;
			}
			buf.append(c);
		}
		
		return buf.toString().trim();
	}
	
	public static Date calculateDateTime(String timezone, Date from, String time) {

		Calendar c = Calendar.getInstance();
		if(StringUtils.isBlank(timezone)) {
			timezone = TimeZone.getDefault().getID();
		}
		
		c.setTime(HypersocketUtils.today());
		c.setTimeZone(TimeZone.getTimeZone(timezone));
		
		Date ret = null;

		if (from != null) {
			c.setTime(from);
			ret = c.getTime();
		}

		
		if (StringUtils.isEmpty(time)) {
			return ret;
		}
				
		int idx = time.indexOf(":");
		if(idx==-1) {
			log.warn("calculateDateTime has invalid time parameter {}", time);
			return ret;
		}
		
		c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.substring(0, idx)));
		c.set(Calendar.MINUTE, Integer.parseInt(time.substring(idx + 1)));
		ret = c.getTime();
		

		return ret;
	}
	
	public static void resetInterval() {
		times.set(System.currentTimeMillis());
	}

	public static void logInterval(String msg) {

		log.info("REMOVE ME: " + msg + ": "
				+ (System.currentTimeMillis() - times.get()));
		resetInterval();
	}

	public static File getConfigDir() {
		return new File(System.getProperty("hypersocket.conf", "conf"));
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
		if(date==null) {
			return "";
		}
		
		return new SimpleDateFormat(format).format(date);
	}
	
	public static String formatDate(Date date) {
		return formatDate(date, "MMM d, yyyy");
	}
	
 	public static String formatDateTime(Long date) {
		return formatDateTime(new Date(date));
	}
	
	public static String formatDateTime(Date date) {
		return formatDate(date, "EEE, d MMM yyyy HH:mm:ss");
	}
	
	public static String formatShortDate(Date date) {
		return formatDate(date, "EEE, d MMM yyyy");
	}
	
	public static String formatShortDate(long date) {
		return formatShortDate(new Date(date));
	}
	
	/**
	 * Parse a date on a given format. 
	 * @param date
	 * @param format
	 * @return
	 * @throws ParseException
	 */
	public static Date parseDate(String date, String format) throws ParseException {
		
		/* BUG: Date formatters are not thread safe */
		
		/*
		if(!dateFormats.containsKey(format)) {
			dateFormats.put(format, new SimpleDateFormat(format));
		}
		
		return dateFormats.get(format).parse(date);
		*/
		
		return new SimpleDateFormat(format).parse(date);
	}
	
	public static Date today() {
		return todayCalendar().getTime();
	}
	
	public static Calendar todayCalendar() {
		
		Calendar date = Calendar.getInstance();
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		
		return date;
	}
	
	public static Date tomorrow() {
		
		Calendar date = Calendar.getInstance();
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		
		date.add(Calendar.DAY_OF_MONTH, 1);
		
		return date.getTime();
	}
	
	public static Date yesterday() {
		
		Calendar date = Calendar.getInstance();
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		
		date.add(Calendar.DAY_OF_MONTH, -1);
		return date.getTime();
	}

	/**
	 * Strip the port from a host header.
	 * @param hostHeader
	 * @return
	 */
	public static String stripPort(String hostHeader) {
		return before(hostHeader, ":");
	}

	public static String after(String value, String string) {
		int idx = value.indexOf(string);
		if(idx > -1) {
			return value.substring(idx+string.length());
		}
		return "";
	}
	
	
	public static String before(String value, String string) {
		int idx = value.indexOf(string);
		if(idx > -1) {
			return value.substring(0,idx);
		}
		return value;
	}
	
	public static String base64Encode(byte[] bytes) {
		try {
			return new String(Base64.encodeBase64(bytes, false, true), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("System does not support UTF-8 encoding!");
		}
	}
	
	public static String base64Encode(String resourceKey) {
		try {
			return base64Encode(resourceKey.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("System does not appear to support UTF-8!", e);
		}
	}

	public static byte[] base64Decode(String property) throws IOException {
		return Base64.decodeBase64(property.getBytes("UTF-8"));
	}
	
	public static String base64DecodeToString(String value) throws IOException {
		return new String(base64Decode(value), "UTF-8");
	}
	
	public static boolean isValidJSON(final String json) {
		   try {
		      final JsonParser parser = new ObjectMapper().getFactory()
		            .createParser(json);
		      while (parser.nextToken() != null) {
		      }
		      return true;
		   } catch (JsonParseException jpe) {
		      return false;
		   } catch (IOException ioe) {
			   return false;
		   }

		}

	public static String format(Double d) {
		return new DecimalFormat("0.00").format(d);
	}
	
	public static String format(Float f) {
		return new DecimalFormat("0.00").format(f);
	}
	
	public static String prettyPrintXml(SOAPMessage message) throws SOAPException, IOException, TransformerFactoryConfigurationError, TransformerException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		message.writeTo(out);
		
		return prettyPrintXml(out.toString("UTF-8"));
	}
	
	public static String prettyPrintXml(String unformattedXml) throws TransformerFactoryConfigurationError, UnsupportedEncodingException, TransformerException {

		Transformer transformer = TransformerFactory.newInstance()
				.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		StreamResult result = new StreamResult(new StringWriter());
		transformer.transform(
				new StreamSource(new ByteArrayInputStream(unformattedXml.getBytes("UTF-8"))),
				result);
		return result.getWriter().toString();
	}
	
	public static String checkNull(String str) {
		if(str==null) {
			return "";
		}
		return str;
	}
	
	public static String checkNull(String str, String def) {
		if(str==null) {
			return def;
		}
		return str;
	}

	public static String urlEncode(String message) {
		try {
			return URLEncoder.encode(message, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("System does not appear to support UTF-8!", e);
		}
	}
	
	public static String[] urlDecodeAll(String... message) {
		String[] a = new String[message.length];
		for(int i = 0 ; i < a.length ; i++)
			a[i] = urlDecode(message[i]);
		return a;
	}
	
	public static String urlDecode(String message) {
		try {
			return URLDecoder.decode(message, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("System does not appear to support UTF-8!", e);
		}
	}

	public static boolean isUUID(String attachment) {
		 try {
			UUID.fromString(attachment);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static String stripQuery(String url) {
		int idx = url.indexOf('?');
		if(idx > -1) {
			url = url.substring(0,  idx);
		}
		return url;
	}

	public static boolean isIPAddress(String ip) {
		return IPAddressValidator.getInstance().validate(ip);
	}
	
	public static boolean isCIDRString(String val) {
		return CIDRValidator.getInstance().validate(val);
	}
	
	public static String generateRandomAlphaNumericString(int length) {
	    return new BigInteger(length * 8, random).toString(32).substring(0,  length);
	}


	public static <T> List<T> nullSafe(List<T> list){
		if(list == null){
			return Collections.<T>emptyList();
		}
		return list;
	}

	public static <T> Set<T> nullSafe(Set<T> set){
		if(set == null){
			return Collections.<T>emptySet();
		}
		return set;
	}


	public static <S extends Throwable,D extends Throwable> S chain(S source, D target) {
		try {
			FieldUtils.writeField(source, "cause", target, true);
			return source;
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Problem in chaining exceptions", e);
		}
	}

	public static String csv(String[] items) {
		StringBuffer b = new StringBuffer();
		for(String i : items) {
			if(b.length() > 0) {
				b.append(",");
			}
			b.append(i);
		}
		return b.toString();
	}
	
	public static String csv(Object[] items) {
		StringBuffer b = new StringBuffer();
		for(Object i : items) {
			if(b.length() > 0) {
				b.append(",");
			}
			b.append(i.toString());
		}
		return b.toString();
	}

	public static String prettyPrintJson(String output) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readValue(output, Object.class));
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage(), e);

		}
	}

	public static String encodeURIPath(String url) {
		return url.replace(" ", "%20");
	}

	public static Date thirtyDays() {
		
		Calendar date = Calendar.getInstance();
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		
		date.add(Calendar.DAY_OF_MONTH, 30);
		
		return date.getTime();
	}

	public static byte[] getUTF8Bytes(String str) {
		try {
			return str.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	public static Date convertToUTC(Date date, TimeZone tz) {
		Calendar c = Calendar.getInstance();
		long serverOffset = -c.getTimeZone().getOffset(date.getTime());
		long clientOffset = tz.getOffset(date.getTime());
		long utc  = date.getTime() - serverOffset;
		return new Date(utc - clientOffset);
	}

	public static String stripHostname(String uri) {
		return after(uri, ":");
	}

	public static String getBaseURL(String url) {
		String protocol = before(url, "//");
		String tmp = after(url, "//");
		
		return protocol + "//" + before(tmp, "/");
	}
	
	public static void memDbg(String ctx) {
		if("true".equalsIgnoreCase(System.getProperty("hypersocket.memDbg", "false")))
			System.out.println(String.format("Total: %6d MiB   Free: %6d   Used: %6d   Max: %6d    Ctx: %s",
					Runtime.getRuntime().totalMemory() / 1024 / 1024, Runtime.getRuntime().freeMemory() / 1024 / 1024,
					(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024,
					Runtime.getRuntime().maxMemory() / 1024 / 1024, ctx));
		
	}

	public static Date oneWeek() {
		return fromToday(7);
	}
	
	public static Date oneMonth() {
		return fromToday(30);
	}
	
	public static Date oneYear() {
		return fromToday(365);
	}
	
	public static Date fromToday(int days) {
		Calendar c = todayCalendar();
		c.add(Calendar.DAY_OF_MONTH, days);
		return c.getTime();
	}

	@SafeVarargs
	public static <T> boolean in(T element, T... values) {
		for(T value : values) {
			if(value.equals(element)) {
				return true;
			}
		}
		return false;
	}
	
	@SafeVarargs
	public static <T> boolean notIn(T element, T... values) {
		for(T value : values) {
			if(value.equals(element)) {
				return false;
			}
		}
		return true;
	}

	public static String[] toStringArray(Object[] args) {
		String[] a = new String[args.length];
		for(int i = 0 ; i < args.length; i++)
			a[i] = args[i] == null ? null : String.valueOf(args[i]);
		return a;
	}

	public static boolean isEmailAddress(String email) {
		if(Objects.isNull(email)) {
			return false;
		}
		return Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE).matcher(email).matches();
	}

	/**
	 * Checks a given a numeric value as String it has consecutive numbers or same repeating numbers.
	 * 
	 * @param pin as String that is checked
	 * @param consecutiveLength The length of consecutive sequence should be checked, 3 it will check for 3 consecutive 
	 * sequence 645 or 123 or 222 as per order defined.
	 * @param order In which order to check for, 1 for increasing, -1 for decreasing order, 0 for same repeating numbers
	 * @return
	 */
	public static boolean isConsecutive(String pin, int consecutiveLength, int order) {
		boolean consecutive = false;
		char[] parts = pin.toCharArray();
		int localConsecutiveLength = consecutiveLength;
		for (int i = 1; i < parts.length; ++i) {
			if ((parts[i - 1] + order) == parts[i]) {
				--localConsecutiveLength;
			} else {
				localConsecutiveLength = consecutiveLength;
			}
			if (localConsecutiveLength <= 1) {
				consecutive = true;
			}
		}
		return consecutive;
	}
}
