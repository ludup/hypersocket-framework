package com.hypersocket.password.policy;

import java.util.Locale;

public interface DictionaryService {

	String randomWord(Locale locale);

	boolean containsWord(Locale locale, String word);

}
