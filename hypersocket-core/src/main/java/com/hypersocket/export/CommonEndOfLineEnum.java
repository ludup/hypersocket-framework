package com.hypersocket.export;

public enum CommonEndOfLineEnum {
	R("\r", "\\r"), N("\n", "\\n"), RN("\r\n", "\\r\\n");

	private CommonEndOfLineEnum(String character, String value) {
		this.character = character;
		this.value = value;
	}

	private final String character;
	private final String value;

	public String getCharacter() {
		return character;
	}
	
	public String getValue() {
		return value;
	}
}
