package com.hypersocket.util;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.hypersocket.tables.ColumnSort;

public abstract class PagedIterator<T> implements Iterator<T> {
	/**
	 * 
	 */
	private final ColumnSort[] sorting;
	private final int pageSize;
	private T next;
	private Iterator<T> it;
	private int start = 0;
	private int currentIndex = -1;
	private T current;
	private boolean removed;

	public PagedIterator() {
		this(getDefaultPageSize());
	}

	protected static int getDefaultPageSize() {
		return Integer.parseInt(System.getProperty("hypersocket.defaultPagedIteratorSize", "100"));
	}
	
	public PagedIterator(ColumnSort[] sorting) {
		this(sorting, getDefaultPageSize());
	}

	public PagedIterator(int pageSize) {
		this(new ColumnSort[0], pageSize);
	}

	public PagedIterator(ColumnSort[] sorting, int pageSize) {
		this.sorting = sorting;
		this.pageSize = pageSize;
	}
	
	public T getCurrent() {
		return current;
	}

	@Override
	public boolean hasNext() {
		checkNext();
		return next != null;
	}

	@Override
	public final void remove() {
		if (current == null)
			throw new IllegalStateException("No current element.");
		if (removed)
			throw new IllegalStateException("Already removed.");
		remove(current);
		start--;
		removed = true;
	}

	protected void remove(T principal) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T next() {
		try {
			checkNext();
			if (next == null)
				throw new NoSuchElementException();
			removed = false;
			return current = next;
		} finally {
			next = null;
		}
	}

	private void checkNext() {
		if (next == null) {
			if (it != null && !it.hasNext()) {
				it = null;
			}

			if (it == null) {
				List<T> l = listItems(start, pageSize, sorting);
				if (!l.isEmpty())
					it = l.iterator();
				currentIndex = start - 1;
				start += l.size();
			}

			if (it != null && it.hasNext()) {
				currentIndex++;
				next = it.next();
			}
		}
	}

	protected abstract List<T> listItems(int start, int pageSize, ColumnSort[] sorting);
}