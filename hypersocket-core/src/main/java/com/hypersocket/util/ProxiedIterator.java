package com.hypersocket.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class ProxiedIterator<I> implements Iterator<I> {
	private I item;

	@Override
	public boolean hasNext() {
		item = checkNext(item);
		return item != null;
	}

	@Override
	public I next() {
		item = checkNext(item);
		if(item == null)
			throw new NoSuchElementException();
		try {
			return item;
		} finally {
			item = null;
		}
	}

	protected abstract I checkNext(I item);
}
