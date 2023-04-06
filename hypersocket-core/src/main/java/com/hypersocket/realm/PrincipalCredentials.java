package com.hypersocket.realm;

import java.util.Optional;

public interface PrincipalCredentials {
	
	public enum Encoding {
		RFC2307, NTLM, LM
	}
	
	/**
	 * Get the password in a specified format (if stored). 
	 * 
	 * @param encoding encoding
	 * @return optional encoded password
	 */
	Optional<String> getEncodedPassword(Encoding encoding);
}
