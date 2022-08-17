package com.hypersocket.telemetry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.ResourceException;

@Service
public class TelemetryServiceImpl implements TelemetryService {

	private List<TelemetryProducer> producers = new ArrayList<>();

	@Autowired
	private RealmService realmService;

	public TelemetryServiceImpl() {
	}

	@Override
	public void registerProducer(TelemetryProducer producer) {
		producers.add(producer);
	}

	@Override
	public void deregisterProducer(TelemetryProducer producer) {
		producers.add(producer);
	}

	@Override
	public String collect() throws AccessDeniedException, ResourceException {
		Gson gson = new Gson();

		/* Root obj */
		JsonObject o = new JsonObject();
		for (TelemetryProducer producer : producers) {
			producer.fill(o);
		}

		/* Realms */
		Map<PrincipalType, Long> realmTotals = new HashMap<>();
		JsonArray realmsObj = new JsonArray();
		for (Realm realm : realmService.allRealms()) {
			JsonObject realmObj = new JsonObject();
			realmObj.addProperty("name", realm.getName());
			RealmProvider provider = realmService.getProviderForRealm(realm);
			realmObj.addProperty("module", provider.getModule());
			for(PrincipalType ptype : PrincipalType.values()) {
				long l = realmService.getPrincipalCount(realm, ptype);
				long t = l;
				if(realmTotals.containsKey(ptype)) {
					t += realmTotals.get(ptype);
				}
				realmTotals.put(ptype, t);
				realmObj.addProperty(ptype.name().toLowerCase() + "s", l);
			}
			for (TelemetryProducer producer : producers) {
				producer.fillRealm(realm, realmObj);
			}
			realmsObj.add(realmObj);
		}
		o.add("realms", realmsObj);
		o.addProperty("realmsTotal", realmsObj.size());
		for(Map.Entry<PrincipalType,Long> en : realmTotals.entrySet()) {
			o.addProperty(en.getKey().name().toLowerCase() + "sTotal", en.getValue());
		}

		return gson.toJson(o);
	}

	@PostConstruct
	private void setup() {

	}

}
