package com.hypersocket.i18n;

public class Message {

	String bundle;
	String id;
	String original;
	String translated;
	
	public Message() {
		
	}
	
	public Message(String bundle, String id, String original, String translated) {
		this.bundle = bundle;
		this.id = id;
		this.original = original;
		this.translated = translated;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getOriginal() {
		return original;
	}
	public void setOriginal(String original) {
		this.original = original;
	}
	public String getTranslated() {
		return translated;
	}
	public void setTranslated(String translated) {
		this.translated = translated;
	}
	public String getBundle() {
		return bundle;
	}
	public void setBundle(String bundle) {
		this.bundle = bundle;
	}
	
	

}
