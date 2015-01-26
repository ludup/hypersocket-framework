package com.hypersocket.triggers;

public class TriggerValidationError {

	public enum ErrorType { MISSING_VALUE, INVALID_VALUE };
	String attributeName;
	String attributeValue;
	ErrorType type;
	
	public TriggerValidationError(String attributeName, String attributeValue) {
		this.attributeName = attributeName;
		this.attributeValue = attributeValue;
		this.type = ErrorType.INVALID_VALUE;
	}
	
	public TriggerValidationError(String attributeName) {
		this.attributeName = attributeName;
		this.type = ErrorType.MISSING_VALUE;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public String getAttributeValue() {
		return attributeValue;
	}

	public void setAttributeValue(String attributeValue) {
		this.attributeValue = attributeValue;
	}
	
	

}
