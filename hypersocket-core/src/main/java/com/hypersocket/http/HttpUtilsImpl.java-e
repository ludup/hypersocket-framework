package com.hypersocket.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.config.SystemConfigurationService;
import com.hypersocket.utils.HttpUtils;
import com.hypersocket.utils.HttpUtilsHolder;

@Component
public class HttpUtilsImpl implements HttpUtils {

	static BasicCookieStore cookieStore;

	@Autowired
	SystemConfigurationService systemConfigurationService; 
	
	
	
	@PostConstruct
	private void postConstruct() {
		/**
		 * Override HttpUtils so common non HS code can utilise proxy
		 */
		HttpUtilsHolder.setInstance(this);
	}
	
	@Override
	public CloseableHttpClient createHttpClient(boolean allowSelfSigned)
			throws IOException {
		
		CloseableHttpClient httpclient = null;
		
		boolean strict = systemConfigurationService.getBooleanValue("ssl.strict");
		
		try {
			if (!strict) {
				
				SSLContextBuilder builder = new SSLContextBuilder();
				builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
				
				Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create()
				        .register("http", new ProxiedSocketFactory())
				        .register("https", new ProxiedSSLSocketFactory(builder.build(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER))
				        .build();
				
				PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(reg);

				httpclient = HttpClients.custom().setConnectionManager(cm)
						.setDefaultCookieStore(cookieStore).build();

			} else {
				
				Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create()
				        .register("http", new ProxiedSocketFactory())
				        .register("https", new ProxiedSSLSocketFactory(SSLContexts.createSystemDefault()))
				        .build();
				
				PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(reg);

				httpclient = HttpClients.custom().setConnectionManager(cm)
						.setDefaultCookieStore(cookieStore).build();
			}

			return httpclient;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	@Override
	public String doHttpPost(String url, Map<String, String> parameters,
			boolean allowSelfSigned) throws IOException {

		CloseableHttpClient client = createHttpClient(allowSelfSigned);

		try {
			HttpPost request = new HttpPost(url);

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

			for (String name : parameters.keySet()) {
				nameValuePairs.add(new BasicNameValuePair(name, parameters
						.get(name)));
			}
			request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = client.execute(request);

			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				throw new IOException("Received "
						+ response.getStatusLine().toString());
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
	public InputStream doHttpGet(String uri, boolean allowSelfSigned)
			throws IOException {

		CloseableHttpClient client = createHttpClient(allowSelfSigned);

		HttpGet request = new HttpGet(uri);
	
		return new ContentInputStream(client, client.execute(request)
				.getEntity().getContent());

	}

	@Override
	public CloseableHttpResponse doHttpGet(String uri, boolean allowSelfSigned, Map<String,String> headers)
			throws IOException {

		CloseableHttpClient client = createHttpClient(allowSelfSigned);

		HttpGet request = new HttpGet(uri);
		for(String key : headers.keySet()) {
			request.setHeader(key, headers.get(key));
		}
		
		return client.execute(request);

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
	    
	    public ProxiedSSLSocketFactory(final SSLContext sslContext, X509HostnameVerifier verifier) {
	        super(sslContext, verifier);
	    }

	    @Override
	    public Socket createSocket(final HttpContext context) throws IOException {
	    	
			HttpHost currentHost= (HttpHost) context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);
	    	
	    	if(systemConfigurationService.getBooleanValue("proxy.enabled")
	    			&& !checkProxyBypass(currentHost.getHostName()) 
	    			&& !checkProxyBypass(currentHost.getAddress())) {
		        InetSocketAddress socksaddr = new InetSocketAddress(systemConfigurationService.getValue("proxy.host"),
		        		systemConfigurationService.getIntValue("proxy.port"));
		        Proxy proxy = new Proxy(Proxy.Type.valueOf(systemConfigurationService.getValue("proxy.type")), socksaddr);  
		        return new Socket(proxy);
	    	} else {
	    		return new Socket();
	    	}
	    }

	}
	
	protected boolean checkProxyBypass(String hostname) {
		
		String[] bypass = systemConfigurationService.getValues("proxy.bypass");
		for(String addr : bypass) {
			
			if(hostname.equalsIgnoreCase(addr)) {
				return true;
			}
			
			if(addr.contains(".*")) {
				if(hostname.matches("^" + addr + "$")) {
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

			HttpHost currentHost= (HttpHost) context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);
	    	
	    	if(systemConfigurationService.getBooleanValue("proxy.enabled")
	    			&& !checkProxyBypass(currentHost.getHostName()) 
	    			&& !checkProxyBypass(currentHost.getAddress())) {
		        InetSocketAddress socksaddr = new InetSocketAddress(systemConfigurationService.getValue("proxy.host"),
		        		systemConfigurationService.getIntValue("proxy.port"));
		        Proxy proxy = new Proxy(Proxy.Type.valueOf(systemConfigurationService.getValue("proxy.type")), socksaddr);  
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
}
