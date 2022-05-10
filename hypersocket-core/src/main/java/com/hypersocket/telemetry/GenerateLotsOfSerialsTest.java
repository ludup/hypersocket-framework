package com.hypersocket.telemetry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hypersocket.HypersocketVersion;
import com.hypersocket.extensions.ExtensionHelper;
import com.hypersocket.extensions.ExtensionPlace;
import com.hypersocket.extensions.ExtensionTarget;
import com.hypersocket.extensions.ExtensionVersion;
import com.hypersocket.extensions.PropertyCallback;
import com.hypersocket.utils.FileUtils;

public class GenerateLotsOfSerialsTest {

	final static long MB = 1024 * 1024;

	public final static String[] PRODUCT_NAMES = { "LogonBox SSPR", "LogonBox VPN", "LogonBox Directory" };
	public final static String[] VERSIONS = { "2.3.14", "2.3.13", "2.3.12", "2.3.11", "2.3.10", "2.3.9", "2.3.8",
			"2.3.7", "2.4.0", "2.3.6", "2.2.10", "2.3.5", "2.2.9", "2.2.8", "2.3.4", "2.2.7", "2.3.3", "2.3.2", "2.3.1",
			"2.2.6", "2.3.0", "2.2.5", "2.2.4", "2.2.3", "2.2.2", "2.2.1", "2.2.0" };
	public final static TimeZone[] TIME_ZONES = { TimeZone.getTimeZone("Etc/GMT"), TimeZone.getTimeZone("PST"),
			TimeZone.getTimeZone("CET") };
	public final static Locale[] LOCALES = { Locale.US, Locale.UK, Locale.FRANCE, Locale.ITALY, Locale.GERMAN };
	public final static String[] OS = { "Linux", "Windows", "Linux", "Windows", "Linux", "Windows", "Mac OS" };
	public final static String[] OS_VERSIONS = { "1.0", "1.1", "1.5", "2.0", "2.1", "3", "4", "5" };
	public final static String[] INITIAL_AUTHENTICATION_MODULES = { "usernameAndPassword", "2faAuthenticationFlow", "username" };
	public final static String[] TRAILING_AUTHENTICATION_MODULES = { "securityQuestions", "pin", "sms", "otp", "2faAuthenticationFlow", "authy", "msauthenticator" };
	public final static String[] VM_PLATFORM_VERSION = { "bullseye/sid", "bullseye/stable", "buster/stable", "stretch/stable", "jessie/stable", "wheezy/stable" };
	public final static String[] ARCH = { "amd64", "amd64", "amd64", "amd64", "amd64", "amd64", "aarch64" };
	public final static String[] PHASES= { "stable2_3_x", "ea_2_3_x", "stable2_2_x", "ea_2_2_x", "testing_2_3_x", "beta2_4_x" };
	public final static String[] LICENSE_TYPES = { "Enterprise Subscription (On-Premise)",
			"Enterprise Subscription (On-Premise)", "Enterprise Subscription (On-Premise)", "Enterprise Subscription",
			"Evaluation License", "Professional License", "Foundation Subscription" };
	public final static String[] REPOS = { "hypersocket-core", "hypersocket-enterprise" };
	public final static String[] EXTENSIONS = {
	"server-core",
	"x-hypersocket-account-unlock",
	"x-hypersocket-advanced-tasks",
	"x-hypersocket-audit",
	"x-hypersocket-auth",
	"x-hypersocket-auth-time",
	"x-hypersocket-brand",
	"x-hypersocket-captcha",
	"x-hypersocket-create-account",
	"x-hypersocket-duo",
	"x-hypersocket-enhanced-security",
	"x-hypersocket-google-authenticator",
	"x-hypersocket-idm",
	"x-hypersocket-ip",
	"x-hypersocket-multi-tenancy",
	"x-hypersocket-otp",
	"x-hypersocket-password-reset",
	"x-hypersocket-perfmon",
	"x-hypersocket-personal",
	"x-hypersocket-pin",
	"x-hypersocket-radius",
	"x-hypersocket-saml",
	"x-hypersocket-scripting",
	"x-hypersocket-secure-node",
	"x-hypersocket-syslog",
	"x-hypersocket-tasks-aws",
	"x-hypersocket-tasks-csv",
	"x-hypersocket-tasks-http",
	"x-hypersocket-tasks-xml",
	"x-hypersocket-translate",
	"x-hypersocket-userlogo",
	"x-hypersocket-webhooks",
	"x-hypersocket-yubico",
	"x-identity4j-activedirectory",
	"x-identity4j-azure",
	"x-identity4j-google",
	"x-identity4j-ldap",
	"x-identity4j-logonbox-directory",
	"x-identity4j-ssh",
	"x-logonbox-authenticator",
	"x-logonbox-support-callback",
	"x-nervepoint-sso-saml" };


	@SuppressWarnings("unchecked")
	static <O> O pickOne(O... vals) {
		while (true) {
			for (int i = 0; i < vals.length; i++) {
				if (Math.random() >  0.5f) {
					return vals[i];
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		for (int i = 0; i < 10000; i++) {
			if (i % 1000 == 0)
				System.out.println(i);

			JsonObject obj = new JsonObject();
			obj.addProperty("cores", pickOne(4, 2, 8, 16, 1, 32, 64));
			obj.addProperty("lastAdminSignOn", TelemetryProducer.formatAsUTC(
					new Date(new Date().getTime() - (long) (Math.random() * (double) (60000 * 60 * 24 * 33)))));
			TimeZone tz = pickOne(TIME_ZONES);
			obj.addProperty("timeZone", tz.getID());
			obj.addProperty("timeZoneName", tz.getDisplayName());
			obj.addProperty("locale", pickOne(LOCALES).getCountry());
			obj.addProperty("os", pickOne(OS));
			obj.addProperty("osVersion", pickOne(OS_VERSIONS));
			obj.addProperty("vmPlatformVersion", pickOne(VM_PLATFORM_VERSION));
			obj.addProperty("arch", pickOne(ARCH));
			obj.addProperty("memory", pickOne(ARCH));
			long mem = (300l * MB) + (long) (Math.random() * (double) (3 * 1024l * MB));
			obj.addProperty("memory", mem);
			obj.addProperty("maxMemory", (long) ((double) mem * (double) (2 + (Math.random() * 2.0))));

			Set<String> allModsSet = new HashSet<>();

			long usersTotal = 0;
			long groupsTotal = 0;
			long servicesTotal = 0;
			long systemsTotal = 0;
			long templatesTotal = 0;
			long profilesTotal = 0;

			int r = pickOne(1, 1, 1, 1, 2, 2, 2, 2, 2, 3 + (int) (Math.random() * 5), 3 + (int) (Math.random() * 5),
					3 + (int) (Math.random() * 5), 3 + (int) (Math.random() * 5), 3 + (int) (Math.random() * 5));
			obj.addProperty("realmsTotal", r);
			JsonArray realmsObj = new JsonArray();
			for (int ri = 0; ri < r; ri++) {
				JsonObject robj = new JsonObject();
				robj.addProperty("name", "Realm " + i + ":" + ri);
				if (ri == 0)
					robj.addProperty("module", "local");
				else
					robj.addProperty("module", pickOne("identity4j-ads", "identity4j-ads", "local", "identity4j-ads",
							"identity4j-ldap", "identity4j-mysql"));
				realmsObj.add(robj);
				long users = 5 + (long) (Math.random() * 10000);
				long groups = 1 + (long) (Math.random() * 1000);
				long services = 0 + (long) (Math.random() * 10);
				long systems = 0 + (long) (Math.random() * 10);
				long templates = 0 + (long) (Math.random() * 5);
				long profiles = (long) ((double) users * Math.random());
				obj.addProperty("users", users);
				obj.addProperty("groups", groups);
				obj.addProperty("services", services);
				obj.addProperty("systems", systems);
				obj.addProperty("templates", templates);
				obj.addProperty("profiles", profiles);
				usersTotal += users;
				groupsTotal += groups;
				servicesTotal += services;
				systemsTotal += systems;
				templatesTotal += templates;
				profilesTotal += profiles;

				Date licenseExpires = new Date(new Date().getTime()
						+ (long) (Math.random() * (double) (60000 * 60 * 24 * 128)) - (60000 * 60 * 24 * 48));
				int status = 4;
				if (licenseExpires.after(new Date())) {
					status = 1;
				} else {
					if (Math.random() < 0.05)
						status = 16;
					else if (Math.random() < 0.05)
						status = 2;
					else if (Math.random() < 0.05)
						status = 8;
				}
				String desc = pickOne(LICENSE_TYPES);
				if (ri == 0) {
					obj.addProperty("licenseExpires", TelemetryProducer.formatAsUTC(licenseExpires));
					obj.addProperty("licenseStatus", status);
					obj.addProperty("licenseDescription", desc);
				}
				robj.addProperty("licenseExpires", TelemetryProducer.formatAsUTC(licenseExpires));
				robj.addProperty("licenseStatus", status);
				robj.addProperty("licenseDescription", desc);
				
//				JsonArray schemesObj = new JsonArray();
//				for(AuthenticationScheme scheme : schemeRepository.allEnabledSchemes(realm)) {
//					JsonObject schemeObj = new JsonObject();
//					schemeObj.addProperty("name", scheme.getName());
//					schemeObj.addProperty("modules", String.join(",",  scheme.getModules().stream().map(s -> s.getTemplate()).collect(Collectors.toList())));
//					schemesObj.add(schemeObj);
//				}
//				data.add("authenticationSchemes", schemesObj);
//				data.addProperty("authenticationSchemesTotal", schemesObj.size());
				
				JsonArray authSchemesArray = new JsonArray();
				int sc = Math.max(1, (int)(Math.random() * 4.0f));
				for(int si = 0 ; si < sc; si++) {
					JsonObject schemeObj = new JsonObject();
					schemeObj.addProperty("name", "Scheme " + i + ":" + ri + ":" + si);
					List<String> modules = new ArrayList<>();
					modules.add(pickOne(INITIAL_AUTHENTICATION_MODULES));

					int ssc = (int)(Math.random() * 4.0f);
					Set<String> available = new LinkedHashSet<>(Arrays.asList(TRAILING_AUTHENTICATION_MODULES));
					for(int ssi = 0 ; ssi < ssc; ssi++) {
						String picked = pickOne(available.toArray(new String[0]));
						available.remove(picked);
						modules.add(picked);
					}
					allModsSet.addAll(modules);
					JsonArray sarr = new JsonArray();
					for(String m : modules) {
						sarr.add(m);
					}
					schemeObj.add("modules", sarr);
					authSchemesArray.add(schemeObj);
				}
				
				robj.add("authenticationSchemes", authSchemesArray);
				robj.addProperty("authenticationSchemesTotal", authSchemesArray.size());
				
			}

			JsonArray sarr = new JsonArray();
			for(String m : allModsSet) {
				sarr.add(m);
			}
			obj.add("allModules", sarr);
			
			obj.add("realms", realmsObj);
			obj.addProperty("usersTotal", usersTotal);
			obj.addProperty("groupsTotal", groupsTotal);
			obj.addProperty("servicesTotal", servicesTotal);
			obj.addProperty("systemsTotal", systemsTotal);
			obj.addProperty("templatesTotal", templatesTotal);
			obj.addProperty("profilesTotal", profilesTotal);
			

			obj.addProperty("updatesDevelopmentMode", Math.random() > 0.95);
			obj.addProperty("updatesBetaMode", Math.random() > 0.95);
			obj.addProperty("updatesActivePhase", pickOne(PHASES));
			JsonArray extObj = new JsonArray();
			Set<String> available = new HashSet<>(Arrays.asList(EXTENSIONS));
			int c = (int)(Math.random() * (float)EXTENSIONS.length);
			for(int xi = 0 ; xi < c ; xi++) {
				String xp = pickOne(available.toArray(new String[0]));
				available.remove(xp);
				extObj.add(xp);
			}
			obj.add("extensions", extObj);


			Map<String, String> params = new HashMap<>();
			params.put("product", pickOne(PRODUCT_NAMES));
			params.put("customer", "Org" + i);
			params.put("telemetry", obj.toString());
			
			Map<String, ExtensionVersion> extsByName = ExtensionHelper.resolveExtensions(true,
					FileUtils
							.checkEndsWithSlash(System.getProperty("hypersocket.archivesURL", "https://blue:8443/app/"))
							+ "api/store/repos2",
					REPOS, pickOne(VERSIONS), UUID.randomUUID().toString(), params,
					ExtensionPlace.getDefault(), true, new PropertyCallback() {
						
						@Override
						public void processRemoteProperties(Map<String, String> properties) {
							// TODO Auto-generated method stub
							
						}
					}, pickOne(ExtensionTarget.values()));

		}

	}

}
