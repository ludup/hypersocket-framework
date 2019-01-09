package com.hypersocket.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class EmptyIterator<I> implements Iterator<I> {
	
	@Override
	public boolean hasNext() {
		return false;
	}

	@Override
	public I next() {
		throw new NoSuchElementException();
	}

}
