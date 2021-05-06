package com.hypersocket.i18n;

public enum I18NGroup {
	USER_PORTAL("user_portal"), ADMINISTRATION_PORTAL("administration_portal"), DEFAULT_GROUP("_default_i18n_group");

	private String title;
	
	I18NGroup(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}
	
	public static I18NGroup fromTitle(String title) {
		switch (title) {
			case "user_portal":
				return I18NGroup.USER_PORTAL;
			
			case "administration_portal":
				return I18NGroup.ADMINISTRATION_PORTAL;
				
			case "_default_i18n_group":
				return I18NGroup.DEFAULT_GROUP;	
	
			default:
				throw new IllegalStateException(String.format("Could not find group from title %s", title));
		}
	}
}
