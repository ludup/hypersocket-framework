package com.hypersocket.migration.execution;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.hypersocket.realm.Realm;

@Component
public class MigrationContext {

	public static final String EXPORT = "EXPORT";

	public static final String IMPORT = "IMPORT";

	public static final String PROCESS = "PROCESS";
	
	public static final String REALM = "REALM";
	
	ThreadLocal<Map<String, Object>> context = new ThreadLocal<>();
	
	public void init() {
		context.set(new HashMap<String, Object>());
	}
	
	public void initImport() {
		init();
		put(PROCESS, IMPORT);
	}
	
	public void initExport() {
		init();
		put(PROCESS, EXPORT);
	}
	
	public void addRealm(Realm realm) {
		put(REALM, realm);
    }

    public Realm getCurrentRealm() {
        return (Realm) get(REALM);
    }
	
	public void put(String key, Object object) {
		assertion();
		context.get().put(key, object);
	}
	
	public Object get(String key) {
		assertion();
		return context.get().get(key);
	}
	
	public void clearContext() {
		if(context != null && context.get() != null){
			context.get().clear();
			context.remove();
		}
		
		if(context != null){
			context.remove();
		}
		
	}
	
	private void assertion() {
        if(context == null || context.get() == null) {
            throw new IllegalStateException("Context is not initiallzed for use.");
        }
    }
}
