package com.hypersocket.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SingleItemIterator<I> implements Iterator<I> {

	private I item;

	public SingleItemIterator(I item) {
		this.item = item;
	}

	@Override
	public boolean hasNext() {
		return item != null;
	}

	@Override
	public I next() {
		if (item == null)
			throw new NoSuchElementException();
		try {
			return item;
		} finally {
			item = null;
		}
	}

}
