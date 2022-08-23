/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.server.handlers.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.mail.javamail.ConfigurableMimeFileTypeMap;

import com.hypersocket.ApplicationContextServiceImpl;
import com.hypersocket.cache.CacheUtils;
import com.hypersocket.json.ControllerInterceptor;
import com.hypersocket.server.handlers.HttpRequestHandler;
import com.hypersocket.session.Session;
import com.hypersocket.session.SessionService;
import com.hypersocket.session.json.SessionUtils;
import com.hypersocket.utils.HypersocketUtils;

public abstract class ContentHandlerImpl extends HttpRequestHandler implements ContentHandler {

	private static Logger LOG = LoggerFactory.getLogger(ContentHandlerImpl.class);
	
	public static final String CONTENT_INPUTSTREAM = "ContentInputStream";
	
	public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
    public static final int HTTP_CACHE_SECONDS = 60 * 60;
    
    /**
     *
     * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Feature-Policy
     *
     * MDN: says....
     *  
     * Feature-Policy
     *
	 * Experimental: This is an experimental technology
	 * Check the Browser compatibility table carefully before using this in production.
     *
	 *	Warning: The header has now been renamed to Permissions-Policy in the spec, and this article will eventually be updated to reflect that change.
     *
     * 
     * accelerometer
     * ambient-light-sensor
     * battery
     * camera
     * display-capture
     * geolocation
     * gyroscope
     * magnetometer
     * microphone
     * payment
     * speaker-selection
     * usb
     * autoplay
     * publickey-credentials-get - (Webauthn)
     * 
     * e.g.
     * Permissions-Policy: geolocation=(self "https://example.com"), microphone=()
     */
    private static final String PERMISSIONS_POLICY_HEADER_OPTIONS = "accelerometer=(), "
    		+ "ambient-light-sensor=(), "
    		+ "battery=(), "
    		+ "camera=(), "
    		+ "display-capture=(), "
    		+ "geolocation=(), "
    		+ "gyroscope=(), "
    		+ "magnetometer=(), "
    		+ "microphone=(), "
    		+ "payment=(), "
    		+ "speaker-selection=(), "
    		+ "usb=(), "
    		+ "autoplay=(), "
    		+ "publickey-credentials-get=(self)";
    
    /**
     * e.g.
     * Feature-Policy: geolocation 'self' https://example.com; microphone 'none'
     */
    private static final String FEATURE_POLICY_HEADER_OPTIONS = "accelerometer 'none'; "
    		+ "ambient-light-sensor 'none'; "
    		+ "battery 'none'; "
    		+ "camera 'none'; "
    		+ "display-capture 'none'; "
    		+ "geolocation 'none'; "
    		+ "gyroscope 'none'; "
    		+ "magnetometer 'none'; "
    		+ "microphone 'none'; "
    		+ "payment 'none'; "
    		+ "speaker-selection 'none'; "
    		+ "usb 'none'; "
    		+ "autoplay 'none'; "
    		+ "publickey-credentials-get 'self'";
    

    private ConfigurableMimeFileTypeMap mimeTypesMap = new ConfigurableMimeFileTypeMap();
    
    private Map<String,String> aliases = new HashMap<String,String>();
    private Set<String> dynamic = new HashSet<String>();
    private List<ContentFilter> filters = new ArrayList<ContentFilter>();
    
    protected ContentHandlerImpl(String name, int priority) {
    	super(name, priority);
    }
	
	@Override
	public boolean handlesRequest(HttpServletRequest request) {
		return request.getRequestURI().startsWith(server.resolvePath(getBasePath()));
	}
	
	public abstract String getBasePath();

	@Override
	public void handleHttpRequest(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		
		SessionUtils sessionUtils = 
				ApplicationContextServiceImpl.getInstance().getBean(SessionUtils.class);
				
		SessionService sessionService = ApplicationContextServiceImpl.getInstance().getBean(SessionService.class);
		
		Session session = sessionUtils.getActiveSession(request);
		if(Objects.nonNull(session)) {
			try(var c = sessionService.tryAs(sessionUtils.getActiveSession(request), sessionUtils.getLocale(request))) {
				doHandleRequest(request, response, sessionService, session);
			}
		}
		else {
			doHandleRequest(request, response, sessionService, session);	
		}

    }

	protected void doHandleRequest(HttpServletRequest request, HttpServletResponse response,
			SessionService sessionService, Session session) throws IOException, FileNotFoundException {
		try {
			
			if (request.getMethod() != HttpMethod.GET.toString()) {
			    response.sendError(HttpStatus.SC_METHOD_NOT_ALLOWED);
			    return;
			}

			String requri = request.getRequestURI();
			String path = translatePath(sanitizeUri(requri));
			if(path.startsWith("/"))
				path = path.substring(1);
			
			if (path == null) {
				response.sendError(HttpStatus.SC_FORBIDDEN);
			    return;
			}
			
			String basePath = getBasePath();
			
			if(LOG.isDebugEnabled()) {
				LOG.debug("Resolving " + getResourceName() + " resource in " + basePath + ": " + requri);
			}
			
			int status = getResourceStatus(path);

			if(status!=HttpStatus.SC_OK) {
				if(LOG.isDebugEnabled()) {
					LOG.debug("Resource error in " + basePath + " [" + status + "]: " + requri);
				}
				response.sendError(status);
				return;
			}
			
			if(LOG.isDebugEnabled()) {
				LOG.debug("Resource found in " + basePath + ": " + requri);
			}
			
			// Cache Validation
			if(!isDynamic(path)) {
				String etag = request.getHeader(HttpHeaders.IF_NONE_MATCH);
				if (etag != null && !etag.equals("")) {
					String currentEtag = DigestUtils.sha256Hex(path + "|" + getLastModified(path));
					if(currentEtag.equals(etag)) {
						if(LOG.isDebugEnabled()) {
							LOG.debug(path + " has not been modified");
						}
						sendNotModified(response);
						return;
					}
					
				}
				String ifModifiedSince = request.getHeader(HttpHeaders.IF_MODIFIED_SINCE);
				if (ifModifiedSince != null && !ifModifiedSince.equals("")) {
				    SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
				    try {
						Date ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);
	
						// Only compare up to the second because the datetime format we send to the client does
						// not have milliseconds
						long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
						long fileLastModifiedSeconds = getLastModified(path) / 1000;
						if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
							if(LOG.isDebugEnabled()) {
								LOG.debug(path + " has not been modified since " + HypersocketUtils.formatDateTime(ifModifiedSinceDate));
							}
							sendNotModified(response);
						    return;
						}
					} catch (Throwable e) {
						response.sendError(HttpStatus.SC_BAD_REQUEST);
						return;
					}
				}

			}
			else
				request.setAttribute(ControllerInterceptor.CACHEABLE, false);

			long fileLength = getResourceLength(path);
			long actualLength = 0;
			InputStream in = getInputStream(path, request);
			
			if(fileLength <= 131072) {
				int r;
				byte[] buf = new byte[4096];
				try {
					while((r = in.read(buf)) > -1) {
						response.getOutputStream().write(buf,0,r);
						if(fileLength < 0) {
							actualLength += r;
						}
					}
				}
				finally {
					in.close();
				}
				response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(actualLength));
				
			} else {
				request.setAttribute(CONTENT_INPUTSTREAM, in);
			}
			
			setContentTypeHeader(response, path);
			CacheUtils.setDateAndCacheHeaders(response, getLastModified(path), true, path);
			
			if (requri.endsWith(".js") || requri.endsWith(".css") || requri.endsWith(".xml") || requri.endsWith(".html") || requri.indexOf('.') == -1) {
				addDefaultCSPHeaders(response);
			}
			
			response.setStatus(HttpStatus.SC_OK);
		} catch (RedirectException e) {
			if(e.getMessage().startsWith("/") && !e.getMessage().equals("/")) {
				if(e.isPermanent()) {
					response.setStatus(HttpStatus.SC_MOVED_PERMANENTLY);
					response.setHeader(HttpHeaders.LOCATION, e.getMessage());
				} else {
					response.sendRedirect(e.getMessage());
				}
			} else {
				String path = server.resolvePath(getBasePath() + (e.getMessage().startsWith("/") ? "" : "/") + e.getMessage());
				if(e.isPermanent()) {
					response.setStatus(HttpStatus.SC_MOVED_PERMANENTLY);
					response.setHeader(HttpHeaders.LOCATION, path);
				} else {
					response.sendRedirect(path);
				}
			}
		}
	}

	public static void addDefaultCSPHeaders(HttpServletResponse response) {
		response.addHeader("Referrer-Policy", "no-referrer");
		
		response.addHeader("Permissions-Policy", PERMISSIONS_POLICY_HEADER_OPTIONS);
		response.addHeader("Feature-Policy", FEATURE_POLICY_HEADER_OPTIONS);
		response.addHeader("Content-Security-Policy", "default-src 'self';  style-src 'self' 'unsafe-inline' 'unsafe-hashes'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; img-src * data:");
	}
	
	protected String processReplacements(String path) {
		path = path.replace("${apiPath}", server.getApiPath());
		path = path.replace("${uiPath}", server.getUiPath());
		return path.replace("${basePath}", server.getBasePath());
	}
	
	public InputStream getInputStream(String path, HttpServletRequest request) throws FileNotFoundException, RedirectException {
		
		InputStream original = getResourceStream(path);
		for(ContentFilter filter : filters) {
			if(filter.filtersPath(path)) {
				original = filter.getFilterStream(original, request);
			}
		}
		return original;
	}

    @Override
	public abstract String getResourceName();

	@Override
	public abstract InputStream getResourceStream(String path) throws FileNotFoundException;
    
	@Override
	public abstract long getResourceLength(String path) throws FileNotFoundException;

	@Override
	public abstract long getLastModified(String path) throws FileNotFoundException;

	@Override
	public abstract int getResourceStatus(String path) throws RedirectException;
  
	protected boolean isDynamic(String path) {
		return dynamic.contains(path);
	}
	
	protected String translatePath(String path) throws RedirectException {
		
		for(Map.Entry<String, String> alias : aliases.entrySet()) {
			String a = processReplacements(alias.getKey());
			String toPath = processReplacements(alias.getValue());
			if(path.matches(a)) {
				if(toPath.startsWith("redirect:")) {
					throw new RedirectException(toPath.substring(9));
				}
				return toPath;
			}
		}
		return path;
	}

	@Override
	public void addDynamicPage(String path) {
		dynamic.add(path);
	}
	
	@Override
	public void addAlias(String alias, String path) {
		aliases.put(alias, path);
	}

	@Override
	public boolean hasAlias(String alias) {
		return aliases.containsKey(alias);
	}
	
	@Override
	public void removeAlias(String alias) {
		aliases.remove(alias);
	}
	
	@Override
	public void addFilter(ContentFilter filter) {
		filters.add(filter);
		Collections.sort(filters, new Comparator<ContentFilter>() {

			@Override
			public int compare(ContentFilter o1, ContentFilter o2) {
				return o1.getWeight().compareTo(o2.getWeight());
			}
		});
	}
	
	protected String sanitizeUri(String uri) {
        // Decode the path.
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            try {
                uri = URLDecoder.decode(uri, "ISO-8859-1");
            } catch (UnsupportedEncodingException e1) {
                throw new Error();
            }
        }

        uri = uri.replaceAll(server.resolvePath(getBasePath()), "");

        return uri;
    }

	/**
     * When file timestamp is the same as what the browser is sending up, send a "304 Not Modified"
     *
     * @param ctx
     *            Context
     */
	protected void sendNotModified(HttpServletResponse response) {
        response.setStatus(HttpStatus.SC_NOT_MODIFIED);
        setDateHeader(response);
    }

    /**
     * Sets the Date header for the HTTP response
     *
     * @param response
     *            HTTP response
     */
	protected void setDateHeader(HttpServletResponse response) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        Calendar time = new GregorianCalendar();
        response.setHeader(HttpHeaders.DATE, dateFormatter.format(time.getTime()));
    }

    /**
     * Sets the content type header for the HTTP Response
     *
     * @param response
     *            HTTP response
     * @param file
     *            file to extract content type
     */
    protected void setContentTypeHeader(HttpServletResponse response, String path) {
        response.setHeader(HttpHeaders.CONTENT_TYPE, getContentType(path));
    }

    protected String getContentType(String path) {
    	return mimeTypesMap.getContentType(path);
    }
}
