/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.netty;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.client.HypersocketClientTransport;
import com.hypersocket.client.NetworkResource;
import com.hypersocket.netty.websocket.WebSocket;
import com.hypersocket.netty.websocket.WebSocketListener;

public class NettyClientTransport implements HypersocketClientTransport {

	static Logger log = LoggerFactory.getLogger(NettyClientTransport.class);

	private ServerBootstrap serverBootstrap = null;

	private Map<SocketAddress, NetworkResource> resourcesBySocketAddress = new HashMap<SocketAddress, NetworkResource>();
	private Map<SocketAddress, Channel> channelsBySocketAddress = new HashMap<SocketAddress, Channel>();

	HttpClient httpClient;
	String path;
	String apiPath;
	long requestTimeout = 30000L;

	ExecutorService bossExecutor;
	ExecutorService workerExecutor;
	boolean ownsExecutors = true;

	public NettyClientTransport() {
		bossExecutor = Executors.newCachedThreadPool();
		workerExecutor = Executors.newCachedThreadPool();
	}

	public NettyClientTransport(ExecutorService bossExecutor,
			ExecutorService workerExecutor) {
		this.bossExecutor = bossExecutor;
		this.workerExecutor = workerExecutor;
		this.ownsExecutors = false;
	}

	@Override
	public void setHeader(String name, String value) {
		httpClient.addStaticHeader(name, value);
	}

	@Override
	public void removeHeader(String name) {
		httpClient.removeStaticHeader(name);
	}

	@Override
	public void connect(String host, int port, String path)
			throws UnknownHostException, IOException {

		this.httpClient = new HttpClient(host, port, true);
		this.httpClient.connect(bossExecutor, workerExecutor);
		this.path = path;

		if (!this.path.endsWith("/")) {
			this.path += "/";
		}

		this.apiPath = this.path + "api/";

//		createServerBootstrap();

	}

	private void createServerBootstrap() {
		// Configure the server.
		serverBootstrap = new ServerBootstrap(
				new NioServerSocketChannelFactory(bossExecutor, workerExecutor));

		// Set up the event pipeline factory.
		serverBootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();
				pipeline.addLast("handler", new LocalForwardingHandler(
						NettyClientTransport.this));
				return pipeline;
			}
		});
	}

	public void setRequestTimeout(long requestTimeout) {
		if (requestTimeout < 0) {
			throw new IllegalArgumentException(
					"requestTimeout cannot be negative");
		}
		this.requestTimeout = requestTimeout;
	}

	@Override
	public boolean isConnected() {
		return httpClient != null && !httpClient.isDisconnected();
	}

	@Override
	public void disconnect(boolean onError) {
		try {
			stopAllForwarding();
		} finally {
			httpClient.disconnect();
		}
	}

	@Override
	public void stopAllForwarding() {
		Set<SocketAddress> tmp = new HashSet<SocketAddress>();
		tmp.addAll(resourcesBySocketAddress.keySet());
		for (SocketAddress addr : tmp) {
			stopLocalForwarding(addr);
		}
	}

	@Override
	public void shutdown() {

		if (ownsExecutors) {
			if (log.isDebugEnabled()) {
				log.debug("Releasing thread pool");
			}

			bossExecutor.shutdownNow();
			workerExecutor.shutdownNow();
		}
		if(serverBootstrap != null)
			serverBootstrap.shutdown();
	}

	@Override
	public void stopLocalForwarding(String listenAddress, int listenPort) {

		try {
			if (log.isInfoEnabled()) {
				log.info("Stopping forwarding on " + listenAddress + ":"
						+ listenPort);
			}

			SocketAddress addr = new InetSocketAddress(listenAddress,
					listenPort);
			stopLocalForwarding(addr);
		} catch (Throwable e) {
			log.error("Failed to stop local forwarding " + listenAddress + ":"
					+ listenPort, e);
		}
	}

	protected void stopLocalForwarding(SocketAddress addr) {
		if (channelsBySocketAddress.containsKey(addr)) {
			Channel channel = channelsBySocketAddress.remove(addr);
			channel.close();
		}
		if (resourcesBySocketAddress.containsKey(addr)) {
			resourcesBySocketAddress.remove(addr);
		}

	}

	@Override
	public int startLocalForwarding(String listenAddress, int listenPort,
			NetworkResource resource) throws IOException {
		try {
			if(serverBootstrap == null) {
				createServerBootstrap();
			}

			if (log.isInfoEnabled()) {
				log.info("Starting forwarding on " + listenAddress + ":"
						+ listenPort + " to " + resource.getHostname() + ":"
						+ resource.getPort());
			}
			Channel channel = serverBootstrap.bind(new InetSocketAddress(
					listenAddress, listenPort));

			resourcesBySocketAddress.put(channel.getLocalAddress(), resource);
			channelsBySocketAddress.put(channel.getLocalAddress(), channel);

			return ((InetSocketAddress) channel.getLocalAddress()).getPort();
		} catch (Exception e) {
			if (log.isErrorEnabled()) {
				log.error("Could not start local forwarding " + e.getMessage());
			}
			if (listenPort > 0) {
				return startLocalForwarding(listenAddress, 0, resource);
			} else {
				return 0;
			}
		}
	}

	@Override
	public String get(String uri) throws IOException {
		return get(uri, requestTimeout);
	}
	
	public String resolveUrl(String uri) {
		
		return "https://" + getHost() + (getPort()!=443 ? ":" + getPort() : "")
				+ apiPath + uri;
	}

	@Override
	public String get(String uri, long timeout) throws IOException {

		HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1,
				HttpMethod.GET, apiPath + uri);

		HttpHandlerResponse response = httpClient.sendRequest(request, timeout);

		if (response.getStatusCode() == 200) {
			return response.getContent().toString(Charset.forName("UTF-8"));
		} else {
			throw new IOException("GET did not respond with 200 OK ["
					+ response.getStatusCode() + "]");
		}

	}

	@Override
	public byte[] getBlob(String uri, long timeout) throws IOException {

		HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1,
				HttpMethod.GET, apiPath + uri);

		HttpHandlerResponse response = httpClient.sendRequest(request, timeout);

		if (response.getStatusCode() == 200) {
			return response.getContent().array();
		} else {
			throw new IOException("GET did not respond with 200 OK ["
					+ response.getStatusCode() + "]");
		}

	}

	@Override
	public InputStream getContent(String uri, long timeout) throws IOException {

		HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1,
				HttpMethod.GET, apiPath + uri);

		HttpHandlerResponse response = httpClient.sendRequest(request, timeout);

		if (response.getStatusCode() == 200) {
			return new ChannelBufferInputStream(response.getContent());
		} else {
			throw new IOException("GET did not respond with 200 OK ["
					+ response.getStatusCode() + "]");
		}

	}

	@Override
	public String post(String uri, Map<String, String> params)
			throws IOException {
		return post(uri, params, requestTimeout);
	}

	@Override
	public String post(String uri, Map<String, String> params, long timeout)
			throws IOException {

		HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1,
				HttpMethod.POST, apiPath + uri);

		StringBuffer buf = new StringBuffer();
		for (Map.Entry<String, String> e : params.entrySet()) {
			if (buf.length() > 0) {
				buf.append('&');
			}
			buf.append(URLEncoder.encode(e.getKey(), "UTF-8"));
			buf.append('=');
			buf.append(URLEncoder.encode(e.getValue(), "UTF-8"));
		}

		if (buf.length() > 0) {
			ChannelBuffer buffer = ChannelBuffers.copiedBuffer(buf.toString(),
					Charset.defaultCharset());
			request.setHeader("Content-Type",
					"application/x-www-form-urlencoded");
			request.setHeader("Content-Length", buffer.readableBytes());
			request.setContent(buffer);
		}

		HttpHandlerResponse response = httpClient.sendRequest(request, timeout);

		if (response.getStatusCode() == 200) {
			return response.getContent().toString(Charset.forName("UTF-8"));
		} else {
			throw new IOException("POST did not respond with 200 OK ["
					+ response.getStatusCode() + "]");
		}

	}

	public WebSocket createWebsocket(String request, WebSocketListener callback)
			throws IOException {

		try {
			URI uri = new URI("ws://" + httpClient.getHost() + ":"
					+ httpClient.getPort() + path + request);

			return httpClient.createWebsocket(uri, callback);

		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
	}

	public WebSocket createTunnel(Channel channel, WebSocketListener callback)
			throws IOException {

		NetworkResource resource = resourcesBySocketAddress.get(channel
				.getLocalAddress());

		return createWebsocket(resource.getUri() + "?resourceId=" + resource.getId()
				+ "&hostname=" + resource.getDestinationHostname()
				+ "&port=" + resource.getPort(), callback);

	}

	/**
	 * For testing purposes. Commented out, please leave for now.
	 */
//	public static void main(String[] args) throws UnknownHostException,
//			IOException {
//
//		//BasicConfigurator.configure();
//
//		NettyClientTransport c = new NettyClientTransport();
//		EmbeddedClient client = new EmbeddedClient(c);
//
//		client.connect("localhost", 8443, "/hypersocket",
//				Locale.getDefault());
//
//		Map<String, String> params = new HashMap<String, String>();
//		params.put("realm", "Default");
//		params.put("username", "admin");
//		params.put("password", "admin");
//
//		client.loginHttp("Default", "admin", "admin");
//
//		final WebSocket ws = c.createWebsocket("bind?id=12345", new WebSocketListener() {
//			
//			Random r = new Random();
//			@Override
//			public void onMessage(WebSocket client, WebSocketFrame frame) {
//				System.out.println("Channel: " + frame.getRsv() + " " + frame.getBinaryData().toString("UTF-8"));
//				client.send(new BinaryWebSocketFrame(true, r.nextInt(100), ChannelBuffers.copiedBuffer("echo\n", "UTF-8")));
//			}
//			
//			@Override
//			public void onError(Throwable t) {
//				t.printStackTrace();
//			}
//			
//			@Override
//			public void onDisconnect(WebSocket client) {
//				System.out.println("Disconnected");
//			}
//			
//			@Override
//			public void onConnect(WebSocket client) {
//				System.out.println("Connected");
//				client.send(new BinaryWebSocketFrame(true, r.nextInt(100), ChannelBuffers.copiedBuffer("echo\n", "UTF-8")));
//			}
//		});
//		
//		System.out.println("Connecting");
//		ws.connect().awaitUninterruptibly();
//		System.out.println("Waiting for bind");
//		
//	}

	@Override
	public String getHost() {
		return httpClient.getHost();
	}

	@Override
	public int getPort() {
		return httpClient.getPort();
	}
}
