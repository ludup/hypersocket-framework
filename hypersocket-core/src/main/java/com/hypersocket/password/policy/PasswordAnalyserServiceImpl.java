package com.hypersocket.password.policy;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PasswordAnalyserServiceImpl implements PasswordAnalyserService, Serializable {

	private static final long serialVersionUID = 1L;
	
	@Autowired
	DictionaryService dictionaryService;

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
			throws PasswordPolicyException {

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
			throw new PasswordPolicyException(PasswordPolicyException.Type.tooShort, strength);
		}
		if (characteristics.getMaximumLength() != -1 && password.length > characteristics.getMaximumLength()) {
			throw new PasswordPolicyException(PasswordPolicyException.Type.tooLong, strength);
		}

		// Check the password doesn't contain the username
		if (username != null && !characteristics.getContainUsername()) {
			if (new String(password).toLowerCase().contains(username.toLowerCase())) {
				throw new PasswordPolicyException(PasswordPolicyException.Type.containsUsername, strength);
			}
		}

		// Check the password doesn't contain any dictionary words
		if (!characteristics.getContainDictionaryWord()) {
			// Break up the passphrase into what look like words
			StringBuilder bui = new StringBuilder();
			List<String> words = new ArrayList<String>();
			for (char ch : password) {
				if (Character.isLetter(ch)) {
					bui.append(ch);
				} else {
					if (bui.length() > 0) {
						words.add(bui.toString());
						bui.setLength(0);
					}
				}
			}

			// Now look for those words
			for (String word : words) {
				// Minimum of 4 letter words
				if (word.length() > 3) {
					if (dictionaryService.containsWord(locale, word)) {
						throw new PasswordPolicyException(PasswordPolicyException.Type.containsDictionaryWords, strength);
					}
				}
			}
		}

		// Check against policy
		if (characteristics.getMinimumCriteriaMatches() == 4) {
			check(PasswordPolicyException.Type.notEnoughDigits, digit, characteristics.getMinimumDigits(), strength);
			check(PasswordPolicyException.Type.notEnoughLowerCase, lowerCase, characteristics.getMinimumLower(), strength);
			check(PasswordPolicyException.Type.notEnoughUpperCase, upperCase, characteristics.getMinimumUpper(), strength);
			check(PasswordPolicyException.Type.notEnoughSymbols, other, characteristics.getMinimumSymbol(), strength);
		} else {
			int matches = 0;
			if (matches(PasswordPolicyException.Type.notEnoughDigits, digit, characteristics.getMinimumDigits(), strength)) {
				matches++;
			}
			if (matches(PasswordPolicyException.Type.notEnoughLowerCase, lowerCase, characteristics.getMinimumLower(), strength)) {
				matches++;
			}
			if (matches(PasswordPolicyException.Type.notEnoughUpperCase, upperCase, characteristics.getMinimumUpper(), strength)) {
				matches++;
			}
			if (matches(PasswordPolicyException.Type.notEnoughSymbols, other, characteristics.getMinimumSymbol(), strength)) {
				matches++;
			}
			if (matches < characteristics.getMinimumCriteriaMatches()) {
				throw new PasswordPolicyException(PasswordPolicyException.Type.doesNotMatchComplexity, strength);
			}
		}

		return strength;

	}

	private void check(PasswordPolicyException.Type type, int val, int req, float strength) throws PasswordPolicyException {
		if (!matches(type, val, req, strength)) {
			throw new PasswordPolicyException(type, strength);
		}
	}

	private boolean matches(PasswordPolicyException.Type type, int val, int req, float strength) throws PasswordPolicyException {
		return req == -1 || val >= req;
	}
}
