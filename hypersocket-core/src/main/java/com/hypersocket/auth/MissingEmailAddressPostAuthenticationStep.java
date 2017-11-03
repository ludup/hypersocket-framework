package com.hypersocket.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.config.ConfigurationService;
import com.hypersocket.email.EmailNotificationServiceImpl;
import com.hypersocket.i18n.I18N;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.input.FormTemplate;
import com.hypersocket.input.ParagraphField;
import com.hypersocket.input.TextInputField;
import com.hypersocket.local.LocalUser;
import com.hypersocket.local.LocalUserRepository;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.profile.ProfileRepository;

@Component
public class MissingEmailAddressPostAuthenticationStep implements PostAuthenticationStep{

	public static final String RESOURCE_KEY = "missingEmailAddress";
	public static final String RESOURCE_BUNDLE = "MissingEmailAddressService";
	
	public static final String REQUIRE_NONE = "required.none";
	public static final String REQUIRE_PRIMARY = "required.pripary";
	public static final String REQUIRE_SECONDARY = "required.secondary";
	public static final String REQUIRE_ALL = "required.all";
	
	public static final String PARAM_PRIMARY = "primaryEmail";
	public static final String PARAM_SECONDARY = "secondaryEmail";
	
	@Autowired
	I18NService i18nService;
	
	@Autowired
	MissingEmailAddressRepository repository;
	
	@Autowired
	ProfileRepository profileRepository;
	
	@Autowired
	AuthenticationService authenticationService;
	
	@Autowired
	ConfigurationService configurationService;
	
	@Autowired
	LocalUserRepository localUserRepository;
	
	@PostConstruct
	private void postConstruct() {
		i18nService.registerBundle(RESOURCE_BUNDLE);
		authenticationService.registerPostAuthenticationStep(this);
	}
	
	@Override
	public boolean requiresProcessing(AuthenticationState state) {
		LocalUser localUser = (LocalUser)localUserRepository.getUserById(state.getPrincipal().getId(), state.getRealm(), false);
		String required = configurationService.getValue(state.getRealm(), "missingEmail.required");
		boolean primariExists = localUser.getEmail() != null &&  !"".equals(localUser.getEmail());
		boolean secondaryExists = localUser.getSecondaryEmail() != null && !"".equals(localUser.getSecondaryEmail());
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
	public AuthenticatorResult process(AuthenticationState state, Map parameters) throws AccessDeniedException {
		String required = configurationService.getValue(state.getRealm(), "missingEmail.required");
		LocalUser localUser = (LocalUser)localUserRepository.getUserById(state.getPrincipal().getId(), state.getRealm(), false);
		boolean primariExists = localUser.getEmail() != null &&  !"".equals(localUser.getEmail());
		boolean secondaryExists = localUser.getSecondaryEmail() != null && !"".equals(localUser.getSecondaryEmail());
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
		if(error){
			return AuthenticatorResult.INSUFFICIENT_DATA;
		}else{
			if((REQUIRE_ALL.equals(required) || REQUIRE_PRIMARY.equals(required)) && !primariExists){
				localUser.setEmail((String)parameters.get(PARAM_PRIMARY));
			}
			if((REQUIRE_ALL.equals(required) || REQUIRE_SECONDARY.equals(required)) && !secondaryExists){
				localUser.setSecondaryEmail((String)parameters.get(PARAM_SECONDARY));
			}
			localUserRepository.saveUser(localUser, new HashMap<String, String>());
			return AuthenticatorResult.AUTHENTICATION_SUCCESS;
		}
	}

	@Override
	public FormTemplate createTemplate(AuthenticationState state) {
		String required = configurationService.getValue(state.getRealm(), "missingEmail.required");
		LocalUser localUser = (LocalUser)localUserRepository.getUserById(state.getPrincipal().getId(), state.getRealm(), false);
		boolean primariExists = localUser.getEmail() != null &&  !"".equals(localUser.getEmail());
		boolean secondaryExists = localUser.getSecondaryEmail() != null && !"".equals(localUser.getSecondaryEmail());
		
		FormTemplate t = new FormTemplate(state.getScheme().getResourceKey());
		t.getInputFields().add(new ParagraphField("missingEmail.paragraph", true));
		if((REQUIRE_ALL.equals(required) || REQUIRE_PRIMARY.equals(required)) && !primariExists && state.getParameter(PARAM_PRIMARY) != null){
			t.getInputFields().add(new TextInputField(PARAM_PRIMARY, state.getParameter(PARAM_PRIMARY), true, I18N.getResource(state.getLocale(), RESOURCE_BUNDLE, "missingEmailAddress.primary")));
		}else{
			t.getInputFields().add(new TextInputField(PARAM_PRIMARY, "", true, I18N.getResource(state.getLocale(), RESOURCE_BUNDLE, "missingEmailAddress.primary")));
		}
			
		if((REQUIRE_ALL.equals(required) || REQUIRE_SECONDARY.equals(required)) && !secondaryExists && state.getParameter(PARAM_SECONDARY) != null){
			t.getInputFields().add(new TextInputField(PARAM_SECONDARY, state.getParameter(PARAM_SECONDARY), true, I18N.getResource(state.getLocale(), RESOURCE_BUNDLE, "missingEmailAddress.secondary")));
		}else{
			t.getInputFields().add(new TextInputField(PARAM_SECONDARY, "", true, I18N.getResource(state.getLocale(), RESOURCE_BUNDLE, "missingEmailAddress.secondary")));
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
