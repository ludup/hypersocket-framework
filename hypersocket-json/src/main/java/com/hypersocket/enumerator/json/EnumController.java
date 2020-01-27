package com.hypersocket.enumerator.json;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import com.hypersocket.auth.json.AuthenticatedController;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.json.ResourceList;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.NameValuePair;
import com.hypersocket.properties.enumeration.Displayable;
import com.hypersocket.session.json.SessionTimeoutException;

@Controller
public class EnumController extends AuthenticatedController {
	
	static long lastModified = System.currentTimeMillis();
	
	@RequestMapping(value = "enum/{className}/", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	public ResourceList<NameValuePair> getStates(
			WebRequest request, HttpServletResponse response,
			@PathVariable("className") String className)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {
		setupSystemContext();
		try {
			if(request.checkNotModified(lastModified)) {
				response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
				return null;
			}
			Set<String> ignoredTypes = new HashSet<String>();
			String ignored = request.getParameter("ignore");
			if(ignored!=null) {
				for(String ignore : ignored.split(",")) {
					ignoredTypes.add(ignore);
				}
			}
			Class<?> enumType = Class.forName(className);
			List<NameValuePair> results = new ArrayList<NameValuePair>();
			for(Enum<?> enumConstant : (Enum<?>[]) enumType.getEnumConstants()) {
				if(!ignoredTypes.contains(enumConstant.name())) {
					results.add(new NameValuePair(enumConstant.name(), String.valueOf(enumConstant.ordinal())));
				}
			}
			return new ResourceList<NameValuePair>(results);
		} catch(Exception e) { 
			return new ResourceList<NameValuePair>(false, e.getMessage());
		} finally {
			clearAuthenticatedContext();
		}
	}
	
	@RequestMapping(value = "enum/{className}/{ignore}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	public ResourceList<NameValuePair> getStates(
			WebRequest request, HttpServletResponse response,
			@PathVariable("className") String className,
			@PathVariable("ignore") String ignored)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {
		setupSystemContext();
		try {
			if(request.checkNotModified(lastModified)) {
				response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
				return null;
			}
			Set<String> ignoredTypes = new HashSet<String>();
			if(ignored!=null) {
				for(String ignore : ignored.split(",")) {
					ignoredTypes.add(ignore);
				}
			}
			Class<?> enumType = Class.forName(className);
			List<NameValuePair> results = new ArrayList<NameValuePair>();
			for(Enum<?> enumConstant : (Enum<?>[]) enumType.getEnumConstants()) {
				if(!ignoredTypes.contains(enumConstant.name())) {
					results.add(new NameValuePair(enumConstant.name(), String.valueOf(enumConstant.ordinal())));
				}
			}
			return new ResourceList<NameValuePair>(results);
		} catch(Exception e) { 
			return new ResourceList<NameValuePair>(false, e.getMessage());
		} finally {
			clearAuthenticatedContext();
		}
	}

	@RequestMapping(value = "enum/displayable/{className}/", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	public ResourceList<Displayable<?>> getDisplayableEnums(
			WebRequest request, HttpServletResponse response,
			@PathVariable("className") String className)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {
		setupSystemContext();
		try {
			if(request.checkNotModified(lastModified)) {
				response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
				return null;
			}
			Class<?> enumType = Class.forName(className);
			if(!Displayable.class.isAssignableFrom(enumType)) {
				throw new IllegalStateException("Enum is not of type Displayable.");
			}
			List<Displayable<?>> results = new ArrayList<>();
			for(Enum<?> enumConstant : (Enum<?>[]) enumType.getEnumConstants()) {
				results.add((Displayable<?>) enumConstant);
			}
			return new ResourceList<Displayable<?>>(results);
		} catch(Exception e) {
			return new ResourceList<Displayable<?>>(false, e.getMessage());
		} finally {
			response.setDateHeader("Last-Modified", lastModified);
		}
	}

}
