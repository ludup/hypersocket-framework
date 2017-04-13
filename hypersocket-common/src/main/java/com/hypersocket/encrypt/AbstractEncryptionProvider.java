package com.hypersocket.encrypt;

import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractEncryptionProvider implements EncryptionProvider {

	static Logger log = LoggerFactory.getLogger(AbstractEncryptionProvider.class);
	
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
	
		if(log.isDebugEnabled()) {
			log.debug("Decrypting " + toDecrypt);
		}
		StringBuffer ret = new StringBuffer();
		StringTokenizer t = new StringTokenizer(toDecrypt, "|");
		
		while(t.hasMoreTokens()) {
		
			String data = t.nextToken();
			if(log.isDebugEnabled()) {
				log.debug("Decrypting fragment " + data);
			}
			ret.append(doDecrypt(data));
		}

		return ret.toString();
	}

	protected abstract int getLength();
	
	protected abstract String doEncrypt(String toEncrypt) throws Exception;
	
	protected abstract String doDecrypt(String toDecrypt) throws Exception;
}
