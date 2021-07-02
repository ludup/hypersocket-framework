package com.hypersocket.password.policy;

public class PasswordPolicyException extends Exception {
    
    private static final long serialVersionUID = 5936231273264738494L;

    public enum Type {
        tooShort, tooLong, notEnoughLowerCase, notEnoughUpperCase, notEnoughSymbols, notEnoughDigits, containsDictionaryWords, containsUsername, doesNotMatchComplexity,
    }
    
    private Type type;
    private float strength;

    public PasswordPolicyException(Type type, float strength) {
        super();
        this.type = type;
        this.strength = strength;
    }
    
    public PasswordPolicyException(Type type, float strength, String message) {
        super(message);
        this.type = type;
        this.strength = strength;
    }
    
    public Type getType() {
        return type;
    }
    
    public float getStrength() {
        return strength;
    }

}
