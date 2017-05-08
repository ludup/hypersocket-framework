package com.hypersocket.cache;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpHeaders;

public class CacheUtils {

	public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
    public static final int HTTP_CACHE_SECONDS = 60 * 60 * 24;
    

	/**
     * Sets the Date and Cache headers for the HTTP Response
     *
     * @param response
     *            HTTP response
     * @param lastModified
     *            The last modified timestamp for this resource
     */
    public static void setDateAndCacheHeaders(HttpServletResponse response, long lastModified) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        // Date header
        Calendar time = new GregorianCalendar();
        response.setHeader(HttpHeaders.DATE, dateFormatter.format(time.getTime()));

        // Add cache headers
        time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
        response.setHeader(HttpHeaders.EXPIRES, dateFormatter.format(time.getTime()));
        response.setHeader(HttpHeaders.CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);
		response.setHeader( HttpHeaders.LAST_MODIFIED, dateFormatter.format(new Date(lastModified)));
    }

    public static void setDateAndCacheHeaders(HttpServletResponse response) {
    	setDateAndCacheHeaders(response, System.currentTimeMillis());
    }
}
