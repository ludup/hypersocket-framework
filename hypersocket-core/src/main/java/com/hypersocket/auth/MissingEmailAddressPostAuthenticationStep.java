package com.hypersocket.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.config.ConfigurationService;
import com.hypersocket.email.EmailNotificationServiceImpl;
import com.hypersocket.i18n.I18N;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.input.FormTemplate;
import com.hypersocket.input.ParagraphField;
import com.hypersocket.input.TextInputField;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.profile.ProfileRepository;
import com.hypersocket.realm.RealmService;
import com.hypersocket.realm.UserPrincipal;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.session.SessionService;

@Component
public class MissingEmailAddressPostAuthenticationStep implements PostAuthenticationStep{

	public static final String RESOURCE_KEY = "missingEmailAddress";
	public static final String RESOURCE_BUNDLE = "MissingEmailAddressService";
	
	public static final String REQUIRE_NONE = "required.none";
	public static final String REQUIRE_PRIMARY = "required.primary";
	public static final String REQUIRE_SECONDARY = "required.secondary";
	public static final String REQUIRE_ALL = "required.all";
	
	public static final String PARAM_PRIMARY = "primaryEmail";
	public static final String PARAM_SECONDARY = "secondaryEmail";
	
	@Autowired
	I18NService i18nService;
	
	@Autowired
	ProfileRepository profileRepository;
	
	@Autowired
	AuthenticationService authenticationService;
	
	@Autowired
	ConfigurationService configurationService;
	
	@Autowired
	RealmService realmService; 
	
	@Autowired
	SessionService sessionService; 
	
	@PostConstruct
	private void postConstruct() {
		i18nService.registerBundle(RESOURCE_BUNDLE);
		authenticationService.registerPostAuthenticationStep(this);
	}
	
	@Override
	public boolean requiresProcessing(AuthenticationState state) {
		
		UserPrincipal principal = (UserPrincipal) state.getPrincipal();
		String required = configurationService.getValue(state.getRealm(), "missingEmail.required");
		boolean primariExists = StringUtils.isNotBlank(principal.getEmail());
		boolean secondaryExists = StringUtils.isNotBlank(principal.getSecondaryEmail());
		if(((REQUIRE_ALL.equals(required) || REQUIRE_PRIMARY.equals(required)) && !primariExists)
				|| ((REQUIRE_ALL.equals(required) || REQUIRE_SECONDARY.equals(required)) && !secondaryExists)){
			return true;
		}
		return false;
	}

	@Override
	public int getOrderPriority() {
		return 0;
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	@Override
	public AuthenticatorResult process(AuthenticationState state, @SuppressWarnings("rawtypes") Map parameters) throws AccessDeniedException {
		String required = configurationService.getValue(state.getRealm(), "missingEmail.required");
		
		final UserPrincipal principal = (UserPrincipal)state.getPrincipal();
		boolean primariExists = StringUtils.isNotBlank(principal.getEmail());
		boolean secondaryExists = StringUtils.isNotBlank(principal.getSecondaryEmail());
		boolean error = false;
		
		if((REQUIRE_ALL.equals(required) || REQUIRE_PRIMARY.equals(required)) && !primariExists){
			String primaryEmail = (String)parameters.get(PARAM_PRIMARY);
			state.getParameters().put(PARAM_PRIMARY, primaryEmail);
			if(primaryEmail == null || "".equals(primaryEmail)){
				state.setLastErrorMsg("error.primaryMandatory");
				state.setLastErrorIsResourceKey(true);
				error = true;
			}else if(!validateEmailAddress(primaryEmail)){
				state.setLastErrorMsg("error.primaryInvalid");
				state.setLastErrorIsResourceKey(true);
				error = true;
			}
		}
		if((REQUIRE_ALL.equals(required) || REQUIRE_SECONDARY.equals(required)) && !secondaryExists){
			String secondaryEmail = (String)parameters.get(PARAM_SECONDARY);
			state.getParameters().put(PARAM_SECONDARY, secondaryEmail);
			if(!error && secondaryEmail == null || "".equals(secondaryEmail)){
				state.setLastErrorMsg("error.secondaryMandatory");
				state.setLastErrorIsResourceKey(true);
				error = true;
			}else if(!error && !validateEmailAddress(secondaryEmail)){
				state.setLastErrorMsg("error.secondaryInvalid");
				state.setLastErrorIsResourceKey(true);
				error = true;
			}
		}
		if(!error && (REQUIRE_ALL.equals(required) || REQUIRE_SECONDARY.equals(required))){
			String primary = (principal.getEmail() != null && !"".equals(principal.getEmail())) ? principal.getEmail() : (String)parameters.get(PARAM_PRIMARY);
			String secondary = (principal.getSecondaryEmail() != null && !"".equals(principal.getSecondaryEmail())) ? principal.getSecondaryEmail() : (String)parameters.get(PARAM_SECONDARY);
			if(primary != null && primary.equals(secondary)){
				state.setLastErrorMsg("error.duplicatedEmail");
				state.setLastErrorIsResourceKey(true);
				error = true;
			}
		}
		if(error){
			return AuthenticatorResult.INSUFFICIENT_DATA;
		}else{
			final Map<String,String> properties = new HashMap<String,String>();
			if((REQUIRE_ALL.equals(required) || REQUIRE_PRIMARY.equals(required)) && !primariExists){
				properties.put("email", (String)parameters.get(PARAM_PRIMARY));
			}
			if((REQUIRE_ALL.equals(required) || REQUIRE_SECONDARY.equals(required)) && !secondaryExists){
				properties.put("secondaryEmail", (String)parameters.get(PARAM_SECONDARY));
			}
			
			sessionService.executeInSystemContext(new Runnable() {
				public void run() {
					try {
						realmService.updateUserProperties(principal, properties);
					} catch (ResourceException | AccessDeniedException e) {
						e.printStackTrace();
					}
				}
			});
			
			return AuthenticatorResult.AUTHENTICATION_SUCCESS;
		}
	}

	@Override
	public FormTemplate createTemplate(AuthenticationState state) {
		String required = configurationService.getValue(state.getRealm(), "missingEmail.required");
		
		UserPrincipal principal = (UserPrincipal)state.getPrincipal();
		boolean primariExists = StringUtils.isNotBlank(principal.getEmail());
		boolean secondaryExists = StringUtils.isNotBlank(principal.getSecondaryEmail());
		
		FormTemplate t = new FormTemplate(state.getInitialScheme());
		t.getInputFields().add(new ParagraphField("missingEmail.paragraph", true));
		if((REQUIRE_ALL.equals(required) || REQUIRE_PRIMARY.equals(required)) && !primariExists){
			t.getInputFields().add(new TextInputField(PARAM_PRIMARY, state.getParameter(PARAM_PRIMARY)!=null ? state.getParameter(PARAM_PRIMARY) : "", true, I18N.getResource(state.getLocale(), RESOURCE_BUNDLE, "missingEmailAddress.primary")));
		}
			
		if((REQUIRE_ALL.equals(required) || REQUIRE_SECONDARY.equals(required)) && !secondaryExists){
			t.getInputFields().add(new TextInputField(PARAM_SECONDARY, state.getParameter(PARAM_SECONDARY)!=null ? state.getParameter(PARAM_SECONDARY) : "", true, I18N.getResource(state.getLocale(), RESOURCE_BUNDLE, "missingEmailAddress.secondary")));
		}
		return t;
	}

	@Override
	public boolean requiresUserInput(AuthenticationState state) {
		return true;
	}

	@Override
	public boolean requiresSession(AuthenticationState state) {
		return true;
	}
	
	private boolean validateEmailAddress(String val) {
		
		Pattern p = Pattern.compile(EmailNotificationServiceImpl.EMAIL_NAME_PATTERN);

		Matcher m = p.matcher(val);

		if (m.find()) {
			@SuppressWarnings("unused")
			String name = m.group(1).replaceAll("[\\n\\r]+", "");
			String email = m.group(2).replaceAll("[\\n\\r]+", "");

			if (Pattern.matches(EmailNotificationServiceImpl.EMAIL_PATTERN, email)) {
				return true;
			} else {
				return false;
			}
		}

		if (Pattern.matches(EmailNotificationServiceImpl.EMAIL_PATTERN, val)) {
			return true;
		}
		return false;
	}

}
