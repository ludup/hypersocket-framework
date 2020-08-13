package com.hypersocket.extensions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hypersocket.json.JsonResourceList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonExtensionList extends JsonResourceList<ExtensionVersion> {
	
}
