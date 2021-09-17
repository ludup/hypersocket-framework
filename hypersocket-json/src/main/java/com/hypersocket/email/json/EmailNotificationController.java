package com.hypersocket.email.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.simplejavamail.api.mailer.config.TransportStrategy;
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
import com.hypersocket.config.ConfigurationService;
import com.hypersocket.email.EmailTrackerService;
import com.hypersocket.json.ResourceList;
import com.hypersocket.json.SelectOption;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.upload.FileUploadService;

@Controller
public class EmailNotificationController extends AuthenticatedController {
	
	@Autowired
	private EmailTrackerService trackerService; 
	
	@Autowired
	private FileUploadService uploadService; 
	
	@Autowired
	private ConfigurationService configurationService; 
	
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
	
	
	@RequestMapping(value = "emails/receipt/{id}/{filename}", method = RequestMethod.GET, produces = { "image/*" })
	public void getReceipt(
			HttpServletRequest request, HttpServletResponse response,
			@PathVariable Long id,
			@PathVariable String filename /* Not used */)
			throws AccessDeniedException, UnauthorizedException, ResourceNotFoundException, IOException {

		Realm currentRealm = realmService.getRealmByHost(request.getServerName());
		
		trackerService.finaliseReceipt(id);
		
		String trackerImage = configurationService.getValue(currentRealm, "email.trackingImage");
		
		uploadService.downloadURIFile(trackerImage, request, response, false, true, false);
	}

}
