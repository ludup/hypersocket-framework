/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
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
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadFactory;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.config.SystemConfigurationService;
import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.ip.ExtendedIpFilterRuleHandler;
import com.hypersocket.netty.forwarding.SocketForwardingWebsocketClientHandler;
import com.hypersocket.netty.log.NCSARequestLog;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.RealmService;
import com.hypersocket.server.ClientConnector;
import com.hypersocket.server.HypersocketServerImpl;
import com.hypersocket.server.interfaces.http.HTTPInterfaceResource;
import com.hypersocket.server.interfaces.http.HTTPInterfaceResourceRepository;
import com.hypersocket.server.interfaces.http.HTTPProtocol;
import com.hypersocket.server.interfaces.http.events.HTTPInterfaceResourceCreatedEvent;
import com.hypersocket.server.interfaces.http.events.HTTPInterfaceResourceDeletedEvent;
import com.hypersocket.server.interfaces.http.events.HTTPInterfaceResourceEvent;
import com.hypersocket.server.interfaces.http.events.HTTPInterfaceResourceUpdatedEvent;
import com.hypersocket.server.interfaces.http.events.HTTPInterfaceStartedEvent;
import com.hypersocket.server.interfaces.http.events.HTTPInterfaceStoppedEvent;
import com.hypersocket.server.websocket.TCPForwardingClientCallback;
import com.hypersocket.session.SessionService;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MessageSizeEstimator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.AttributeKey;

@Component
public class NettyServer extends HypersocketServerImpl implements MessageSizeEstimator  {

	public static final AttributeKey<HTTPInterfaceResource> INTERFACE_RESOURCE = AttributeKey.newInstance(HTTPInterfaceResource.class.getSimpleName());
	

	public static final String RESOURCE_BUNDLE = "NettyServer";
	public static final String HTTPD = "httpd";

	static Logger log = LoggerFactory.getLogger(NettyServer.class);

	@Autowired
	private ExtendedIpFilterRuleHandler ipFilterHandler;

	@Autowired
	private SystemConfigurationService configurationService;

	@Autowired
	private EventService eventService;

	@Autowired
	private SessionService sessionService;

	@Autowired
	private I18NService i18nService;

	@Autowired
	private RealmService realmService;

	private Bootstrap clientBootstrap = null;
	private ServerBootstrap serverBootstrap = null;
	private Map<HTTPInterfaceResource,Set<Channel>> httpChannels;
	private Map<HTTPInterfaceResource,Set<Channel>> httpsChannels;

	private EventLoopGroup workerGroup;

	private MonitorChannelHandler monitorChannelHandler = new MonitorChannelHandler();
	private Map<String,List<Channel>> channelsByIPAddress = new HashMap<String,List<Channel>>();

	private NettyThreadFactory nettyThreadFactory;

	private List<ClientConnector> clientConnectors = new ArrayList<ClientConnector>();

	private Set<String> preventUrlRedirection = new HashSet<>();
	private NCSARequestLog requestLog;
	private boolean setupMode;

	public NettyServer() {

	}
	
	public NCSARequestLog getRequestLog() {
		return requestLog;
	}

	
//	public ExecutionHandler getHandler() {
//		return executionHandler;
//	}
//	
//	public OrderedMemoryAwareThreadPoolExecutor getExecutionHandler() {
//		return (OrderedMemoryAwareThreadPoolExecutor) executionHandler.getExecutor();
//	}

	@PostConstruct
	private void postConstruct() {

		nettyThreadFactory = new NettyThreadFactory();

		// TODO how to configre this now?
//		executionHandler = new DefaultEventExecutor(nettyThreadFactory);
//	            new OrderedMemoryAwareThreadPoolExecutor(
//	            		configurationService.getIntValue("netty.maxChannels"),
//	            		configurationService.getIntValue("netty.maxChannelMemory"),
//	            		configurationService.getIntValue("netty.maxTotalMemory"),
//	            		60 * 5,
//	            		TimeUnit.SECONDS,
//	            		this,
//	            		nettyThreadFactory));
		
		requestLog = new NCSARequestLog();
		requestLog.setFilename("logs/request.log");
		requestLog.setRetainDays(30);
		requestLog.setAppend(true);
		requestLog.setLogDispatch(true);
		requestLog.setLogLatency(true);
		requestLog.setPreferProxiedForAddress(true);
		try {
			requestLog.start();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to start NCSA request log.", e);
		}

		i18nService.registerBundle(RESOURCE_BUNDLE);
		
		try {
			setupMode = !configurationService.getBooleanValue("setup.completed");
		}
		catch(IllegalStateException ise) {
			/* Non-commercial configuration */
		}
	}

	public Bootstrap getClientBootstrap() {
		return clientBootstrap;
	}

	public ServerBootstrap getServerBootstrap() {
		return serverBootstrap;
	}

	@Override
	public void registerClientConnector(ClientConnector connector) {
		clientConnectors.add(connector);
		Collections.sort(clientConnectors, new Comparator<ClientConnector>() {

			@Override
			public int compare(ClientConnector o1, ClientConnector o2) {
				return o1.getWeight().compareTo(o2.getWeight());
			}
		});
	}

	@Override
	public EventLoopGroup getExecutor() {
		return workerGroup;
	}

	@Override
	protected void doStart() throws IOException {

		System.setProperty("hypersocket.netty.debug", "true");

	    EventLoopGroup bossGroup = new NioEventLoopGroup(new NettyThreadFactory());
	    workerGroup = new NioEventLoopGroup(new NettyThreadFactory());

		clientBootstrap = new Bootstrap();
		clientBootstrap.group(workerGroup);
		clientBootstrap.handler(new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				pipeline.addLast("handler", new SocketForwardingWebsocketClientHandler());
			}
		});

		// Configure the server.
		serverBootstrap = new ServerBootstrap();
		serverBootstrap.group(bossGroup, workerGroup);
		serverBootstrap.channel(NioServerSocketChannel.class);

		// Set up the event pipeline factory.
		serverBootstrap.childHandler(new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				if(Boolean.getBoolean("hypersocket.netty.debug")) {
					pipeline.addLast("logger", new LoggingHandler(LogLevel.DEBUG));
				}
				pipeline.addLast("ipFilter", ipFilterHandler);
				pipeline.addLast("channelMonitor", monitorChannelHandler);
				pipeline.addLast("switcherA", new SSLSwitchingHandler(
						NettyServer.this));
			}
		});

		serverBootstrap.option(NioChannelOption.SO_RCVBUF, 
				configurationService.getIntValue("netty.receiveBuffer"));
		serverBootstrap.option(NioChannelOption.SO_SNDBUF,
				configurationService.getIntValue("netty.sendBuffer"));
		serverBootstrap.option(NioChannelOption.SO_BACKLOG,
				configurationService.getIntValue("netty.backlog"));

		httpChannels = new HashMap<HTTPInterfaceResource,Set<Channel>>();
		httpsChannels = new HashMap<HTTPInterfaceResource,Set<Channel>>();

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
			tmp.setId(0L);
			tmp.setInterfaces("127.0.0.1");
			tmp.setPort(0);
			tmp.setProtocol(HTTPProtocol.HTTP.toString());
			tmp.setRealm(realmService.getSystemRealm());
			bindInterface(tmp);

			HTTPInterfaceResource tmp2 = new HTTPInterfaceResource();
			tmp.setId(1L);
			tmp2.setInterfaces("::");
			tmp2.setPort(0);
			tmp2.setProtocol(HTTPProtocol.HTTP.toString());
			tmp.setRealm(realmService.getSystemRealm());

			bindInterface(tmp2);
		}
	}

	@Override
	public int getActualHttpPort() {
		if(httpChannels==null) {
			throw new IllegalStateException("You cannot get the actual port in use because the server is not started");
		}
		return ((InetSocketAddress)httpChannels.values().iterator().next().iterator().next().localAddress()).getPort();
	}

	@Override
	public int getActualHttpsPort() {
		if(httpsChannels==null) {
			throw new IllegalStateException("You cannot get the actual port in use because the server is not started");
		}
		return ((InetSocketAddress)httpsChannels.values().iterator().next().iterator().next().localAddress()).getPort();
	}

	protected synchronized void unbindInterface(HTTPInterfaceResource interfaceResource) {

		Set<Channel> channels = httpChannels.get(interfaceResource);

		closeChannels(interfaceResource, channels);

		channels = httpsChannels.get(interfaceResource);

		closeChannels(interfaceResource, channels);
	}

	protected void closeChannels(HTTPInterfaceResource interfaceResource, Set<Channel> channels) {

		for(Channel channel : channels) {
			InetSocketAddress addr = (InetSocketAddress) channel.localAddress();
			try {
				ChannelFuture future = channel.close();
				future.await(30000);
				if(future.isDone() && future.isSuccess()) {
					eventService.publishEvent(new HTTPInterfaceStoppedEvent(this,
							sessionService.getSystemSession(),
							interfaceResource, addr.getAddress().getHostAddress(), addr.getPort()));
				} else {
					eventService.publishEvent(new HTTPInterfaceStoppedEvent(this,
							interfaceResource,
							new IllegalStateException("Timeout exceeded before channel was closed."),
							sessionService.getSystemSession(),
							addr.getAddress().getHostAddress(), addr.getPort()));
				}

			} catch (InterruptedException e) {

				eventService.publishEvent(new HTTPInterfaceStoppedEvent(this,
						interfaceResource,
						e,
						sessionService.getSystemSession(),
						addr.getAddress().getHostAddress(), addr.getPort()));
			}

		}

		channels.clear();
	}

	protected synchronized void bindInterface(HTTPInterfaceResource interfaceResource) throws IOException {

		Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();

		Set<String> interfacesToBind = new HashSet<String>();

		if(!interfaceResource.getAllInterfaces()) {
			interfacesToBind.addAll(ResourceUtils.explodeCollectionValues(interfaceResource.getInterfaces()));
		}

		clearSSLContexts(interfaceResource);
		
		httpChannels.put(interfaceResource, new HashSet<Channel>());
		httpsChannels.put(interfaceResource, new HashSet<Channel>());

		if(interfacesToBind.isEmpty()) {

			try {
				int port = Integer.parseInt(
								System.getProperty("hypersocket." + interfaceResource.getProtocol().name().toLowerCase() + ".port", 
								String.valueOf(interfaceResource.getPort())));
				if(log.isInfoEnabled()) {
					log.info("Binding server to all interfaces on port "
							+ port);
				}
				InetSocketAddress localAddr = new InetSocketAddress(port);
				Channel ch = serverBootstrap.bind(localAddr).channel();
				ch.attr(INTERFACE_RESOURCE).set(interfaceResource);
				switch(interfaceResource.getProtocol()) {
				case HTTP:
					httpChannels.get(interfaceResource).add(ch);
					break;
				default:
					httpsChannels.get(interfaceResource).add(ch);
				}

				InetAddress iaddr = localAddr.getAddress();
				eventService.publishEvent(new HTTPInterfaceStartedEvent(this,
						sessionService.getSystemSession(),
						interfaceResource,
						iaddr == null ? null : iaddr.getHostAddress()));

				if(log.isInfoEnabled()) {
					log.info("Bound " + interfaceResource.getProtocol() + " to port "
							+ localAddr.getPort());
				}
			} catch (Exception e1) {
				eventService.publishEvent(new HTTPInterfaceStartedEvent(this,
						interfaceResource,
						e1,
						sessionService.getSystemSession(),
						"::"));
			}
		} else {
			while(e.hasMoreElements())  {

				NetworkInterface i = e.nextElement();

				Enumeration<InetAddress> inetAddresses = i.getInetAddresses();

				for (InetAddress inetAddress : Collections.list(inetAddresses)) {

					if(interfacesToBind.contains(inetAddress.getHostAddress())) {
						try {
							int port = Integer.parseInt(
									System.getProperty("hypersocket." + interfaceResource.getProtocol().name().toLowerCase() + ".port", 
									String.valueOf(interfaceResource.getPort())));
							if(log.isInfoEnabled()) {
								log.info("Binding " + interfaceResource.getProtocol() + " server to interface "
										+ i.getDisplayName()
										+ " "
										+ inetAddress.getHostAddress()
										+ ":"
										+ port);
							}

							Channel ch = serverBootstrap.bind(new InetSocketAddress(inetAddress, port)).channel();

							ch.attr(INTERFACE_RESOURCE).set(interfaceResource);
							switch(interfaceResource.getProtocol()) {
							case HTTP:
								httpChannels.get(interfaceResource).add(ch);
								break;
							default:
								httpsChannels.get(interfaceResource).add(ch);
							}

							eventService.publishEvent(new HTTPInterfaceStartedEvent(this,
									sessionService.getSystemSession(),
									interfaceResource,
									((InetSocketAddress)ch.localAddress()).getAddress().getHostAddress()));

							if(log.isInfoEnabled()) {
								log.info("Bound " + interfaceResource.getProtocol() + " to "
										+ inetAddress.getHostAddress()
										+ ":"
										+ ((InetSocketAddress)ch.localAddress()).getPort());
							}

						} catch(ChannelException ex) {

							eventService.publishEvent(new HTTPInterfaceStartedEvent(this,
									interfaceResource,
									ex,
									sessionService.getSystemSession(),
									inetAddress.getHostAddress()));

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
	public void connect(TCPForwardingClientCallback callback) throws IOException {

		InetSocketAddress addr = new InetSocketAddress(callback.getHostname(), callback.getPort());

		for(ClientConnector connector : clientConnectors) {
			if(connector.handlesConnect(addr)) {
				connector.connect(addr, callback);
				return;
			}
		}

		SocketAddress local = callback.getLocalAddress();
		if(local!=null) {
			clientBootstrap.connect(
					addr, local).addListener(
					new ClientConnectCallbackImpl(callback));
		} else {
			clientBootstrap.connect(
					addr).addListener(
					new ClientConnectCallbackImpl(callback));
		}

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


	@Sharable
	class MonitorChannelHandler extends ChannelDuplexHandler {

		@Override
		public void channelActive(ChannelHandlerContext ctx)
				throws Exception {
			
			InetAddress addr = ((InetSocketAddress)ctx.channel().remoteAddress()).getAddress();

			if(log.isDebugEnabled()) {
				log.debug("Opening channel from " + addr.toString());
			}

			synchronized (channelsByIPAddress) {
				if(!channelsByIPAddress.containsKey(addr.getHostAddress())) {
					channelsByIPAddress.put(addr.getHostAddress(), new ArrayList<Channel>());
				}
				channelsByIPAddress.get(addr.getHostAddress()).add(ctx.channel());
			}

		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			InetAddress addr = ((InetSocketAddress)ctx.channel().remoteAddress()).getAddress();

			if(log.isDebugEnabled()) {
				log.debug("Closing channel from " + addr.toString());
			}

			synchronized (channelsByIPAddress) {
				channelsByIPAddress.get(addr.getHostAddress()).remove(ctx.channel());
				if(channelsByIPAddress.get(addr.getHostAddress()).isEmpty()) {
					channelsByIPAddress.remove(addr.getHostAddress());
				}
			}

		}
	}

	protected void processApplicationEvent(final SystemEvent event) {
		if(event instanceof HTTPInterfaceResourceEvent) {
			workerGroup.execute(() -> {
				try {
					HTTPInterfaceResource resource = (HTTPInterfaceResource) ((HTTPInterfaceResourceEvent) event)
							.getResource();

					if (event instanceof HTTPInterfaceResourceCreatedEvent) {
						bindInterface(resource);
					} else if (event instanceof HTTPInterfaceResourceUpdatedEvent) {
						unbindInterface(resource);
						bindInterface(resource);
					} else if (event instanceof HTTPInterfaceResourceDeletedEvent) {
						bindInterface(resource);
					}

				} catch (IOException e) {
					log.error("Failed to reconfigure interfaces", e);
				}
			});
		}
	}

	public void setRedirectable(String uri, boolean redirectable) {
		if(redirectable)
			preventUrlRedirection.remove(uri);
		else
			preventUrlRedirection.add(uri);
	}

	public boolean isRedirectable(String uri) {
		return !preventUrlRedirection.contains(uri);
	}

	@Override
	public Handle newHandle() {
		return new Handle() {
			@Override
			public int size(Object obj) {
				int size = 1024;
				System.out.println(">>> " + obj);
//				if(obj instanceof ChannelUpstreamEventRunnable) {
//					ChannelUpstreamEventRunnable e = (ChannelUpstreamEventRunnable) obj;
//					if(e.getEvent() instanceof UpstreamMessageEvent) {
//						UpstreamMessageEvent evt = (UpstreamMessageEvent) e.getEvent();
//
//						if(evt.getMessage() instanceof HttpRequest) {
//							HttpRequest request = (HttpRequest) evt.getMessage();
//
//							size = (int) HttpUtil.getContentLength(request, 1024);
//						}
//
//						if(evt.getMessage() instanceof HttpChunk) {
//							HttpChunk chunk = (HttpChunk) evt.getMessage();
//							size = chunk.getContent().readableBytes();
//						}
//
//						if(evt.getMessage() instanceof WebSocketFrame) {
//							WebSocketFrame frame = (WebSocketFrame) evt.getMessage();
//							size = frame.content().readableBytes();
//						}
//					}
//					if(log.isDebugEnabled()) {
//						log.debug(String.format("Incoming message is %d bytes in size", size));
//					}
//				} else if(obj instanceof ChannelDownstreamEventRunnable) {
//					ChannelDownstreamEventRunnable e = (ChannelDownstreamEventRunnable) obj;
//					if(e.getEvent() instanceof DownstreamMessageEvent) {
//
//						DownstreamMessageEvent evt = (DownstreamMessageEvent) e.getEvent();
//
//						if(evt.getMessage() instanceof HttpRequest) {
//							HttpRequest request = (HttpRequest) evt.getMessage();
//
//							size = (int) HttpUtil.getContentLength(request, 1024);
//						}
//
//						if(evt.getMessage() instanceof HttpChunk) {
//							HttpChunk chunk = (HttpChunk) evt.getMessage();
//							size = chunk.getContent().readableBytes();
//						}
//
//						if(evt.getMessage() instanceof WebSocketFrame) {
//							WebSocketFrame frame = (WebSocketFrame) evt.getMessage();
//							size = frame.content().readableBytes();
//						}
//
//						if(log.isDebugEnabled()) {
//							log.debug(String.format("Outgoing message is %d bytes in size", size));
//						}
//					}
//				}


				return size;
			}
		};
	}

	@Override
	public void processDefaultResponse(HttpServletRequest request, HttpServletResponse nettyResponse, boolean disableCache) {

		if(configurationService.getBooleanValue("security.xFrameOptionsEnabled")) {
			nettyResponse.setHeader("X-Frame-Options", configurationService.getValue("security.xFrameOptionsValue"));
		}

		if(setupMode || ( disableCache && !nettyResponse.containsHeader("Cache-Control"))) {
			nettyResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, private");
			nettyResponse.setHeader("Pragma", "no-cache");
		}
		nettyResponse.setHeader("X-Content-Type-Options", "nosniff");
		nettyResponse.setHeader("X-XSS-Protection", "1; mode=block");

		if(request.isSecure()
				&& configurationService.getBooleanValue("security.strictTransportSecurity")
				&& "true".equals(System.getProperty("hypersocket.security.strictTransportSecurity","true"))) {
			nettyResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubdomains");
		}
	}
}
