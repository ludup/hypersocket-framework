package com.hypersocket.util;

import java.util.List;

import com.hypersocket.realm.Realm;
import com.hypersocket.tables.ColumnSort;

public abstract class PrincipalIterator<T> extends PagedIterator<T> {

	protected static final int ITERATOR_PAGE_SIZE = Integer.parseInt(System.getProperty("hypersocket.principalIteratorSize", "1024"));
	/**
	 * 
	 */
	private final Realm realm;

	public PrincipalIterator(ColumnSort[] sorting, Realm realm) {
		super(sorting, ITERATOR_PAGE_SIZE);
		this.realm = realm;
	}

	@Override
	protected final List<T> listItems(int start, int pageSize, ColumnSort[] sorting) {
		return listUsers(realm, start, pageSize, sorting);
	}

	protected abstract List<T> listUsers(Realm realm, int start, int pageSize, ColumnSort[] sorting);
}