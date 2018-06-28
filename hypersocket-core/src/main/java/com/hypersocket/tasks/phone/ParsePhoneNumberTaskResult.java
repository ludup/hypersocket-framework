package com.hypersocket.tasks.phone;

import org.apache.commons.lang3.ArrayUtils;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;
import com.hypersocket.triggers.AbstractTaskResult;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public class ParsePhoneNumberTaskResult extends AbstractTaskResult {

	private static final long serialVersionUID = 1L;
	
	public static final String EVENT_RESOURCE_KEY = "parsePhoneNumber.result";
	
	public ParsePhoneNumberTaskResult(Object source, 
			boolean success, Realm currentRealm, Task task, PhoneNumber parse) {
		super(source, EVENT_RESOURCE_KEY, success, currentRealm, task);
		addAttribute("result.phoneCountryCode", parse.getCountryCode());
		addAttribute("result.phoneE164", PhoneNumberUtil.getInstance().format(parse, PhoneNumberFormat.E164));
		addAttribute("result.phoneInternational", PhoneNumberUtil.getInstance().format(parse, PhoneNumberFormat.INTERNATIONAL));
		addAttribute("result.phoneNational", PhoneNumberUtil.getInstance().format(parse, PhoneNumberFormat.NATIONAL));
		addAttribute("result.phoneRFC3966", PhoneNumberUtil.getInstance().format(parse, PhoneNumberFormat.RFC3966));
	}

	public ParsePhoneNumberTaskResult(Object source, Throwable e,
			Realm currentRealm, Task task) {
		super(source, EVENT_RESOURCE_KEY, e, currentRealm, task);
	}


	@Override
	public boolean isPublishable() {
		return true;
	}

	@Override
	public String getResourceBundle() {
		return ParsePhoneNumberTask.RESOURCE_BUNDLE;
	}
	
	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}

}
