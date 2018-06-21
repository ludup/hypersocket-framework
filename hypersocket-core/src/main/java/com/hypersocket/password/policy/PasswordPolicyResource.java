package com.hypersocket.password.policy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.hypersocket.resource.AssignableResource;

@Entity
@Table(name="password_policies")
public class PasswordPolicyResource extends AssignableResource {

	private static final long serialVersionUID = 1194762176398433781L;

	@Column(name="min_length")
	Integer minimumLength;
	
	@Column(name="max_length")
	Integer maximumLength;
	
	@Column(name="contain_username")
	Boolean containUsername;
	
	@Column(name="contain_dictionary")
	Boolean containDictionaryWord;
	
	@Column(name="priority")
	Integer priority;
	
	@Column(name="min_criteria")
	Integer minimumCriteriaMatches;
	
	@Column(name="min_digits")
	Integer minimumDigits;
	
	@Column(name="min_loewer")
	Integer minimumLower;
	
	@Column(name="min_upper")
	Integer minimumUpper;
	
	@Column(name="min_symbol")
	Integer minimumSymbol;

	@Column(name="allowed_symbols")
	String validSymbols;
	
	@Column(name="password_history")
	Integer passwordHistory;
	
	@Column(name="min_age")
	Integer minimumAge;
	
	@Column(name="max_age")
	Integer maximumAge;
	
	@Column(name="hash_value")
	Integer hashValue;
	
	@Column(name="dn", length=1024)
	String dn;
	
	@Column(name="provider")
	String provider;
	
	@Column(name="allow_edit")
	Boolean allowEdit;
	
	@Column(name="default_policy")
	Boolean defaultPolicy;
	
	@Column(name="additional_analysis", nullable = false, columnDefinition = "bit default TRUE")
	Boolean additionalAnalysis;
	
	public Integer getMinimumLength() {
		return minimumLength;
	}

	public void setMinimumLength(Integer minimumLength) {
		this.minimumLength = minimumLength;
	}

	public Integer getMaximumLength() {
		return maximumLength;
	}

	public void setMaximumLength(Integer maximumLength) {
		this.maximumLength = maximumLength;
	}

	public Boolean getContainUsername() {
		return containUsername;
	}

	public void setContainUsername(Boolean containUsername) {
		this.containUsername = containUsername;
	}

	public Boolean getContainDictionaryWord() {
		return containDictionaryWord;
	}

	public void setContainDictionaryWord(Boolean containDictionaryWord) {
		this.containDictionaryWord = containDictionaryWord;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public Integer getMinimumCriteriaMatches() {
		return minimumCriteriaMatches;
	}

	public void setMinimumCriteriaMatches(Integer minimumCriteriaMatches) {
		this.minimumCriteriaMatches = minimumCriteriaMatches;
	}

	public Integer getMinimumDigits() {
		return minimumDigits;
	}

	public void setMinimumDigits(Integer minimumDigits) {
		this.minimumDigits = minimumDigits;
	}

	public Integer getMinimumLower() {
		return minimumLower;
	}

	public void setMinimumLower(Integer minimumLower) {
		this.minimumLower = minimumLower;
	}

	public Integer getMinimumUpper() {
		return minimumUpper;
	}

	public void setMinimumUpper(Integer minimumUpper) {
		this.minimumUpper = minimumUpper;
	}

	public Integer getMinimumSymbol() {
		return minimumSymbol;
	}

	public void setMinimumSymbol(Integer minimumSymbol) {
		this.minimumSymbol = minimumSymbol;
	}

	public String getValidSymbols() {
		return validSymbols;
	}

	public void setValidSymbols(String allowedSymbols) {
		this.validSymbols = allowedSymbols;
	}

	public Integer getPasswordHistory() {
		return passwordHistory;
	}

	public void setPasswordHistory(Integer passwordHistory) {
		this.passwordHistory = passwordHistory;
	}

	public Integer getMinimumAge() {
		return minimumAge;
	}

	public void setMinimumAge(Integer minimumAge) {
		this.minimumAge = minimumAge;
	}

	public Integer getMaximumAge() {
		return maximumAge;
	}

	public void setMaximumAge(Integer maximumAge) {
		this.maximumAge = maximumAge;
	}

	public float getVeryStrongFactor() {
		return 2;
	}

	public void setHashValue(Integer hashValue) {
		this.hashValue = hashValue;
	}
	
	public Integer getHashValue() {
		return hashValue==null ? 0 : hashValue;
	}

	public String getDN() {
		return dn;
	}
	
	public void setDN(String dn) {
		this.dn = dn;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public Boolean getAllowEdit() {
		return allowEdit == null ? !isSystem() : allowEdit;
	}

	public void setAllowEdit(Boolean allowEdit) {
		this.allowEdit = allowEdit;
	}

	public Boolean getDefaultPolicy() {
		return defaultPolicy==null ? false : defaultPolicy;
	}

	public void setDefaultPolicy(Boolean defaultPolicy) {
		this.defaultPolicy = defaultPolicy;
	}

	public Boolean getAdditionalAnalysis() {
		return additionalAnalysis;
	}

	public void setAdditionalAnalysis(Boolean additionalAnalysis) {
		this.additionalAnalysis = additionalAnalysis;
	}
	
	
}
