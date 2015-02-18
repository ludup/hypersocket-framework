package com.hypersocket.encrypt;

import java.util.StringTokenizer;

public abstract class AbstractEncryptionProvider implements EncryptionProvider {


	@Override
	public String encrypt(String toEncrypt) throws Exception {
		
		int pos = 0;
		StringBuffer ret = new StringBuffer();
		while(pos < toEncrypt.length()) {
			int count = Math.min(toEncrypt.length() - pos, getLength());
			ret.append(doEncrypt(toEncrypt.substring(pos, pos+count)));
			ret.append('|');
			pos += count;
		}
		return ret.toString();
	}

	@Override
	public String decrypt(String toDecrypt) throws Exception {
		
		StringBuffer ret = new StringBuffer();
		StringTokenizer t = new StringTokenizer(toDecrypt, "|");
		while(t.hasMoreTokens()) {
			ret.append(doDecrypt(t.nextToken()));
		}

		return ret.toString();
	}

	protected abstract int getLength();
	
	protected abstract String doEncrypt(String toEncrypt) throws Exception;
	
	protected abstract String doDecrypt(String toDecrypt) throws Exception;
}
