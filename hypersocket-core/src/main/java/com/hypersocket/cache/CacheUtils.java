package com.hypersocket.cache;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpHeaders;

public class CacheUtils {

	public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
	public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
	public static final int HTTP_CACHE_SECONDS = getDefaultCacheSeconds();
	public static final String IGNORE_CACHE = "ignoreCache";

	protected static int getDefaultCacheSeconds() {
		return Integer.parseInt(System.getProperty("hypersocket.cacheMaxAgeSeconds", String.valueOf(System.getProperty("hypersocket.development", "false").equals("true")
				? 60
				: 60 * 10)));
	}
	
	private static GregorianCalendar expiration;
	private static GregorianCalendar expirationForward;

	static {
		expiration = new GregorianCalendar();
		expiration.roll(Calendar.YEAR, -10);
		expirationForward = new GregorianCalendar();
		expirationForward.roll(Calendar.YEAR, 10);
	}

	/**
	 * Sets the Date and Cache headers for the HTTP Response
	 *
	 * @param response     HTTP response
	 * @param lastModified The last modified timestamp for this resource
	 * @param cache        TODO
	 * @param path         TODO
	 */
	public static void setDateAndCacheHeaders(HttpServletResponse response, long lastModified, boolean cache,
			String path) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
		dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

		// Date header
		Calendar time = new GregorianCalendar();
		response.setHeader(HttpHeaders.DATE, dateFormatter.format(time.getTime()));

		// Add cache headers
		if (cache) {
			time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
			response.setHeader(HttpHeaders.EXPIRES, dateFormatter.format(expirationForward.getTime()));
			response.setHeader(HttpHeaders.CACHE_CONTROL, "public, must-revalidate, max-age=" + HTTP_CACHE_SECONDS);
			if (lastModified > -1) {
				response.setHeader(HttpHeaders.LAST_MODIFIED, dateFormatter.format(new Date(lastModified)));
				if (path != null)
					response.setHeader(HttpHeaders.ETAG, DigestUtils.sha256Hex(path + "|" + lastModified));
			}
		} else {
			response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate, private");
			response.setHeader(HttpHeaders.PRAGMA, "no-cache");
			response.setHeader(HttpHeaders.EXPIRES, dateFormatter.format(expiration.getTime()));
		}
	}

	public static void setDateAndCacheHeaders(HttpServletResponse response) {
		setDateAndCacheHeaders(response, System.currentTimeMillis(), true, null);
	}

	public static boolean checkValidCache(HttpServletRequest request, HttpServletResponse response, long lastModified)
			throws IOException {

		long ifModifiedSince = request.getDateHeader("If-Modified-Since");
		if (ifModifiedSince != -1 && lastModified <= ifModifiedSince) {
			CacheUtils.setDateAndCacheHeaders(response, lastModified, true, request.getRequestURI());
			response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
			return true;
		}
		return false;
	}
}
