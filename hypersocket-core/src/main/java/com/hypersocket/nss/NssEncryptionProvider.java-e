package com.hypersocket.nss;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hypersocket.encrypt.EncryptionProvider;

public class NssEncryptionProvider implements EncryptionProvider {

	static Log log = LogFactory.getLog(NssEncryptionProvider.class);
	
	private String dbPassword;
	private KeyStore keystore;
	
	private Provider cryptoProvider;
	
	private static NssEncryptionProvider instance;

	private NssEncryptionProvider() throws Exception {
		
		try {
			openDatabase();
			return;
		} catch (Exception e) {
			log.error("Could not open NSS database", e);
			createDatabase();
		}
		
		openDatabase();
	}
	
	public static NssEncryptionProvider getInstance() throws Exception {
		return instance==null ? instance = new NssEncryptionProvider() : instance;
	}
	
	@Override
	public String encrypt(String toEncrypt) throws Exception {

		Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding", cryptoProvider);
		c.init(Cipher.ENCRYPT_MODE, keystore.getKey("hypersocket", null));
		return Base64.encodeBase64String(c.doFinal(toEncrypt.getBytes("UTF-8")));
		
	}
	
	@Override
	public String decrypt(String toDecrypt) throws Exception {
		
		Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding", cryptoProvider);
		c.init(Cipher.DECRYPT_MODE, keystore.getCertificate("hypersocket"));
		return new String(c.doFinal(Base64.decodeBase64(toDecrypt)), "UTF-8");
	}
	
	public static String generateString(int length)
	{
		String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	    char[] text = new char[length];
	    SecureRandom rng = new SecureRandom();
	    for (int i = 0; i < length; i++)
	    {
	        text[i] = characters.charAt(rng.nextInt(characters.length()));
	    }
	    return new String(text);
	}
	
	@SuppressWarnings("restriction")
	private void openDatabase() throws Exception {
		
		File libFolder64bit = new File("/usr/lib/x86_64-linux-gnu/");
		
		String filename = "nss.cfg";
		
		if(libFolder64bit.exists()) {
			filename = "nss64.cfg";
		}
		
		String configName = new File(System.getProperty("hypersocket.conf", "conf"), filename).getAbsolutePath();
		
		for(Provider p : Security.getProviders()) {
			log.warn(p.getName());
		}
		
		if(cryptoProvider==null) {
			cryptoProvider = new sun.security.pkcs11.SunPKCS11(configName);
		}
		
		if(keystore==null) {
			File dbFile = new File(System.getProperty("hypersocket.conf", "conf"), ".private");
			File keyFile = new File(dbFile, ".key");
			dbPassword = IOUtils.toString(new FileInputStream(keyFile), "US-ASCII");
			char[] nssDBPassword = dbPassword.toCharArray();
			keystore = KeyStore.getInstance("PKCS11", cryptoProvider);
			keystore.load(null, nssDBPassword);
		}


	}
	private void createDatabase() throws IOException, InterruptedException {

		File dbFile = new File(System.getProperty("hypersocket.conf", "conf"), ".private");
		dbFile.mkdirs();
		
		String password =  new BigInteger(130, new SecureRandom()).toString(32);
		File keyFile = new File(dbFile, ".key");
		FileOutputStream o = new FileOutputStream(keyFile);
		o.write(password.getBytes("US-ASCII"));
		o.close();
		
		File noiseFile = new File(System.getProperty("hypersocket.conf", "conf"), "noise.dat");
		File random = new File("/dev/urandom");

		FileInputStream in = new FileInputStream(random);
		FileOutputStream out = new FileOutputStream(noiseFile);

		for (int i = 0; i < 128; i++) {
			out.write(in.read());
		}
		out.close();
		in.close();

		String db = dbFile.getAbsolutePath();
		
		String[] createDir = new String[] {"mkdir", db};
		exec(createDir);
		String[] createCmd = new String[] {"certutil", "-N", "-d", db, "-f", ".private/.key"};
		exec(createCmd);
		String[] makeFips = new String[] {"modutil", "-fips", "true", "-dbdir", db, "-force" };
		exec(makeFips);
		String[] certCmd = new String[] { "certutil", "-S", "-s",
				"CN=Hypersocket Keystore", "-n", "hypersocket", "-x", "-t", "CT,C,C", "-v",
				"120", "-m", "1234", "-d", db, "-z", "noise.dat", "-f",
				".private/.key", "-g", "4096" };
		exec(certCmd);
		exec(new String[] { "chmod", "400", keyFile.getAbsolutePath()});
		exec(new String[] { "chmod", "500", dbFile.getAbsolutePath()});
		noiseFile.delete();
		
	}

	public static void exec(String... cmd) throws IOException,
			InterruptedException {

		Process p = cmd.length == 1 ? Runtime.getRuntime().exec(cmd[0], null, new File(System.getProperty("hypersocket.conf", "conf")))
				: Runtime.getRuntime().exec(cmd, null, new File(System.getProperty("hypersocket.conf", "conf")));

		int b;
		while ((b = p.getInputStream().read()) > -1) {
			System.out.write(b);
		}
		;
		while ((b = p.getErrorStream().read()) > -1) {
			System.err.write(b);
		}
		;

		System.out.flush();
		System.err.flush();

		p.waitFor();

		System.out.println("Exit code: " + p.exitValue());

	}
	
	public static void main(String[] args) throws Exception {
		
		System.setProperty("hypersocket.conf", "/home/lee/workspaces/nervepoint-1.2/wui-server/conf");
		
		NssEncryptionProvider tk = new NssEncryptionProvider();
		
		String encryped = tk.encrypt("this is an encrypted string");
		
		System.out.println(encryped);
		
		String decrypted = tk.decrypt(encryped);
		
		System.out.print(decrypted);
	}
}
