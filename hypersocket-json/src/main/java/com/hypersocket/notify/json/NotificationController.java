package com.hypersocket.notify.json;

import java.util.List;
import java.util.stream.Collectors;

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
import com.hypersocket.auth.json.AuthenticationRequiredButDontTouchSession;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.context.AuthenticatedContext;
import com.hypersocket.json.ResourceStatus;
import com.hypersocket.notify.Notification;
import com.hypersocket.notify.NotificationService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.session.json.SessionTimeoutException;

@Controller
public class NotificationController extends AuthenticatedController {

	@Autowired
	private NotificationService notificationService;

	@AuthenticationRequiredButDontTouchSession
	@RequestMapping(value = "notifications/notification/{context}", method = RequestMethod.GET, produces = {
			"application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceStatus<List<Notification>> getNotifications(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String context) throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		return new ResourceStatus<List<Notification>>(notificationService.getNotifications(context).stream()
				.filter((n) -> request.getSession().getAttribute(n.getKey()) == null)
				.peek((n) -> request.getSession().setAttribute(n.getKey(), n)).collect(Collectors.toList()));
	}

}
