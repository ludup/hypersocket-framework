package com.hypersocket.properties;

import com.hypersocket.triggers.ValidationException;

public interface PropertyValidator {
	 void validate(PropertyTemplate template, String value) throws ValidationException;
}
