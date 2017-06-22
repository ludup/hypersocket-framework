package com.hypersocket.migration.lookup;

import java.util.Arrays;

public class LookUpKey {

    private String property;
    private Object value;
    private boolean composite;
    private String[] properties;
    private Object[] values;
    private boolean legacyId;

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isComposite() {
        return composite;
    }

    public void setComposite(boolean composite) {
        this.composite = composite;
    }

    public String[] getProperties() {
        return properties;
    }

    public void setProperties(String[] properties) {
        this.properties = properties;
    }

    public Object[] getValues() {
        return values;
    }

    public void setValues(Object[] values) {
        this.values = values;
    }
    
    public boolean isLegacyId() {
		return legacyId;
	}

	public void setLegacyId(boolean legacyId) {
		this.legacyId = legacyId;
	}

	public boolean hasProperty(String property) {
    	if(composite) {
    		for (int i = 0; i < properties.length; i++) {
				if(this.properties[i].equals(property)) {
					return true;
				}
			}
    		return false;
    	} 
    	
    	return this.property.equals(property);
    }

    @Override
    public String toString() {
        return "LookUpKey{" +
                "property='" + property + '\'' +
                ", value=" + value +
                ", composite=" + composite +
                ", properties=" + Arrays.toString(properties) +
                ", values=" + Arrays.toString(values) +
                '}';
    }
}
