package com.hypersocket.tasks.count;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.realm.Realm;
import com.hypersocket.resource.ResourceException;

@Service
public class CountServiceImpl implements CountService {


	@Autowired
	private CountKeyRepository repository;
	
	@Override
	public void adjustCount(Realm realm, String resourceKey, Long amount) throws ResourceException {
		
		CountKey key = repository.getCountKey(realm, resourceKey);
		key.add(amount);
		
		repository.saveResource(key);
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Long sum(Realm realm, boolean reset, String... resourceKeys) throws ResourceException {
		
		Collection<CountKey> values = repository.getCountValues(realm, resourceKeys);
		
		Long result = 0L;
		
		for(CountKey value : values) {
			result += value.getCount();
		}
		
		if(reset) {
			repository.deleteResources(values);
		}
		
		return result;
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public void resetKeys(Realm realm, Collection<String> keys) throws ResourceException {
		
		repository.deleteResources(repository.getCountValues(realm, keys.toArray(new String[0])));
		
	}
}
