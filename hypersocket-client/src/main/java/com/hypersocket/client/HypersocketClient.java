/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.client;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypersocket.client.i18n.I18N;
import com.hypersocket.json.JsonPrincipal;

public abstract class HypersocketClient<T> {

	static Logger log = LoggerFactory.getLogger(HypersocketClient.class);

	String sessionId = null;
	
	HypersocketClientTransport transport;
	Map<String, String> staticHeaders = new HashMap<String, String>();
	SessionKeepAliveThread keepAliveThread = null;
	long keepAliveInterval = 10000L;
	Locale currentLocale;
	Set<HypersocketClientListener<T>> listeners = new HashSet<HypersocketClientListener<T>>();
	T attachment;
	
	boolean userDisconnect = false;
	boolean isDisconnecting = false;

	String principalName;
	
	String cachedUsername;
	String cachedPassword;
	
	protected HypersocketClient(HypersocketClientTransport transport, Locale currentLocale)
			throws IOException {
		this.transport = transport;
		this.currentLocale = currentLocale;
		try {
			I18N.initialize(null, currentLocale);
		} catch (Exception e) {

		}
	}
	
	protected HypersocketClient(HypersocketClientTransport transport,
			Locale currentLocale, HypersocketClientListener<T> listener)
			throws IOException {
		this(transport, currentLocale);
		listeners.add(listener);
	}
	
	public T getAttachment() {
		return attachment;
	}
	
	public void setAttachment(T attachment) {
		this.attachment = attachment;
	}
	
	public void addListener(HypersocketClientListener<T> listener) {
		listeners.add(listener);
	}
	
	public void changeLocale(Locale locale) {
		currentLocale = locale;

		if (transport.isConnected()) {
			try {
				loadResources();
			} catch (IOException e) {
				log.error("Failed to load resources", e);
			}

		} else {
			I18N.initialize(null, currentLocale);
		}
	}

	public String getHost() {
		return transport.getHost();
	}

	public int getPort() {
		return transport.getPort();
	}

	public void connect(String hostname, int port, String path, Locale locale)
			throws IOException, UnknownHostException {

		this.userDisconnect = false;
		this.isDisconnecting = false;
		this.currentLocale = locale;

		try {
			for(HypersocketClientListener<T> l : listeners) {
				try {
					l.connectStarted(this);
				} catch (Throwable t) {
				}
			}
			
			transport.connect(hostname, port, path);
	
			loadResources();
		
		} catch(IOException ex) {
			
			transport.disconnect(true);
			
			for(HypersocketClientListener<T> l : listeners) {
				try {
					l.connectFailed(ex, this);
				} catch (Throwable t) {
				}
			}
			throw ex;
		}
	}

	protected void loadResources() throws IOException {

		String resources = transport.get("i18n/" + currentLocale.getLanguage());

		JSONParser parser = new JSONParser();
		try {
			JSONObject jsonResources = (JSONObject) parser.parse(resources);
			I18N.initialize(jsonResources, currentLocale);
		} catch (ParseException e) {
			throw new IOException(e);
		}

	}

	public void disconnect(boolean onError) {

		isDisconnecting = true;
		
		if(!onError && isLoggedOn()) {
			
			userDisconnect = true;
			try {
				String json = transport.get("logoff");
				if (log.isDebugEnabled()) {
					log.debug(json);
				}
			} catch (IOException e) {
				if (log.isErrorEnabled()) {
					log.error("Error during logoff", e);
				}
			}
		}

		if (keepAliveThread != null) {
			keepAliveThread.interrupt();
		}

		if(transport.isConnected()) {
			onDisconnecting();
			
			transport.disconnect(onError);
	
			sessionId = null;
			keepAliveThread = null;
	
			for(HypersocketClientListener<T> l : listeners) {
				try {
					l.disconnected(this, onError);
				} catch (Throwable t) {
				}
			}
			try {
				onDisconnect();
			} catch (Throwable e) {
			}
		}
	}

	public void exit() {
		if (isLoggedOn()) {
			disconnect(false);
		}
		transport.shutdown();
	}

	protected abstract void onDisconnect();

	protected abstract void onDisconnecting();
	 
	public void loginHttp(String realm, String username, String password) throws IOException {
		loginHttp(realm, username, password, false);
	}
	
	public void loginHttp(String realm, String username, String password, boolean hashedPassword)
			throws IOException {

		Map<String, String> params = new HashMap<String, String>();
		Base64 base64 = new Base64();
		String authorization = "Basic "
				+ new String(
						base64.encode((username + (StringUtils.isBlank(realm) ? "" : "@" + realm) + ":" + password)
								.getBytes()));

		transport.setHeader("Authorization", authorization);

		String json = transport.post("logon", params);
		processLogon(json, params, new ArrayList<Prompt>());

		if (!isLoggedOn()) {
			disconnect(false);
			throw new IOException("Failed to perform http logon");
		}

		postLogin();

	}

	public void login() throws IOException, UserCancelledException {

		Map<String, String> params = new HashMap<String, String>();

		/**
		 * Reset authentication with client scheme
		 */
//		String json = transport.post("logon", params);
		
		int maxAttempts = 3;
		int attempts = maxAttempts;
		boolean attemptedCached = false;
		List<Prompt> prompts = new ArrayList<Prompt>();
		
		if(cachedUsername!=null && cachedPassword!=null) {
			params.put("username", cachedUsername);
			params.put("password", cachedPassword);
			attemptedCached = true;
		}
		
		while (!isLoggedOn()) {

			if(attempts==0) {
				disconnect(false);
				throw new IOException("Too many failed authentication attempts");
			}
			
			String json = transport.post("logon/hypersocketClient", params);
			
			params.clear();
			boolean success = processLogon(json, params, prompts);
			if(!success) {
				if(!attemptedCached) {
					attempts--;
				}
				attemptedCached = false;
			}
			if (!isLoggedOn() && prompts.size() > 0) {
				
				// If failed, and it's not the very first attempt (i.e. the one that triggers username and password entry), show an error
				if(!success) {
					showError("Incorrect username or password.");
				}
				
				Map<String, String> results  = showLogin(prompts, attempts, success);
				
				if (results != null) {

					params.putAll(results);
					
					if(params.containsKey("username")) {
						cachedUsername = params.get("username");
					}
					if(params.containsKey("password")) {
						cachedPassword = params.get("password");
					}
					
				} else {
					disconnect(false);
					throw new UserCancelledException("User has cancelled authentication");
				}
			}
		}
		
		

		if (log.isInfoEnabled()) {
			log.info("Logon complete sessionId=" + getSessionId());
		}

		postLogin();
		
		onConnected();
	}
	

	protected abstract void onConnected();
	
	private void postLogin() {
		if (keepAliveInterval > 0) {
			keepAliveThread = new SessionKeepAliveThread();
			keepAliveThread.start();
		}
		for(HypersocketClientListener<T> l : listeners) {
			try {
				l.connected(this);
			} catch (Throwable t) {
				log.error("Caught error in listener", t);
			}
		}
	}

	protected boolean processLogon(String json, Map<String, String> params, List<Prompt> prompts)
			throws IOException {

		try {
			return parseLogonJSON(json, params, prompts);
		} catch (ParseException e) {
			throw new IOException("Failed to parse logon request", e);
		}
	}


	public boolean isLoggedOn() {
		return sessionId != null && transport.isConnected();
	}

	public String getSessionId() {
		return sessionId;
	}

	public List<JsonPrincipal> getGroups() throws IOException {
		return parsePrincipals(transport.get("groups"));
	}

	public List<JsonPrincipal> getUsers() throws IOException {
		return parsePrincipals(transport.get("users"));
	}

	protected List<JsonPrincipal> parsePrincipals(String json)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		PrincipalResourcesWrapper wrapper = mapper.readValue(json,
				PrincipalResourcesWrapper.class);
		return wrapper.getResources();
	}

	protected boolean parseLogonJSON(String json,
			Map<String, String> params, List<Prompt> prompts) throws ParseException, IOException {

		JSONParser parser = new JSONParser();
		prompts.clear();

		JSONObject result = (JSONObject) parser.parse(json);
		if (!(Boolean) result.get("success")) {
			JSONObject template = (JSONObject) result.get("formTemplate");
			JSONArray fields = (JSONArray) template.get("inputFields");
			Boolean lastResultSuccessfull = (Boolean) result.get("lastResultSuccessfull");
			@SuppressWarnings("unchecked")
			Iterator<JSONObject> it = (Iterator<JSONObject>) fields.iterator();
			while (it.hasNext()) {
				JSONObject field = it.next();

				PromptType type = PromptType.TEXT;
				try {
					type = PromptType.valueOf(field.get("type").toString().toUpperCase());
				}
				catch(IllegalArgumentException iae) {
					log.warn("Unknown prompt type, default to text.", iae);
				}
						

				switch (type) {
				case HIDDEN: {
					params.put((String) field.get("resourceKey"),
							(String) field.get("defaultValue"));
					break;
				}
				case SELECT: {
					Prompt p = new Prompt(type,
							(String) field.get("resourceKey"),
							(String) field.get("defaultValue"));
					JSONArray options = (JSONArray) field.get("options");
					@SuppressWarnings("unchecked")
					Iterator<JSONObject> it2 = (Iterator<JSONObject>) options
							.iterator();
					while (it2.hasNext()) {
						JSONObject o = it2.next();
						Boolean isResourceKey = (Boolean) o.get("isNameResourceKey");
						p.addOption(new Option(isResourceKey ? I18N.getResource((String) o.get("name")) : (String) o.get("name"),
								(String) o.get("value"), (Boolean) o
										.get("selected")));
					}
					prompts.add(p);
					break;
				}
				default: {
					prompts.add(new Prompt(type, (String) field
							.get("resourceKey"), (String) field
							.get("defaultValue")));
					break;
				}
				}

			}

			return lastResultSuccessfull == null ? true : lastResultSuccessfull;
		} else {
			JSONObject session = (JSONObject) result.get("session");
			sessionId = (String) session.get("id");
			principalName = (String) ((JSONObject)session.get("currentPrincipal")).get("principalName");
			return true;
		}
	}

	public String getPrincipalName() {
		return principalName;
	}
	
	public String getRealm(String name) throws IOException {

		return transport.get("realm/" + name);
	}
	
	protected abstract Map<String, String> showLogin(List<Prompt> prompts, int attempt, boolean success) throws IOException;
	
	public abstract void showWarning(String msg);

	public abstract void showError(String msg);

	class SessionKeepAliveThread extends Thread {

		public void run() {

			while (sessionId != null) {

				try {
					Thread.sleep(keepAliveInterval);
				} catch (InterruptedException e) {
				}

				if(!userDisconnect && !isDisconnecting) {
					try {
						transport.get("session/touch");
					} catch (IOException e) {
						if(!userDisconnect && !isDisconnecting) {
							disconnect(true);
						}
					}
				}
			}
		}
	}

	public HypersocketClientTransport getTransport() {
		return transport;
	}

	public Map<String,String> getUserVariables() throws IOException {
		
		String json = transport.get("currentRealm/user/allVariables");
		
		ObjectMapper mapper = new ObjectMapper();
		
		VariableResult result = null;
		result = mapper.readValue(json, VariableResult.class);
		
		return result.getResource();
	}
	
	public String processReplacements(String value, Map<String,String> replacements) {
		for(String key : replacements.keySet()) {
			String variable = "${" + key + "}";
			value = value.replace(variable, replacements.get(key));
		}
		return value;
	}

}
