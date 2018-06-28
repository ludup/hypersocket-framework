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
public class MissingPhoneNumberPostAuthenticationStep implements PostAuthenticationStep{

	public static final String RESOURCE_KEY = "missingPhone";
	public static final String RESOURCE_BUNDLE = "MissingEmailAddressService";
	
	public static final String REQUIRE_NONE = "required.none";
	public static final String REQUIRE_PRIMARY = "required.primary";
	public static final String REQUIRE_SECONDARY = "required.secondary";
	public static final String REQUIRE_ALL = "required.all";
	
	public static final String PARAM_PRIMARY = "primaryPhone";
	public static final String PARAM_SECONDARY = "secondaryPhone";
	
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
		String required = configurationService.getValue(state.getRealm(), "missingPhone.required");
		boolean primariExists = StringUtils.isNotBlank(principal.getMobile());
		boolean secondaryExists = StringUtils.isNotBlank(principal.getSecondaryMobile());
		if(((REQUIRE_ALL.equals(required) || REQUIRE_PRIMARY.equals(required)) && !primariExists)
				|| ((REQUIRE_ALL.equals(required) || REQUIRE_SECONDARY.equals(required)) && !secondaryExists)){
			return true;
		}
		return false;
	}

	@Override
	public int getOrderPriority() {
		return 1;
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	@Override
	public AuthenticatorResult process(AuthenticationState state, @SuppressWarnings("rawtypes") Map parameters) throws AccessDeniedException {
		String required = configurationService.getValue(state.getRealm(), "missingPhone.required");
		
		final UserPrincipal principal = (UserPrincipal)state.getPrincipal();
		boolean primariExists = StringUtils.isNotBlank(principal.getMobile());
		boolean secondaryExists = StringUtils.isNotBlank(principal.getSecondaryMobile());
		boolean error = false;
		
		if((REQUIRE_ALL.equals(required) || REQUIRE_PRIMARY.equals(required)) && !primariExists){
			String primaryPhone = (String)parameters.get(PARAM_PRIMARY);
			state.getParameters().put(PARAM_PRIMARY, primaryPhone);
			if(primaryPhone == null || "".equals(primaryPhone)){
				state.setLastErrorMsg("error.primaryPhoneMandatory");
				state.setLastErrorIsResourceKey(true);
				error = true;
			}else if(!validatePhoneNumber(primaryPhone)){
				state.setLastErrorMsg("error.primaryPhoneInvalid");
				state.setLastErrorIsResourceKey(true);
				error = true;
			}
		}
		if((REQUIRE_ALL.equals(required) || REQUIRE_SECONDARY.equals(required)) && !secondaryExists){
			String secondaryPhone = (String)parameters.get(PARAM_SECONDARY);
			state.getParameters().put(PARAM_SECONDARY, secondaryPhone);
			if(!error && secondaryPhone == null || "".equals(secondaryPhone)){
				state.setLastErrorMsg("error.secondaryPhoneMandatory");
				state.setLastErrorIsResourceKey(true);
				error = true;
			}else if(!error && !validatePhoneNumber(secondaryPhone)){
				state.setLastErrorMsg("error.secondaryPhoneInvalid");
				state.setLastErrorIsResourceKey(true);
				error = true;
			}
		}
		if(!error && (REQUIRE_ALL.equals(required) || REQUIRE_SECONDARY.equals(required))){
			String primary = (principal.getMobile() != null && !"".equals(principal.getMobile())) ? principal.getMobile() : (String)parameters.get(PARAM_PRIMARY);
			String secondary = (principal.getSecondaryMobile() != null && !"".equals(principal.getSecondaryMobile())) ? principal.getSecondaryMobile() : (String)parameters.get(PARAM_SECONDARY);
			if(primary != null && primary.equals(secondary)){
				state.setLastErrorMsg("error.duplicatedPhone");
				state.setLastErrorIsResourceKey(true);
				error = true;
			}
		}
		if(error){
			return AuthenticatorResult.INSUFFICIENT_DATA;
		}else{
			final Map<String,String> properties = new HashMap<String,String>();
			if((REQUIRE_ALL.equals(required) || REQUIRE_PRIMARY.equals(required)) && !primariExists){
				properties.put("mobile", (String)parameters.get(PARAM_PRIMARY));
			}
			if((REQUIRE_ALL.equals(required) || REQUIRE_SECONDARY.equals(required)) && !secondaryExists){
				properties.put("secondaryMobile", (String)parameters.get(PARAM_SECONDARY));
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
		String required = configurationService.getValue(state.getRealm(), "missingPhone.required");
		
		UserPrincipal principal = (UserPrincipal)state.getPrincipal();
		boolean primariExists = StringUtils.isNotBlank(principal.getMobile());
		boolean secondaryExists = StringUtils.isNotBlank(principal.getSecondaryMobile());
		
		FormTemplate t = new FormTemplate(state.getScheme().getResourceKey());
		t.getInputFields().add(new ParagraphField("missingPhone.paragraph", true));
		if((REQUIRE_ALL.equals(required) || REQUIRE_PRIMARY.equals(required)) && !primariExists){
			t.getInputFields().add(new TextInputField(PARAM_PRIMARY, state.getParameter(PARAM_PRIMARY)!=null ? state.getParameter(PARAM_PRIMARY) : "", true, I18N.getResource(state.getLocale(), RESOURCE_BUNDLE, "missingPhone.primary")));
		}
			
		if((REQUIRE_ALL.equals(required) || REQUIRE_SECONDARY.equals(required)) && !secondaryExists){
			t.getInputFields().add(new TextInputField(PARAM_SECONDARY, state.getParameter(PARAM_SECONDARY)!=null ? state.getParameter(PARAM_SECONDARY) : "", true, I18N.getResource(state.getLocale(), RESOURCE_BUNDLE, "missingPhone.secondary")));
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
	
	private boolean validatePhoneNumber(String val) {
		return true;
	}

}
