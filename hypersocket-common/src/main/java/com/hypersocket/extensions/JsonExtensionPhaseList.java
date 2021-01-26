package com.hypersocket.extensions;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hypersocket.json.JsonResourceList;

@SuppressWarnings("serial")
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonExtensionPhaseList extends JsonResourceList<JsonExtensionPhase> implements Serializable {

	public JsonExtensionPhase getFirstResult() {
		return getResult() == null || getResult().length == 0 ? null : getResult()[0];
	}

	public JsonExtensionPhase getResultByName(String phase) {
		if (getResult() != null) {
			for (JsonExtensionPhase p : getResult()) {
				if (p.getName().equals(phase)) {
					return p;
				}
			}
		}
		return null;
	}

}
