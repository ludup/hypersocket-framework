package com.hypersocket.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.annotation.PostConstruct;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.certificates.CertificateVerificationException;
import com.hypersocket.config.SystemConfigurationService;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.RealmService;
import com.hypersocket.utils.HttpUtils;
import com.hypersocket.utils.HttpUtilsHolder;

@Component
public class HttpUtilsImpl implements HttpUtils, HostnameVerifier, TrustStrategy {

	static Logger log = LoggerFactory.getLogger(HttpUtilsImpl.class);

	static BasicCookieStore cookieStore;

	@Autowired
	SystemConfigurationService systemConfigurationService;

	@Autowired
	RealmService realmService;

	//
	private ThreadLocal<Stack<HostnameVerifier>> verifier = new ThreadLocal<>();

	@PostConstruct
	private void postConstruct() {
		/**
		 * Override HttpUtils so common non HS code can utilise proxy
		 */
		HttpUtilsHolder.setInstance(this);
	}

	public void setVerifier(HostnameVerifier verifier) {
		Stack<HostnameVerifier> v = this.verifier.get();
		if (verifier == null && (v == null || v.isEmpty()))
			throw new IllegalStateException("Cannot unset a verifier if none has been set.");

		if (verifier == null) {
			v.pop();
			if (v.isEmpty())
				this.verifier.remove();
		} else {
			if (v == null) {
				v = new Stack<>();
				this.verifier.set(v);
			}
			v.push(verifier);
		}
	}

	@Override
	public CloseableHttpClient createHttpClient(boolean allowSelfSigned) throws IOException {
		return createHttpClient(allowSelfSigned, 30000);
	}

	protected CloseableHttpClient createHttpClient(boolean allowSelfSigned, int requestTimeout) throws IOException {

		if (log.isDebugEnabled()) {
			log.debug("Creating a new client");
		}

		CloseableHttpClient httpclient = null;

		try {
			Stack<HostnameVerifier> cbl = verifier.get();
			HostnameVerifier cb;
			if (cbl == null || cbl.isEmpty())
				cb = this;
			else
				cb = cbl.get(cbl.size() - 1);

			SSLContextBuilder builder = new SSLContextBuilder();
			builder.loadTrustMaterial(null, this);
			Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create()
					.register("http", new ProxiedSocketFactory())
					.register("https", new ProxiedSSLSocketFactory(builder.build(), cb)).build();

			PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(reg);
			RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(requestTimeout)
					.setSocketTimeout(requestTimeout).setConnectTimeout(requestTimeout).build();
			httpclient = HttpClients.custom().setConnectionManager(cm).setDefaultRequestConfig(requestConfig)
					.setDefaultCookieStore(cookieStore).build();

			return httpclient;
		} catch (Exception e) {
			throw new IOException(e.getMessage(), e);
		}
	}

	@Override
	public String doHttpPost(String url, Map<String, String> parameters, boolean allowSelfSigned) throws IOException {
		return doHttpPost(url, parameters, allowSelfSigned, null);
	}

	@Override
	public String doHttpPost(String url, Map<String, String> parameters, boolean allowSelfSigned,
			Map<String, String> additionalHeaders) throws IOException {

		CloseableHttpClient client = createHttpClient(allowSelfSigned);

		try {
			HttpPost request = new HttpPost(url);

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

			for (String name : parameters.keySet()) {
				nameValuePairs.add(new BasicNameValuePair(name, parameters.get(name)));
			}

			if (additionalHeaders != null) {
				for (Map.Entry<String, String> entry : additionalHeaders.entrySet()) {
					request.addHeader(entry.getKey(), entry.getValue());
				}
			}
			request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = client.execute(request);

			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				throw new IOException("Received " + response.getStatusLine().toString());
			}
			HttpEntity entity = response.getEntity();
			return EntityUtils.toString(entity);

		} finally {
			try {
				client.close();
			} catch (IOException e) {
			}
		}
	}

	@Override
	public InputStream doHttpGet(String uri, boolean allowSelfSigned) throws IOException {

		CloseableHttpClient client = createHttpClient(allowSelfSigned);

		HttpGet request = new HttpGet(uri);

		return new ContentInputStream(client, client.execute(request).getEntity().getContent());

	}

	@Override
	public CloseableHttpResponse doHttpGet(String uri, boolean allowSelfSigned, Map<String, String> headers)
			throws IOException {

		CloseableHttpClient client = createHttpClient(allowSelfSigned);

		HttpGet request = new HttpGet(uri);
		if (headers != null) {
			for (String key : headers.keySet()) {
				request.setHeader(key, headers.get(key));
			}
		}

		return client.execute(request);

	}

	@Override
	public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		if (systemConfigurationService.getBooleanValue("ssl.strict") && chain.length == 1)
			return false;

		return true;
	}

	@Override
	public boolean verify(String hostname, SSLSession session) {
		/*
		 * This default implementation uses the system realm to store the known hosts
		 * for anything that isn't a realm connection. When realms are involved {@link
		 * #setVerifier} should be used.
		 * 
		 * Match against all known hosts we have connected and accepted before.
		 * 
		 * LDP - I changed this to a system property because its now possible to change
		 * the realm provider of the system realm and this broke any external HTTPS
		 * connection as it was trying to load, for example, SSH known hosts after
		 * changing system realm to a SSH realm.
		 * 
		 * The problem is I don't see where these "other" hosts are getting set? only
		 * the realm subsystem appears to use this.
		 */
		X509KnownHost thisX509KnownHost = new X509KnownHost(hostname, session);
		boolean found = false;
		boolean hostMatches = false;

		String val = systemConfigurationService.getValue("security.knownHosts");
		if (StringUtils.isNotBlank(val)) {
			for (String knownHost : (ResourceUtils.explodeCollectionValues(val))) {
				X509KnownHost x509KnownHost = new X509KnownHost(knownHost);
				if (thisX509KnownHost.matches(x509KnownHost)) {
					found = true;
					break;
				} else if (thisX509KnownHost.hostMatches(x509KnownHost)) {
					hostMatches = true;
				}
			}
		}

		/*
		 * We can't return false here, because we'll lose the SSLSession which we need
		 * for the signature confirmation.
		 */
		if (hostMatches)
			throw new CertificateVerificationException(CertificateVerificationException.Type.SIGNATURE_CHANGED,
					hostname, session);

		if (!found) {

			// If not in strict mode, just let this pass, otherwise just make sure the
			// hostname we are connecting to matches the hostname of the cert
			if (systemConfigurationService.getBooleanValue("ssl.strict")) {
				String subject = thisX509KnownHost.getSubject();
				try {
					LdapName name = new LdapName(subject);
					boolean foundCN = false;
					for (Rdn rdn : name.getRdns()) {
						if (rdn.getType().equalsIgnoreCase("cn")) {
							if (((String) rdn.getValue()).equals(hostname)) {
								log.error(String.format("Certificate hostname %s does not match %s.", rdn.getValue(),
										hostname));
								throw new CertificateVerificationException(
										CertificateVerificationException.Type.SIGNATURE_INVALID, hostname, session);
							} else {
								foundCN = true;
								break;
							}
						}
					}
					if (!foundCN) {
						log.error(String.format("Certificate subject %s contains no Common Name.", subject, hostname));
						throw new CertificateVerificationException(
								CertificateVerificationException.Type.SIGNATURE_INVALID, hostname, session);
					}
				} catch (InvalidNameException e) {
					log.error(String.format("Certificate subjecct %s is invalid.", subject, hostname));
					throw new CertificateVerificationException(CertificateVerificationException.Type.SIGNATURE_INVALID,
							hostname, session);
				}
			}
		}

		return true;
	}

	class ContentInputStream extends InputStream {

		CloseableHttpClient client;
		InputStream wrapped;

		ContentInputStream(CloseableHttpClient client, InputStream wrapped) {
			this.client = client;
			this.wrapped = wrapped;
		}

		@Override
		public int read() throws IOException {
			int b = wrapped.read();
			if (b == -1) {
				client.close();
			}
			return b;
		}

		public int read(byte[] buf, int off, int len) throws IOException {
			int b = wrapped.read(buf, off, len);
			if (b == -1) {
				client.close();
			}
			return b;
		}
	}

	class ProxiedSSLSocketFactory extends SSLConnectionSocketFactory {

		public ProxiedSSLSocketFactory(final SSLContext sslContext) {
			super(sslContext);
		}

		public ProxiedSSLSocketFactory(final SSLContext sslContext, HostnameVerifier verifier) {
			super(sslContext, verifier);
		}

		@Override
		public Socket createSocket(final HttpContext context) throws IOException {

			HttpHost currentHost = (HttpHost) context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);

			if (systemConfigurationService.getBooleanValue("proxy.enabled")
					&& !checkProxyBypass(currentHost.getHostName()) && !checkProxyBypass(currentHost.getAddress())) {
				InetSocketAddress socksaddr = new InetSocketAddress(systemConfigurationService.getValue("proxy.host"),
						systemConfigurationService.getIntValue("proxy.port"));
				Proxy proxy = new Proxy(Proxy.Type.valueOf(systemConfigurationService.getValue("proxy.type")),
						socksaddr);
				return new Socket(proxy);
			} else {
				return new Socket();
			}
		}

	}

	protected boolean checkProxyBypass(String hostname) {

		String[] bypass = systemConfigurationService.getValues("proxy.bypass");
		for (String addr : bypass) {

			if (hostname.equalsIgnoreCase(addr)) {
				return true;
			}

			if (addr.contains(".*")) {
				if (hostname.matches("^" + addr + "$")) {
					return true;
				}
			}
		}

		return false;
	}

	protected boolean checkProxyBypass(InetAddress hostname) {

		return false;
	}

	class ProxiedSocketFactory implements ConnectionSocketFactory {

		public ProxiedSocketFactory() {
		}

		@Override
		public Socket createSocket(final HttpContext context) throws IOException {

			HttpHost currentHost = (HttpHost) context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);

			if (systemConfigurationService.getBooleanValue("proxy.enabled")
					&& !checkProxyBypass(currentHost.getHostName()) && !checkProxyBypass(currentHost.getAddress())) {
				InetSocketAddress socksaddr = new InetSocketAddress(systemConfigurationService.getValue("proxy.host"),
						systemConfigurationService.getIntValue("proxy.port"));
				Proxy proxy = new Proxy(Proxy.Type.valueOf(systemConfigurationService.getValue("proxy.type")),
						socksaddr);
				return new Socket(proxy);
			} else {
				return new Socket();
			}
		}

		@Override
		public Socket connectSocket(int connectTimeout, Socket socket, HttpHost host, InetSocketAddress remoteAddress,
				InetSocketAddress localAddress, HttpContext context) throws IOException {
			Socket sock;
			if (socket != null) {
				sock = socket;
			} else {
				sock = createSocket(context);
			}
			if (localAddress != null) {
				sock.bind(localAddress);
			}
			try {
				sock.connect(remoteAddress, connectTimeout);
			} catch (SocketTimeoutException ex) {
				throw new ConnectTimeoutException(ex, host, remoteAddress.getAddress());
			}
			return sock;
		}

	}

	@Override
	public String doHttpGetContent(String uri, boolean allowSelfSigned, Map<String, String> headers)
			throws IOException {
		CloseableHttpResponse response = doHttpGet(uri, allowSelfSigned, headers);
		try {
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				throw new IOException("Received " + response.getStatusLine().toString());
			}
			HttpEntity entity = response.getEntity();
			return EntityUtils.toString(entity);

		} finally {
			try {
				response.close();
			} catch (IOException e) {
			}
		}
	}

	@Override
	public CloseableHttpResponse execute(HttpUriRequest request, boolean allowSelfSigned) throws IOException {
		CloseableHttpClient client = createHttpClient(allowSelfSigned);
		return client.execute(request);
	}
}
