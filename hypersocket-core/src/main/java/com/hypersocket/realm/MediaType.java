
package com.hypersocket.realm;

public enum MediaType {

	EMAIL,
	PHONE;
	
	public String defaultProviderKey() {
		/* NOTE: This is a bit of a quirk of the architecture. SMS support itself is provided
		 *       by an extension, but this enumeration is in the core. So the key 
		 *       'sms.defaultProvider' won't exist until that is installed. This doesn't really
		 *       as it would never be possible to send an SMS until that is installed, but it
		 *       should be fixed at some point. If the core SMS code was moved to the core,
		 *       or if this was made to be registerable type.
		 */
		switch(this) {
		case PHONE:
			return "sms.defaultProvider";
		default:
			return "email.defaultProvider";
		}
	}
}
