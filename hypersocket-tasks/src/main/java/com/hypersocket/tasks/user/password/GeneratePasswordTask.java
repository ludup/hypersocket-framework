package com.hypersocket.tasks.user.password;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.auth.PrincipalNotFoundException;
import com.hypersocket.config.ConfigurationService;
import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.tasks.AbstractTaskProvider;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.triggers.AbstractTaskResult;
import com.hypersocket.triggers.ValidationException;

import edu.vt.middleware.password.CharacterRule;
import edu.vt.middleware.password.DigitCharacterRule;
import edu.vt.middleware.password.LowercaseCharacterRule;
import edu.vt.middleware.password.NonAlphanumericCharacterRule;
import edu.vt.middleware.password.PasswordGenerator;
import edu.vt.middleware.password.UppercaseCharacterRule;

@Component
public class GeneratePasswordTask extends AbstractTaskProvider {

	public static final String TASK_RESOURCE_KEY = "generatePasswordTask";

	public static final String RESOURCE_BUNDLE = "GeneratePasswordTask";
	
	@Autowired
	GeneratePasswordTaskRepository repository;

	@Autowired
	TaskProviderService taskService;

	@Autowired
	EventService eventService;

	@Autowired
	I18NService i18nService; 
	
	@Autowired
	ConfigurationService configurationService; 
	
	@Autowired
	RealmService realmService; 
	
	public GeneratePasswordTask() {
	}
	
	@PostConstruct
	private void postConstruct() {
		taskService.registerTaskProvider(this);

		i18nService.registerBundle(RESOURCE_BUNDLE);

		eventService.registerEvent(GeneratePasswordTaskResult.class,
				RESOURCE_BUNDLE);
	}

	@Override
	public String getResourceBundle() {
		return RESOURCE_BUNDLE;
	}

	@Override
	public String[] getResourceKeys() {
		return new String[] { TASK_RESOURCE_KEY };
	}

	@Override
	public void validate(Task task, Map<String, String> parameters)
			throws ValidationException {

	}

	@Override
	public AbstractTaskResult execute(Task task, Realm currentRealm, List<SystemEvent> event)
			throws ValidationException {

		String principalName = processTokenReplacements(repository.getValue(task, "generatePassword.principal"), event);
		Principal principal = realmService.getPrincipalByName(
				task.getRealm(), 
				principalName,
				PrincipalType.USER);
		
		if(principal==null) {
			return new GeneratePasswordTaskResult(this, 
					new PrincipalNotFoundException(String.format("%s is not a valid username", principalName)), 
					currentRealm, 
					task, 
					principalName);
		}
		
		int minDigis = repository.getIntValue(task, "generatePassword.minDigits");
		int minLowercase = repository.getIntValue(task, "generatePassword.minLower");
		int minUppcase = repository.getIntValue(task, "generatePassword.minUpper");
		int minNonAlpha = repository.getIntValue(task, "generatePassword.minSymbols");

		try {
			String password = generatePassword(minDigis, minLowercase, minUppcase, minNonAlpha, 
				repository.getIntValue(task, "generatePassword.length"));
		
			realmService.setPassword(principal, password, 
					repository.getBooleanValue(task, "generatePassword.forceChange"), 
					true);
			
			return new GeneratePasswordTaskResult(this, true, currentRealm, task, principal, password);
		
		} catch(Throwable t) {
			return new GeneratePasswordTaskResult(this, t, currentRealm, task, principalName);
		}
	}
	
	public static String generatePassword(int digits, 
			int lower, 
			int upper, 
			int symbols, 
			int maxLength) {
		
		List<CharacterRule> rules = new ArrayList<CharacterRule>();
		
		if(digits > 0) {
			rules.add(new DigitCharacterRule(digits));
		}
		if(lower > 0) {
			rules.add(new LowercaseCharacterRule(lower));
		}
		if(upper > 0) {
			rules.add(new UppercaseCharacterRule(upper));
		}
		if(symbols > 0) {
			rules.add(new NonAlphanumericCharacterRule(symbols));
		}
	
		PasswordGenerator generator = new PasswordGenerator();
		String password = generator.generatePassword(maxLength, rules);
		return password;
	}
	
	public String getResultResourceKey() {
		return GeneratePasswordTaskResult.EVENT_RESOURCE_KEY;
	}

	@Override
	public ResourceTemplateRepository getRepository() {
		return repository;
	}

	@Override
	public boolean isSystem() {
		return false;
	}
}
