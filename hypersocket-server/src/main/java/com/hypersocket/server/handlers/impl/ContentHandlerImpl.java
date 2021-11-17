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

import com.hypersocket.server.handlers.HttpRequestHandler;
import com.hypersocket.server.handlers.HttpResponseProcessor;
import com.hypersocket.utils.HypersocketUtils;

public abstract class ContentHandlerImpl extends HttpRequestHandler implements ContentHandler {

	private static Logger log = LoggerFactory.getLogger(ContentHandlerImpl.class);
	
	public static final String CONTENT_INPUTSTREAM = "ContentInputStream";
	
	public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
    public static final int HTTP_CACHE_SECONDS = 60 * 60;

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
			HttpServletResponse response, HttpResponseProcessor responseProcessor) throws IOException {
		
		try {
			if (request.getMethod() != HttpMethod.GET.toString()) {
			    response.sendError(HttpStatus.SC_METHOD_NOT_ALLOWED);
			    return;
			}

			String path = translatePath(sanitizeUri(request.getRequestURI()));
			if(path.startsWith("/"))
				path = path.substring(1);
			
			if (path == null) {
				response.sendError(HttpStatus.SC_FORBIDDEN);
			    return;
			}
			
			String basePath = getBasePath();
			
			if(log.isDebugEnabled()) {
				log.debug("Resolving " + getResourceName() + " resource in " + basePath + ": " + request.getRequestURI());
			}
			
			int status = getResourceStatus(path);

			if(status!=HttpStatus.SC_OK) {
				if(log.isDebugEnabled()) {
					log.debug("Resource not found in " + basePath + " [" + status + "]: " + request.getRequestURI());
				}
				if(status==HttpStatus.SC_NOT_FOUND) {
					responseProcessor.send404(request, response);
				} else if(status==HttpStatus.SC_INTERNAL_SERVER_ERROR) {
					responseProcessor.send500(request, response);
				} else {
					response.sendError(status);
				}
				return;
			}
			
			if(log.isDebugEnabled()) {
				log.debug("Resource found in " + basePath + ": " + request.getRequestURI());
			}
			
			// Cache Validation
			if(!isDynamic(path)) {
				String etag = request.getHeader(HttpHeaders.IF_NONE_MATCH);
				if (etag != null && !etag.equals("")) {
					String currentEtag = DigestUtils.sha256Hex(path + "|" + getLastModified(path));
					if(currentEtag.equals(etag)) {
						if(log.isDebugEnabled()) {
							log.debug(path + " has not been modified");
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
							if(log.isDebugEnabled()) {
								log.debug(path + " has not been modified since " + HypersocketUtils.formatDateTime(ifModifiedSinceDate));
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
			setDateAndCacheHeaders(response, path);
			
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
		} finally {
			responseProcessor.sendResponse(request, response);
		}

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
     * Sets the Date and Cache headers for the HTTP Response
     *
     * @param response
     *            HTTP response
     * @param fileToCache
     *            file to extract content type
     */
    protected void setDateAndCacheHeaders(HttpServletResponse response, String path) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        // Date header
        Calendar time = new GregorianCalendar();
        response.setHeader(HttpHeaders.DATE, dateFormatter.format(time.getTime()));

        // Add cache headers
        time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
        response.setHeader(HttpHeaders.EXPIRES, dateFormatter.format(time.getTime()));
        response.setHeader(HttpHeaders.CACHE_CONTROL, "max-age=" + HTTP_CACHE_SECONDS);
       
        try {
        	 long lastModified = getLastModified(path);
			response.setHeader(HttpHeaders.LAST_MODIFIED, dateFormatter.format(lastModified));
			if(supportsEtag()) {
	        	response.setHeader(HttpHeaders.ETAG, DigestUtils.sha256Hex(path + "|" + lastModified));
	        }
		} catch (FileNotFoundException e) {
		}
   
        
    }
    
    protected boolean supportsEtag() {
    	return true;
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
