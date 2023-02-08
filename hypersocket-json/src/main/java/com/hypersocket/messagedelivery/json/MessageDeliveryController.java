package com.hypersocket.messagedelivery.json;

import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.hypersocket.auth.json.AuthenticatedController;
import com.hypersocket.auth.json.AuthenticationRequired;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.context.AuthenticatedContext;
import com.hypersocket.json.ResourceList;
import com.hypersocket.messagedelivery.MessageDeliveryService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.NameValuePair;
import com.hypersocket.realm.MediaType;
import com.hypersocket.session.json.SessionTimeoutException;

@Controller
public class MessageDeliveryController extends AuthenticatedController {

	@Autowired
	private MessageDeliveryService messageDeliveryService;

	@PostConstruct
	private void postConstruct() {
	}

	@AuthenticationRequired
	@RequestMapping(value = "messageDelivery/providers/{mediaType}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<NameValuePair> getProviders(HttpServletRequest request, HttpServletResponse response, @PathVariable MediaType mediaType)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		return new ResourceList<>(messageDeliveryService.getProviders(mediaType).stream()
				.map(p -> new NameValuePair(p.getResourceKey(), p.getResourceKey())).collect(Collectors.toList()));
	}

	@AuthenticationRequired
	@RequestMapping(value = "messageDelivery/defaultProviders/{mediaType}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<NameValuePair> getDefaultProviders(HttpServletRequest request, HttpServletResponse response, @PathVariable MediaType mediaType)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		return new ResourceList<>(messageDeliveryService.getProviders(mediaType).stream()
				.filter(p -> p.isDefault()).map(p -> new NameValuePair(p.getResourceKey(), p.getResourceKey())).collect(Collectors.toList()));
	}
}
