/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.netty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.jboss.netty.logging.InternalLogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.hypersocket.netty.forwarding.SocketForwardingWebsocketClientHandler;
import com.hypersocket.server.HypersocketServerImpl;
import com.hypersocket.server.websocket.TCPForwardingClientCallback;

@Component
public class NettyServer extends HypersocketServerImpl {

	static Logger log = LoggerFactory.getLogger(NettyServer.class);

	private ClientBootstrap clientBootstrap = null;
	private ServerBootstrap serverBootstrap = null;
	Channel httpChannel;
	Channel httpsChannel;
	
	ExecutorService bossExecutor;
	ExecutorService workerExecutors;
	
	public NettyServer() {

	}

	public ClientBootstrap getClientBootstrap() {
		return clientBootstrap;
	}

	public ServerBootstrap getServerBootstrap() {
		return serverBootstrap;
	}

	@Override
	protected void doStart() throws IOException {
		
		System.setProperty("hypersocket.netty.debug", "true");
		
		clientBootstrap = new ClientBootstrap(
				new NioClientSocketChannelFactory(
						bossExecutor = Executors.newCachedThreadPool(new NettyThreadFactory()),
						workerExecutors = Executors.newCachedThreadPool(new NettyThreadFactory())));

		clientBootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();
				pipeline.addLast("handler", new SocketForwardingWebsocketClientHandler());
				return pipeline;
			}
		});

		// Configure the server.
		serverBootstrap = new ServerBootstrap(
				new NioServerSocketChannelFactory(
						Executors.newCachedThreadPool(),
						Executors.newCachedThreadPool()));

		// Set up the event pipeline factory.
		serverBootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();
				if(Boolean.getBoolean("hypersocket.netty.debug")) {
					pipeline.addLast("logger", new LoggingHandler(InternalLogLevel.DEBUG));
				}
				pipeline.addLast("switcherA", new SSLSwitchingHandler(
						NettyServer.this, getHttpPort(), getHttpsPort()));
				return pipeline;
			}
		});

		
		serverBootstrap.setOption("child.receiveBufferSize", 1048576);
		serverBootstrap.setOption("child.sendBufferSize", 1048576);
		serverBootstrap.setOption("backlog", 5000);
		
		httpChannel = bindInterface(getHttpPort());
		httpsChannel = bindInterface(getHttpsPort());
		
	}

	@Override
	public int getActualHttpPort() {
		if(httpChannel==null) {
			throw new IllegalStateException("You cannot get the actual port in use because the server is not started");
		}
		return ((InetSocketAddress)httpChannel.getLocalAddress()).getPort();
	}

	@Override
	public int getActualHttpsPort() {
		if(httpChannel==null) {
			throw new IllegalStateException("You cannot get the actual port in use because the server is not started");
		}
		return ((InetSocketAddress)httpChannel.getLocalAddress()).getPort();
	}
	
	protected Channel bindInterface(Integer port) throws IOException {
		try {
			if(log.isInfoEnabled()) {
				log.info("Binding server to " + port);
			}
			
			Channel ch = serverBootstrap.bind(new InetSocketAddress(port));
			
			if(log.isInfoEnabled()) {
				log.info("Bound to port " + ((InetSocketAddress)ch.getLocalAddress()).getPort());
			}
			
			return ch;
			
		} catch (ChannelException e) {
			log.error("Failed to bind port", e);
			throw e;
		}
	}
	
	@Override
	protected void doStop() {

	}

	@Override
	public void connect(TCPForwardingClientCallback callback) {

		clientBootstrap.connect(
				new InetSocketAddress(callback.getHostname(), callback.getPort())).addListener(
				new ClientConnectCallbackImpl(callback));

	}

	@Override
	public void restart(final Long delay) {
		
		Thread t = new Thread() {
			public void run() {
				if(log.isInfoEnabled()) {
					log.info("Restarting the server in " + delay + " seconds");
				}
				
				try {
					Thread.sleep(delay * 1000);
					
					if(log.isInfoEnabled()) {
						log.info("Restarting...");
					}
					Main.getInstance().restartServer();
				} catch (Exception e) {
					log.error("Failed to restart", e);
				}
			}
		};
		
		t.start();
		
	}

	@Override
	public void shutdown(final Long delay) {
		
		Thread t = new Thread() {
			public void run() {
				if(log.isInfoEnabled()) {
					log.info("Shutting down the server in " + delay + " seconds");
				}
				try {
					Thread.sleep(delay * 1000);
					
					if(log.isInfoEnabled()) {
						log.info("Shutting down");
					}
					Main.getInstance().shutdownServer();
				} catch (Exception e) {
					log.error("Failed to shutdown", e);
				}
			}
		};
		
		t.start();
		
		
	}
	
	class NettyThreadFactory implements ThreadFactory {

		@Override
		public Thread newThread(Runnable run) {
			Thread t = new Thread(run);
			t.setContextClassLoader(Main.getInstance().getClassLoader());
			return t;
		}
		
	}

}
