package com.hypersocket.email.json;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codemonkey.simplejavamail.TransportStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.hypersocket.auth.json.AuthenticatedController;
import com.hypersocket.auth.json.AuthenticationRequired;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.email.EmailNotificationService;
import com.hypersocket.json.ResourceList;
import com.hypersocket.json.SelectOption;
import com.hypersocket.permissions.AccessDeniedException;

@Controller
public class EmailNotificationController extends AuthenticatedController {

	@Autowired
	EmailNotificationService emailService;
	
	@AuthenticationRequired
	@RequestMapping(value = "emails/smtpProtocols", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<SelectOption> getLocales(
			HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException {

		List<SelectOption> protocols = new ArrayList<SelectOption>();

		for (TransportStrategy t : TransportStrategy.values()) {
			protocols.add(new SelectOption(String.valueOf(t.ordinal()), t.name().toLowerCase().replace("_", ".protocol.")));
		}
		return new ResourceList<SelectOption>(protocols);
	}

}
