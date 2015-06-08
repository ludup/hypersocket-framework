/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.netty;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketVersion;
import org.jboss.netty.handler.ssl.SslHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.netty.websocket.WebSocket;
import com.hypersocket.netty.websocket.WebSocketHandler;
import com.hypersocket.netty.websocket.WebSocketListener;

public class HttpConnectionPool {

	static Logger log = LoggerFactory.getLogger(HttpConnectionPool.class);
	
	List<HttpConnection> availableConnections = new ArrayList<HttpConnection>();
	List<HttpConnection> leasedConnections = new ArrayList<HttpConnection>();

	HttpHandler httpHandler;
	SSLContext sslContext;
	NioClientSocketChannelFactory clientSocketFactory = null;
	ClientBootstrap httpBootstrap;
	HttpClient client;
	ExecutorService bossExecutor;
	ExecutorService workerExecutor;

	public HttpConnectionPool(HttpClient client, 
			ExecutorService bossExecutor,
			ExecutorService workerExecutor,
			final ChannelUpstreamHandler handler)
			throws IOException {

		this.client = client;
		this.bossExecutor = bossExecutor;
		this.workerExecutor = workerExecutor;
		
		clientSocketFactory = new NioClientSocketChannelFactory(
				bossExecutor,
				workerExecutor);
		
		httpHandler = new HttpHandler();
		httpBootstrap = new ClientBootstrap(clientSocketFactory);

		try {
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, new TrustManager[] {new HttpsTrustManager()}, null);
		} catch (Exception e) {
			throw new IOException("Unexpected error creating SSL context", e);
		} 

		httpBootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();
				if (HttpConnectionPool.this.client.isSecure()) {
					SSLEngine engine = sslContext.createSSLEngine();
					engine.setUseClientMode(true);
					pipeline.addLast("ssl", new SslHandler(engine));
				}
				pipeline.addLast("decoder", new HttpResponseDecoder());
				pipeline.addLast("encoder", new HttpRequestEncoder());
				pipeline.addLast("http-handler", handler);
				return pipeline;
			}
		});

		for (int i = 0; i < client.getMinimumConnections(); i++) {
			availableConnections.add(createConnection());
		}
	}
	
	public synchronized HttpConnection checkout() throws IOException {
		
		if(availableConnections.size() == 0) {
			availableConnections.add(createConnection());
		}
		
		HttpConnection con = availableConnections.remove(0);
		leasedConnections.add(con);
		
		if(log.isDebugEnabled()) {
			log.debug("Checked out http connection id=" + con.getId());
		}
		
		return con;
	}
	
	public synchronized void checkin(HttpConnection con) throws IOException {
		
		if(log.isDebugEnabled()) {
			log.debug("Checking in http connection id=" + con.getId());
		}
		
		leasedConnections.remove(con);
		
		if(!con.isConnected()) {
			con.cleanup();
		} else {
			if(!client.isDisconnected()) {
				availableConnections.add(con);
			} else {
				con.disconnect();
			}
		}
	
	}

	protected HttpConnection createConnection() throws IOException {

		if(log.isDebugEnabled()) {
			log.debug("Creating pooled http connection");
		}
		
		ChannelFuture future = httpBootstrap.connect(new InetSocketAddress(
				InetAddress.getByName(client.getHost()), client.getPort()));

		future.awaitUninterruptibly();

		if (!future.isSuccess()) {
			throw new IOException("Could not connect to " + client.getHost()
					+ ":" + client.getPort());
		}

		return new HttpConnection(future.getChannel(), httpHandler, this);

	}
	
	public WebSocket createWebsocketConnection(URI uri, final WebSocketListener callback) throws IOException {
		
		if(log.isDebugEnabled()) {
			log.debug("Creating websocket to " + uri.toASCIIString());
		}
		
		ClientBootstrap websocketBootstrap = new ClientBootstrap(
				clientSocketFactory);
		
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Cookie", client.getCookies());
		
		final WebSocketHandler clientHandler = new WebSocketHandler(
				websocketBootstrap, uri, callback,
				new WebSocketClientHandshakerFactory().newHandshaker(uri,
						WebSocketVersion.V13, null, true, headers));

		websocketBootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();
				if(client.isSecure()) {
					SSLEngine engine = sslContext.createSSLEngine();
					engine.setUseClientMode(true);
					pipeline.addLast("ssl", new SslHandler(engine));
				}
				pipeline.addLast("decoder", new HttpResponseDecoder());
				pipeline.addLast("encoder", new HttpRequestEncoder());
				pipeline.addLast("ws-handler", clientHandler);
				return pipeline;
			}
		});

		return clientHandler;
	}

	public synchronized void disconnectAll() {
		
		if(log.isDebugEnabled()) {
			log.debug("Disconnecting all connections available=" + availableConnections.size() + " leased=" + leasedConnections.size());
		}
		
		for(HttpConnection con : availableConnections) {
			con.disconnect();
		}
		
		for(HttpConnection con : leasedConnections) {
			con.disconnect();
		}
		
		clientSocketFactory.shutdown();
		
		if(log.isDebugEnabled()) {
			log.debug("Disconnection complete");
		}
		
	}

}
