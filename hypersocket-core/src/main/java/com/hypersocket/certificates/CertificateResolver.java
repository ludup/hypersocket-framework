package com.hypersocket.certificates;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;

import com.hypersocket.utils.HypersocketUtils;
import com.hypersocket.utils.StaticResolver;

public class CertificateResolver extends StaticResolver {

	public CertificateResolver(CertificateResource resource, X509Certificate cert) {
		addToken("certName", resource.getName());
		addToken("certCommonName", resource.getCommonName());
		addToken("cert", resource.getCertificate());
		addToken("certBundle", resource.getBundle());
		addToken("certType", resource.getCertType().name());
		addToken("certExpiry", HypersocketUtils.formatDate(cert.getNotAfter()));
		addToken("certFingerprint", getThumbprint(cert));
	}
	
	public static Set<String> getVariables() {
		return new HashSet<String>(Arrays.asList("certName", "certCommonName", "cert", "certBundle", "certType", "certExpiry"));
	}
	
	private static String getThumbprint(X509Certificate cert)  {
        try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] der = cert.getEncoded();
			md.update(der);
			byte[] digest = md.digest();
			String digestHex = DatatypeConverter.printHexBinary(digest);
			return digestHex.toLowerCase();
		} catch (NoSuchAlgorithmException | CertificateEncodingException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
    }
}
