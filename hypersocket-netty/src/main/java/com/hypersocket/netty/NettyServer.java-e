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
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.annotation.PostConstruct;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.jboss.netty.logging.InternalLogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.config.SystemConfigurationService;
import com.hypersocket.ip.ExtendedIpFilterRuleHandler;
import com.hypersocket.ip.IPRestrictionService;
import com.hypersocket.netty.forwarding.SocketForwardingWebsocketClientHandler;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.server.HypersocketServerImpl;
import com.hypersocket.server.interfaces.http.HTTPInterfaceResource;
import com.hypersocket.server.interfaces.http.HTTPInterfaceResourceRepository;
import com.hypersocket.server.interfaces.http.HTTPProtocol;
import com.hypersocket.server.websocket.TCPForwardingClientCallback;

@Component
public class NettyServer extends HypersocketServerImpl {

	static Logger log = LoggerFactory.getLogger(NettyServer.class);

	private ClientBootstrap clientBootstrap = null;
	private ServerBootstrap serverBootstrap = null;
	Set<Channel> httpChannels;
	Set<Channel> httpsChannels;
	
	ExecutorService executor;
	
	@Autowired
	ExtendedIpFilterRuleHandler ipFilterHandler;
	
	MonitorChannelHandler monitorChannelHandler = new MonitorChannelHandler();
	Map<String,List<Channel>> channelsByIPAddress = new HashMap<String,List<Channel>>();
	
	@Autowired
	IPRestrictionService ipRestrictionService; 
	
	@Autowired
	SystemConfigurationService configurationService; 
	
	public NettyServer() {

	}
	
	@PostConstruct
	private void postConstruct() {
		
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
		
		executor = Executors.newCachedThreadPool(new NettyThreadFactory());
		
		clientBootstrap = new ClientBootstrap(
				new NioClientSocketChannelFactory(
						executor,
						executor));

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
						executor,
						executor));

		// Set up the event pipeline factory.
		serverBootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();
				if(Boolean.getBoolean("hypersocket.netty.debug")) {
					pipeline.addLast("logger", new LoggingHandler(InternalLogLevel.DEBUG));
				}
				pipeline.addLast("ipFilter", ipFilterHandler);
				pipeline.addLast("channelMonitor", monitorChannelHandler);
				pipeline.addLast("switcherA", new SSLSwitchingHandler(
						NettyServer.this));
				return pipeline;
			}
		});

		
		serverBootstrap.setOption("child.receiveBufferSize", 1048576);
		serverBootstrap.setOption("child.sendBufferSize", 1048576);
		serverBootstrap.setOption("backlog", 5000);
		
		httpChannels = new HashSet<Channel>();
		httpsChannels = new HashSet<Channel>();
		
		HTTPInterfaceResourceRepository interfaceRepository = 
				(HTTPInterfaceResourceRepository) getApplicationContext().getBean("HTTPInterfaceResourceRepositoryImpl");
		
		if(interfaceRepository==null) {
			throw new IOException("Cannot get interface configuration from application context!");
		}
		
		for(HTTPInterfaceResource resource : interfaceRepository.allInterfaces()) {
			bindInterface(resource);
		}
		
		if(httpChannels.size()==0 && httpsChannels.size()==0) {
			
			if(log.isInfoEnabled()) {
				log.info("Failed to startup any interfaces. Creating emergency listeners");
			}
			
			HTTPInterfaceResource tmp = new HTTPInterfaceResource();
			tmp.setInterfaces("127.0.0.1");
			tmp.setPort(0);
			tmp.setProtocol(HTTPProtocol.HTTP.toString());
			
			bindInterface(tmp);
			
			HTTPInterfaceResource tmp2 = new HTTPInterfaceResource();
			tmp2.setInterfaces("::");
			tmp2.setPort(8080);
			tmp2.setProtocol(HTTPProtocol.HTTP.toString());
			
			bindInterface(tmp2);
		}
	}

	@Override
	public int getActualHttpPort() {
		if(httpChannels==null) {
			throw new IllegalStateException("You cannot get the actual port in use because the server is not started");
		}
		return ((InetSocketAddress)httpChannels.iterator().next().getLocalAddress()).getPort();
	}

	@Override
	public int getActualHttpsPort() {
		if(httpsChannels==null) {
			throw new IllegalStateException("You cannot get the actual port in use because the server is not started");
		}
		return ((InetSocketAddress)httpsChannels.iterator().next().getLocalAddress()).getPort();
	}
	
	protected void bindInterface(HTTPInterfaceResource interfaceResource) throws IOException {
			
		Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
		
		Set<String> interfacesToBind = new HashSet<String>(
				ResourceUtils.explodeCollectionValues(interfaceResource.getInterfaces()));
		
		if(interfacesToBind.isEmpty()) {
			
			if(log.isInfoEnabled()) {
				log.info("Binding server to all interfaces on port " 
						+ interfaceResource.getPort());
			}
			Channel ch = serverBootstrap.bind(new InetSocketAddress(interfaceResource.getPort()));
			ch.setAttachment(interfaceResource);
			switch(interfaceResource.getProtocol()) {
			case HTTP:
				httpChannels.add(ch);
				break;
			default:
				httpsChannels.add(ch);
			}
			
			if(log.isInfoEnabled()) {
				log.info("Bound " + interfaceResource.getProtocol() + " to port "
						+ ((InetSocketAddress)ch.getLocalAddress()).getPort());
			}
		} else {
			while(e.hasMoreElements())  {
				
				NetworkInterface i = e.nextElement();
			
				Enumeration<InetAddress> inetAddresses = i.getInetAddresses();
				
				for (InetAddress inetAddress : Collections.list(inetAddresses)) {
					
					if(interfacesToBind.contains(inetAddress.getHostAddress())) {
						try {
							if(log.isInfoEnabled()) {
								log.info("Binding " + interfaceResource.getProtocol() + " server to interface " 
										+ i.getDisplayName() 
										+ " " 
										+ inetAddress.getHostAddress() 
										+ ":" 
										+ interfaceResource.getPort());
							}
							
							Channel ch = serverBootstrap.bind(new InetSocketAddress(inetAddress, interfaceResource.getPort()));
							
							ch.setAttachment(interfaceResource);
							switch(interfaceResource.getProtocol()) {
							case HTTP:
								httpChannels.add(ch);
								break;
							default:
								httpsChannels.add(ch);
							}
							
							if(log.isInfoEnabled()) {
								log.info("Bound " + interfaceResource.getProtocol() + " to " 
										+ inetAddress.getHostAddress() 
										+ ":" 
										+ ((InetSocketAddress)ch.getLocalAddress()).getPort());
							}
						
						} catch(ChannelException ex) {
							log.error("Failed to bind port", ex);
						}
					}
				}
			}
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

	public ChannelHandler getIpFilter() {
		return ipFilterHandler;
	}
	

	class MonitorChannelHandler extends SimpleChannelHandler {

		@Override
		public void channelBound(ChannelHandlerContext ctx, ChannelStateEvent e)
				throws Exception {
			InetAddress addr = ((InetSocketAddress)ctx.getChannel().getRemoteAddress()).getAddress();
			
			if(log.isDebugEnabled()) {
				log.debug("Opening channel from " + addr.toString());
			}
			
			synchronized (channelsByIPAddress) {
				if(!channelsByIPAddress.containsKey(addr.getHostAddress())) {
					channelsByIPAddress.put(addr.getHostAddress(), new ArrayList<Channel>());
				}
				channelsByIPAddress.get(addr.getHostAddress()).add(ctx.getChannel());				
			}

		}

		@Override
		public void channelUnbound(ChannelHandlerContext ctx,
				ChannelStateEvent e) throws Exception {
			InetAddress addr = ((InetSocketAddress)ctx.getChannel().getRemoteAddress()).getAddress();
			
			if(log.isDebugEnabled()) {
				log.debug("Closing channel from " + addr.toString());
			}
			
			synchronized (channelsByIPAddress) {
				channelsByIPAddress.get(addr.getHostAddress()).remove(ctx.getChannel());
				if(channelsByIPAddress.get(addr.getHostAddress()).isEmpty()) {
					channelsByIPAddress.remove(addr.getHostAddress());
				}
			}
			
		}
	}
}
