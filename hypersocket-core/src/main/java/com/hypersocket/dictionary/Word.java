/* HEADER */
package com.hypersocket.dictionary;

import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.hypersocket.resource.SimpleResource;

@Entity
@Table(name = "words", uniqueConstraints = @UniqueConstraint(columnNames = {"locale","word_text"}))
public class Word extends SimpleResource {

	private static final long serialVersionUID = 3518709658570275084L;

	@Column(name = "locale", nullable = false, length = 30)
	private String locale = "*";

	@Column(name = "word_text", nullable = false, length = 255)
	private String text;

	@Column(name = "word_index", nullable = false, length = 255)
	private long wordIndex;

	Word() {
	}

	public Word(Locale locale, String text, long wordIndex) {
		this.text = text;
		this.wordIndex = wordIndex;
		setLocale(locale);
	}

	public long getWordIndex() {
		return wordIndex;
	}

	public void setWordIndex(long wordIndex) {
		this.wordIndex = wordIndex;
	}

	public Locale getLocale() {
		try {
			return locale == null || locale.equals("*") ? null : Locale.forLanguageTag(locale);
		}
		catch(Exception e) {
			return null;
		}
	}

	public void setLocale(Locale locale) {
		this.locale = locale == null ? "*" : locale.toLanguageTag();
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public String getName() {
		return text == null ? getId().toString() : text;
	}

}