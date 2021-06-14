package com.hypersocket.encrypt;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Curve25519Utils {

	public static String encrypt(byte[] secret, String val, String field) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		
		MessageDigest digest = MessageDigest.getInstance("SHA512");
		digest.update(field.getBytes("UTF-8"));
		digest.update(secret);
		
		byte[] keydata = digest.digest();
		byte[] iv = new byte[16];
		byte[] key = new byte[32];

		System.arraycopy(keydata, 0, iv, 0, iv.length);
		System.arraycopy(keydata, iv.length, key, 0, key.length);
		
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
		
		IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
		byte[] value = val.getBytes("UTF-8");
		String result = Base64.getEncoder().encodeToString(cipher.doFinal(value, 0, value.length));
		return result;
	}
	
	public static String decrypt(byte[] secret, String val, String field) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		
		MessageDigest digest = MessageDigest.getInstance("SHA512");
		digest.update(field.getBytes("UTF-8"));
		digest.update(secret);
		
		byte[] keydata = digest.digest();
		byte[] iv = new byte[16];
		byte[] key = new byte[32];

		System.arraycopy(keydata, 0, iv, 0, iv.length);
		System.arraycopy(keydata, iv.length, key, 0, key.length);
		
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
		
		IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

		cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
		byte[] value = Base64.getDecoder().decode(val);
		return new String(cipher.doFinal(value, 0, value.length), "UTF-8");
	}
}
