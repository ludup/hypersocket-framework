package com.hypersocket.extensions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hypersocket.json.JsonResourceList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonExtensionList extends JsonResourceList<ExtensionVersion> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1620611397716483105L;
	
}
