package com.hypersocket.extensions;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hypersocket.json.JsonResource;

@SuppressWarnings("serial")
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonExtensionPhase extends JsonResource implements Serializable {

	String version;
	boolean publicPhase;

	public JsonExtensionPhase() {
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public boolean isPublicPhase() {
		return publicPhase;
	}

	public void setPublicPhase(boolean publicPhase) {
		this.publicPhase = publicPhase;
	}

}
