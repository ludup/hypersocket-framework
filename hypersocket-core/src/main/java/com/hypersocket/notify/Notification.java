package com.hypersocket.notify;

public class Notification {

	private boolean titleIsResourceKey = false;
	private boolean textIsResourceKey = false;
	private String key;
	private String title;
	private String text;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public boolean isTitleIsResourceKey() {
		return titleIsResourceKey;
	}

	public void setTitleIsResourceKey(boolean titleIsResourceKey) {
		this.titleIsResourceKey = titleIsResourceKey;
	}

	public boolean isTextIsResourceKey() {
		return textIsResourceKey;
	}

	public void setTextIsResourceKey(boolean textIsResourceKey) {
		this.textIsResourceKey = textIsResourceKey;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
