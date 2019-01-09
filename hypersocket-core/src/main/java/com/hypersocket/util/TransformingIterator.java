package com.hypersocket.util;

import java.util.Iterator;

public abstract class TransformingIterator<F, T> implements Iterator<T> {
	
	private Iterator<F> from;

	public TransformingIterator(Iterator<F> from) {
		this.from = from;
	}

	@Override
	public boolean hasNext() {
		return from.hasNext();
	}

	@Override
	public T next() {
		return transform(from.next());
	}
	
	protected abstract T transform(F from);

}
