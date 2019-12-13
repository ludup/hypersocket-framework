/* HEADER */
package com.hypersocket.dictionary;

import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.hypersocket.resource.SimpleResource;

@Entity
@Table(name = "words")
public class Word extends SimpleResource {

	private static final long serialVersionUID = 3518709658570275084L;

	@Column(name = "locale", nullable = true, length = 30)
	private Locale locale = null;

	@Column(name = "word_text", nullable = false, length = 255)
	private String text;

	@Column(name = "word_index", nullable = false, length = 255)
	private long wordIndex;

	Word() {
	}

	public Word(Locale locale, String text, long wordIndex) {
		this.locale = locale;
		this.text = text;
		this.wordIndex = wordIndex;
	}

	public final long getWordIndex() {
		return wordIndex;
	}

	public final void setWordIndex(long wordIndex) {
		this.wordIndex = wordIndex;
	}

	public final Locale getLocale() {
		return locale;
	}

	public final void setLocale(Locale locale) {
		this.locale = locale;
	}

	public final String getText() {
		return text;
	}

	public final void setText(String text) {
		this.text = text;
	}
	
	public String getName() {
		return getId().toString();
	}

}