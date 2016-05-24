package com.hypersocket.utils;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import net.jpountz.xxhash.StreamingXXHash32;
import net.jpountz.xxhash.XXHashFactory;

public class FastHash {

	StreamingXXHash32 hash32;
	boolean print = false;
	public FastHash() {
		XXHashFactory factory = XXHashFactory.fastestInstance();
		int seed = 0x9747b28f; 
		hash32 = factory.newStreamingHash32(seed);
	}
	
	public void setPrint(boolean print) {
		this.print = print;
	}
	
	public void putString(String str) {
		if(print) {
			System.out.println(str);
		}
		byte[] b;
		try {
			b = str.getBytes("UTF-8");
			putInt(b.length);
			if(b.length > 0) {
				hash32.update(b, 0, b.length);
			}
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("Cannot encode string to UTF-8");
		}
	}
	
	public void putBoolean(boolean b) {
		if(print) {
			System.out.println(b ? "1" : "0");
		}
		hash32.update(new byte[] { (byte) (b ? 1 : 0)}, 0, 1);
	}
	
	public void putDate(Date date) {
		putString(date==null ? "NULL" : String.valueOf(date.getTime()));
	}
	
	public void putLong(long l) {
		putString(String.valueOf(l));
	}
	
	public void putInt(int i) {
		hash32.update(encodeInt(i), 0, 4);
	}
	
	private byte[] encodeInt(int i) {
		byte[] raw = new byte[4];
		raw[0] = (byte) (i >> 24);
		raw[1] = (byte) (i >> 16);
		raw[2] = (byte) (i >> 8);
		raw[3] = (byte) (i);
		return raw;
	}

	public int doFinal() {
		try {
			return hash32.getValue();
		} finally {
			hash32.reset();
		}
	}
}
