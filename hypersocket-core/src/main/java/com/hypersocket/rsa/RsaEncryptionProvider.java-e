package com.hypersocket.rsa;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.lf5.util.StreamUtils;

import com.hypersocket.encrypt.AbstractEncryptionProvider;
import com.hypersocket.encrypt.EncryptionProvider;

public class RsaEncryptionProvider extends AbstractEncryptionProvider {

	static public RsaEncryptionProvider instance;
	
	File prvFile = new File(System.getProperty("hypersocket.conf", "conf"), "secrets");
	File pubFile = new File(System.getProperty("hypersocket.conf", "conf"), "secrets.pub");
	
	PrivateKey privateKey;
	PublicKey publicKey;
	
	public RsaEncryptionProvider() throws Exception {
		
		try {
			loadKeys();
		} catch(Exception e) {
			generateKeys();
		}
	}
	
	public Provider getProvider() {
		return null;
	}
	
	private void generateKeys() throws Exception {
		KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
		gen.initialize(2048);
		KeyPair key = gen.generateKeyPair();
		privateKey = key.getPrivate();
		publicKey = key.getPublic();

		FileOutputStream pvt = new FileOutputStream(prvFile);
		try {
		    pvt.write(privateKey.getEncoded());
		    pvt.flush();
		} finally {
		    pvt.close();
		}
		FileOutputStream pub = new FileOutputStream(pubFile);
		try {
		    pub.write(publicKey.getEncoded());
		    pub.flush();
		} finally {
		    pub.close();
		}
	}
	
	private void loadKeys() throws Exception {
		
		InputStream in = new FileInputStream(prvFile);
		byte[] prvBytes = StreamUtils.getBytes(in);
		IOUtils.closeQuietly(in);
		
		in = new FileInputStream(pubFile);
		byte[] pubBytes = StreamUtils.getBytes(in);
		IOUtils.closeQuietly(in);

		PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(prvBytes);
		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(pubBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
        
        privateKey = kf.generatePrivate(privateKeySpec);
        publicKey = kf.generatePublic(publicKeySpec);
        
	}
	
	
	public static RsaEncryptionProvider getInstance() throws Exception {
		return instance==null ? instance = new RsaEncryptionProvider() : instance;
	}
	
	public int getLength() {
		return 245;
	}
	
	@Override
	public String doEncrypt(String toEncrypt) throws Exception {

		Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		c.init(Cipher.ENCRYPT_MODE, privateKey);
		return Base64.encodeBase64String(c.doFinal(toEncrypt.getBytes("UTF-8")));
		
	}
	
	@Override
	public String doDecrypt(String toDecrypt) throws Exception {
		
		Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		c.init(Cipher.DECRYPT_MODE, publicKey);
		return new String(c.doFinal(Base64.decodeBase64(toDecrypt)), "UTF-8");
	}
	
	public static void main(String[] args) throws Exception {
		
		System.out.println(URLEncoder.encode("=", "UTF-8"));
		System.setProperty("hypersocket.conf", "/Users/lee");
		
		EncryptionProvider tk = RsaEncryptionProvider.getInstance();
		
		StringBuffer buf = new StringBuffer();
		for(int i=0;i<1000;i+=10) {
			buf.append("0123456789");
		}
		String encryped = tk.encrypt(buf.toString());
		System.out.println(encryped);
		System.out.println(encryped.length());
		String decrypted = tk.decrypt(encryped);
		
		System.out.print(decrypted);
	}

	@Override
	public boolean supportsSecretKeyStorage() {
		return false;
	}

	@Override
	public void createSecretKey(String reference) throws IOException {
		
	}

	@Override
	public SecretKey getSecretKey(String reference) throws IOException {
		throw new IllegalStateException("Cannot access secret key when EncryptionProvider does not support secret keys");
	}

	@Override
	public String getName() {
		return "RSA";
	}

}
