package com.hypersocket.attributes.user;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.auth.AuthenticationService;
import com.hypersocket.auth.AuthenticationServiceImpl;
import com.hypersocket.auth.AuthenticationState;
import com.hypersocket.auth.AuthenticatorResult;
import com.hypersocket.auth.PostAuthenticationStep;
import com.hypersocket.i18n.I18N;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.json.input.FormTemplate;
import com.hypersocket.json.input.TextInputField;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.AbstractPropertyTemplate;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.RealmService;
import com.hypersocket.util.ArrayValueHashMap;

@Component
public class RequiredAttributeCheckerPostAuthenticationStep implements PostAuthenticationStep{
	
	private static final String CUSTOM_ATTRIBUTE = "custom_attribute";
	private static final String CUSTOM_ATTRIBUTE_FILTER_KEY = "custom";
	private static final String CUSTOM_ATTRIBUTE_MISSING_NAME_TAG = "_name";
	private static final String CUSTOM_ATTRIBUTE_MISSING_VALUE_TAG = "_value";
	
	public static final String RESOURCE_KEY = "requiredAttributeChecker";
	public static final String RESOURCE_BUNDLE = "RequiredAttributeChecker";
	
	static Logger log = LoggerFactory.getLogger(RequiredAttributeCheckerPostAuthenticationStep.class);

	@Autowired
	private I18NService i18nService;
	
	@Autowired
	private AuthenticationService authenticationService;
	
	@Autowired
	private RealmService realmService;
	
	@PostConstruct
	private void postConstruct() {
		i18nService.registerBundle(RESOURCE_BUNDLE);
		authenticationService.registerPostAuthenticationStep(this);
	}
	
	@Override
	public boolean requiresProcessing(AuthenticationState state) throws AccessDeniedException {
		if(state.getInitialSchemeResourceKey().equals(AuthenticationServiceImpl.BASIC_AUTHENTICATION_RESOURCE_KEY)) {
			return false;
		}
		Collection<PropertyCategory> propertyCategories = realmService.getUserPropertyTemplates(state.getPrincipal());
		for (PropertyCategory propertyCategory : propertyCategories) {
			String filter = propertyCategory.getFilter();
			if (CUSTOM_ATTRIBUTE_FILTER_KEY.equals(filter)) {
				log.debug("Processing property with name {} category group {} category key {}.", 
						propertyCategory.getName(),
						propertyCategory.getCategoryGroup(),
						propertyCategory.getCategoryKey());
		
				List<AbstractPropertyTemplate> templates = propertyCategory.getTemplates();
				for (AbstractPropertyTemplate template : templates) {
					
					log.debug("Processing template with name {} required {} resource key {}.", 
							template.getName(),
							template.isRequired(),
							template.getResourceKey());
					
					boolean required = template.isRequired();
					
					String value = template.getValue();
					
					String resourceKey = template.getResourceKey();
					
					String key = getCustomAttributeKey(resourceKey);
					
					Map<String, String> stateParameters = state.getParameters();
					
					// In case it is missing in state first add, this will be the case first time page loads
					// post that on subsequent submits everything will be in state
					// we store three values for a missing attribute to track value, name and resource key all 
					// are required in various phases for computation or UI
					if (required && !stateParameters.containsKey(key) && StringUtils.isBlank(value)) {
						
						log.debug("Required is missing a value for key {}.", key);
						
						String keyValue = state.getParameter(key);
						if (StringUtils.isBlank(keyValue)) {
							state.addParameter(key, resourceKey);
							state.addParameter(getCustomAttributeValueKey(key), null);
							state.addParameter(getCustomAttributeNameKey(key), template.getName());
						}
						
					}
				}
				
			}
		}
		
		Set<String> missingCustomAttributes = getAllCustomAttributeKeysFromState(state);
		
		boolean missingAttributes = !missingCustomAttributes.isEmpty();
		
		log.info("Missing attributes check result is {}.", missingAttributes);
		
		return missingAttributes;
	}

	@Override
	public int getOrderPriority() {
		return 0;
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	/**
	 * Idea is to maintain state parameters as source of truth for processing and tracking.
	 * Hence we keep it up to date from data source and parameters and execute logic
	 */
	@Override
	public AuthenticatorResult process(AuthenticationState state, Map<String, String[]> parameters) throws AccessDeniedException {
		
		;
		boolean missing = false;
		try(var c = authenticationService.tryAs(state.getPrincipal())) {
			
			// move any custom attributes from parameters to state
			// any user updates would be moved from parameters to state
			moveCustomAttributesFromParametersToState(state, parameters);
			
			// check if we have custom attributes in state we might want to persist them, else if none
			// continue.
			Set<String> missingCustomAttributesValueKeys = getAllCustomAttributeValueKeysFromState(state);
			for (String string : missingCustomAttributesValueKeys) {
				if (StringUtils.isBlank(state.getParameter(string))) {
					missing = true;
				}
			}
			
			if (missing) {
				state.setLastErrorMsg("missing.custom.attribute");
				state.setLastErrorIsResourceKey(true);
				return AuthenticatorResult.INSUFFICIENT_DATA;
			} else {
				log.info("All missing atttributes have value now, persisting them.");
				for (String key : getAllCustomAttributeKeysFromState(state)) {
					realmService.setUserProperty(state.getPrincipal(), 
							state.getParameter(key), 
							state.getParameter(getCustomAttributeValueKey(key)));
				}
				
				return AuthenticatorResult.AUTHENTICATION_SUCCESS;
			}

			
			
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public FormTemplate createTemplate(AuthenticationState state) throws AccessDeniedException {
		
		Set<String> missingCustomAttributes = getAllCustomAttributeKeysFromState(state);
		
		if (!missingCustomAttributes.isEmpty()) {
			
			FormTemplate t =  new FormTemplate(I18N.getResource(state.getLocale(), RESOURCE_BUNDLE, RESOURCE_KEY));
			
			for (String string : missingCustomAttributes) {
				
				TextInputField field = new TextInputField(string, state.getParameter(string + CUSTOM_ATTRIBUTE_MISSING_VALUE_TAG), 
						true, state.getParameter(string + CUSTOM_ATTRIBUTE_MISSING_NAME_TAG));
				t.getInputFields().add(field);
			}
			
			t.setShowStartAgain(false);
		
			return t;
		}
		
		return null;
	}

	@Override
	public boolean requiresUserInput(AuthenticationState state) throws AccessDeniedException {
		return true;
	}

	@Override
	public boolean requiresSession(AuthenticationState state) throws AccessDeniedException {
		return false;
	}

	private String getCustomAttributeKey(String key) {
		return CUSTOM_ATTRIBUTE + "_" + key;
	}
	
	private String getCustomAttributeValueKey(String key) {
		return key + CUSTOM_ATTRIBUTE_MISSING_VALUE_TAG;
	}
	
	private String getCustomAttributeNameKey(String key) {
		return key + CUSTOM_ATTRIBUTE_MISSING_NAME_TAG;
	}
	
	private void moveCustomAttributesFromParametersToState(AuthenticationState state, Map<String, String[]> parameters) {
		Set<String> keys = parameters.keySet();
		for (String key : keys) {
			if (key.startsWith(CUSTOM_ATTRIBUTE)) {
				state.addParameter(key + CUSTOM_ATTRIBUTE_MISSING_VALUE_TAG, ArrayValueHashMap.getSingle(parameters, key));
			}
		}
	}
	
	private Set<String> getAllCustomAttributeKeysFromState(AuthenticationState state) {
		Set<String> missingCustomAttributes = new HashSet<>();
		Map<String, String> parameters = state.getParameters();
		for (Entry<String, String> entry : parameters.entrySet()) {
			if (entry.getKey().startsWith(CUSTOM_ATTRIBUTE) 
					&& !entry.getKey().endsWith(CUSTOM_ATTRIBUTE_MISSING_VALUE_TAG) 
					&& !entry.getKey().endsWith(CUSTOM_ATTRIBUTE_MISSING_NAME_TAG)) {
				missingCustomAttributes.add(entry.getKey());
			}
		}
		
		return missingCustomAttributes;
	}
	
	private Set<String> getAllCustomAttributeValueKeysFromState(AuthenticationState state) {
		Set<String> missingCustomAttributesValueKeys = new HashSet<>();
		Map<String, String> parameters = state.getParameters();
		for (Entry<String, String> entry : parameters.entrySet()) {
			if (entry.getKey().startsWith(CUSTOM_ATTRIBUTE) 
					&& entry.getKey().endsWith(CUSTOM_ATTRIBUTE_MISSING_VALUE_TAG)) {
				missingCustomAttributesValueKeys.add(entry.getKey());
			}
		}
		
		return missingCustomAttributesValueKeys;
	}
}
