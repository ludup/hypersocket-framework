package com.hypersocket.realm;

import java.util.Map;

public interface CommunicationDataView {
	
	String COMMON_TYPE_MOBILE = "mobile";
	String COMMON_TYPE_LANDLINE = "landline";
	String COMMON_TYPE_EMAIL = "email";

	String getType();
	
	String[] getValue();
	
	Map<String, String> getProperties();
	
	public class CoreCommunicationDataView implements CommunicationDataView {
		
		private final String type;
		
		private final String[] value;
		
		private final Map<String, String> properties;
		
	
		public CoreCommunicationDataView(String type, String[] value, Map<String, String> properties) {
			this.type = type;
			this.value = value;
			this.properties = properties;
		}


		@Override
		public String getType() {
			return this.type;
		}


		@Override
		public String[] getValue() {
			return this.value;
		}


		@Override
		public Map<String, String> getProperties() {
			return this.properties;
		}
		
	}
}

