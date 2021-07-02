package com.hypersocket.password.policy;

import java.io.IOException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.config.SystemConfigurationService;
import com.hypersocket.dictionary.DictionaryResourceService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.password.policy.PasswordPolicyException.Type;

@Service
public class PasswordAnalyserServiceImpl implements PasswordAnalyserService, Serializable {

	private static final long serialVersionUID = 1L;
	
	@Autowired
	private DictionaryResourceService dictionaryService;
	
	@Autowired
	private SystemConfigurationService systemConfigurationService;
	
	@Autowired
	private I18NService i18nService;

	final String DEFAULT_SYMBOLS;
	
	public PasswordAnalyserServiceImpl() {
		try {
			DEFAULT_SYMBOLS = IOUtils.toString(getClass().getResourceAsStream("/defaultSymbols.txt"), "UTF-8");
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	
	@Override
	public float analyse(Locale locale, String username, char[] password, PasswordPolicyResource characteristics)
			throws PasswordPolicyException, IOException {

		// Count up the character types
		int lowerCase = 0;
		int upperCase = 0;
		int digit = 0;
		int other = 0;
		int unmatched = 0;

		// Use the default symbols if the provided characteristics doesn't
		// supply them
		char[] symbols;
		if(characteristics.getValidSymbols()==null) {
			symbols = DEFAULT_SYMBOLS.toCharArray();
		} else {
			symbols = characteristics.getValidSymbols().toCharArray();
		}
		String others = new String(symbols);
		for (char ch : password) {
			if (Character.isDigit(ch)) {
				digit++;
			} else if (Character.isLowerCase(ch)) {
				lowerCase++;
			} else if (Character.isUpperCase(ch)) {
				upperCase++;
			} else if (others.indexOf(ch) != -1) {
				other++;
			} else {
				unmatched++;
			}
		}
		
		/**
		 * Calculate the ideal strength. First determine what would be
		 * considered a strong password. This is a total of all the minimum
		 * character type values multiplied by the <i>Very Strong Password
		 * Factor</i>
		 */

		float idealStrength = characteristics.getMinimumLength();
		idealStrength += Math.max(0, characteristics.getMinimumDigits());
		idealStrength += Math.max(0, characteristics.getMinimumLower());
		idealStrength += Math.max(0, characteristics.getMinimumUpper());
		idealStrength += Math.max(0, characteristics.getMinimumSymbol());
		idealStrength *= Math.max(0, characteristics.getVeryStrongFactor());
		idealStrength = Math.max(1, idealStrength);

		// Actual strength is a total of actual counts
		float actualStrength = digit + lowerCase + upperCase + other;

		// Bonus points for unmatched characters
		actualStrength += unmatched;

		// Don't exceed the ideal
		actualStrength = Math.min(idealStrength, actualStrength);

		float strength = actualStrength / idealStrength;

		// First check the password is within the size range
		if (characteristics.getMinimumLength() != -1 && password.length < characteristics.getMinimumLength()) {
			throw new PasswordPolicyException(PasswordPolicyException.Type.tooShort, 
					strength, i18nService.getResource("password.policy.too.short", locale));
		}
		if (characteristics.getMaximumLength() != -1 && password.length > characteristics.getMaximumLength()) {
			throw new PasswordPolicyException(PasswordPolicyException.Type.tooLong, 
					strength, i18nService.getResource("password.policy.too.long", locale));
		}

		// Check the password doesn't contain the username
		if (username != null && !characteristics.getContainUsername()) {
			if (new String(password).toLowerCase().contains(username.toLowerCase())) {
				throw new PasswordPolicyException(PasswordPolicyException.Type.containsUsername, 
						strength, i18nService.getResource("password.policy.contains.username", locale));
			}
		}

		// Check the password doesn't contain any dictionary words
		if (!characteristics.getContainDictionaryWord()) {
			systemConfigurationService.setupSystemContext();
			int minWordLength;
			boolean matchAlphaOnly;
			try {
				minWordLength = systemConfigurationService.getIntValue("dictionary.minWordLength");
				matchAlphaOnly = systemConfigurationService.getBooleanValue("dictionary.matchAlphaOnly");
			}
			finally {
				systemConfigurationService.clearPrincipalContext();
			}
			
			// Break up the passphrase into what look like words
			StringBuilder bui = new StringBuilder();
			List<String> words = new ArrayList<String>();
			for (char ch : password) {
				if ((matchAlphaOnly && Character.isLetter(ch)) || (!matchAlphaOnly && !Character.isWhitespace(ch))) {
					bui.append(ch);
 				} else {
					if (bui.length() > 0) {
						words.add(bui.toString());
						bui.setLength(0);
					}
				}
			}
			if (bui.length() > 0) {
				words.add(bui.toString());
				bui.setLength(0);
			}

			// Now look for those words
			for (String word : words) {
				// Minimum of 4 letter words
				if (word.length() >= minWordLength) {
					if (dictionaryService.containsWord(locale, word)) {
						throw new PasswordPolicyException(PasswordPolicyException.Type.containsDictionaryWords, 
								strength, i18nService.getResource("password.policy.contains.dictionary.word", locale));
					}
				}
			}
		}

		// Check against policy
		if (characteristics.getMinimumCriteriaMatches() == 4) {
			check(PasswordPolicyException.Type.notEnoughDigits, digit, characteristics.getMinimumDigits(), strength, locale);
			check(PasswordPolicyException.Type.notEnoughLowerCase, lowerCase, characteristics.getMinimumLower(), strength, locale);
			check(PasswordPolicyException.Type.notEnoughUpperCase, upperCase, characteristics.getMinimumUpper(), strength, locale);
			check(PasswordPolicyException.Type.notEnoughSymbols, other, characteristics.getMinimumSymbol(), strength, locale);
		} else {
			int matches = 0;
			List<Type> messageForCharacteristicsTypes = new ArrayList<>();
			
			if (matches(PasswordPolicyException.Type.notEnoughDigits, digit, characteristics.getMinimumDigits(), strength)) {
				matches++;
			} else {
				messageForCharacteristicsTypes.add(PasswordPolicyException.Type.notEnoughDigits);
			}
			
			if (matches(PasswordPolicyException.Type.notEnoughLowerCase, lowerCase, characteristics.getMinimumLower(), strength)) {
				matches++;
			} else {
				messageForCharacteristicsTypes.add(PasswordPolicyException.Type.notEnoughLowerCase);
			}
			
			if (matches(PasswordPolicyException.Type.notEnoughUpperCase, upperCase, characteristics.getMinimumUpper(), strength)) {
				matches++;
			} else {
				messageForCharacteristicsTypes.add(PasswordPolicyException.Type.notEnoughUpperCase);
			}
			
			if (matches(PasswordPolicyException.Type.notEnoughSymbols, other, characteristics.getMinimumSymbol(), strength)) {
				matches++;
			} else {
				messageForCharacteristicsTypes.add(PasswordPolicyException.Type.notEnoughSymbols);
			}
			
			if (matches < characteristics.getMinimumCriteriaMatches()) {
				
				List<String> messageForCharacteristics = collectCharacteristics(locale, messageForCharacteristicsTypes.toArray(new Type[0]));
				
				throw new PasswordPolicyException(PasswordPolicyException.Type.doesNotMatchComplexity, 
						strength, MessageFormat.format(
								i18nService.getResource("password.policy.contains.characteristics.no.match", locale), 
								StringUtils.join(messageForCharacteristics, ","))
						);
			}
		}

		return strength;

	}

	private void check(PasswordPolicyException.Type type, int val, int req, float strength, Locale locale) throws PasswordPolicyException {
		if (!matches(type, val, req, strength)) {
			
			List<String> messageForCharacteristics = collectCharacteristics(locale, type);
			
			if (messageForCharacteristics.isEmpty()) {
				throw new PasswordPolicyException(type, strength);
			} else {
				throw new PasswordPolicyException(type, strength, MessageFormat.format(
							i18nService.getResource("password.policy.contains.characteristics.no.match", locale), 
							StringUtils.join(messageForCharacteristics, ","))
						);
			}
		}
	}
	
	private List<String> collectCharacteristics(Locale locale, PasswordPolicyException.Type...types) {
		List<String> messageForCharacteristics = new ArrayList<String>();
		
		for (PasswordPolicyException.Type type : types) {
			if (PasswordPolicyException.Type.notEnoughDigits.equals(type)) {
				messageForCharacteristics.add(i18nService.getResource("password.policy.contains.characteristics.digits", locale));
			} else if (PasswordPolicyException.Type.notEnoughLowerCase.equals(type)) {
				messageForCharacteristics.add(i18nService.getResource("password.policy.contains.characteristics.lowercase", locale));
			} else if (PasswordPolicyException.Type.notEnoughUpperCase.equals(type)) {
				messageForCharacteristics.add(i18nService.getResource("password.policy.contains.characteristics.uppercase", locale));
			} else if (PasswordPolicyException.Type.notEnoughSymbols.equals(type)) {
				messageForCharacteristics.add(i18nService.getResource("password.policy.contains.characteristics.symbols", locale));
			}
		}
		
		return messageForCharacteristics;
	}

	private boolean matches(PasswordPolicyException.Type type, int val, int req, float strength) throws PasswordPolicyException {
		return req == -1 || val >= req;
	}
}
