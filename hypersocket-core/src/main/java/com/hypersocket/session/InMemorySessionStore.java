package com.hypersocket.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.base.Objects;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.Sort;

public class InMemorySessionStore implements SessionStore {

	private Map<String, Session> sessions =  Collections.synchronizedMap(new HashMap<>());
	private SessionRepository repository;
	
	public InMemorySessionStore(SessionRepository repository) {
		this.repository = repository;
	}

	@Override
	public Session getSessionById(String id) {
		return sessions.get(id);
	}

	@Override
	public List<Session> getActiveSessions() {
		return Collections.unmodifiableList(new ArrayList<>(sessions.values()));
	}

	@Override
	public List<Session> getPrincipalActiveSessions(Principal principal) {
		return getActiveSessions().stream().filter(s -> principal.equals(s.getPrincipal())).collect(Collectors.toList());
	}

	@Override
	public List<Session> getSystemSessions() {
		return getActiveSessions().stream().filter(s -> s.isSystem() && s.getSignedOut() == null).collect(Collectors.toList());
	}

	@Override
	public Long getActiveSessionCount() {
		return (long)sessions.size();
	}

	@Override
	public Long getActiveSessionCount(boolean distinctUsers) {
		if(distinctUsers)
			return (long)getActiveSessions().stream().collect(Collectors.groupingBy(Session::getPrincipal, Collectors.counting())).size();
		else
			return getActiveSessionCount();
	}

	@Override
	public Long getActiveSessionCount(boolean distinctUsers, Realm realm) {
		if(distinctUsers)
			return (long)getActiveSessions().stream().filter(s -> realm.equals(s.getCurrentRealm())).collect(Collectors.groupingBy(Session::getPrincipal, Collectors.counting())).size();
		else
			return (long)getActiveSessions().stream().filter(s -> realm.equals(s.getCurrentRealm())).collect(Collectors.toList()).size();
	}

	@Override
	public void deleteRealm(Realm realm) {
		synchronized(sessions) {
			for(var k : getActiveSessions().stream().filter(s -> realm.equals(s.getCurrentRealm())).map(s -> s.getId()).collect(Collectors.toList())) {
				sessions.remove(k);
			}
		}
	}

	@Override
	public void saveSession(Session session) {
		if(session.getId() == null) {
			repository.saveEntity(session);
			sessions.put(session.getId(), session);
		}
		if(session.getSignedOut() != null) {
			sessions.remove(session.getId());
			repository.saveEntity(session);
		}
		else {
			sessions.put(session.getId(), session);
		}
	}

	@Override
	public List<Session> search(Realm realm, String searchPattern, int start, int length, ColumnSort[] sorting,
			CriteriaConfiguration... configs) {
		var l = new ArrayList<Session>();
		for (var r : getActiveSessions(realm)) {
			if (matches(r, searchPattern))
				l.add(r);
		}
		Collections.sort(l, new Comparator<Session>() {

			@SuppressWarnings("unchecked")
			@Override
			public int compare(Session o1, Session o2) {
				for (ColumnSort s : sorting) {
					int i = 0;
					Comparable<?> v1 = o1.getId();
					Comparable<?> v2 = o2.getId();
					if (s.getColumn() == SessionColumns.CREATEDATE) {
						v1 = o1.getCreateDate();
						v2 = o2.getCreateDate();
					}
					if (v1 == null && v2 != null)
						i = -1;
					else if (v2 == null && v1 != null)
						i = 1;
					else if (v2 != null && v1 != null) {
						i = (((Comparable<Object>) v1).compareTo((Comparable<Object>) v2));
					}
					if (i != 0) {
						return s.getSort() == Sort.ASC ? i * -1 : i;
					}
				}
				return 0;
			}
		});
		return l.subList(Math.min(l.size(), start), Math.min(l.size(), start + length));
	}

	@Override
	public long getResourceCount(Realm realm, String searchPattern, CriteriaConfiguration... configs) {
		long v = 0;
		for (var r : getActiveSessions(realm)) {
			if (matches(r, searchPattern))
				v++;
		}
		return v;
	}
	
	private List<Session> getActiveSessions(Realm realm) {
		return getActiveSessions().stream().filter(s -> Objects.equal(realm, s.getCurrentRealm())).collect(Collectors.toList());
	}

	private boolean matches(Session r, String searchPattern) {
		return !r.isSystem() && r.getSignedOut() == null && r.getPrincipal() != null && r.getPrincipal().getName().toLowerCase().contains(searchPattern.toLowerCase());
	}

	@Override
	public void updateRealmSessions(Realm realm) {
		synchronized(sessions) {
			for(var s : getActiveSessions().stream().filter(s -> ( s.getPrincipalRealm() != null && realm.getId().equals(s.getPrincipalRealm().getId())) || ( s.getCurrentRealm() != null && realm.getId().equals(s.getCurrentRealm().getId()))).collect(Collectors.toList())) {
				if(s.getPrincipalRealm() != null && s.getPrincipalRealm().getId().equals(realm.getId())) {
					s.setPrincipalRealm(realm);	
				}
				if(s.getCurrentRealm() != null && s.getCurrentRealm().getId().equals(realm.getId())) {
					s.setCurrentRealm(realm);
				}
			}
		}
	}

	@Override
	public void updatePrincipalSessions(Principal principal) {
		synchronized(sessions) {
			for(var s : getActiveSessions().stream().filter(s -> s.getPrincipal() != null && principal.getId().equals(s.getPrincipal().getId())).collect(Collectors.toList())) {
				s.setPrincipal(principal);
			}
		}
	}
}
