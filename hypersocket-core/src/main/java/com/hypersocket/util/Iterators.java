package com.hypersocket.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Iterators {
	public static <K> Iterable<K> iterable(Iterator<K> iterator) {
		return new Iterable<K>() {
			@Override
			public Iterator<K> iterator() {
				return iterator;
			}
		};
	}
	
	public static <K> List<K> listFromIteration(Iterator<K> iterator) {
		List<K> l = new ArrayList<>();
		while(iterator.hasNext())
			l.add(iterator.next());
		return l;
	}

	public static <K> Set<K> setFromIteration(Iterator<K> iterator) {
		Set<K> l = new LinkedHashSet<>();
		while(iterator.hasNext())
			l.add(iterator.next());
		return l;
	}
	
}
