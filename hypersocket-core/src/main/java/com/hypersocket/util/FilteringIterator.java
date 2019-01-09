package com.hypersocket.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class FilteringIterator<I> extends ProxiedIterator<I> {

	private Iterator<I> source;
	private Set<I> include = new HashSet<>();
	private Set<I> exclude = new HashSet<>();

	public FilteringIterator(Iterator<I> source) {
		this.source = source;
	}

	public FilteringIterator<I> include(I item) {
		include.add(item);
		return this;
	}

	public FilteringIterator<I> exclude(I item) {
		exclude.add(item);
		return this;
	}

	protected boolean isInclude(I item) {
		return (include.contains(item) || include.isEmpty()) && !exclude.contains(item);
	}

	@Override
	protected I checkNext(I item) {
		if (item == null) {
			while(source.hasNext()) {
				I i = source.next();
				if(isInclude(i)) {
					item = i;
					break;
				}
			}
		}
		return item;
	}

}
