package com.hypersocket.server.json;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

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

import com.hypersocket.HypersocketVersion;
import com.hypersocket.auth.json.AuthenticatedController;
import com.hypersocket.auth.json.AuthenticationRequired;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.config.ConfigurationPermission;
import com.hypersocket.i18n.I18N;
import com.hypersocket.json.MultiselectElement;
import com.hypersocket.json.RequestStatus;
import com.hypersocket.json.ResourceList;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.permissions.PermissionStrategy;
import com.hypersocket.permissions.SystemPermission;
import com.hypersocket.server.HypersocketServer;
import com.hypersocket.session.SessionService;
import com.hypersocket.session.json.SessionTimeoutException;
import com.hypersocket.session.json.SessionUtils;

@Controller
public class ServerController extends AuthenticatedController {

	@Autowired
	HypersocketServer server;

	@Autowired
	SessionUtils sessionUtils;

	@Autowired
	PermissionService permissionService;
	
	@Autowired
	SessionService sessionService; 
	
	@RequestMapping(value = "server/ping", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public RequestStatus ping(HttpServletRequest request,
			HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		return new RequestStatus(true, "");
	}

	@AuthenticationRequired
	@RequestMapping(value = "server/version", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public RequestStatus version(HttpServletRequest request,
			HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		return new RequestStatus(true, HypersocketVersion.getVersion() + ";" + HypersocketVersion.getSerial());
	}

	@AuthenticationRequired
	@RequestMapping(value = "server/restart/{delay}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public RequestStatus restartServer(HttpServletRequest request,
			HttpServletResponse response, @PathVariable Long delay)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {

		permissionService.verifyPermission(sessionUtils.getPrincipal(request),
				PermissionStrategy.INCLUDE_IMPLIED,
				SystemPermission.SYSTEM_ADMINISTRATION);

		server.restart(delay);
		
		return new RequestStatus(true, I18N.getResource(
				sessionUtils.getLocale(request),
				HypersocketServer.RESOURCE_BUNDLE, "message.restartIn", delay));
	}

	@AuthenticationRequired
	@RequestMapping(value = "server/shutdown/{delay}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public RequestStatus shutdownServer(HttpServletRequest request,
			HttpServletResponse response, @PathVariable Long delay)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {

		permissionService.verifyPermission(sessionUtils.getPrincipal(request),
				PermissionStrategy.INCLUDE_IMPLIED,
				SystemPermission.SYSTEM_ADMINISTRATION);

		server.shutdown(delay);
		
		return new RequestStatus(true, I18N.getResource(
				sessionUtils.getLocale(request),
				HypersocketServer.RESOURCE_BUNDLE, "message.shutdownIn", delay));
	}

	@AuthenticationRequired
	@RequestMapping(value = "server/sslProtocols", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<MultiselectElement> getSslProtocols(
			HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException {

		permissionService.verifyPermission(
				getSessionUtils().getPrincipal(request),
				PermissionStrategy.INCLUDE_IMPLIED,
				ConfigurationPermission.READ);

		List<MultiselectElement> protocols = new ArrayList<MultiselectElement>();

		for (String proto : server.getSSLProtocols()) {
			protocols.add(new MultiselectElement(proto, proto));
		}
		return new ResourceList<MultiselectElement>(protocols);
	}

	@AuthenticationRequired
	@RequestMapping(value = "server/sslCiphers", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<MultiselectElement> getSslCiphers(
			HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException {

		permissionService.verifyPermission(
				getSessionUtils().getPrincipal(request),
				PermissionStrategy.INCLUDE_IMPLIED,
				ConfigurationPermission.READ);
		
		List<MultiselectElement> ciphers = new ArrayList<MultiselectElement>();

		for (String proto : server.getSSLCiphers()) {
			ciphers.add(new MultiselectElement(proto, proto));
		}
		return new ResourceList<MultiselectElement>(ciphers);
	}

	@AuthenticationRequired
	@RequestMapping(value = "server/networkInterfaces", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<MultiselectElement> getCategories(
			HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException {

		permissionService.verifyPermission(
				getSessionUtils().getPrincipal(request),
				PermissionStrategy.INCLUDE_IMPLIED,
				ConfigurationPermission.READ);
		
		List<MultiselectElement> interfaces = new ArrayList<MultiselectElement>();

		try {
			Enumeration<NetworkInterface> nets = NetworkInterface
					.getNetworkInterfaces();
			for (NetworkInterface netint : Collections.list(nets)) {
				Enumeration<InetAddress> inetAddresses = netint
						.getInetAddresses();
				for (InetAddress inetAddress : Collections.list(inetAddresses)) {
					interfaces.add(new MultiselectElement(inetAddress
							.getHostAddress(), inetAddress.getHostAddress()));
				}
			}
		} catch (SocketException e) {
		}
		return new ResourceList<MultiselectElement>(interfaces);
	}
}
