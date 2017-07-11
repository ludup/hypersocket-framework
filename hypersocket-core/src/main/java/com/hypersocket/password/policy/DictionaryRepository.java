package com.hypersocket.password.policy;

import java.util.Locale;

public interface DictionaryRepository {

	void setup();

	String randomWord(Locale locale);

	boolean containsWord(Locale locale, String word);

}
