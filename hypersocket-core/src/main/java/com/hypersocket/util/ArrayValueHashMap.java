package com.hypersocket.util;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class ArrayValueHashMap<K, V> extends HashMap<K, V[]> {
	
	public static ArrayValueHashMap<String, String> parameterMap() {
		return new ArrayValueHashMap<>(String.class);
	}

	private final Class<V> clazz;

	public ArrayValueHashMap(Class<V> clazz) {
		super();
		this.clazz = clazz;
	}

	public ArrayValueHashMap(Class<V> clazz, int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
		this.clazz = clazz;
	}

	public ArrayValueHashMap(Class<V> clazz, int initialCapacity) {
		super(initialCapacity);
		this.clazz = clazz;
	}

	public ArrayValueHashMap(Class<V> clazz, Map<? extends K, ? extends V[]> m) {
		super(m);
		this.clazz = clazz;
	}
	
	public void putSingle(K key, V singleValue) {
		if(singleValue == null)
			put(key, null);
		else {
			@SuppressWarnings("unchecked")
			final V[] a = (V[]) Array.newInstance(clazz, 1);
			a[0] = singleValue;
			put(key, a);
		}
	}
	
	public static <K, V> Map<K, V> toSingleMap(Map<K, V[]> map) {
		var m = new HashMap<K, V>();
		map.forEach((a, b) -> {
			m.put(a, b == null || b.length == 0 ? null : b[0]);
		});
		return m;
	}
	
	public static <K, V> V getSingle(Map<K, V[]> map, K key) {
		var a = map.get(key);
		return a == null || a.length == 0 ? null : a[0];
	}

	public static  <K, V> void putSingle(Map<K, V[]> parameterMap, K key, Class<V> clazz, V val) {
		if(val == null)
			parameterMap.put(key, null);
		else {
			@SuppressWarnings("unchecked")
			final V[] a = (V[]) Array.newInstance(clazz, 1);
			a[0] = val;
			parameterMap.put(key, a);
		}
	}

	public static  <K> void putStringVal(Map<K, String[]> parameterMap, K key, String val) {
		putSingle(parameterMap, key, String.class, val);
	}

}
